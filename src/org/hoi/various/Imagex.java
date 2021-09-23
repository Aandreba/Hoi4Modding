package org.hoi.various;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Imagex {
    public static Color getPixel (BufferedImage img, int x, int y) {
        return new Color(img.getRGB(x, y), true);
    }

    public static BufferedImage resizeTo (BufferedImage img, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, img.getType());
        Graphics2D graphics = resized.createGraphics();
        graphics.drawImage(img, 0, 0, width, height, null);
        graphics.dispose();

        return resized;
    }

    public static BufferedImage clone (BufferedImage img) {
        BufferedImage clone = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        clone.setData(img.getRaster());

        return clone;
    }

    public static void tint (BufferedImage img, Color color) {
        for (int i=0;i<img.getWidth();i++) {
            for (int j=0;j<img.getHeight();j++) {
                Color input = getPixel(img, i, j);
                Color output = new Color(
                        Math.min(255, input.getRed() * color.getRed() / 255),
                        Math.min(255, input.getGreen() * color.getGreen() / 255),
                        Math.min(255, input.getBlue() * color.getBlue() / 255),
                        Math.min(255, input.getAlpha() * color.getAlpha() / 255)
                );

                img.setRGB(i, j, output.getRGB());
            }
        }
    }

    public static BufferedImage drawBorder (BufferedImage img, Color color) {
        BufferedImage clone = clone(img);

        int limX = img.getWidth() - 1;
        int limY = img.getHeight() - 1;

        for (int i=0;i<img.getWidth();i++) {
            int l = i - 1;
            int r = i + 1;

            for (int j = 0; j < img.getHeight(); j++) {
                Color self = getPixel(img, i, j);
                if (self.getAlpha() == 0) {
                    continue;
                }

                if (i == 0 || !getPixel(img, l, j).equals(self)) {
                    clone.setRGB(i, j, Color.black.getRGB());
                    continue;
                }

                if (i == limX || !getPixel(img, r, j).equals(self)) {
                    clone.setRGB(i, j, Color.black.getRGB());
                    continue;
                }

                if (j == 0 || !getPixel(img, i, j-1).equals(self)) {
                    clone.setRGB(i, j, Color.black.getRGB());
                    continue;
                }

                if (j == limY || !getPixel(img, i, j+1).equals(self)) {
                    clone.setRGB(i, j, Color.black.getRGB());
                }
            }
        }

        return clone;
    }

    public static BufferedImage transform (BufferedImage image, float scale, int dx, int dy) {
        if (scale == 1 && dx == 0 && dy == 0) {
            return image;
        }

        int w = (int) (image.getWidth() / scale);
        int h = (int) (image.getHeight() / scale);

        dx -= (image.getWidth() - w) / 2f;
        dy -= (image.getHeight() - h) / 2f;

        BufferedImage newImage = new BufferedImage(w, h, image.getType());
        Graphics2D graphics = newImage.createGraphics();

        graphics.drawImage(image, dx, dy, image.getWidth(), image.getHeight(), null);
        graphics.dispose();
        return newImage;
    }
}
