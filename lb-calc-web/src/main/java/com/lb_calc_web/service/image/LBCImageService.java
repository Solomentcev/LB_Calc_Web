package com.lb_calc_web.service.image;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.equipment.Equipment;
import com.lb_calc_web.domain.model.LBC;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/** Формирует визуальное представление физической геометрии LBC. */
public final class LBCImageService {

    private static final int MILLIMETERS_PER_PIXEL = 10;
    private static final int SCALE = 3;

    private LBCImageService() {
    }

    static BufferedImage createLBCImage(LBC lbc) {
        BufferedImage image = new BufferedImage(
                Math.max(1, lbc.getWidth() / MILLIMETERS_PER_PIXEL + 1),
                Math.max(1, lbc.getHeight() / MILLIMETERS_PER_PIXEL + 1),
                BufferedImage.TYPE_INT_ARGB
        );
        drawLBC(image, lbc, 0);

        BufferedImage scaled = new BufferedImage(
                image.getWidth() * SCALE,
                image.getHeight() * SCALE,
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
        graphics.dispose();
        return scaled;
    }

    /** Рисует корпус, техническую ячейку и складские ячейки по координатам доменной модели. */
    static void drawLBC(BufferedImage image, LBC lbc, int x) {
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        int width = mm(lbc.getWidth());
        int height = mm(lbc.getHeight());
        int upperFrame = mm(lbc.getUpperFrame());
        int bottomFrame = mm(lbc.getBottomFrame());
        int shelf = Math.max(1, mm(lbc.getTypeLb().getShelfThick()));
        int technicalTop = mm(lbc.getTechnicalCellTop());
        int technicalHeight = Math.max(1, mm(lbc.getTechnicalCellHeight()));
        int technicalWidth = Math.max(1, mm(lbc.getTechnicalCellWidth()));
        int technicalX = x + (width - technicalWidth) / 2;

        graphics.setColor(lbc.getColorBody().getColor());
        graphics.fillRect(x, 0, width, height);
        graphics.setColor(lbc.getColorDoor().getColor());
        graphics.fillRect(x + 1, 1, Math.max(1, width - 2), Math.max(1, height - 2));

        graphics.setColor(lbc.getColorBody().getColor());
        graphics.fillRect(x, 0, width, upperFrame);
        graphics.fillRect(x, height - bottomFrame, width, bottomFrame);

        // Перегородки складских ячеек под технической ячейкой.
        int bottomBoundary = technicalTop + technicalHeight;
        for (int i = 0; i < lbc.getStorageCellsBelow(); i++) {
            int y = bottomBoundary + i * (mm(lbc.getCellHeight()) + shelf);
            graphics.fillRect(x, y, width, shelf);
        }

        // Перегородки складских ячеек над технической ячейкой.
        for (int i = 0; i < lbc.getStorageCellsAbove(); i++) {
            int y = technicalTop - (i + 1) * shelf
                    - i * mm(lbc.getCellHeight());
            graphics.fillRect(x, y, width, shelf);
        }

        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRect(technicalX, technicalTop, technicalWidth, technicalHeight);
        graphics.setColor(Color.BLACK);
        graphics.drawRect(technicalX, technicalTop, technicalWidth, technicalHeight);

        Equipment display = lbc.getDisplay();
        if (display != null && display.getWidth() > 0 && display.getHeight() > 0) {
            int displayWidth = mm(display.getWidth());
            int displayHeight = mm(display.getHeight());
            int displayX = x + (width - displayWidth) / 2;
            int displayY = mm(lbc.getDisplayTop());
            graphics.setColor(Color.GRAY);
            graphics.fillRect(displayX, displayY, displayWidth, displayHeight);
            graphics.setColor(Color.BLACK);
            graphics.drawRect(displayX, displayY, displayWidth, displayHeight);
        }

        graphics.setColor(Color.BLACK);
        graphics.drawRect(x, 0, width, height);
        graphics.dispose();
    }

    private static int mm(int value) {
        return value / MILLIMETERS_PER_PIXEL;
    }

    /** Создаёт PNG-файл изображения LBC. */
    public static File getFileLBCImage(LBC lbc) {
        BufferedImage image = (BufferedImage) createLBCImage(lbc);
        File file = new File("src/main/resources/static/lbcs/lbc" + lbc.getId() + ".png");
        try {
            file.createNewFile();
            ImageIO.write(image, "png", file);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось создать изображение LBC", exception);
        }
        return file;
    }

    /** Возвращает PNG-изображение LBC в виде массива байтов. */
    public static byte[] getBytesArrayLBCImage(LBC lbc) {
        BufferedImage image = (BufferedImage) createLBCImage(lbc);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось сформировать изображение LBC", exception);
        }
        return output.toByteArray();
    }

    /** Возвращает изображение LBC в кодировке Base64. */
    public static String getStringLBCImage(LBC lbc) {
        return Base64.getEncoder().encodeToString(getBytesArrayLBCImage(lbc));
    }
}