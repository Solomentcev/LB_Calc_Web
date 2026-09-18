package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.equipment.BarReader;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.dto.LBCDTO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

public final class LBCImageService {

    private LBCImageService() {
    }

    static Image createLBCImage(LBCDTO lbc) {
        BufferedImage img =
                new BufferedImage(
                        lbc.getWidth() / 10 + 1,
                        lbc.getHeight() / 10 + 1,
                        BufferedImage.TYPE_INT_ARGB
                );

        AffineTransform scalingTransform = new AffineTransform();
        scalingTransform.scale(3, 3);

        AffineTransformOp scaleOp =
                new AffineTransformOp(
                        scalingTransform,
                        AffineTransformOp.TYPE_BILINEAR
                );

        drawLBC(img, lbc, 0);

        BufferedImage scaledImg =
                new BufferedImage(
                        img.getWidth() * 3,
                        img.getHeight() * 3,
                        BufferedImage.TYPE_INT_ARGB
                );

        return scaleOp.filter(img, scaledImg);
    }

    /**
     * Рисует LBC как один физический модуль:
     * единый корпус, storage-ячейки и встроенная панель управления.
     */
    static void drawLBC(
            Image img,
            LBCDTO lbc,
            int x
    ) {
        Graphics2D g2d =
                (Graphics2D) img.getGraphics();

        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        g2d.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );

        int width = lbc.getWidth() / 10;
        int height = lbc.getHeight() / 10;
        int upperFrame = lbc.getUpperFrame() / 10;
        int bottomFrame = lbc.getBottomFrame() / 10;
        int serviceZoneWidth = lbc.getServiceZoneWidth() / 10;
        int shelfThickness = Math.max(1, lbc.getShelfThick() / 10);

        Colors bodyColor =
                Colors.valueOf(lbc.getColorBody());

        Colors doorColor =
                Colors.valueOf(lbc.getColorDoor());

        // Единый корпус
        g2d.setColor(bodyColor.getColor());
        g2d.fillRoundRect(
                x, 0, width, height, 1, 1
        );

        // Дверная поверхность
        g2d.setColor(doorColor.getColor());
        g2d.fillRoundRect(
                x, 0, width, height, 1, 1
        );

        // Верхняя и нижняя рамы
        g2d.setColor(bodyColor.getColor());

        g2d.fillRoundRect(
                x, 0, width, upperFrame, 1, 1
        );

        g2d.fillRoundRect(
                x,
                height - bottomFrame,
                width,
                bottomFrame,
                1,
                1
        );

        // Storage-зона и встроенная control-зона.
        boolean directionLeft =
                DirectionDoorOpening.LEFT.name()
                        .equals(
                                lbc.getDirectionDoorOpening()
                        );

        int storageWidth =
                Math.max(
                        1,
                        width - serviceZoneWidth
                );

        int storageX =
                directionLeft
                        ? x
                        : x + serviceZoneWidth;

        g2d.setColor(bodyColor.getColor());

        for (int i = 2; i <= lbc.getCountCells(); i++) {
            int y =
                    (
                            lbc.getHeight()
                                    - lbc.getBottomFrame()
                                    - (
                                    lbc.getHeightCell()
                                            + lbc.getShelfThick()
                            ) * (i - 1)
                    ) / 10;

            g2d.fillRoundRect(
                    storageX,
                    y,
                    storageWidth,
                    shelfThickness,
                    1,
                    1
            );
        }

        /*
         * Control-зона LBC занимает сервисную зону того же физического
         * корпуса, поэтому LBC не раскладывается на отдельные LC и LB.
         */
        int usableHeight =
                Math.max(
                        1,
                        height - upperFrame - bottomFrame
                );

        int controlWidth =
                serviceZoneWidth > 0
                        ? Math.max(
                        1,
                        serviceZoneWidth
                        )
                        : Math.max(
                        1,
                        width
                );

        int panelX =
                directionLeft
                        ? x + storageWidth
                        : x;

        int panelY =
                upperFrame + 5;

        int panelHeight =
                Math.max(
                        1,
                        usableHeight - 10
                );

