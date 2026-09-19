package io.github.henrique02w.displayauctionhouse.utils;

import io.github.henrique02w.displayauctionhouse.DisplayAuctionHouse;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Converte ItemStack em texto para salvar em YAML.
 *
 * O formato atual ("v2:" + Base64 dos bytes) usa a serialização nativa do Paper, que preserva
 * todos os dados do item e o atualiza automaticamente entre versões do Minecraft.
 */
public class ItemSerializer {

    private static final String PREFIX = "v2:";

    public static String serialize(ItemStack item) {
        if (item == null) return null;
        return PREFIX + Base64.getEncoder().encodeToString(item.serializeAsBytes());
    }

    public static ItemStack deserialize(String data) {
        if (data == null || data.isEmpty()) return null;

        try {
            if (data.startsWith(PREFIX)) {
                byte[] bytes = Base64.getDecoder().decode(data.substring(PREFIX.length()));
                return ItemStack.deserializeBytes(bytes);
            }
            return deserializeLegacy(data);
        } catch (Exception e) {
            DisplayAuctionHouse.getInstance().getLogger().warning("Falha ao ler um item salvo: " + e.getMessage());
            return null;
        }
    }

    /**
     * Lê o formato antigo (1.x): pares "chave=valor;" em Base64. Esse formato só guardava o
     * toString() de cada campo, então é possível recuperar o tipo e a quantidade, mas não os
     * metadados (nome, encantamentos etc.).
     */
    private static ItemStack deserializeLegacy(String data) {
        String decoded = new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);

        Map<String, String> fields = new HashMap<>();
        for (String part : decoded.split(";")) {
            String[] split = part.split("=", 2);
            if (split.length == 2) fields.putIfAbsent(split[0], split[1]);
        }

        Material material = Material.matchMaterial(fields.getOrDefault("type", ""));
        if (material == null || material.isAir()) return null;

        int amount = 1;
        try {
            amount = Integer.parseInt(fields.getOrDefault("amount", "1"));
        } catch (NumberFormatException ignored) {
            // mantém 1
        }

        if (fields.containsKey("meta")) {
            DisplayAuctionHouse.getInstance().getLogger().warning(
                    "Item " + material.name() + " no formato antigo: os metadados (nome, encantamentos etc.) foram perdidos.");
        }
        return new ItemStack(material, Math.max(1, amount));
    }
}
