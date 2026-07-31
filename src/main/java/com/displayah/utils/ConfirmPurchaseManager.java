package com.displayah.utils;

import com.displayah.DisplayAuctionHouse;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ConfirmPurchaseManager {

    private static final Map<UUID, ConfirmSession> sessions = new ConcurrentHashMap<>();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public static void awaitConfirm(DisplayAuctionHouse plugin, Player player, String listingId) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");
        player.sendMessage(ColorUtils.color(prefix + "&aDigite &2&lCOMPRAR &apara confirmar, ou &ccancelar&a para cancelar."));

        ConfirmSession session = new ConfirmSession(plugin, player, listingId);
        sessions.put(player.getUniqueId(), session);
        plugin.getServer().getPluginManager().registerEvents(session, plugin);

        session.task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (sessions.remove(player.getUniqueId()) != null) {
                HandlerList.unregisterAll(session);
                player.sendMessage(ColorUtils.color(prefix + "&cConfirmação expirada."));
            }
        }, 20L * 20);
    }

    private static class ConfirmSession implements Listener {
        private final DisplayAuctionHouse plugin;
        private final Player player;
        private final String listingId;
        private BukkitTask task;

        ConfirmSession(DisplayAuctionHouse plugin, Player player, String listingId) {
            this.plugin = plugin;
            this.player = player;
            this.listingId = listingId;
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncChatEvent event) {
            if (!event.getPlayer().getUniqueId().equals(player.getUniqueId())) return;

            event.setCancelled(true);

            String msg = LEGACY.serialize(event.message()).trim();
            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");

            sessions.remove(player.getUniqueId());
            HandlerList.unregisterAll(this);
            if (task != null) task.cancel();

            if (msg.equalsIgnoreCase("comprar") || msg.equalsIgnoreCase("buy") || msg.equalsIgnoreCase("confirmar") || msg.equalsIgnoreCase("confirm")) {
                plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getListingManager().purchaseListing(player, listingId));
            } else {
                player.sendMessage(ColorUtils.color(prefix + "&7Compra cancelada."));
            }
        }
    }
}