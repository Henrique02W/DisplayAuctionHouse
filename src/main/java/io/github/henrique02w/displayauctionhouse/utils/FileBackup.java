package io.github.henrique02w.displayauctionhouse.utils;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/** Copia um arquivo de dados antes que ele seja sobrescrito, quando algum item não pôde ser lido. */
public final class FileBackup {

    private FileBackup() {}

    public static void create(JavaPlugin plugin, File file) {
        if (!file.exists()) return;

        File backup = new File(file.getParentFile(), file.getName() + ".bak-" + System.currentTimeMillis());
        try {
            Files.copy(file.toPath(), backup.toPath());
            plugin.getLogger().warning("Alguns itens de " + file.getName() + " não puderam ser lidos. Backup criado: " + backup.getName());
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível criar backup de " + file.getName() + ": " + e.getMessage());
        }
    }
}
