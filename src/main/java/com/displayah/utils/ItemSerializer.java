package com.displayah.utils;

import org.bukkit.inventory.ItemStack;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ItemSerializer {

    public static String serialize(ItemStack item) {
        if (item == null) return null;

        Map<String, Object> map = item.serialize();
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey()).append('=').append(entry.getValue()).append(';');
        }

        return Base64.getEncoder().encodeToString(sb.toString().getBytes());
    }

    public static ItemStack deserialize(String data) {
        if (data == null || data.isEmpty()) return null;

        try {
            String decoded = new String(Base64.getDecoder().decode(data));
            Map<String, Object> map = new HashMap<>();

            for (String part : decoded.split(";")) {
                if (part.isEmpty()) continue;
                String[] split = part.split("=", 2);
                if (split.length == 2) {
                    map.put(split[0], split[1]);
                }
            }

            return ItemStack.deserialize(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}