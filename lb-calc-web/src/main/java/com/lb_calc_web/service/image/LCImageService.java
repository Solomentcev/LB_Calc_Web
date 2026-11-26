package com.lb_calc_web.service.image;

import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.equipment.EquipmentFunctionalType;
import com.lb_calc_web.domain.model.LC;

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
public final class LCImageService {

    private static final int MILLIMETERS_PER_PIXEL = 10;
    private static final int IMAGE_SCALE = 3;

    private LCImageService() {
    }

    /** Создаёт масштабированное изображение LC. */
    static Image createLCImage(LC lc) {
        BufferedImage image = new BufferedImage(
                toPixels(lc.getWidth()) + 1,
                toPixels(lc.getHeight()) + 1,
                BufferedImage.TYPE_INT_ARGB
        );

        drawLC(image, lc, 0);

        AffineTransform transform = new AffineTransform();
        transform.scale(IMAGE_SCALE, IMAGE_SCALE);
        AffineTransformOp scaleOperation = new AffineTransformOp(
                transform,
                AffineTransformOp.TYPE_BILINEAR
        );

        BufferedImage scaledImage = new BufferedImage(
                image.getWidth() * IMAGE_SCALE,
                image.getHeight() * IMAGE_SCALE,
                BufferedImage.TYPE_INT_ARGB
        );
        return scaleOperation.filter(image, scaledImage);
    }

    /**
     * Рисует LC в переданном изображении.
     *
     * <p>Все геометрические размеры и координаты до преобразования
     * выражены в миллиметрах. Сервис отвечает только за перевод
     * физических размеров в пиксели и графическое представление.</p>
     */
    static void drawLC(Image image, LC lc, int x) {
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            int width = toPixels(lc.getWidth());
            int height = toPixels(lc.getHeight());
            int upperFrame = toPixels(lc.getUpperFrame());
            int bottomFrame = toPixels(lc.getBottomFrame());

            // Лицевая поверхность дверцы.
            graphics.setColor(lc.getColorDoor().getColor());
            graphics.fillRoundRect(x, 0, width, height, 1, 1);

            // Верхняя и нижняя рамы корпуса.
            graphics.setColor(lc.getColorBody().getColor());
            graphics.fillRoundRect(x, 0, width, upperFrame, 1, 1);
            graphics.fillRoundRect(
                    x,
                    height - bottomFrame,
                    width,
                    bottomFrame,
                    1,
                    1
            );

            // Контур рабочей панели ограничен рамами, а не фиксированной высотой.
            graphics.setColor(Color.BLACK);
            graphics.drawRoundRect(
                    x,
                    upperFrame,
                    width,
                    Math.max(0, height - upperFrame - bottomFrame),
                    1,
                    1
            );

            drawDisplay(graphics, lc, x);

            // Внешний контур корпуса.
            graphics.drawRoundRect(x, 0, width, height, 1, 1);
        } finally {
            graphics.dispose();
        }
    }

    /** Размещает дисплей по рассчитанной доменом физической координате. */
    private static void drawDisplay(Graphics2D graphics, LC lc, int x) {
        Equipment display = findDisplay(lc);
        if (display == null || display.getWidth() <= 0 || display.getHeight() <= 0) {
            return;
        }

        int displayWidth = toPixels(display.getWidth());
        int displayHeight = toPixels(display.getHeight());
        if (displayWidth <= 0 || displayHeight <= 0) {
            return;
        }

        int displayX = x + toPixels((lc.getWidth() - display.getWidth()) / 2);
        int displayY = toPixels(lc.getDisplayTop());

        graphics.setColor(Color.GRAY);
        graphics.fillRoundRect(
                displayX, displayY, displayWidth, displayHeight, 1, 1
        );

        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(
                displayX, displayY, displayWidth, displayHeight, 1, 1
        );
    }

    private static Equipment findDisplay(LC lc) {
        return lc.getEquipment().keySet().stream()
                .filter(item -> item.getFunctionalType() == EquipmentFunctionalType.DISPLAY)
                .findFirst()
                .orElse(null);
    }

    private static int toPixels(int millimeters) {
        return (int) Math.round((double) millimeters / MILLIMETERS_PER_PIXEL);
    }

    /** Создаёт PNG-файл изображения управляющего модуля LC. */
    public static File getFileLCImage(LC lc) {
        BufferedImage image = (BufferedImage) createLCImage(lc);
        File file = new File(
                "src/main/resources/static/lcs/lc" + lc.getId() + ".png"
        );

        try {
            file.createNewFile();
            ImageIO.write(image, "png", file);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Не удалось создать файл изображения LC",
                    exception
            );
        }
        return file;
    }

    /** Возвращает PNG-изображение LC в виде массива байтов. */
    public static byte[] getBytesArrayLCImage(LC lc) {
        BufferedImage image = (BufferedImage) createLCImage(lc);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", output);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Не удалось сформировать изображение LC",
                    exception
            );
        }
        return output.toByteArray();
    }

    /** Возвращает изображение LC в кодировке Base64. */
    public static String getStringLCImage(LC lc) {
        return Base64.getEncoder().encodeToString(getBytesArrayLCImage(lc));
    }
}
