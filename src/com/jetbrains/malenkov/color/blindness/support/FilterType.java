package com.jetbrains.malenkov.color.blindness.support;

/**
 * @author Sergey.Malenkov
 */
public enum FilterType {
    DISABLED("Disabled"),
    PREDEFINED_MATRIX("Predefined matrix"),
    MODIFIED_MATRIX("Modified matrix"),
    CUSTOM_MATRIX("Custom matrix"),
    DALTONIZATION("Daltonization"),
    SIMULATION("Simulation"),
    GRAY("Gray");

    private final String text;

    FilterType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
