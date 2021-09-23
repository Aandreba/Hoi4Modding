package org.hoi.classes.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageResult {
    final public BufferedImage img;
    final public Point point; // LEFT TOP

    public ImageResult (BufferedImage img, Point point) {
        this.img = img;
        this.point = point;
    }
}