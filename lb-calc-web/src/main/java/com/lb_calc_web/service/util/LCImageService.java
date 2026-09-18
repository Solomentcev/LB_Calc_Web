package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Display;
import com.lb_calc_web.dto.LCDTO;

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

public class LCImageService {

    static Image createLCImage(
            LCDTO lc
    ) {

        BufferedImage img =
                new BufferedImage(
                        lc.getWidth() / 10 + 1,
                        lc.getHeight() / 10 + 1,
                        BufferedImage.TYPE_INT_ARGB
                );

        AffineTransform scalingTransform =
                new AffineTransform();

        scalingTransform.scale(3, 3);

        AffineTransformOp scaleOp =
                new AffineTransformOp(
                        scalingTransform,
                        AffineTransformOp.TYPE_BILINEAR
                );

        drawLC(
                img,
                lc,
                0
        );

        BufferedImage scaledImg =
                new BufferedImage(
                        img.getWidth() * 3,
                        img.getHeight() * 3,
                        BufferedImage.TYPE_INT_ARGB
                );

        scaledImg =
                scaleOp.filter(
                        img,
                        scaledImg
                );

        return scaledImg;
    }

    static void drawLC(
            Image img,
            LCDTO lc,
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

        Colors colorBody =
                Colors.valueOf(
                        lc.getColorBody()
                );

        Display display =
                findDisplay(
                        lc.getDisplay()
                );

        g2d.setColor(
                colorBody.getColor()
        );

        // Габариты модуля
        g2d.fillRoundRect(
                x,
                0,
                lc.getWidth() / 10,
                lc.getHeight() / 10,
                1,
                1
        );

        g2d.setColor(Color.BLACK);

        // Габариты модуля
        g2d.drawRoundRect(
                x,
                0,
                lc.getWidth() / 10,
                lc.getHeight() / 10,
                1,
                1
        );

        // Верхняя рама
        g2d.drawRoundRect(
                x,
                0,
                lc.getWidth() / 10,
                lc.getUpperFrame() / 10,
                1,
                1
        );

        // Нижняя рама
        g2d.drawRoundRect(
                x,
                (lc.getHeight() - lc.getBottomFrame()) / 10,
                lc.getWidth() / 10,
                lc.getBottomFrame() / 10,
                1,
                1
        );

        // Дисплей
        g2d.setColor(Color.GRAY);

        int displayX =
                x
                        + (
                        (lc.getWidth()
                                - display.getDisplayWidth())
                                / 2
                ) / 10;

        int displayY =
                (
                        lc.getHeight()
                                - 1300
                                - display.getDisplayHeight()
                ) / 10;

        int displayWidth =
                display.getDisplayWidth() / 10;

        int displayHeight =
                display.getDisplayHeight() / 10;

        g2d.fillRoundRect(
                displayX,
                displayY,
                displayWidth,
                displayHeight,
                1,
                1
        );

        g2d.setColor(Color.BLACK);

        g2d.drawRoundRect(
                displayX,
                displayY,
                displayWidth,
                displayHeight,
                1,
                1
        );

        // Панель
        g2d.drawRoundRect(
                x,
                lc.getUpperFrame() / 10,
                lc.getWidth() / 10,
                (lc.getHeight() - 1100) / 10,
                1,
                1
        );

        g2d.dispose();
    }

    private static Display findDisplay(
            String name
    ) {

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
                .filter(
                        display ->
                                display.getName().equals(name)
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Неизвестный дисплей LC: "
                                                + name
                                )
                );
    }

    public static File getFileLCImage(
            LCDTO lc
    ) {

        BufferedImage img =
                (BufferedImage) createLCImage(lc);

        File file =
                new File(
                        "src/main/resources/static/lcs/lc"
                                + lc.getId()
                                + ".png"
                );

        try {

            file.createNewFile();

            ImageIO.write(
                    img,
                    "png",
                    file
            );

        } catch (IOException e) {

            throw new RuntimeException(e);
        }

        return file;
    }

    public static byte[] getBytesArrayLCImage(
            LCDTO lc
    ) {

        BufferedImage img =
                (BufferedImage) createLCImage(lc);

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        try {

            ImageIO.write(
                    img,
                    "png",
                    baos
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Error creating image: "
                            + e.getMessage(),
                    e
            );
        }

        return baos.toByteArray();
    }

    public static String getStringLCImage(
            LCDTO lc
    ) {

        return Base64.getEncoder().encodeToString(
                getBytesArrayLCImage(lc)
        );
    }
}