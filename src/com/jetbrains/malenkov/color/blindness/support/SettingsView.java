package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.ide.ui.ColorBlindness;
import com.intellij.ide.ui.ColorBlindnessSupport;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.options.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.ImageFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sergey.Malenkov
 */
public final class SettingsView extends TabbedConfigurable {
    @Nls
    @Override
    public String getDisplayName() {
        return "ColorBlindness Support";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    protected List<Configurable> createConfigurables() {
        List<Configurable> list = new ArrayList<>();
        for (ColorBlindness blindness : ColorBlindness.values()) {
            ColorBlindnessSupport support = ColorBlindnessSupport.get(blindness);
            if (support != null) list.add(new Tab(blindness, support));
        }
        return list;
    }

    private static final class Tab extends CompositeConfigurable<UnnamedConfigurable> implements Configurable.NoScroll {
        private final ColorBlindness blindness;
        private final ColorBlindnessSupport support;

        private Tab(ColorBlindness blindness, ColorBlindnessSupport support) {
            this.blindness = blindness;
            this.support = support;
        }

        @Nls
        @Override
        public String getDisplayName() {
            return UIBundle.message(blindness.key);
        }

        @Nullable
        @Override
        public String getHelpTopic() {
            return null;
        }

        @Override
        protected List<UnnamedConfigurable> createConfigurables() {
            return Collections.singletonList(new FilterPage());
        }

        @Nullable
        @Override
        public JComponent createComponent() {
            JPanel panel = new JPanel(new VerticalLayout(10));
            List<UnnamedConfigurable> list = getConfigurables();
            for (UnnamedConfigurable configurable : list) {
                panel.add(configurable.createComponent());
            }
            return ScrollPaneFactory.createScrollPane(panel,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        }

        private final class FilterPage implements UnnamedConfigurable {
            private final ActionListener comboListener = event -> updateFilter();

            private ImageFilter filter;
            private JPanel panel;
            private JLabel error;
            private JComboBox<FilterType> combo;
            private ColorView view;

            @Nullable
            @Override
            public JComponent createComponent() {
                if (panel == null) {
                    panel = new JPanel(new BorderLayout());
                    if (support instanceof ExtensionPoint) {
                        JPanel north = new JPanel(new HorizontalLayout(5));
                        panel.add(BorderLayout.NORTH, north);
                        combo = new ComboBox<>(FilterType.values());
                        north.add(combo);
                    } else {
                        error = new JLabel();
                        panel.add(BorderLayout.NORTH, error);
                    }
                    if (error == null || filter != null) {
                        view = new ColorView();
                        panel.add(BorderLayout.CENTER, view);
                    }
                }
                if (combo != null) {
                    combo.addActionListener(comboListener);
                }
                return panel;
            }

            @Override
            public void disposeUIResources() {
                if (combo != null) {
                    combo.removeActionListener(comboListener);
                }

            }

            @Override
            public boolean isModified() {
                if (combo == null) return false;

                FilterType type = getFilterType(combo.getSelectedItem());
                Settings settings = SettingsState.get(blindness);
                if (type != getFilterType(settings.filter)) return true;
                if (type != FilterType.CUSTOM_MATRIX) return false;
                // TODO
                return false;
            }

            @Override
            public void apply() throws ConfigurationException {
                if (support instanceof ExtensionPoint) {
                    ExtensionPoint point = (ExtensionPoint) support;
                    point.setFilter(filter);
                    if (blindness == UISettings.getShadowInstance().COLOR_BLINDNESS) {
                        IconLoader.setFilter(filter);
                    }

                    FilterType type = getFilterType(combo.getSelectedItem());
                    Settings settings = SettingsState.get(blindness);
                    settings.filter = type;
                    if (type == FilterType.CUSTOM_MATRIX) {
                        //TODO
                    }
                }
            }

            @Override
            public void reset() {
                Settings settings = SettingsState.get(blindness);
                if (combo != null) combo.setSelectedItem(getFilterType(settings.filter));
                // TODO
                filter = support.getFilter();
                if (view != null) view.setFilter(filter);
                if (error != null) error.setText(filter == null ? "No filter provided." : "The following filter provided:");
            }

            private void updateFilter() {
                if (combo != null) {
                    FilterType type = getFilterType(combo.getSelectedItem());
                    //TODO
                    filter = getFilter(blindness, type);
                    if (view != null) view.setFilter(filter);
                }
            }
        }
    }

    static ImageFilter getFilter(ColorBlindness blindness) {
        Settings settings = SettingsState.get(blindness);
        return getFilter(blindness, settings.filter);
    }

    private static ImageFilter getFilter(ColorBlindness blindness, FilterType type) {
        if (type == FilterType.MATRIX) return MatrixFilter.get(blindness);
        if (type == FilterType.CUSTOM_MATRIX) return MatrixFilter.get(blindness);
        if (type == FilterType.DALTONIZATION) return DaltonizationFilter.get(blindness);
        if (type == FilterType.SIMULATION) return SimulationFilter.get(blindness);
        return null;
    }

    private static FilterType getFilterType(Object type) {
        return type instanceof FilterType ? (FilterType) type : FilterType.DISABLED;
    }
}
