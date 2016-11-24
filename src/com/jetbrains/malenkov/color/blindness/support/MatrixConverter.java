package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.util.Matrix;
import com.intellij.util.Vector;

/**
 * @author Sergey.Malenkov
 */
final class MatrixConverter implements Converter {
    private final Matrix myMatrix;
    private final String myName;

    MatrixConverter(String name, Matrix matrix) {
        int rows = matrix.getRows();
        if (rows != 3 && rows != 4) throw new IllegalArgumentException("unsupported rows");
        int columns = matrix.getColumns();
        if (columns != 3 && columns != 4) throw new IllegalArgumentException("unsupported columns");
        myMatrix = matrix;
        myName = name;
    }

    @Override
    public String toString() {
        return myName;
    }

    @Override
    public void convert(double[] rgba) {
        Vector vector = myMatrix.getRows() == 4
                ? Vector.create(rgba[0], rgba[1], rgba[2], rgba[3]).multiply(myMatrix)
                : Vector.create(rgba[0], rgba[1], rgba[2]).multiply(myMatrix);
        // return result via input parameter
        int i = vector.getSize();
        while (0 < i--) rgba[i] = vector.get(i);
    }
}
