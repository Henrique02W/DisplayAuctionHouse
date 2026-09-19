package io.github.henrique02w.displayauctionhouse.utils;

import io.github.henrique02w.displayauctionhouse.DisplayAuctionHouse;
import io.github.henrique02w.displayauctionhouse.models.AuctionListing;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PurchaseGuiManager implements Listener {

    private static final int ITEM_SLOT = 13;
    private static final int CONFIRM_SLOT = 11;
    private static final int CANCEL_SLOT = 15;

    private final DisplayAuctionHouse plugin;

    public PurchaseGuiManager(DisplayAuctionHouse plugin) {
        this.plugin = plugin;
    }

    public void openPurchaseGui(Player player, AuctionListing listing) {
        Inventory gui = plugin.getServer().createInventory(
                new PurchaseHolder(listing.getId()),
                27,
                Component.text("Confirmar compra")
        );

        ItemStack filler = createButton(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        ItemStack preview = listing.getItem();
        ItemMeta previewMeta = preview.getItemMeta();
        if (previewMeta != null) {
            List<Component> lore = previewMeta.hasLore() ? new ArrayList<>(previewMeta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            lore.add(Component.text("Vendedor: " + listing.getSellerName()));
            lore.add(Component.text("Preco: $" + String.format("%.2f", listing.getPrice())));
            lore.add(Component.text("Expira: " + listing.getTimeRemaining()));
            previewMeta.lore(lore);
            preview.setItemMeta(previewMeta);
        }

        gui.setItem(ITEM_SLOT, preview);
        gui.setItem(CONFIRM_SLOT, createButton(
                Material.LIME_CONCRETE,
                "Confirmar compra",
                List.of("Clique para comprar por $" + String.format("%.2f", listing.getPrice()))
        ));
        gui.setItem(CANCEL_SLOT, createButton(
                Material.RED_CONCRETE,
                "Cancelar",
                List.of("Clique para fechar sem comprar")
        ));

        player.openInventory(gui);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof PurchaseHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType() == InventoryType.PLAYER) return;

        int slot = event.getRawSlot();
        if (slot == CANCEL_SLOT) {
            player.closeInventory();
            return;
        }

        if (slot != CONFIRM_SLOT) return;

        AuctionListing listing = plugin.getListingManager().getListingById(holder.listingId());
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");

        if (listing == null) {
            player.closeInventory();
            player.sendMessage(ColorUtils.color(prefix + "&cEste item nao esta mais a venda."));
            return;
        }

        if (listing.getSellerUUID().equals(player.getUniqueId())) {
            player.closeInventory();
            player.sendMessage(ColorUtils.color(prefix +
                    plugin.getConfig().getString("messages.your-own-listing", "&cVoce nao pode comprar seu proprio item.")));
            return;
        }

        if (!plugin.getEconomyManager().has(player, listing.getPrice())) {
            player.closeInventory();
            player.sendMessage(ColorUtils.color(prefix +
                    plugin.getConfig().getString("messages.not-enough-money", "&cSaldo insuficiente.")
                            .replace("{price}", String.format("%.2f", listing.getPrice()))));
            return;
        }

        player.closeInventory();
        plugin.getListingManager().purchaseListing(player, holder.listingId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof PurchaseHolder) {
            event.setCancelled(true);
        }
    }

    private ItemStack createButton(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name));
            meta.lore(loreLines.stream().map(Component::text).toList());
            item.setItemMeta(meta);
        }
        return item;
    }

    private record PurchaseHolder(String listingId) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
