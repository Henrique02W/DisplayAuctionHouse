package com.displayah.managers;

import com.displayah.DisplayAuctionHouse;
import com.displayah.models.AuctionDisplay;
import com.displayah.models.AuctionListing;
import com.displayah.utils.ColorUtils;
import com.displayah.utils.ItemSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ListingManager {

    private final DisplayAuctionHouse plugin;
    private final Map<String, AuctionListing> listings = new HashMap<>();
    private final Set<String> purchasesInProgress = new HashSet<>();
    private File listingsFile;
    private FileConfiguration listingsConfig;

    public ListingManager(DisplayAuctionHouse plugin) {
        this.plugin = plugin;
        listingsFile = new File(plugin.getDataFolder(), "listings.yml");
        listingsConfig = YamlConfiguration.loadConfiguration(listingsFile);
    }

    public void loadListings() {
        listings.clear();
        ConfigurationSection section = listingsConfig.getConfigurationSection("listings");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            try {
                String sellerUUIDStr = section.getString(id + ".seller-uuid");
                String sellerName = section.getString(id + ".seller-name");
                double price = section.getDouble(id + ".price");
                long listedAt = section.getLong(id + ".listed-at");
                long expiresAt = section.getLong(id + ".expires-at");
                String displayId = section.getString(id + ".display-id");
                String itemData = section.getString(id + ".item");

                if (sellerUUIDStr == null || itemData == null) continue;

                ItemStack item = ItemSerializer.deserialize(itemData);
                if (item == null) continue;

                AuctionListing listing = new AuctionListing(
                        id, UUID.fromString(sellerUUIDStr), sellerName,
                        item, price, listedAt, expiresAt
                );
                listing.setDisplayId(displayId);
                listings.put(id, listing);
            } catch (Exception e) {
                plugin.getLogger().warning("Erro ao carregar listing " + id + ": " + e.getMessage());
            }
        }

        for (AuctionListing listing : listings.values()) {
            if (listing.getDisplayId() != null) {
                AuctionDisplay display = plugin.getDisplayManager().getDisplayById(listing.getDisplayId());
                if (display != null) {
                    display.setCurrentListingId(listing.getId());
                    plugin.getDisplayManager().updateDisplayItem(display, listing);
                }
            }
        }

        plugin.getLogger().info("Carregados " + listings.size() + " listings.");
    }

    public void saveListings() {
        listingsConfig.set("listings", null);
        for (AuctionListing listing : listings.values()) {
            String path = "listings." + listing.getId();
            listingsConfig.set(path + ".seller-uuid", listing.getSellerUUID().toString());
            listingsConfig.set(path + ".seller-name", listing.getSellerName());
            listingsConfig.set(path + ".price", listing.getPrice());
            listingsConfig.set(path + ".listed-at", listing.getListedAt());
            listingsConfig.set(path + ".expires-at", listing.getExpiresAt());
            listingsConfig.set(path + ".display-id", listing.getDisplayId());
            listingsConfig.set(path + ".item", ItemSerializer.serialize(listing.getItem()));
        }
        try {
            listingsConfig.save(listingsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar listings: " + e.getMessage());
        }
    }

    public String createListing(Player seller, ItemStack item, double price, AuctionDisplay display) {
        int durationHours = plugin.getConfig().getInt("listing-duration-hours", 48);
        long listedAt = System.currentTimeMillis();
        long expiresAt = durationHours > 0 ? listedAt + (durationHours * 3600000L) : -1;

        String id = UUID.randomUUID().toString().substring(0, 8);
        AuctionListing listing = new AuctionListing(
                id, seller.getUniqueId(), seller.getName(),
                item, price, listedAt, expiresAt
        );
        listing.setDisplayId(display.getId());

        listings.put(id, listing);
        display.setCurrentListingId(id);
        plugin.getDisplayManager().updateDisplayItem(display, listing);
        saveListings();
        return id;
    }

    public synchronized boolean purchaseListing(Player buyer, String listingId) {
        if (purchasesInProgress.contains(listingId)) return false;

        AuctionListing listing = listings.get(listingId);
        if (listing == null) return false;
        purchasesInProgress.add(listingId);

        try {
            String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");

            if (listing.isExpired() || listing.getSellerUUID().equals(buyer.getUniqueId())) {
                buyer.sendMessage(ColorUtils.color(prefix + "&cEste item nao pode ser comprado."));
                return false;
            }

            double price = listing.getPrice();
            if (!Double.isFinite(price) || price <= 0) {
                buyer.sendMessage(ColorUtils.color(prefix + "&cPreco invalido neste item."));
                return false;
            }

            double taxRate = plugin.getConfig().getDouble("tax-rate", 0.05);
            double tax = price * taxRate;
            double sellerReceives = Math.max(0, price - tax);

            reserveListing(listingId, listing);

            if (!plugin.getEconomyManager().withdraw(buyer, price)) {
                restoreListing(listing);
                buyer.sendMessage(ColorUtils.color(
                        prefix + plugin.getConfig().getString("messages.not-enough-money", "&cSaldo insuficiente.")
                                .replace("{price}", String.format("%.2f", price))
                ));
                return false;
            }

            OfflinePlayer seller = Bukkit.getOfflinePlayer(listing.getSellerUUID());
            if (!plugin.getEconomyManager().deposit(seller, sellerReceives)) {
                plugin.getEconomyManager().deposit(buyer, price);
                restoreListing(listing);
                buyer.sendMessage(ColorUtils.color(prefix + "&cCompra cancelada: falha ao pagar o vendedor."));
                return false;
            }

            ItemStack item = listing.getItem();
            if (buyer.getInventory().firstEmpty() == -1) {
                plugin.getInboxManager().addToInbox(buyer.getUniqueId(), item);
                buyer.sendMessage(ColorUtils.color(
                        prefix + plugin.getConfig().getString("messages.item-bought-full-inv", "&eSua compra foi enviada para sua caixa de entrada.")
                ));
            } else {
                buyer.getInventory().addItem(item.clone());
            }

            Player onlineSeller = Bukkit.getPlayer(listing.getSellerUUID());
            if (onlineSeller != null) {
                String msg = plugin.getConfig().getString("messages.item-sold", "")
                        .replace("{price}", String.format("%.2f", price))
                        .replace("{tax}", String.format("%.2f", tax))
                        .replace("{received}", String.format("%.2f", sellerReceives));
                onlineSeller.sendMessage(ColorUtils.color(prefix + msg));
            }

            String buyMsg = plugin.getConfig().getString("messages.item-bought", "")
                    .replace("{item}", item.getType().name())
                    .replace("{price}", String.format("%.2f", price));
            buyer.sendMessage(ColorUtils.color(prefix + buyMsg));

            saveListings();
            return true;
        } finally {
            purchasesInProgress.remove(listingId);
        }
    }

    private void reserveListing(String listingId, AuctionListing listing) {
        listings.remove(listingId);
        if (listing.getDisplayId() != null) {
            AuctionDisplay display = plugin.getDisplayManager().getDisplayById(listing.getDisplayId());
            if (display != null) {
                plugin.getDisplayManager().clearDisplayItem(display);
            }
        }
        saveListings();
    }

    private void restoreListing(AuctionListing listing) {
        listings.put(listing.getId(), listing);
        if (listing.getDisplayId() != null) {
            AuctionDisplay display = plugin.getDisplayManager().getDisplayById(listing.getDisplayId());
            if (display != null) {
                display.setCurrentListingId(listing.getId());
                plugin.getDisplayManager().updateDisplayItem(display, listing);
            }
        }
        saveListings();
    }

    public void removeListing(String listingId, boolean returnToSeller) {
        AuctionListing listing = listings.get(listingId);
        if (listing == null) return;

        if (returnToSeller) {
            Player onlineSeller = Bukkit.getPlayer(listing.getSellerUUID());
            if (onlineSeller != null && onlineSeller.getInventory().firstEmpty() != -1) {
                onlineSeller.getInventory().addItem(listing.getItem());
                onlineSeller.sendMessage(ColorUtils.color(
                        plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r") +
                                plugin.getConfig().getString("messages.item-removed", "&aItem devolvido.")
                ));
            } else {
                plugin.getInboxManager().addToInbox(listing.getSellerUUID(), listing.getItem());
                if (onlineSeller != null) {
                    onlineSeller.sendMessage(ColorUtils.color(
                            plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r") +
                                    "&eItem enviado para sua caixa de entrada (inv cheio)."
                    ));
                }
            }
        }

        if (listing.getDisplayId() != null) {
            AuctionDisplay display = plugin.getDisplayManager().getDisplayById(listing.getDisplayId());
            if (display != null) {
                plugin.getDisplayManager().clearDisplayItem(display);
            }
        }

        listings.remove(listingId);
        saveListings();
    }

    public void forceRemoveListing(String listingId, boolean returnToSeller) {
        removeListing(listingId, returnToSeller);
    }

    public void checkExpiredListings() {
        List<String> toExpire = new ArrayList<>();
        for (AuctionListing listing : listings.values()) {
            if (listing.isExpired()) toExpire.add(listing.getId());
        }

        if (toExpire.isEmpty()) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String id : toExpire) {
                AuctionListing listing = listings.get(id);
                if (listing == null) continue;
                Player onlineSeller = Bukkit.getPlayer(listing.getSellerUUID());
                String expMsg = plugin.getConfig().getString("messages.item-expired", "")
                        .replace("{item}", listing.getItem().getType().name());
                if (onlineSeller != null) {
                    onlineSeller.sendMessage(ColorUtils.color(
                            plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r") + expMsg
                    ));
                }

                plugin.getInboxManager().addToInbox(listing.getSellerUUID(), listing.getItem());
                removeListing(id, false);
            }
        });
    }

    public AuctionListing getListingById(String id) { return listings.get(id); }

    public int getActiveListingCount(UUID playerUUID) {
        int count = 0;
        for (AuctionListing l : listings.values()) {
            if (l.getSellerUUID().equals(playerUUID)) count++;
        }
        return count;
    }

    public Collection<AuctionListing> getAllListings() { return listings.values(); }
}
