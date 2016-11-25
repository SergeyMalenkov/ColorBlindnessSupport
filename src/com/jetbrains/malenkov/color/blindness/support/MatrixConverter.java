package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.util.Matrix;
import com.intellij.util.Vector;

/**
 * @author Sergey.Malenkov
 */
final class MatrixConverter implements Converter {
    private final Matrix matrix;
    private final String name;

    MatrixConverter(String name, Matrix matrix) {
        VectorConverter.checkSize("rows", matrix.getRows());
        VectorConverter.checkSize("columns", matrix.getColumns());
        this.matrix = matrix;
        this.name = name + matrix;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void convert(double[] rgba) {
        Vector vector = VectorConverter.toVector(rgba, matrix.getRows()).multiply(matrix);
        // return result via input parameter
        int i = vector.getSize();
        while (0 < i--) rgba[i] = vector.get(i);
    }
}