        int panelMargin =
                Math.min(
                        5,
                        Math.max(0, controlWidth / 10)
                );

        int panelWidth =
                Math.max(
                        1,
                        controlWidth - panelMargin * 2
                );

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(
                panelX + panelMargin,
                panelY,
                panelWidth,
                panelHeight,
                4,
                4
        );

        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(
                panelX + panelMargin,
                panelY,
                panelWidth,
                panelHeight,
                4,
                4
        );

        Display display = findDisplay(lbc.getDisplay());

        if (display != Display.NONE) {
            int displayWidth =
                    Math.min(
                            Math.max(
                                    10,
                                    display.getDisplayWidth() / 10
                            ),
                            Math.max(
                                    1,
                                    panelWidth - 4
                            )
                    );

            int displayHeight =
                    Math.min(
                            Math.max(
                                    10,
                                    display.getDisplayHeight() / 10
                            ),
                            Math.max(
                                    1,
                                    panelHeight - 4
                            )
                    );

            int displayX =
                    panelX
                            + panelMargin
                            + (panelWidth - displayWidth) / 2;

            int displayY =
                    panelY
                            + (panelHeight - displayHeight) / 2;

            g2d.setColor(Color.GRAY);
            g2d.fillRoundRect(
                    displayX,
                    displayY,
                    displayWidth,
                    displayHeight,
                    3,
                    3
            );

            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(
                    displayX,
                    displayY,
                    displayWidth,
                    displayHeight,
                    3,
                    3
            );
        }

        g2d.setColor(Color.BLACK);

        if (!BarReader.NONE.getName().equals(lbc.getBarReader())) {
            g2d.fillRect(
                    panelX + panelMargin + 2,
                    panelY + 2,
                    6,
                    6
            );
        }

        if (lbc.isPrinter()) {
            g2d.fillRect(
                    panelX + panelMargin + 2,
                    panelY + panelHeight - 8,
                    6,
                    6
            );
        }

        if (lbc.isRfidReader()) {
            g2d.fillRect(
                    panelX + controlWidth - panelMargin - 8,
                    panelY + panelHeight - 8,
                    6,
                    6
            );
        }

        // Внешняя граница единого модуля и сервисной зоны.
        g2d.setColor(Color.BLACK);

        g2d.drawRoundRect(
                x,
                0,
                width,
                height,
                1,
                1
        );

        if (serviceZoneWidth > 0) {
            int zoneX =
                    directionLeft
                            ? x + width - serviceZoneWidth
                            : x;

            g2d.drawRoundRect(
                    zoneX,
                    upperFrame,
                    serviceZoneWidth,
                    usableHeight,
                    1,
                    1
            );
        }

        g2d.dispose();
    }

    private static Display findDisplay(String name) {
        if (name == null || name.isBlank()) {
            return Display.NONE;
        }

        return List.of(
                        Display.NONE,
                        Display.LC10,
                        Display.LC17,
                        Display.LC19
                )
                .stream()
                .filter(display ->
                        display.getName().equals(name))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Неизвестный дисплей LBC: " + name
                        )
                );
    }

    public static File getFileLBCImage(LBCDTO lbc) {
        BufferedImage img =
                (BufferedImage) createLBCImage(lbc);

        File file =
                new File(
                        "src/main/resources/static/lbcs/lbc"
                                + lbc.getId()
                                + ".png"
                );

        try {
            file.createNewFile();
            ImageIO.write(img, "png", file);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error creating LBC image: "
                            + e.getMessage(),
                    e
            );
        }

        return file;
    }

    public static byte[] getBytesArrayLBCImage(LBCDTO lbc) {
        BufferedImage img =
                (BufferedImage) createLBCImage(lbc);

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        try {
            ImageIO.write(img, "png", baos);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Error creating LBC image: "
                            + e.getMessage(),
                    e
            );
        }

        return baos.toByteArray();
    }

    public static String getStringLBCImage(LBCDTO lbc) {
        return Base64.getEncoder().encodeToString(
                getBytesArrayLBCImage(lbc)
        );
    }
}
