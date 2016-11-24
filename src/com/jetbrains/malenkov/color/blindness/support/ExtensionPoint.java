/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.malenkov.color.blindness.support;

import com.intellij.ide.ui.ColorBlindness;
import com.intellij.ide.ui.ColorBlindnessSupport;
import com.intellij.ide.ui.UISettings;
import com.intellij.openapi.util.IconLoader;

import java.awt.image.ImageFilter;

public class ExtensionPoint extends ColorBlindnessSupport {

    public static final class Protanopia extends ExtensionPoint {
        public Protanopia() {
            setFilter(SettingsView.getFilter(ColorBlindness.protanopia));
        }
    }

    public static final class Deuteranopia extends ExtensionPoint {
        public Deuteranopia() {
            setFilter(SettingsView.getFilter(ColorBlindness.deuteranopia));
        }
    }

    public static final class Tritanopia extends ExtensionPoint {
        public Tritanopia() {
            setFilter(SettingsView.getFilter(ColorBlindness.tritanopia));
        }
    }

    private ImageFilter filter;

    @Override
    public ImageFilter getFilter() {
        return filter;
    }

    void setFilter(ImageFilter filter) {
        this.filter = filter;
    }
}
