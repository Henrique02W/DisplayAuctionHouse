package com.displayah.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ColorUtils {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacy('&');

    public static Component color(String text) {
        return LEGACY.deserialize(text);
    }
}