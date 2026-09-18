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

        // Storage-зона
        boolean directionLeft =
                DirectionDoorOpening.LEFT.name()
                        .equals(lbc.getDirectionDoorOpening());

        int storageX =
                directionLeft
                        ? x
                        : x + serviceZoneWidth;

        int storageWidth =
                Math.max(
                        1,
                        width - serviceZoneWidth
                );

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

            g2d.fillRect(
                    storageX,
                    y,
                    storageWidth,
                    shelfThickness
            );
        }

        // Встроенная панель управления внутри того же корпуса.
        int usableHeight =
                height - upperFrame - bottomFrame;

        int panelHeight =
                Math.min(
                        Math.max(60, usableHeight / 4),
                        Math.max(60, usableHeight - 20)
                );

        int panelY =
                upperFrame + 10;

        int panelWidth =
                Math.min(
                        Math.max(100, width - 20),
                        width - 20
                );

        int panelX = x + (width - panelWidth) / 2;

        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRoundRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                4,
                4
        );

        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(
                panelX,
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
                                    20,
                                    display.getDisplayWidth() / 10
                            ),
                            panelWidth - 20
                    );

            int displayHeight =
                    Math.min(
                            Math.max(
                                    15,
                                    display.getDisplayHeight() / 10
                            ),
                            panelHeight - 20
                    );

            int displayX =
                    x + (width - displayWidth) / 2;

            int displayY =
                    panelY + (panelHeight - displayHeight) / 2;

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

        // Небольшие обозначения оборудования панели.
        g2d.setColor(Color.BLACK);

        if (!BarReader.NONE.getName().equals(lbc.getBarReader())) {
            g2d.fillRect(
                    panelX + 5,
                    panelY + 5,
                    8,
                    8
            );
        }

        if (lbc.isPrinter()) {
            g2d.fillRect(
                    panelX + panelWidth - 13,
                    panelY + 5,
                    8,
                    8
            );
        }

        if (lbc.isRfidReader()) {
            g2d.fillRect(
                    panelX + panelWidth - 13,
                    panelY + panelHeight - 13,
                    8,
                    8
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
