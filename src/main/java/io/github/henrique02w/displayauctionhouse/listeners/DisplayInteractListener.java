package io.github.henrique02w.displayauctionhouse.listeners;

import io.github.henrique02w.displayauctionhouse.DisplayAuctionHouse;
import io.github.henrique02w.displayauctionhouse.models.AuctionDisplay;
import io.github.henrique02w.displayauctionhouse.models.AuctionListing;
import io.github.henrique02w.displayauctionhouse.utils.ColorUtils;
import io.github.henrique02w.displayauctionhouse.utils.PriceInputManager;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class DisplayInteractListener implements Listener {

    private final DisplayAuctionHouse plugin;

    public DisplayInteractListener(DisplayAuctionHouse plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractItemDisplay(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemDisplay entity)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        AuctionDisplay display = plugin.getDisplayManager().getDisplayByItemDisplay(entity.getUniqueId());
        if (display == null) return;

        event.setCancelled(true);
        handleDisplayInteract(event.getPlayer(), display);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        AuctionDisplay display = plugin.getDisplayManager().getDisplayByBlock(block.getLocation());
        if (display == null) return;

        event.setCancelled(true);
        handleDisplayInteract(event.getPlayer(), display);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreakDisplayBlock(BlockBreakEvent event) {
        Block block = event.getBlock();
        AuctionDisplay display = plugin.getDisplayManager().getDisplayByBlock(block.getLocation());
        if (display == null) return;

        event.setCancelled(true);
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");
        event.getPlayer().sendMessage(ColorUtils.color(prefix + "&cEste display e sua placa sao protegidos. Use &f/dah removedisplay " + display.getId() + "&c."));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEditDisplaySign(SignChangeEvent event) {
        AuctionDisplay display = plugin.getDisplayManager().getDisplayByBlock(event.getBlock().getLocation());
        if (display == null) return;

        event.setCancelled(true);
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");
        event.getPlayer().sendMessage(ColorUtils.color(prefix + "&cEsta placa pertence ao display &e" + display.getId() + "&c."));
    }

    private void handleDisplayInteract(Player player, AuctionDisplay display) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");

        if (!display.isOccupied()) {
            if (!player.isSneaking()) {
                player.sendMessage(ColorUtils.color(prefix + "&7Display &evazio&7."));
                player.sendMessage(ColorUtils.color(prefix + "&aSHIFT+DIREITO &7para colocar um item à venda."));
                return;
            }

            if (!player.hasPermission("dah.sell")) {
                player.sendMessage(ColorUtils.color(prefix +
                        plugin.getConfig().getString("messages.no-permission", "&cSem permissão.")));
                return;
            }

            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem.getType().isAir()) {
                player.sendMessage(ColorUtils.color(prefix +
                        plugin.getConfig().getString("messages.no-item-hand", "&cSegure um item na mão.")));
                return;
            }

            int maxListings = plugin.getConfig().getInt("max-listings-per-player", 5);
            if (plugin.getListingManager().getActiveListingCount(player.getUniqueId()) >= maxListings) {
                player.sendMessage(ColorUtils.color(prefix +
                        plugin.getConfig().getString("messages.max-listings-reached", "&cLimite atingido.")
                                .replace("{max}", String.valueOf(maxListings))));
                return;
            }

            PriceInputManager.promptPrice(plugin, player, handItem, display);
            return;
        }

        AuctionListing listing = plugin.getListingManager().getListingById(display.getCurrentListingId());
        if (listing == null) {
            plugin.getDisplayManager().clearDisplayItem(display);
            return;
        }

        if (player.isSneaking()) {
            boolean isOwner = listing.getSellerUUID().equals(player.getUniqueId());
            boolean isAdmin = player.hasPermission("dah.admin");
            if (!isOwner && !isAdmin) {
                player.sendMessage(ColorUtils.color(prefix + "&cVocê não pode remover o item de outro jogador."));
                return;
            }
            plugin.getListingManager().removeListing(listing.getId(), true);
            return;
        }

        if (!player.hasPermission("dah.buy")) {
            player.sendMessage(ColorUtils.color(prefix +
                    plugin.getConfig().getString("messages.no-permission", "&cSem permissão.")));
            return;
        }

        if (listing.getSellerUUID().equals(player.getUniqueId())) {
            player.sendMessage(ColorUtils.color(prefix +
                    plugin.getConfig().getString("messages.your-own-listing", "&cNão pode comprar seu próprio item.")));
            return;
        }

        if (!plugin.getEconomyManager().available()) {
            player.sendMessage(ColorUtils.color(prefix +
                    plugin.getConfig().getString("messages.vault-not-found", "&cEconomia indisponível.")));
            return;
        }

        if (!plugin.getEconomyManager().has(player, listing.getPrice())) {
            player.sendMessage(ColorUtils.color(prefix +
                    plugin.getConfig().getString("messages.not-enough-money", "&cSaldo insuficiente.")
                            .replace("{price}", String.format("%.2f", listing.getPrice()))));
            return;
        }

        plugin.getPurchaseGuiManager().openPurchaseGui(player, listing);
    }
}
