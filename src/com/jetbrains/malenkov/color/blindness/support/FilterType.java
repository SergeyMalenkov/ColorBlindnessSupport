package com.jetbrains.malenkov.color.blindness.support;

/**
 * @author Sergey.Malenkov
 */
public enum FilterType {
    DISABLED("Disabled"),
    MATRIX("Predefined matrix"),
    CUSTOM_MATRIX("Custom matrix"),
    DALTONIZATION("Daltonization"),
    SIMULATION("Simulation");
    private final String text;

    FilterType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
