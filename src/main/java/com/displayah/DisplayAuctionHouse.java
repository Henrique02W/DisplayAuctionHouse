package com.displayah;

import com.displayah.commands.DAHCommand;
import com.displayah.listeners.DisplayInteractListener;
import com.displayah.listeners.PlayerJoinListener;
import com.displayah.managers.DisplayManager;
import com.displayah.managers.EconomyManager;
import com.displayah.managers.InboxManager;
import com.displayah.managers.ListingManager;
import com.displayah.utils.PurchaseGuiManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DisplayAuctionHouse extends JavaPlugin {

    private static DisplayAuctionHouse instance;
    private DisplayManager displayManager;
    private ListingManager listingManager;
    private InboxManager inboxManager;
    private EconomyManager economyManager;
    private PurchaseGuiManager purchaseGuiManager;
    private Economy economy;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        if (!setupEconomy()) {
            getLogger().severe("Vault/Economy não encontrado! Desabilitando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.economyManager = new EconomyManager(this, economy);
        this.displayManager = new DisplayManager(this);
        this.listingManager = new ListingManager(this);
        this.inboxManager = new InboxManager(this);
        this.purchaseGuiManager = new PurchaseGuiManager(this);

        displayManager.loadDisplays();
        listingManager.loadListings();
        inboxManager.loadInboxes();
        displayManager.refreshSigns();
        displayManager.restoreRotations();

        getServer().getPluginManager().registerEvents(new DisplayInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(purchaseGuiManager, this);

        getCommand("dah").setExecutor(new DAHCommand(this));
        getCommand("dah").setTabCompleter(new DAHCommand(this));

        startExpirationTask();

        getLogger().info("DisplayAuctionHouse ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        if (displayManager != null) displayManager.saveDisplays();
        if (listingManager != null) listingManager.saveListings();
        if (inboxManager != null) inboxManager.saveInboxes();
        getLogger().info("DisplayAuctionHouse desabilitado.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    private void startExpirationTask() {
        int durationHours = getConfig().getInt("listing-duration-hours", 48);
        if (durationHours <= 0) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                listingManager.checkExpiredListings();
            }
        }.runTaskTimerAsynchronously(this, 20L * 60, 20L * 60);
    }

    public static DisplayAuctionHouse getInstance() { return instance; }
    public DisplayManager getDisplayManager() { return displayManager; }
    public ListingManager getListingManager() { return listingManager; }
    public InboxManager getInboxManager() { return inboxManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public PurchaseGuiManager getPurchaseGuiManager() { return purchaseGuiManager; }
    public Economy getEconomy() { return economy; }
}
