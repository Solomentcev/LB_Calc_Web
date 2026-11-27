package com.lb_calc_web.service.image;

import com.lb_calc_web.domain.attributes.Colors;
import com.lb_calc_web.domain.attributes.DirectionDoorOpening;
import com.lb_calc_web.domain.model.LB;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/** Генерирует графическое представление модуля хранения LB. */
public final class LBImageService {

    private LBImageService() {
    }

static Image createLBImage(
            LB lb
    ) {

        BufferedImage img =
                new BufferedImage(
                        lb.getWidth() / 10 + 1,
                        lb.getHeight() / 10 + 1,
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

        drawLB(
                img,
                lb,
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

    static void drawLB(
            Image img,
            LB lb,
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

        int serviceZoneWidth =
                lb.getTypeLb().getServiceZoneWidth();

        int shelfThickness =
                lb.getTypeLb().getShelfThick();

        boolean directionLeft =
                DirectionDoorOpening.LEFT == lb.getDirectionDoorOpening();

        // Двери
        g2d.setColor(
                new Color(lb.getColorDoor().getRgb())
        );

        g2d.fillRoundRect(
                x,
                0,
                lb.getWidth() / 10,
                lb.getHeight() / 10,
                1,
                1
        );

        // Корпус
        g2d.setColor(
                new Color(lb.getColorBody().getRgb())
        );

        // Верхняя рама
        g2d.fillRoundRect(
                x,
                0,
                lb.getWidth() / 10,
                lb.getUpperFrame() / 10,
                1,
                1
        );

        // Нижняя рама
        g2d.fillRoundRect(
                x,
                (
                        lb.getHeight()
                                - lb.getBottomFrame()
                ) / 10,
                lb.getWidth() / 10,
                lb.getBottomFrame() / 10,
                1,
                1
        );

        if (directionLeft) {

            // Сервисная зона
            g2d.fillRoundRect(
                    x
                            + (
                            lb.getWidth()
                                    - serviceZoneWidth
                    ) / 10,
                    lb.getUpperFrame() / 10,
                    serviceZoneWidth / 10,
                    (
                            lb.getHeight()
                                    - lb.getUpperFrame()
                                    - lb.getBottomFrame()
                    ) / 10,
                    1,
                    1
            );

            // Полки
            for (
                    int i = 2;
                    i <= lb.getCountCells();
                    i++
            ) {

                g2d.fillRoundRect(
                        x,
                        (
                                int
                                ) (
                                (
                                        lb.getHeight()
                                                - lb.getBottomFrame()
                                                - (
                                                lb.getCellHeight()
                                                        + shelfThickness
                                        ) * (i - 1)
                                ) / 10
                        ),
                        (
                                lb.getWidth()
                                        - serviceZoneWidth
                        ) / 10,
                        shelfThickness / 10,
                        1,
                        1
                );
            }

        } else {

            // Сервисная зона
            g2d.fillRoundRect(
                    x,
                    lb.getUpperFrame() / 10,
                    serviceZoneWidth / 10,
                    (
                            lb.getHeight()
                                    - lb.getUpperFrame()
                                    - lb.getBottomFrame()
                    ) / 10,
                    1,
                    1
            );

            // Полки
            for (
                    int i = 2;
                    i <= lb.getCountCells();
                    i++
            ) {

                g2d.fillRoundRect(
                        x
                                + serviceZoneWidth / 10,
                        (
                                int
                                ) (
                                (
                                        lb.getHeight()
                                                - lb.getBottomFrame()
                                                - (
                                                lb.getCellHeight()
                                                        + shelfThickness
                                        ) * (i - 1)
                                ) / 10
                        ),
                        (
                                lb.getWidth()
                                        - serviceZoneWidth
                        ) / 10,
                        shelfThickness / 10,
                        1,
                        1
                );
            }
        }

        // Чёрные границы
        g2d.setColor(Color.BLACK);

        // Габариты
        g2d.drawRoundRect(
                x,
                0,
                lb.getWidth() / 10,
                lb.getHeight() / 10,
                1,
                1
        );

        // Верхняя рама
        g2d.drawRoundRect(
                x,
                0,
                lb.getWidth() / 10,
                lb.getUpperFrame() / 10,
                1,
                1
        );

        // Нижняя рама
        g2d.drawRoundRect(
                x,
                (
                        lb.getHeight()
                                - lb.getBottomFrame()
                ) / 10,
                lb.getWidth() / 10,
                lb.getBottomFrame() / 10,
                1,
                1
        );

        if (directionLeft) {

            g2d.drawRoundRect(
                    x
                            + (
                            lb.getWidth()
                                    - serviceZoneWidth
                    ) / 10,
                    lb.getUpperFrame() / 10,
                    serviceZoneWidth / 10,
                    (
                            lb.getHeight()
                                    - lb.getUpperFrame()
                                    - lb.getBottomFrame()
                    ) / 10,
                    1,
                    1
            );

            for (
                    int i = 2;
                    i <= lb.getCountCells();
                    i++
            ) {

                g2d.drawRoundRect(
                        x,
                        (
                                int
                                ) (
                                (
                                        lb.getHeight()
                                                - lb.getBottomFrame()
                                                - (
                                                lb.getCellHeight()
                                                        * (i - 1)
                                                        + shelfThickness
                                                        * (i - 1)
                                        )
                                ) / 10
                        ),
                        (
                                lb.getWidth()
                                        - serviceZoneWidth
                        ) / 10,
                        shelfThickness / 10,
                        1,
                        1
                );
            }

        } else {

            g2d.drawRoundRect(
                    x,
                    lb.getUpperFrame() / 10,
                    serviceZoneWidth / 10,
                    (
                            lb.getHeight()
                                    - lb.getUpperFrame()
                                    - lb.getBottomFrame()
                    ) / 10,
                    1,
                    1
            );

            for (
                    int i = 2;
                    i <= lb.getCountCells();
                    i++
            ) {

                g2d.drawRoundRect(
                        x
                                + serviceZoneWidth / 10,
                        (
                                int
                                ) (
                                (
                                        lb.getHeight()
                                                - lb.getBottomFrame()
                                                - (
                                                lb.getCellHeight()
                                                        * (i - 1)
                                                        + shelfThickness
                                                        * (i - 1)
                                        )
                                ) / 10
                        ),
                        (
                                lb.getWidth()
                                        - serviceZoneWidth
                        ) / 10,
                        shelfThickness / 10,
                        1,
                        1
                );
            }
        }

        g2d.dispose();
    }

    /**
     * Формирует изображение LB для представления.
     */
    /** Создаёт PNG-файл изображения модуля хранения LB. */
    public static byte[] getBytesArrayLBImage(
            LB lb
    ) {

        BufferedImage img =
                (BufferedImage) createLBImage(lb);

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

    /** Возвращает изображение LB в кодировке Base64. */
    public static String getStringLBImage(
            LB lb
    ) {

        return Base64.getEncoder().encodeToString(
                getBytesArrayLBImage(lb)
        );
    }
}