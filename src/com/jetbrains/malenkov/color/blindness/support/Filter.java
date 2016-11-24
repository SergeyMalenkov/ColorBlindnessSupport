package com.jetbrains.malenkov.color.blindness.support;

import java.awt.image.RGBImageFilter;

/**
 * @author Sergey.Malenkov
 */
final class Filter extends RGBImageFilter {
    private final Converter myConverter;
    private final Double myWeight;

    Filter(Converter converter, Double weight) {
        if (weight != null && (weight <= 0 || 1 <= weight)) {
            throw new IllegalArgumentException("weight " + weight + " out of [0..1]");
        }
        canFilterIndexColorModel = true;
        myConverter = converter;
        myWeight = weight;
    }

    @Override
    public String toString() {
        String name = myConverter.toString();
        return myWeight != null
                ? name + " weight: " + myWeight
                : name;
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
        double a = convert(rgb >> 24);
        double r = convert(rgb >> 16);
        double g = convert(rgb >> 8);
        double b = convert(rgb);
        double[] rgba = {r, g, b, a};
        myConverter.convert(rgba);
        return ((convert(a, rgba[3]) << 24) |
                (convert(r, rgba[0]) << 16) |
                (convert(g, rgba[1]) << 8) |
                (convert(b, rgba[2])));
    }

    private static double convert(int value) {
        return (value & 0xFF);
    }

    private int convert(double oldValue, double newValue) {
        if (newValue < 0) {
            newValue = 0;
        } else if (newValue > 255) {
            newValue = 255;
        }
        if (myWeight != null) {
            newValue = newValue * myWeight + oldValue * (1 - myWeight);
        }
        return (int) (newValue + 0.5);
    }
}
