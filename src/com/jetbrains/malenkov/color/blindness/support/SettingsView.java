package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.ide.ui.ColorBlindness;
import com.intellij.ide.ui.ColorBlindnessSupport;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.options.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
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

    @NotNull
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
            private final ChangeListener changeListener = event -> updateFilter();

            private ImageFilter filter;
            private JPanel panel;
            private JLabel error;
            private JComboBox<FilterType> combo;
            private ModifiedMatrixView modified;
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
                        modified = new ModifiedMatrixView();
                        north.add(combo);
                        north.add(modified);
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
                if (modified != null) {
                    modified.addChangeListener(changeListener);
                }
                return panel;
            }

            @Override
            public void disposeUIResources() {
                if (combo != null) {
                    combo.removeActionListener(comboListener);
                }
                if (modified != null) {
                    modified.removeChangeListener(changeListener);
                }
            }

            @Override
            public boolean isModified() {
                if (support instanceof ExtensionPoint) {
                    FilterType type = getSelectedFilterType();
                    Settings settings = SettingsState.get(blindness);
                    if (type != getFilterType(settings.filterType)) return true;
                    if (type == FilterType.MODIFIED_MATRIX && modified != null) {
                        return modified.isModified(settings.modifierOne, settings.modifierTwo);
                    }
                    // TODO: support custom matrix
                }
                if (combo == null) return false;

                return false;
            }

            @Override
            public void apply() {
                if (support instanceof ExtensionPoint) {
                    storeSettings(SettingsState.get(blindness));
                    ExtensionPoint point = (ExtensionPoint) support;
                    point.setFilter(filter);
                    if (blindness == UISettings.getShadowInstance().getColorBlindness()) {
                        IconLoader.setFilter(filter);
                        LafManager.getInstance().updateUI();
                    }
                }
            }

            @Override
            public void reset() {
                Settings settings = SettingsState.get(blindness);
                if (combo != null) combo.setSelectedItem(getFilterType(settings.filterType));
                if (modified != null) modified.set(settings.filterType, settings.modifierOne, settings.modifierTwo);
                // TODO: support custom matrix

                filter = support.getFilter();
                if (view != null) view.setFilter(filter);
                if (error != null) error.setText(filter == null ? "No filter provided." : "The following filter provided:");
            }

            private FilterType getSelectedFilterType() {
                return getFilterType(combo == null ? null : combo.getSelectedItem());
            }

            private void updateFilter() {
                if (support instanceof ExtensionPoint) {
                    Settings settings = new Settings();
                    storeSettings(settings);
                    filter = getFilter(blindness, settings);
                    if (view != null) view.setFilter(filter);
                }
            }

            private void storeSettings(Settings settings) {
                settings.filterType = getSelectedFilterType();
                settings.filterWeight = null; // TODO: support weight
                storeModifiedMatrix(settings);
                // TODO: support custom matrix
            }

            private void storeModifiedMatrix(Settings settings) {
                if (modified != null && settings.filterType == FilterType.MODIFIED_MATRIX) {
                    settings.modifierOne = modified.getOne();
                    settings.modifierTwo = modified.getTwo();
                }
            }
        }
    }

    static ImageFilter getFilter(ColorBlindness blindness, Settings settings) {
        if (settings == null) settings = SettingsState.get(blindness);
        Converter converter = getConverter(blindness, settings);
        return converter == null ? null : new Filter(converter, settings.filterWeight);
    }

    private static Converter getConverter(ColorBlindness blindness, Settings settings) {
        switch (getFilterType(settings.filterType)) {
            case PREDEFINED_MATRIX:
                return ColorBlindnessMatrix.getConverter(blindness);
            case MODIFIED_MATRIX:
                return ColorBlindnessMatrix.getConverter(blindness, settings.modifierOne, settings.modifierTwo);
            case CUSTOM_MATRIX:
                return ColorBlindnessMatrix.getConverter(blindness); // TODO: support custom matrix
            case DALTONIZATION:
                return DaltonizationConverter.getConverter(blindness);
            case SIMULATION:
                return SimulationConverter.getConverter(blindness);
            case GRAY:
                return VectorConverter.GRAY;
            default:
                return null;
        }
    }

    private static FilterType getFilterType(Object type) {
        return type instanceof FilterType ? (FilterType) type : FilterType.DISABLED;
    }
}
