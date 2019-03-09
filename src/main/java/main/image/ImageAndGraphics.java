/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.image;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 *
 * @author stuart
 */
public class ImageAndGraphics {
    private BufferedImage image;
    private Graphics2D g2;

    public ImageAndGraphics(BufferedImage image, Graphics2D g2) {
        this.image = image;
        this.g2 = g2;
    } 
    
    public void drawImage(BufferedImage img, AffineTransform tr) {
        g2.drawImage(img, tr, null);
    }

    public BufferedImage getImage() {
        return image;
    }
    
    
}
