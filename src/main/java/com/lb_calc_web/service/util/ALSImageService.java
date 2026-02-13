package com.lb_calc_web.service.util;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.model.attributes.PositionLC;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class ALSImageService {
    static Image createALSImage(ALSDTO als) {
        BufferedImage img = new BufferedImage(als.getWidth()/10+1, als.getHeight()/10+1, BufferedImage.TYPE_INT_ARGB);
        AffineTransform scalingTransform = new AffineTransform();

        scalingTransform.scale(3, 3);
        AffineTransformOp scaleOp = new AffineTransformOp(scalingTransform, AffineTransformOp.TYPE_BILINEAR);
        drawALS(img, als);
        BufferedImage scaledImg = new BufferedImage(img.getWidth()*3, img.getHeight()*3, BufferedImage.TYPE_INT_ARGB);
        scaledImg= scaleOp.filter(img, scaledImg);
        return scaledImg;
    }

    static void drawALS(BufferedImage img, ALSDTO als) {
        int x=0;
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (PositionLC.valueOf(als.getPositionLC()).equals(PositionLC.LEFT)) {
            LCImageService.drawLC(img,als.getLC(),x);
            x = x + (als.getLC().getWidth() / 10);
        }
        for (int i = 1; i <=als.getLbList().size(); i++) {
            LBDTO lb = als.getLbList().get(i-1);
            LBImageService.drawLB(img,lb,x);
            x = x + (lb.getWidth() / 10);
            if ((PositionLC.valueOf(als.getPositionLC()).equals(PositionLC.CENTER))) {
                if (als.getLbList().size()/2==i || als.getLbList().size()<=1) {
                        LCImageService.drawLC(img, als.getLC(), x);
                        x = x + (als.getLC().getWidth() / 10);
                }
            }
        }
        if (PositionLC.valueOf(als.getPositionLC()).equals(PositionLC.RIGHT)) {
            LCImageService.drawLC(img,als.getLC(),x);
        }
        g2d.dispose();
    }
    public static File getFileLCImage(ALSDTO als) {
        BufferedImage img = (BufferedImage) createALSImage(als);
        File file=new File("src/main/resources/static/alss/"+"als"+als.getId()+".png");
        try {
            file.createNewFile();
            ImageIO.write(img, "png", file);
        } catch (IOException e) {
            throw new RuntimeException("Error creating file"+e.getMessage());
        }
        return file;
    }
    public static byte[] getBytesArrayALSImage(ALSDTO als) {
        BufferedImage img = (BufferedImage) createALSImage(als);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", baos);

        } catch (IOException e) {
            throw new RuntimeException("Error creating"+e.getMessage());
        }
        return baos.toByteArray();
    }
    public static String getStringALSImage(ALSDTO als) {
        return Base64.getEncoder().encodeToString(getBytesArrayALSImage(als));
    }
}
