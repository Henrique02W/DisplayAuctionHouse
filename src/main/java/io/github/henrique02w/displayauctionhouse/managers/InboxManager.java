package io.github.henrique02w.displayauctionhouse.managers;

import io.github.henrique02w.displayauctionhouse.DisplayAuctionHouse;
import io.github.henrique02w.displayauctionhouse.utils.ColorUtils;
import io.github.henrique02w.displayauctionhouse.utils.FileBackup;
import io.github.henrique02w.displayauctionhouse.utils.ItemSerializer;

import net.kyori.adventure.text.Component;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class InboxManager implements Listener {

    private final DisplayAuctionHouse plugin;
    private final Map<UUID, List<ItemStack>> inboxes = new HashMap<>();
    private File inboxFile;
    private FileConfiguration inboxConfig;

    public InboxManager(DisplayAuctionHouse plugin) {
        this.plugin = plugin;
        inboxFile = new File(plugin.getDataFolder(), "inboxes.yml");
        inboxConfig = YamlConfiguration.loadConfiguration(inboxFile);
    }

    public void loadInboxes() {
        inboxes.clear();
        ConfigurationSection section = inboxConfig.getConfigurationSection("inboxes");
        if (section == null) return;

        boolean loadFailed = false;
        for (String uuidStr : section.getKeys(false)) {
            List<String> itemDataList = section.getStringList(uuidStr + ".items");
            List<ItemStack> items = new ArrayList<>();
            for (String data : itemDataList) {
                ItemStack item = ItemSerializer.deserialize(data);
                if (item != null) {
                    items.add(item);
                } else {
                    loadFailed = true;
                }
            }
            if (!items.isEmpty()) {
                inboxes.put(UUID.fromString(uuidStr), items);
            }
        }

        if (loadFailed) FileBackup.create(plugin, inboxFile);

        plugin.getLogger().info("Carregadas inboxes para " + inboxes.size() + " jogadores.");
    }

    public void saveInboxes() {
        inboxConfig.set("inboxes", null);
        for (Map.Entry<UUID, List<ItemStack>> entry : inboxes.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            String path = "inboxes." + entry.getKey().toString();
            List<String> itemDataList = new ArrayList<>();
            for (ItemStack item : entry.getValue()) {
                itemDataList.add(ItemSerializer.serialize(item));
            }
            inboxConfig.set(path + ".items", itemDataList);
        }
        try {
            inboxConfig.save(inboxFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar inboxes: " + e.getMessage());
        }
    }

    public void addToInbox(UUID playerUUID, ItemStack item) {
        inboxes.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(item.clone());
        saveInboxes();
    }

    public List<ItemStack> getInbox(UUID playerUUID) {
        return inboxes.getOrDefault(playerUUID, new ArrayList<>());
    }

    public void openInbox(Player player) {
        List<ItemStack> items = getInbox(player.getUniqueId());
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");

        if (items.isEmpty()) {
            player.sendMessage(ColorUtils.color(
                    prefix + plugin.getConfig().getString("messages.inbox-empty", "&7Inbox vazia.")
            ));
            return;
        }

        int size = (int) (Math.ceil(items.size() / 9.0) * 9);
        size = Math.min(size, 54);

        InboxHolder holder = new InboxHolder();
        Inventory gui = plugin.getServer().createInventory(holder, size, Component.text("✉ Caixa de Entrada ✉"));
        holder.inventory = gui;

        List<ItemStack> toShow = items.subList(0, Math.min(items.size(), 54));
        for (ItemStack item : toShow) {
            gui.addItem(item.clone());
        }

        player.openInventory(gui);

        if (items.size() <= 54) {
            inboxes.remove(player.getUniqueId());
        } else {
            List<ItemStack> remaining = new ArrayList<>(items.subList(54, items.size()));
            inboxes.put(player.getUniqueId(), remaining);
            player.sendMessage(ColorUtils.color(
                    prefix + "&7Mostrando os primeiros 54 itens. Você ainda tem &e" +
                            remaining.size() + "&7 itens. Use &f/dah inbox&7 novamente."
            ));
        }

        saveInboxes();
    }

    public boolean hasItems(UUID playerUUID) {
        List<ItemStack> items = inboxes.get(playerUUID);
        return items != null && !items.isEmpty();
    }

    /** Ao fechar a caixa de entrada, o que o jogador não pegou volta para ela em vez de ser perdido. */
    @EventHandler
    public void onInboxClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof InboxHolder) {
            returnLeftovers(event.getPlayer().getUniqueId(), event.getInventory());
        }
    }

    /** Usado no desligamento: devolve os itens de quem ainda estava com a caixa de entrada aberta. */
    public void returnOpenInboxes() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof InboxHolder) {
                returnLeftovers(player.getUniqueId(), top);
            }
        }
    }

    private void returnLeftovers(UUID playerUUID, Inventory inventory) {
        boolean returned = false;
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType().isAir()) continue;
            inboxes.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(item.clone());
            returned = true;
        }
        inventory.clear();
        if (returned) saveInboxes();
    }

    private static final class InboxHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
