package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.ide.ui.ColorBlindness;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Sergey.Malenkov
 */
@State(name = "ColorBlindnessSettings", storages = @Storage("ColorBlindnessSettings.xml"))
public final class SettingsState implements PersistentStateComponent<SettingsState> {
    private final Map<ColorBlindness, Settings> map = new EnumMap<>(ColorBlindness.class);

    @SuppressWarnings("unused")
    public Map<ColorBlindness, Settings> getMap() {
        return map;
    }

    @SuppressWarnings("WeakerAccess")
    public void setMap(Map<ColorBlindness, Settings> map) {
        if (this.map != map) {
            this.map.clear();
            this.map.putAll(map);
        }
    }

    public SettingsState getState() {
        return this;
    }

    public void loadState(SettingsState state) {
        if (state != null) setMap(state.map);
    }

    static Settings get(ColorBlindness blindness) {
        return ServiceManager.getService(SettingsState.class).map.computeIfAbsent(blindness, key -> new Settings());
    }
}
