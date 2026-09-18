package com.lb_calc_web.service.util;

import com.lb_calc_web.domain.attributes.PositionControlModule;
import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
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

public class ALSImageService {

    static Image createALSImage(ALSDTO als) {

        BufferedImage img =
                new BufferedImage(
                        als.getWidth() / 10 + 1,
                        als.getHeight() / 10 + 1,
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

        drawALS(img, als);

        BufferedImage scaledImg =
                new BufferedImage(
                        img.getWidth() * 3,
                        img.getHeight() * 3,
                        BufferedImage.TYPE_INT_ARGB
                );

        scaledImg = scaleOp.filter(img, scaledImg);

        return scaledImg;
    }

    static void drawALS(
            BufferedImage img,
            ALSDTO als
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

        PositionControlModule position =
                PositionControlModule.valueOf(
                        als.getPositionControlModule()
                );

        int x = 0;

        if (position == PositionControlModule.LEFT) {
            x += drawControlModule(
                    img,
                    als,
                    x
            );
        }

        List<LBDTO> lbList =
                als.getLbList() == null
                        ? List.of()
                        : als.getLbList();

        for (int i = 0; i < lbList.size(); i++) {
            LBDTO lb = lbList.get(i);

            LBImageService.drawLB(
                    img,
                    lb,
                    x
            );

            x += lb.getWidth() / 10;

            if (position == PositionControlModule.CENTER
                    && (
                    i + 1 >= lbList.size() / 2
                            || lbList.size() <= 1
            )) {
                x += drawControlModule(
                        img,
                        als,
                        x
                );

                position = null;
            }
        }

        if (position == PositionControlModule.RIGHT
                || (
                position == PositionControlModule.CENTER
                        && als.getLbList().isEmpty()
        )) {
            x += drawControlModule(
                    img,
                    als,
                    x
            );
        }

        g2d.dispose();
    }

    /**
     * Рисует единственный control-модуль ALS.
     * LBC остаётся одним физическим модулем и не раскладывается
     * на отдельные LB и LC.
     */
    private static int drawControlModule(
            BufferedImage img,
            ALSDTO als,
            int x
    ) {
        if (als.getLBC() != null) {
            LBCImageService.drawLBC(
                    img,
                    als.getLBC(),
                    x
            );

            return als.getLBC().getWidth() / 10;
        }

        if (als.getLC() != null) {
            LCImageService.drawLC(
                    img,
                    als.getLC(),
                    x
            );

            return als.getLC().getWidth() / 10;
        }

        throw new IllegalStateException(
                "ALS должен содержать модуль управления"
        );
    }

    public static File getFileLCImage(
            ALSDTO als
    ) {

        BufferedImage img =
                (BufferedImage) createALSImage(als);

        File file =
                new File(
                        "src/main/resources/static/alss/als"
                                + als.getId()
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

            throw new RuntimeException(
                    "Error creating file: "
                            + e.getMessage(),
                    e
            );
        }

        return file;
    }

    public static byte[] getBytesArrayALSImage(
            ALSDTO als
    ) {

        BufferedImage img =
                (BufferedImage) createALSImage(als);

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

    public static String getStringALSImage(
            ALSDTO als
    ) {

        return Base64.getEncoder().encodeToString(
                getBytesArrayALSImage(als)
        );
    }
}