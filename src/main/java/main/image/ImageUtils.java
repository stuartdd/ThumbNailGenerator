/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import main.GLoaderException;


/**
 *
 * @author stuar
 */
public class ImageUtils {

    private static final double CONV = (2 * Math.PI) / 360;
    private static final double SIZE = 200;

    public static BufferedImage renderImage(MetaData metaData) {
        BufferedImage img = loadImage(metaData.getFile());
        ImageAndGraphics imageAndGraphics;
        double sca;
        if (img.getWidth() > img.getHeight()) {
            sca = SIZE / (double) img.getWidth();
            if (metaData.isAspectRotated()) {
                imageAndGraphics = initCanvas(SIZE * ((double) img.getHeight() / (double) img.getWidth()), SIZE);
            } else {
                imageAndGraphics = initCanvas(SIZE, SIZE * ((double) img.getHeight() / (double) img.getWidth()));
            }
        } else {
            sca = SIZE / (double) img.getHeight();
            if (metaData.isAspectRotated()) {
                imageAndGraphics = initCanvas(SIZE, SIZE * ((double) img.getHeight() / (double) img.getWidth()));
            } else {
                imageAndGraphics = initCanvas(SIZE * ((double) img.getWidth() / (double) img.getHeight()), SIZE);
            }
        }
        AffineTransform tr = new AffineTransform();
        tr.scale(sca, sca);

        if (metaData.isRotate()) {
            if (metaData.getDegrees() == 90) {
                tr.translate(img.getHeight(), 0);
                tr.rotate(90 * CONV);
            } else {
                if (metaData.getDegrees() == 180) {
                    tr.translate(img.getWidth(), img.getHeight());
                    tr.rotate(180 * CONV);
                } else {
                    if (metaData.getDegrees() == 270) {
                        tr.translate(0, img.getWidth());
                        tr.rotate(270 * CONV);
                    }
                }
            }
        }
        imageAndGraphics.drawImage(img, tr);
        return imageAndGraphics.getImage();
    }

    private static ImageAndGraphics initCanvas(final double imageWidth, final double imageHeight) {
        BufferedImage canvas = new BufferedImage((int) imageWidth, (int) imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = canvas.createGraphics();
        g2.setPaint(Color.DARK_GRAY);
        g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2.setPaint(Color.BLACK);
        return new ImageAndGraphics(canvas, g2);
    }

    public static BufferedImage loadImage(final File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException ex) {
            throw new GLoaderException("Image file [" + file.getAbsolutePath() + "] failed to load. "+ex.getMessage()+".",ex);
        }
    }

}
