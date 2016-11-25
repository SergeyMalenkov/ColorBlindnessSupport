package com.jetbrains.malenkov.color.blindness.support;


import com.intellij.util.Vector;

/**
 * @author Sergey.Malenkov
 */
final class VectorConverter implements Converter {

    static final Converter GRAY = new VectorConverter("Gray", Vector.create(0.212656, 0.715158, 0.072186));

    private final Vector vector;
    private final String name;

    private VectorConverter(String name, Vector vector) {
        checkSize("size", vector.getSize());
        this.vector = vector;
        this.name = name + vector;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void convert(double[] rgba) {
        double value = toVector(rgba, vector.getSize()).multiply(vector);
        // return result via input parameter
        int i = vector.getSize();
        while (0 < i--) rgba[i] = value;
    }

    static void checkSize(String name, int size) {
        if (size != 3 && size != 4) throw new IllegalArgumentException("unsupported " + name);
    }

    static Vector toVector(double[] rgba, int size) {
        return size == 4
                ? Vector.create(rgba[0], rgba[1], rgba[2], rgba[3])
                : Vector.create(rgba[0], rgba[1], rgba[2]);
    }
}
