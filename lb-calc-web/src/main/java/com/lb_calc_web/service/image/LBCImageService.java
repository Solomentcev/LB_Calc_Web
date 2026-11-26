package com.lb_calc_web.service.image;

import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
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

    /**
     * Рисует LBC с сохранением правил оформления LB: сервисная зона
     * располагается со стороны, противоположной открыванию двери,
     * а полки ограничиваются шириной области хранения.
     */
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
        int serviceZoneWidth = mm(lbc.getTypeLb().getServiceZoneWidth());
        boolean directionLeft = lbc.getDirectionDoorOpening() == DirectionDoorOpening.LEFT;

        int serviceZoneX = directionLeft
                ? x + width - serviceZoneWidth
                : x;
        int storageAreaX = directionLeft
                ? x
                : x + serviceZoneWidth;
        int storageAreaWidth = Math.max(0, width - serviceZoneWidth);

        // Дверь и корпус.
        graphics.setColor(lbc.getColorDoor().getColor());
        graphics.fillRoundRect(x, 0, width, height, 1, 1);

        graphics.setColor(lbc.getColorBody().getColor());
        graphics.fillRoundRect(x, 0, width, upperFrame, 1, 1);
        graphics.fillRoundRect(x, height - bottomFrame, width, bottomFrame, 1, 1);

        // Сервисная зона сохраняет визуальное оформление LB,
        // но не участвует в расчёте размеров ячеек LBC.
        graphics.fillRoundRect(
                serviceZoneX,
                upperFrame,
                serviceZoneWidth,
                Math.max(0, height - upperFrame - bottomFrame),
                1,
                1
        );

        // Полки складских ячеек ниже технической ячейки.
        int cellHeight = mm(lbc.getCellHeight());
        int technicalTop = mm(lbc.getTechnicalCellTop());
        int technicalHeight = mm(lbc.getTechnicalCellHeight());
        int bottomBoundary = technicalTop + technicalHeight;

        for (int i = 0; i < lbc.getStorageCellsBelow(); i++) {
            int shelfY = bottomBoundary + i * (cellHeight + shelf);
            drawShelf(graphics, lbc.getColorBody().getColor(), storageAreaX, shelfY, storageAreaWidth, shelf);
        }

        // Полки складских ячеек выше технической ячейки.
        for (int i = 0; i < lbc.getStorageCellsAbove(); i++) {
            int shelfY = technicalTop - (i + 1) * shelf - i * cellHeight;
            drawShelf(graphics, storageAreaX, shelfY, storageAreaWidth, shelf);
        }

        // Техническая ячейка и дисплей отображаются по физическим размерам.
        int technicalWidth = Math.max(1, mm(lbc.getTechnicalCellWidth()));
        int technicalX = x + (width - technicalWidth) / 2;

        graphics.setColor(Color.DARK_GRAY);
        graphics.fillRoundRect(technicalX, technicalTop, technicalWidth, technicalHeight, 1, 1);
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(technicalX, technicalTop, technicalWidth, technicalHeight, 1, 1);

        Equipment display = lbc.getDisplay();
        if (display != null && display.getWidth() > 0 && display.getHeight() > 0) {
            int displayWidth = mm(display.getWidth());
            int displayHeight = mm(display.getHeight());
            int displayX = x + (width - displayWidth) / 2;
            int displayY = mm(lbc.getDisplayTop());
            graphics.setColor(Color.GRAY);
            graphics.fillRoundRect(displayX, displayY, displayWidth, displayHeight, 1, 1);
            graphics.setColor(Color.BLACK);
            graphics.drawRoundRect(displayX, displayY, displayWidth, displayHeight, 1, 1);
        }

        // Контуры корпуса, рам и сервисной зоны.
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(x, 0, width, height, 1, 1);
        graphics.drawRoundRect(x, 0, width, upperFrame, 1, 1);
        graphics.drawRoundRect(x, height - bottomFrame, width, bottomFrame, 1, 1);
        graphics.drawRoundRect(
                serviceZoneX,
                upperFrame,
                serviceZoneWidth,
                Math.max(0, height - upperFrame - bottomFrame),
                1,
                1
        );

        graphics.dispose();
    }

    /** Рисует полку с заливкой и чёрным контуром в пределах области хранения. */
    private static void drawShelf(Graphics2D graphics, Color shelfColor, int x, int y, int width, int shelf) {
        if (width <= 0 || shelf <= 0) {
            return;
        }
        graphics.setColor(shelfColor);
        graphics.fillRoundRect(x, y, width, shelf, 1, 1);
        graphics.setColor(Color.BLACK);
        graphics.drawRoundRect(x, y, width, shelf, 1, 1);
    }

    private static int mm(int value) {
        return value / MILLIMETERS_PER_PIXEL;
    }

    /** Создаёт PNG-файл изображения LBC. */
    public static File getFileLBCImage(LBC lbc) {
        BufferedImage image = createLBCImage(lbc);
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
        BufferedImage image = createLBCImage(lbc);
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
