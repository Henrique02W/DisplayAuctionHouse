package io.github.henrique02w.displayauctionhouse;

import io.github.henrique02w.displayauctionhouse.commands.DAHCommand;
import io.github.henrique02w.displayauctionhouse.listeners.DisplayInteractListener;
import io.github.henrique02w.displayauctionhouse.listeners.PlayerJoinListener;
import io.github.henrique02w.displayauctionhouse.managers.DisplayManager;
import io.github.henrique02w.displayauctionhouse.managers.EconomyManager;
import io.github.henrique02w.displayauctionhouse.managers.InboxManager;
import io.github.henrique02w.displayauctionhouse.managers.ListingManager;
import io.github.henrique02w.displayauctionhouse.utils.PurchaseGuiManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DisplayAuctionHouse extends JavaPlugin {

    private static DisplayAuctionHouse instance;
    private DisplayManager displayManager;
    private ListingManager listingManager;
    private InboxManager inboxManager;
    private EconomyManager economyManager;
    private PurchaseGuiManager purchaseGuiManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.economyManager = new EconomyManager(this);
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
        getServer().getPluginManager().registerEvents(inboxManager, this);

        DAHCommand commandHandler = new DAHCommand(this);
        PluginCommand dahCommand = getCommand("dah");
        if (dahCommand != null) {
            dahCommand.setExecutor(commandHandler);
            dahCommand.setTabCompleter(commandHandler);
        }

        startMaintenanceTask();

        // Roda no primeiro tick, depois que todos os plugins (inclusive o de economia) terminaram de ativar.
        getServer().getScheduler().runTask(this, economyManager::logStatus);

        getLogger().info("DisplayAuctionHouse ativado com sucesso!");
    }

    @Override
    public void onDisable() {
        // Devolve à caixa de entrada os itens de quem estava com ela aberta, antes de salvar.
        if (inboxManager != null) inboxManager.returnOpenInboxes();
        if (displayManager != null) displayManager.saveDisplays();
        if (listingManager != null) listingManager.saveListings();
        if (inboxManager != null) inboxManager.saveInboxes();
        getLogger().info("DisplayAuctionHouse desabilitado.");
    }

    /** Tarefa periódica (a cada minuto, na thread principal): expira listings vencidas e atualiza as placas. */
    private void startMaintenanceTask() {
        getServer().getScheduler().runTaskTimer(this, () -> {
            listingManager.checkExpiredListings();
            displayManager.refreshSignTimes();
        }, 20L * 60, 20L * 60);
    }

    public static DisplayAuctionHouse getInstance() { return instance; }
    public DisplayManager getDisplayManager() { return displayManager; }
    public ListingManager getListingManager() { return listingManager; }
    public InboxManager getInboxManager() { return inboxManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
    public PurchaseGuiManager getPurchaseGuiManager() { return purchaseGuiManager; }
}
