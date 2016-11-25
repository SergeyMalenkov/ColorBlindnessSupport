package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.ide.ui.ColorBlindness;

final class DaltonizationConverter implements Converter {

    private static final Converter PROTANOPIA
            = new DaltonizationConverter("Protanopia", 0, 2.02344, -2.52581, 0, 1, 0, 0, 0, 1);

    private static final Converter DEUTERANOPIA
            = new DaltonizationConverter("Deuteranopia", 1, 0, 0, 0.494207, 0, 1.24827, 0, 0, 1);

    private static final Converter TRITANOPIA
            = new DaltonizationConverter("Tritanopia", 1, 0, 0, 0, 1, 0, -0.395913, 0.801109, 0);

    private final double[] matrix;
    private final String name;

    private DaltonizationConverter(String name, double... matrix) {
        this.matrix = matrix;
        this.name = name + " (daltonization)";
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void convert(double[] rgba) {
        double srcR = rgba[0];
        double srcG = rgba[1];
        double srcB = rgba[2];
        // RGB to LMS matrix conversion
        double L = (17.8824 * srcR) + (43.5161 * srcG) + (4.11935 * srcB);
        double M = (3.45565 * srcR) + (27.1554 * srcG) + (3.86714 * srcB);
        double S = (0.0299566 * srcR) + (0.184309 * srcG) + (1.46709 * srcB);
        // Simulate color blindness
        double l = L * matrix[0] + M * matrix[1] + S * matrix[2];
        double m = L * matrix[3] + M * matrix[4] + S * matrix[5];
        double s = L * matrix[6] + M * matrix[7] + S * matrix[8];
        // LMS to RGB matrix conversion
        double R = (0.0809444479 * l) + (-0.130504409 * m) + (0.116721066 * s);
        double G = (-0.0102485335 * l) + (0.0540193266 * m) + (-0.113614708 * s);
        double B = (-0.000365296938 * l) + (-0.00412161469 * m) + (0.693511405 * s);
        // Isolate invisible colors to color vision deficiency (calculate error matrix)
        R = srcR - R;
        G = srcG - G;
        B = srcB - B;
        // Shift colors towards visible spectrum (apply error modifications)
        // and add compensation to original values
        rgba[0] = fix(srcR + (0.0 * R) + (0.0 * G) + (0.0 * B));
        rgba[1] = fix(srcG + (0.7 * R) + (1.0 * G) + (0.0 * B));
        rgba[2] = fix(srcB + (0.7 * R) + (0.0 * G) + (1.0 * B));
        // return result via input parameter
    }

    private static double fix(double value) {
        return Double.isNaN(value) || value < 0 ? 0 : value > 255 ? 255 : value;
    }

    static Converter getConverter(ColorBlindness blindness) {
        if (blindness == ColorBlindness.protanopia) return PROTANOPIA;
        if (blindness == ColorBlindness.deuteranopia) return DEUTERANOPIA;
        if (blindness == ColorBlindness.tritanopia) return TRITANOPIA;
        return null;
    }
}
