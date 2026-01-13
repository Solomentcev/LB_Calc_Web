package com.lb_calc_web.service;

import com.lb_calc_web.model.ALS;
import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.utils.PositionLC;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ALSImageService {
    static Image createALSImage(ALS als) {
        BufferedImage img = new BufferedImage(als.getWidth()/10+1, als.getHeight()/10+1, BufferedImage.TYPE_INT_ARGB);
        AffineTransform scalingTransform = new AffineTransform();

        scalingTransform.scale(3, 3);
        AffineTransformOp scaleOp = new AffineTransformOp(scalingTransform, AffineTransformOp.TYPE_BILINEAR);
        drawALS(img, als);
        BufferedImage scaledImg = new BufferedImage(img.getWidth()*3, img.getHeight()*3, BufferedImage.TYPE_INT_ARGB);
        scaledImg= scaleOp.filter(img, scaledImg);
        return scaledImg;
    }

    static void drawALS(BufferedImage img, ALS als) {
        int x=0;
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        if (als.getPositionLC().equals(PositionLC.LEFT)) {
            LCImageService.drawLC(img,als.getLc(),x);
            x = x + (als.getLc().getWidth() / 10);
        }
        for (int i = 0; i < als.getLbList().size(); i++) {
            LB lb = als.getLbList().get(i);
            if ((als.getPositionLC().equals(PositionLC.CENTER)) && (i==als.getLbList().size()/2)) {
                LCImageService.drawLC(img,als.getLc(),x);
                x = x + (als.getLc().getWidth() / 10);

            }
            LBImageService.drawLB(img,lb,x);
            x = x + (lb.getWidth() / 10);
        }
        if (als.getPositionLC().equals(PositionLC.RIGHT)) {
            LCImageService.drawLC(img,als.getLc(),x);
        }
        g2d.dispose();
    }
    public static File getFileLCImage(ALS als) {
        BufferedImage img = (BufferedImage) createALSImage(als);
        File file=new File("src/main/resources/static/alss/"+"als"+als.getId()+".png");
        try {
            file.createNewFile();
            ImageIO.write(img, "png", file);

        } catch (IOException e) {
            System.out.println("Error creating file");
            throw new RuntimeException(e);
        }
        return file;
    }
    public static byte[] getBytesArrayALSImage(ALS als) {
        BufferedImage img = (BufferedImage) createALSImage(als);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", baos);

        } catch (IOException e) {
            System.out.println("Error creating");
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
}
