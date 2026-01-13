package com.lb_calc_web.service;

import com.lb_calc_web.model.LB;
import com.lb_calc_web.model.LC;
import com.lb_calc_web.model.utils.DirectionDoorOpening;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class LCImageService {
    static Image createLCImage(LC lc) {
        BufferedImage img = new BufferedImage(lc.getWidth()/10+1, lc.getHeight()/10+1, BufferedImage.TYPE_INT_ARGB);
        AffineTransform scalingTransform = new AffineTransform();

        scalingTransform.scale(3, 3);
        AffineTransformOp scaleOp = new AffineTransformOp(scalingTransform, AffineTransformOp.TYPE_BILINEAR);
        int x=0;
        drawLC(img, lc, x);
        BufferedImage scaledImg = new BufferedImage(img.getWidth()*3, img.getHeight()*3, BufferedImage.TYPE_INT_ARGB);
        scaledImg= scaleOp.filter(img, scaledImg);
        return scaledImg;
    }

    static void drawLC(Image img, LC lc,int x) {
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setColor(lc.getColorBody().getColor());
        g2d.fillRoundRect(x,0, lc.getWidth()/10,lc.getHeight()/10, 1,1);//габариты модуля

        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x,0, lc.getWidth()/10,lc.getHeight()/10, 1,1);//габариты модуля
        g2d.drawRoundRect(x,0, lc.getWidth()/10,lc.getUpperFrame()/10, 1,1); //верхняя рама
        g2d.drawRoundRect(x,(lc.getHeight()-lc.getBottomFrame())/10, lc.getWidth()/10,lc.getBottomFrame()/10, 1,1);//нижняя рама
        g2d.setColor(Color.GRAY);
        g2d.fillRoundRect(x+((lc.getWidth()-lc.getDisplay().getDisplayWidth())/2)/10,(lc.getHeight()-1300-lc.getDisplay().getDisplayHeight())/10,
                lc.getDisplay().getDisplayWidth()/10, lc.getDisplay().getDisplayHeight()/10, 1,1);
        //дисплей
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x+((lc.getWidth()-lc.getDisplay().getDisplayWidth())/2)/10,(lc.getHeight()-1300-lc.getDisplay().getDisplayHeight())/10,
                lc.getDisplay().getDisplayWidth()/10, lc.getDisplay().getDisplayHeight()/10, 1,1);
        g2d.drawRoundRect(x,lc.getUpperFrame()/10, lc.getWidth() /10,(lc.getHeight()-1100)/10, 1,1);//панель

        g2d.dispose();
    }
    public static File getFileLCImage(LC lc) {
        BufferedImage img = (BufferedImage) createLCImage(lc);
        File file=new File("src/main/resources/static/lcs/"+"lc"+lc.getId()+".png");
        try {
            file.createNewFile();
            ImageIO.write(img, "png", file);

        } catch (IOException e) {
            System.out.println("Error creating file");
            throw new RuntimeException(e);
        }
        return file;
    }
    public static byte[] getBytesArrayLCImage(LC lc) {
        BufferedImage img = (BufferedImage) createLCImage(lc);
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
