package com.lb_calc_web.service.image;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/** Генерирует графическое представление управляющего модуля LC. */
public class LCImageService {

static Image createLCImage(
            LC lc
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
            LC lc,
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
                lc.getColorBody();


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

        Equipment display = findDisplay(lc);
        int displayWidthMm = display == null ? 0 : display.getWidth();
        int displayHeightMm = display == null ? 0 : display.getHeight();

        int displayX =
                x + ((lc.getWidth() - displayWidthMm) / 2) / 10;

        int displayY = lc.getDisplayTop() / 10;

        int displayWidth = displayWidthMm / 10;

        int displayHeight = displayHeightMm / 10;

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

    /**
     * Формирует изображение LC для представления.
     */
    private static Equipment findDisplay(LC lc) {
        return lc.getEquipment().keySet().stream()
                .filter(item -> item.getFunctionalType() == EquipmentFunctionalType.DISPLAY)
                .findFirst()
                .orElse(null);
    }

    /** Создаёт PNG-файл изображения управляющего модуля LC. */
    public static File getFileLCImage(
            LC lc
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

    /** Возвращает PNG-изображение LC в виде массива байтов. */
    public static byte[] getBytesArrayLCImage(
            LC lc
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

    /** Возвращает изображение LC в кодировке Base64. */
    public static String getStringLCImage(
            LC lc
    ) {

        return Base64.getEncoder().encodeToString(
                getBytesArrayLCImage(lc)
        );
    }
}