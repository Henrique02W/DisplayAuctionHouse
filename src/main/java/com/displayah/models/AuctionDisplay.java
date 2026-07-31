package com.displayah.models;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;

import java.util.UUID;

public class AuctionDisplay {

    private final String id;
    private final Location location;
    private UUID itemDisplayUUID;
    private String currentListingId;
    private BlockFace signFace;

    public AuctionDisplay(String id, Location location) {
        this.id = id;
        this.location = location.clone();
    }

    public String getId() { return id; }
    public Location getLocation() { return location.clone(); }
    public Location getBlockLocation() { return location.getBlock().getLocation(); }

    public UUID getItemDisplayUUID() { return itemDisplayUUID; }
    public void setItemDisplayUUID(UUID uuid) { this.itemDisplayUUID = uuid; }

    public String getCurrentListingId() { return currentListingId; }
    public void setCurrentListingId(String listingId) { this.currentListingId = listingId; }

    public boolean isOccupied() { return currentListingId != null; }

    public BlockFace getSignFace() { return signFace; }
    public void setSignFace(BlockFace face) { this.signFace = face; }

    public ItemDisplay getItemDisplay() {
        if (itemDisplayUUID == null || location.getWorld() == null) return null;
        var entity = location.getWorld().getEntity(itemDisplayUUID);
        if (entity instanceof ItemDisplay display) return display;
        return null;
    }
}
