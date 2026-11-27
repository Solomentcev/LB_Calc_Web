package com.lb_calc_web.service.image;

import com.lb_calc_web.domain.model.ALS;
import com.lb_calc_web.domain.model.LB;
import com.lb_calc_web.domain.model.LC;
import com.lb_calc_web.domain.model.LBC;
import com.lb_calc_web.domain.model.Module;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

/** Генерирует графическое представление конфигурации ALS. */
public final class ALSImageService {

    private ALSImageService() {
    }

    static Image createALSImage(ALS als) {
        BufferedImage image = new BufferedImage(
                als.getWidth() / 10 + 1,
                als.getHeight() / 10 + 1,
                BufferedImage.TYPE_INT_ARGB
        );
        AffineTransform transform = new AffineTransform();
        transform.scale(3, 3);
        AffineTransformOp scaleOperation = new AffineTransformOp(
                transform, AffineTransformOp.TYPE_BILINEAR);
        drawALS(image, als);
        BufferedImage scaledImage = new BufferedImage(
                image.getWidth() * 3, image.getHeight() * 3,
                BufferedImage.TYPE_INT_ARGB);
        return scaleOperation.filter(image, scaledImage);
    }

    /** Рисует модули ALS в их физическом порядке. */
    static void drawALS(BufferedImage image, ALS als) {
        int x = 0;
        for (Module module : als.getModules()) {
            if (module instanceof LB lb) {
                LBImageService.drawLB(image, lb, x);
                x += lb.getWidth() / 10;
            } else if (module instanceof LC lc) {
                LCImageService.drawLC(image, lc, x);
                x += lc.getWidth() / 10;
            } else if (module instanceof LBC lbc) {
                LBCImageService.drawLBC(image, lbc, x);
                x += lbc.getWidth() / 10;
            } else {
                throw new IllegalStateException(
                        "Неизвестный тип модуля ALS: " + module.getClass().getName());
            }
        }
    }

    /** Создаёт PNG-файл изображения ALS. */
    public static File getFileALSImage(ALS als) {
        BufferedImage image = (BufferedImage) createALSImage(als);
        File file = new File("src/main/resources/static/alss/als" + als.getId() + ".png");
        try {
            file.createNewFile();
            ImageIO.write(image, "png", file);
        } catch (IOException exception) {
            throw new RuntimeException("Не удалось создать файл изображения ALS", exception);
        }
        return file;
    }

    /** Возвращает PNG-изображение ALS в виде массива байтов. */
    public static byte[] getBytesArrayALSImage(ALS als) {
        BufferedImage image = (BufferedImage) createALSImage(als);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", output);
        } catch (IOException exception) {
            throw new RuntimeException("Не удалось сформировать изображение ALS", exception);
        }
        return output.toByteArray();
    }

    /** Возвращает изображение ALS в кодировке Base64. */
    public static String getStringALSImage(ALS als) {
        return Base64.getEncoder().encodeToString(getBytesArrayALSImage(als));
    }
}
