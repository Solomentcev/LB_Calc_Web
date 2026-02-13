package com.lb_calc_web.service.util;

import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.model.attributes.Colors;
import com.lb_calc_web.model.attributes.DirectionDoorOpening;
import com.lb_calc_web.model.attributes.TypeLb;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class LBImageService {
    static Image createLBImage(LBDTO lb) {
        BufferedImage img = new BufferedImage(lb.getWidth()/10+1, lb.getHeight()/10+1, BufferedImage.TYPE_INT_ARGB);
        AffineTransform scalingTransform = new AffineTransform();

        scalingTransform.scale(3, 3);
        AffineTransformOp scaleOp = new AffineTransformOp(scalingTransform, AffineTransformOp.TYPE_BILINEAR);
        int x=0;

        drawLB(img, lb,x);
        BufferedImage scaledImg = new BufferedImage(img.getWidth()*3, img.getHeight()*3, BufferedImage.TYPE_INT_ARGB);
        scaledImg= scaleOp.filter(img, scaledImg);
        return scaledImg;
    }

    static void drawLB(Image img, LBDTO lb, int x) {
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setColor(Colors.valueOf(lb.getColorDoor()).getColor());
        g2d.fillRoundRect(x,0, lb.getWidth()/10,lb.getHeight()/10, 1,1);//габариты модуля
        g2d.setColor(Colors.valueOf(lb.getColorBody()).getColor());
        g2d.fillRoundRect(x,0, lb.getWidth()/10,lb.getUpperFrame()/10, 1,1); //верхняя рама
        g2d.fillRoundRect(x,(lb.getHeight() -lb.getBottomFrame())/10, lb.getWidth()/10,lb.getBottomFrame()/10, 1,1);//нижняя рама
        if (DirectionDoorOpening.valueOf(lb.getDirectionDoorOpening()).equals(DirectionDoorOpening.LEFT)){
            g2d.fillRoundRect(x+(lb.getWidth()- TypeLb.valueOf(lb.getType()).getServiceZoneWidth())/10,(lb.getUpperFrame())/10,
                    TypeLb.valueOf(lb.getType()).getServiceZoneWidth()/10,(lb.getHeight()-lb.getUpperFrame()-lb.getBottomFrame())/10, 1,1);// сервисная планка

            for (int i = 2; i <=lb.getCountCells() ; i++) {
                g2d.fillRoundRect(x,
                        (int) ((lb.getHeight()-lb.getBottomFrame()-(lb.getHeightCell() +lb.getShelfThick()) * (i-1)) / 10),
                        (lb.getWidth() - TypeLb.valueOf(lb.getType()).getServiceZoneWidth()) / 10, lb.getShelfThick() / 10, 1, 1);
            }
        } else {
            g2d.fillRoundRect(x,(lb.getUpperFrame())/10,
                    TypeLb.valueOf(lb.getType()).getServiceZoneWidth()/10,
                    (lb.getHeight()-lb.getUpperFrame()- lb.getBottomFrame())/10, 1,1);// сервисная планка

            for (int i = 2; i <=lb.getCountCells() ; i++) {
                g2d.fillRoundRect(x+(TypeLb.valueOf(lb.getType()).getServiceZoneWidth())/10,
                        (int) ((lb.getHeight()-lb.getBottomFrame()-(lb.getHeightCell()+lb.getShelfThick()) * (i-1)) / 10),
                        (lb.getWidth() -TypeLb.valueOf(lb.getType()).getServiceZoneWidth()) / 10, lb.getShelfThick() / 10, 1, 1);
            }
        }
        g2d.setColor(Color.BLACK);
        g2d.drawRoundRect(x,0, lb.getWidth()/10,lb.getHeight()/10, 1,1);
        g2d.drawRoundRect(x,0, lb.getWidth()/10,lb.getUpperFrame()/10, 1,1); //верхняя рама
        g2d.drawRoundRect(x,(lb.getHeight()-lb.getBottomFrame())/10, lb.getWidth()/10,lb.getBottomFrame()/10, 1,1);//нижняя рама
        if (DirectionDoorOpening.valueOf(lb.getDirectionDoorOpening()).equals(DirectionDoorOpening.LEFT)){
            g2d.drawRoundRect(x+(lb.getWidth()-TypeLb.valueOf(lb.getType()).getServiceZoneWidth())/10,(lb.getUpperFrame())/10,
                    TypeLb.valueOf(lb.getType()).getServiceZoneWidth()/10,(lb.getHeight()-lb.getUpperFrame()- lb.getBottomFrame())/10, 1,1);// сервисная планка

            for (int i = 2; i <=lb.getCountCells() ; i++) {
                g2d.drawRoundRect(x, (int) ((lb.getHeight() - lb.getBottomFrame()-(lb.getHeightCell() * (i - 1) + lb.getShelfThick() * (i-1))) / 10),
                        (lb.getWidth() - TypeLb.valueOf(lb.getType()).getServiceZoneWidth()) / 10, lb.getShelfThick() / 10, 1, 1);
            }
        } else {
            g2d.drawRoundRect(x,(lb.getUpperFrame())/10,
                    TypeLb.valueOf(lb.getType()).getServiceZoneWidth()/10,(lb.getHeight()-lb.getUpperFrame()- lb.getBottomFrame())/10, 1,1);// сервисная планка

            for (int i = 2; i <=lb.getCountCells() ; i++) {
                g2d.drawRoundRect(x+(TypeLb.valueOf(lb.getType()).getServiceZoneWidth())/10,
                        (int) ((lb.getHeight()- lb.getBottomFrame()-(lb.getHeightCell() * (i - 1) + lb.getShelfThick() * (i-1))) / 10),
                        (lb.getWidth() - TypeLb.valueOf(lb.getType()).getServiceZoneWidth()) / 10, lb.getShelfThick() / 10, 1, 1);
            }
        }
        g2d.dispose();
    }
    public static File getFileLBImage(LBDTO lb) {
        BufferedImage img = (BufferedImage) createLBImage(lb);
        File file=new File("src/main/resources/static/lbs/"+"lb"+lb.getId()+".png");
        try {
             file.createNewFile();
            ImageIO.write(img, "png", file);

        } catch (IOException e) {
            System.out.println("Error creating file");
            throw new RuntimeException(e);
        }
        return file;
    }
    public static byte[] getBytesArrayLBImage(LBDTO lb) {
        BufferedImage img = (BufferedImage) createLBImage(lb);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "png", baos);

        } catch (IOException e) {
            System.out.println("Error creating");
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
    public static String getStringLBImage(LBDTO lb) {
        return Base64.getEncoder().encodeToString(getBytesArrayLBImage(lb));
    }
}
