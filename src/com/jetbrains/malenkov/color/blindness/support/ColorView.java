package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.util.RetinaImage;
import com.intellij.util.ui.ImageUtil;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;

import static com.intellij.util.ui.StartupUiUtil.drawImage;

/**
 * @author Sergey.Malenkov
 */
final class ColorView extends JComponent {
    private ImageFilter imageFilter;
    private Image imageDefault;
    private Image imageCached;

    @Override
    public Dimension getMinimumSize() {
        if (isMinimumSizeSet()) return super.getMinimumSize();

        return getPreferredSize();
    }

    @Override
    public Dimension getPreferredSize() {
        if (isPreferredSizeSet()) return super.getPreferredSize();

        Dimension size = new Dimension(
                imageDefault != null ? ImageUtil.getUserWidth(imageDefault) : JBUI.scale(360),
                imageDefault != null ? ImageUtil.getUserWidth(imageDefault) : JBUI.scale(200));

        JBInsets.addTo(size, getInsets());
        return size;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Rectangle bounds = new Rectangle(getWidth(), getHeight());
        JBInsets.removeFrom(bounds, getInsets());
        if (bounds.isEmpty()) return;

        if (imageDefault != null) {
            if (imageCached == null) imageCached = ImageUtil.filter(imageDefault, imageFilter);
        } else if (imageCached == null || bounds.width != ImageUtil.getUserWidth(imageCached) || bounds.height != ImageUtil.getUserWidth(imageCached)) {
            BufferedImage image = ImageUtil.createImage(g, bounds.width, bounds.height, BufferedImage.TYPE_INT_RGB);

            int width = ImageUtil.getRealWidth(image);
            int height = ImageUtil.getRealHeight(image);
            int[] array = new int[width * height];
            float wMax = (float) (width - 1);
            float hMax = (float) (height - 1);
            for (int i = 0, h = 0; h < height; h++) {
                for (int w = 0; w < width; w++, i++) {
                    float level = 2 * h / hMax;
                    float saturation = (level > 1f) ? 1 : level;
                    float brightness = (level > 1f) ? 2 - level : 1;
                    array[i] = Color.HSBtoRGB(w / wMax, saturation, brightness);
                }
            }
            image.setRGB(0, 0, width, height, array, 0, width);
            imageCached = ImageUtil.filter(image, imageFilter);
            if (imageCached != image && (g instanceof Graphics2D ? UIUtil.isRetina((Graphics2D) g) : UIUtil.isRetina())) {
                imageCached = RetinaImage.createFrom(imageCached);
            }
        }
        drawImage(g, imageCached, bounds, null, this);
    }

    void setFilter(ImageFilter filter) {
        imageCached = null;
        imageFilter = filter;
        repaint();
    }

    void setImage(Image image) {
        imageCached = null;
        imageDefault = image;
        repaint();
    }
}
