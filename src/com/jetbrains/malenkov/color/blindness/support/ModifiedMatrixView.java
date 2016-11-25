package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author Sergey.Malenkov
 */
final class ModifiedMatrixView extends JComponent {
    private final JSlider one = new JSlider();
    private final JSlider two = new JSlider();

    ModifiedMatrixView() {
        add(one);
        add(two);
    }

    void addChangeListener(ChangeListener listener) {
        one.addChangeListener(listener);
        two.addChangeListener(listener);
    }

    void removeChangeListener(ChangeListener listener) {
        one.removeChangeListener(listener);
        two.removeChangeListener(listener);
    }

    double getOne() {
        return getValue(one);
    }

    double getTwo() {
        return getValue(two);
    }

    boolean isModified(Double one, Double two) {
        return isModified(this.one, one) || isModified(this.two, two);
    }

    void set(FilterType type, Double one, Double two) {
        setVisible(type == FilterType.MODIFIED_MATRIX);
        setValue(this.one, one);
        setValue(this.two, two);
    }

    @Override
    public Dimension getMinimumSize() {
        if (isMinimumSizeSet()) return super.getMinimumSize();

        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) return super.getPreferredSize();

        Dimension one = this.one.getPreferredSize();
        Dimension two = this.two.getPreferredSize();

        Dimension size = new Dimension(
                one.width + JBUI.scale(5) + two.width,
                Math.max(one.height, two.height));

        JBInsets.addTo(size, getInsets());
        return size;
    }

    @Override
    public void doLayout() {
        Rectangle bounds = new Rectangle(getWidth(), getHeight());
        JBInsets.removeFrom(bounds, getInsets());

        int width = (bounds.width - JBUI.scale(5)) / 2;
        if (width > 0) {
            one.setBounds(bounds.x, bounds.y, width, bounds.height);
            two.setBounds(bounds.x + bounds.width - width, bounds.y, width, bounds.height);
            one.setVisible(true);
            two.setVisible(true);
        } else {
            one.setVisible(false);
            two.setVisible(false);
        }
    }

    private static double getValue(JSlider slider) {
        int min = slider.getMinimum();
        int max = slider.getMaximum();
        double d = slider.getValue() - min;
        return d / (max - min);
    }

    private static int getValue(JSlider slider, Double value) {
        int min = slider.getMinimum();
        int max = slider.getMaximum();
        double d = value != null && Double.isFinite(value) ? value : .7;
        return d <= 0 ? min : 1 <= d ? max : min + (int) Math.round(d * (max - min));
    }

    private static void setValue(JSlider slider, Double value) {
        slider.setValue(getValue(slider, value));
    }

    private static boolean isModified(JSlider slider, Double value) {
        return slider.getValue() != getValue(slider, value);
    }
}
