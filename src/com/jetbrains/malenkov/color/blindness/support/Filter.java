package com.jetbrains.malenkov.color.blindness.support;

import java.awt.image.RGBImageFilter;

/**
 * @author Sergey.Malenkov
 */
final class Filter extends RGBImageFilter {
    private final Converter converter;
    private final Double weight;

    Filter(Converter converter, Double weight) {
        if (weight != null && (weight <= 0 || 1 <= weight)) {
            throw new IllegalArgumentException("weight " + weight + " out of [0..1]");
        }
        canFilterIndexColorModel = true;
        this.converter = converter;
        this.weight = weight;
    }

    @Override
    public String toString() {
        String name = converter.toString();
        return weight != null
                ? name + " weight: " + weight
                : name;
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
        double a = convert(rgb >> 24);
        double r = convert(rgb >> 16);
        double g = convert(rgb >> 8);
        double b = convert(rgb);
        double[] rgba = {r, g, b, a};
        converter.convert(rgba);
        return ((convert(a, rgba[3]) << 24) |
                (convert(r, rgba[0]) << 16) |
                (convert(g, rgba[1]) << 8) |
                (convert(b, rgba[2])));
    }

    private static double convert(int value) {
        return (value & 0xFF);
    }

    private int convert(double oldValue, double newValue) {
        if (!Double.isFinite(newValue)) {
            newValue = 0;
        } else if (newValue < 0) {
            newValue = 0;
        } else if (newValue > 255) {
            newValue = 255;
        }
        if (weight != null) {
            newValue = newValue * weight + oldValue * (1 - weight);
        }
        return (int) (newValue + 0.5);
    }
}
