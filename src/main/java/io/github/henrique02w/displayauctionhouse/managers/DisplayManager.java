package io.github.henrique02w.displayauctionhouse.managers;

import io.github.henrique02w.displayauctionhouse.DisplayAuctionHouse;
import io.github.henrique02w.displayauctionhouse.models.AuctionDisplay;
import io.github.henrique02w.displayauctionhouse.models.AuctionListing;
import io.github.henrique02w.displayauctionhouse.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DisplayManager {

    private final DisplayAuctionHouse plugin;
    private final Map<String, AuctionDisplay> displays = new LinkedHashMap<>();
    private final Map<UUID, String> itemDisplayToDisplay = new HashMap<>();
    private final File displaysFile;
    private final FileConfiguration displaysConfig;
    private BukkitTask rotationTask;
    private double rotationAngleDeg = 0;

    public DisplayManager(DisplayAuctionHouse plugin) {
        this.plugin = plugin;
        displaysFile = new File(plugin.getDataFolder(), "displays.yml");
        displaysConfig = YamlConfiguration.loadConfiguration(displaysFile);
    }

    public void loadDisplays() {
        displays.clear();
        itemDisplayToDisplay.clear();

        ConfigurationSection section = displaysConfig.getConfigurationSection("displays");
        if (section == null) return;

        for (String id : section.getKeys(false)) {
            String worldName = section.getString(id + ".world");
            int x = section.getInt(id + ".x");
            int y = section.getInt(id + ".y");
            int z = section.getInt(id + ".z");
            float yaw = (float) section.getDouble(id + ".yaw", 0);

            World world = plugin.getServer().getWorld(worldName);
            if (world == null) continue;

            Location loc = new Location(world, x, y, z, yaw, 0);
            AuctionDisplay display = new AuctionDisplay(id, loc);

            String entityUUIDStr = section.getString(id + ".item-display-uuid");
            if (entityUUIDStr != null) {
                UUID uuid = UUID.fromString(entityUUIDStr);
                display.setItemDisplayUUID(uuid);
                itemDisplayToDisplay.put(uuid, id);
            }

            display.setCurrentListingId(section.getString(id + ".listing-id"));

            String signFaceStr = section.getString(id + ".sign-face");
            if (signFaceStr != null) {
                try {
                    display.setSignFace(BlockFace.valueOf(signFaceStr));
                } catch (IllegalArgumentException ignored) {}
            }

            displays.put(id, display);
            repairSignLink(display);
        }

        plugin.getLogger().info("Carregados " + displays.size() + " displays.");
    }

    public void saveDisplays() {
        displaysConfig.set("displays", null);
        for (AuctionDisplay display : displays.values()) {
            String path = "displays." + display.getId();
            Location loc = display.getLocation();
            displaysConfig.set(path + ".world", loc.getWorld().getName());
            displaysConfig.set(path + ".x", loc.getBlockX());
            displaysConfig.set(path + ".y", loc.getBlockY());
            displaysConfig.set(path + ".z", loc.getBlockZ());
            displaysConfig.set(path + ".yaw", loc.getYaw());
            if (display.getItemDisplayUUID() != null) {
                displaysConfig.set(path + ".item-display-uuid", display.getItemDisplayUUID().toString());
            }
            displaysConfig.set(path + ".listing-id", display.getCurrentListingId());
            if (display.getSignFace() != null) {
                displaysConfig.set(path + ".sign-face", display.getSignFace().name());
            }
        }
        try {
            displaysConfig.save(displaysFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar displays: " + e.getMessage());
        }
    }

    public AuctionDisplay createDisplay(Location adminLocation) {
        int next = displays.size() + 1;
        String id = "disp_" + next;
        while (displays.containsKey(id)) {
            next++;
            id = "disp_" + next;
        }

        Location pedestalLoc = adminLocation.getBlock().getLocation();
        pedestalLoc.setYaw(adminLocation.getYaw());

        AuctionDisplay display = new AuctionDisplay(id, pedestalLoc);
        World world = adminLocation.getWorld();
        if (world == null) return null;

        pedestalLoc.getBlock().setType(getPedestalMaterial());

        ItemDisplay itemDisplay = spawnItemDisplay(display, world);
        display.setItemDisplayUUID(itemDisplay.getUniqueId());
        itemDisplayToDisplay.put(itemDisplay.getUniqueId(), id);

        placeSign(display, adminLocation.getYaw());

        displays.put(id, display);
        saveDisplays();
        return display;
    }

    public boolean removeDisplay(String id) {
        AuctionDisplay display = displays.get(id);
        if (display == null) return false;

        if (display.isOccupied()) {
            plugin.getListingManager().forceRemoveListing(display.getCurrentListingId(), true);
        }

        ItemDisplay entity = display.getItemDisplay();
        if (entity != null) {
            itemDisplayToDisplay.remove(entity.getUniqueId());
            entity.remove();
        }

        Block pedestal = display.getBlockLocation().getBlock();
        if (pedestal.getType() == getPedestalMaterial()) {
            pedestal.setType(Material.AIR);
        }

        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block candidate = display.getBlockLocation().getBlock().getRelative(face);
            if (candidate.getBlockData() instanceof WallSign) {
                candidate.setType(Material.AIR);
                break;
            }
        }

        displays.remove(id);
        saveDisplays();
        return true;
    }

    private ItemDisplay spawnItemDisplay(AuctionDisplay display, World world) {
        double itemHeight = plugin.getConfig().getDouble("display.item-height", 0.8);
        float scale = (float) plugin.getConfig().getDouble("display.item-scale", 0.6);

        Location spawnLoc = display.getBlockLocation().clone().add(0.5, 1.0 + itemHeight, 0.5);

        // Configura a entidade antes de ela entrar no mundo, evitando um frame com valores padrão.
        return world.spawn(spawnLoc, ItemDisplay.class, entity -> {
            entity.setGravity(false);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.setPersistent(true);
            entity.setBillboard(org.bukkit.entity.Display.Billboard.FIXED);

            entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 1, 0)
            ));
        });
    }

    /**
     * Inicia (ou reinicia) a tarefa única que gira o item de todos os displays.
     *
     * Em vez de uma tarefa por display atualizando a transformação a cada tick, cada passo
     * define a rotação de destino com interpolação: o cliente anima o giro suavemente e o
     * servidor envia bem menos pacotes. Como a tarefa consulta as entidades a cada passo,
     * displays em chunks que carregam depois voltam a girar sozinhos.
     */
    public void restoreRotations() {
        if (rotationTask != null) {
            rotationTask.cancel();
            rotationTask = null;
        }

        double speed = plugin.getConfig().getDouble("display.rotation-speed", 2.0);
        if (speed <= 0) return;

        float scale = (float) plugin.getConfig().getDouble("display.item-scale", 0.6);

        // O cliente interpola entre duas rotações; cada passo deve ficar abaixo de 180 graus
        // para o giro seguir sempre na mesma direção.
        int stepTicks = (int) Math.max(1, Math.min(20, Math.floor(170.0 / speed)));
        double stepDegrees = speed * stepTicks;

        rotationTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            // Volta a zero a cada 720 graus (duas voltas): o quaternion da rotação repete
            // exatamente, então não há salto visível e o número não cresce sem limite.
            rotationAngleDeg = (rotationAngleDeg + stepDegrees) % 720.0;
            float rad = (float) Math.toRadians(rotationAngleDeg);

            for (AuctionDisplay display : displays.values()) {
                ItemDisplay entity = display.getItemDisplay();
                if (entity == null) continue;

                entity.setInterpolationDelay(0);
                entity.setInterpolationDuration(stepTicks);
                entity.setTransformation(new Transformation(
                        new Vector3f(0, 0, 0),
                        new AxisAngle4f(0, 0, 1, 0),
                        new Vector3f(scale, scale, scale),
                        new AxisAngle4f(rad, 0, 1, 0)
                ));
            }
        }, stepTicks, stepTicks);
    }

    public void reapplyItemDisplaySettings() {
        double itemHeight = plugin.getConfig().getDouble("display.item-height", 0.8);
        float scale = (float) plugin.getConfig().getDouble("display.item-scale", 0.6);

        for (AuctionDisplay display : displays.values()) {
            ItemDisplay entity = display.getItemDisplay();
            if (entity == null) continue;

            Location newLoc = display.getBlockLocation().clone().add(0.5, 1.0 + itemHeight, 0.5);
            entity.teleport(newLoc);

            entity.setInterpolationDuration(0);
            entity.setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    new AxisAngle4f(0, 0, 1, 0),
                    new Vector3f(scale, scale, scale),
                    new AxisAngle4f(0, 0, 1, 0)
            ));
        }

        // A velocidade e a escala podem ter mudado no config.
        restoreRotations();
    }

    public void refreshSigns() {
        for (AuctionDisplay display : displays.values()) {
            repairSignLink(display);

            if (!display.isOccupied()) {
                clearSign(display);
                continue;
            }

            AuctionListing listing = plugin.getListingManager().getListingById(display.getCurrentListingId());
            if (listing == null) {
                clearDisplayItem(display);
                continue;
            }

            updateSign(display, listing);
        }

        saveDisplays();
    }

    /** Atualiza o tempo restante mostrado nas placas dos displays ocupados (chamado a cada minuto). */
    public void refreshSignTimes() {
        for (AuctionDisplay display : displays.values()) {
            if (!display.isOccupied()) continue;

            // Evita carregar chunks só para atualizar uma placa.
            Location loc = display.getBlockLocation();
            World world = loc.getWorld();
            if (world == null || !world.isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) continue;

            AuctionListing listing = plugin.getListingManager().getListingById(display.getCurrentListingId());
            if (listing != null) updateSign(display, listing);
        }
    }

    private BlockFace yawToSignFacing(float yaw) {
        yaw = ((yaw % 360) + 360) % 360;
        if (yaw < 45 || yaw >= 315) return BlockFace.SOUTH;
        if (yaw < 135) return BlockFace.WEST;
        if (yaw < 225) return BlockFace.NORTH;
        return BlockFace.EAST;
    }

    private BlockFace faceOpposite(BlockFace face) {
        return switch (face) {
            case NORTH -> BlockFace.SOUTH;
            case SOUTH -> BlockFace.NORTH;
            case EAST -> BlockFace.WEST;
            case WEST -> BlockFace.EAST;
            default -> BlockFace.SOUTH;
        };
    }

    private void placeSign(AuctionDisplay display, float adminYaw) {
        BlockFace signFacing = yawToSignFacing(adminYaw);
        BlockFace signAttach = faceOpposite(signFacing);

        Block signBlock = display.getBlockLocation().getBlock().getRelative(signFacing);
        Block support = signBlock.getRelative(signAttach);

        if (!support.getType().isSolid()) {
            plugin.getLogger().warning("Sem suporte para placa no display " + display.getId());
            return;
        }

        signBlock.setType(getSignMaterial());

        if (signBlock.getBlockData() instanceof WallSign wallSign) {
            wallSign.setFacing(signFacing);
            signBlock.setBlockData(wallSign, false);
        }

        if (signBlock.getState() instanceof Sign sign) {
            writeEmptySign(sign);
        }

        display.setSignFace(signFacing);
    }

    private void writeEmptySign(Sign sign) {
        applySignProtectionAndStyle(sign);

        SignSide front = sign.getSide(Side.FRONT);
        front.line(0, ColorUtils.color("&6&l[AH]"));
        front.line(1, ColorUtils.color("&7Display vazio"));
        front.line(2, Component.empty());
        front.line(3, ColorUtils.color("&aShift+Dir: vender"));
        sign.update(true, false);
    }

    public void updateDisplayItem(AuctionDisplay display, AuctionListing listing) {
        ItemDisplay entity = display.getItemDisplay();
        if (entity != null) {
            entity.setItemStack(listing.getItem().clone());
        }
        updateSign(display, listing);
    }

    public void clearDisplayItem(AuctionDisplay display) {
        ItemDisplay entity = display.getItemDisplay();
        if (entity != null) {
            entity.setItemStack(new ItemStack(Material.AIR));
        }
        clearSign(display);
        display.setCurrentListingId(null);
    }

    private void updateSign(AuctionDisplay display, AuctionListing listing) {
        Block signBlock = getSignBlock(display);
        if (signBlock == null || !(signBlock.getState() instanceof Sign sign)) return;

        String rawName = listing.getItem().getType().name().replace("_", " ").toLowerCase();
        String itemName = Character.toUpperCase(rawName.charAt(0)) + rawName.substring(1);
        if (itemName.length() > 15) itemName = itemName.substring(0, 14) + "…";

        applySignProtectionAndStyle(sign);

        SignSide front = sign.getSide(Side.FRONT);
        front.line(0, ColorUtils.color("&f" + itemName));
        front.line(1, ColorUtils.color("&6$" + String.format("%.2f", listing.getPrice())));
        front.line(2, ColorUtils.color("&7" + listing.getSellerName()));
        front.line(3, ColorUtils.color("&e" + listing.getTimeRemaining()));
        sign.update(true, false);
    }

    private void clearSign(AuctionDisplay display) {
        Block signBlock = getSignBlock(display);
        if (signBlock == null || !(signBlock.getState() instanceof Sign sign)) return;
        writeEmptySign(sign);
    }

    private void applySignProtectionAndStyle(Sign sign) {
        sign.setEditable(false);
        sign.setWaxed(true);
        sign.setColor(DyeColor.ORANGE);
        sign.getSide(Side.FRONT).setGlowingText(true);
        sign.getSide(Side.BACK).setGlowingText(true);
    }

    private void repairSignLink(AuctionDisplay display) {
        Block signBlock = getSignBlock(display);
        if (signBlock == null) {
            signBlock = findAdjacentSign(display);
            if (signBlock != null) {
                display.setSignFace(display.getBlockLocation().getBlock().getFace(signBlock));
            }
        }

        if (signBlock != null && signBlock.getState() instanceof Sign sign) {
            applySignProtectionAndStyle(sign);
            sign.update(true, false);
        }
    }

    private Block findAdjacentSign(AuctionDisplay display) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
            Block candidate = display.getBlockLocation().getBlock().getRelative(face);
            if (candidate.getBlockData() instanceof WallSign) return candidate;
        }
        return null;
    }

    public Block getSignBlock(AuctionDisplay display) {
        BlockFace face = display.getSignFace();
        if (face == null) return null;
        return display.getBlockLocation().getBlock().getRelative(face);
    }

    private Material getPedestalMaterial() {
        try {
            return Material.valueOf(plugin.getConfig().getString("display.pedestal-material", "SMOOTH_STONE"));
        } catch (IllegalArgumentException e) {
            return Material.SMOOTH_STONE;
        }
    }

    private Material getSignMaterial() {
        try {
            return Material.valueOf(plugin.getConfig().getString("display.sign-material", "OAK_WALL_SIGN"));
        } catch (IllegalArgumentException e) {
            return Material.OAK_WALL_SIGN;
        }
    }

    public AuctionDisplay getDisplayById(String id) {
        return displays.get(id);
    }

    public AuctionDisplay getDisplayByItemDisplay(UUID uuid) {
        String id = itemDisplayToDisplay.get(uuid);
        return id != null ? displays.get(id) : null;
    }

    public AuctionDisplay getDisplayByBlock(Location blockLoc) {
        for (AuctionDisplay d : displays.values()) {
            if (isSameBlock(d.getBlockLocation(), blockLoc)) {
                return d;
            }

            Block signBlock = getSignBlock(d);
            if (signBlock != null && isSameBlock(signBlock.getLocation(), blockLoc)) {
                return d;
            }
        }
        return null;
    }

    private boolean isSameBlock(Location first, Location second) {
        if (first.getWorld() == null || second.getWorld() == null) return false;
        return first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ()
                && first.getWorld().equals(second.getWorld());
    }

    public Collection<AuctionDisplay> getAllDisplays() {
        return displays.values();
    }

    public List<AuctionDisplay> getEmptyDisplays() {
        List<AuctionDisplay> empty = new ArrayList<>();
        for (AuctionDisplay d : displays.values()) {
            if (!d.isOccupied()) empty.add(d);
        }
        return empty;
    }
}
