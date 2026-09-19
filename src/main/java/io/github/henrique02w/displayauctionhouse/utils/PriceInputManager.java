package io.github.henrique02w.displayauctionhouse.utils;

import io.github.henrique02w.displayauctionhouse.DisplayAuctionHouse;
import io.github.henrique02w.displayauctionhouse.models.AuctionDisplay;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PriceInputManager {

    private static final Map<UUID, PriceSession> sessions = new ConcurrentHashMap<>();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    public static void promptPrice(DisplayAuctionHouse plugin, Player player, ItemStack item, AuctionDisplay display) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");
        player.sendMessage(ColorUtils.color(prefix + "&7Digite o &epreço &7de venda no chat:"));
        player.sendMessage(ColorUtils.color(prefix + "&7(Digite &ccancelar &7para cancelar)"));

        PriceSession session = new PriceSession(plugin, player, item, display);
        sessions.put(player.getUniqueId(), session);
        plugin.getServer().getPluginManager().registerEvents(session, plugin);

        session.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (sessions.remove(player.getUniqueId()) != null) {
                    HandlerList.unregisterAll(session);
                    player.sendMessage(ColorUtils.color(prefix + "&cTempo esgotado."));
                }
            }
        }.runTaskLater(plugin, 20L * 30);
    }

    private static class PriceSession implements Listener {
        private final DisplayAuctionHouse plugin;
        private final Player player;
        private final AuctionDisplay display;
        BukkitTask task;

        PriceSession(DisplayAuctionHouse plugin, Player player, ItemStack item, AuctionDisplay display) {
            this.plugin = plugin;
            this.player = player;
            this.display = display;
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

            if (msg.equalsIgnoreCase("cancelar") || msg.equalsIgnoreCase("cancel")) {
                player.sendMessage(ColorUtils.color(prefix + "&7Listagem cancelada."));
                return;
            }

            double price;
            try {
                price = Double.parseDouble(msg.replace(",", "."));
            } catch (NumberFormatException e) {
                player.sendMessage(ColorUtils.color(prefix + "&cValor inválido."));
                return;
            }

            double min = plugin.getConfig().getDouble("min-price", 1.0);
            double max = plugin.getConfig().getDouble("max-price", 10000000.0);

            if (price < min || price > max) {
                String invalidMsg = plugin.getConfig().getString("messages.invalid-price", "&cPreço inválido.")
                        .replace("{min}", String.format("%.2f", min))
                        .replace("{max}", String.format("%.2f", max));
                player.sendMessage(ColorUtils.color(prefix + invalidMsg));
                return;
            }

            double finalPrice = price;
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (display.isOccupied()) {
                    player.sendMessage(ColorUtils.color(prefix + "&cEste display foi ocupado enquanto você digitava."));
                    return;
                }

                ItemStack handItem = player.getInventory().getItemInMainHand();
                if (handItem.getType().isAir()) {
                    player.sendMessage(ColorUtils.color(prefix + "&cVocê não tem mais o item na mão."));
                    return;
                }

                ItemStack toSell = handItem.clone();
                toSell.setAmount(1);
                handItem.setAmount(handItem.getAmount() - 1);
                player.getInventory().setItemInMainHand(handItem.getAmount() <= 0 ? null : handItem);

                plugin.getListingManager().createListing(player, toSell, finalPrice, display);

                String listedMsg = plugin.getConfig().getString("messages.item-listed", "&aItem listado!")
                        .replace("{price}", String.format("%.2f", finalPrice));
                player.sendMessage(ColorUtils.color(prefix + listedMsg));
            });
        }
    }
}