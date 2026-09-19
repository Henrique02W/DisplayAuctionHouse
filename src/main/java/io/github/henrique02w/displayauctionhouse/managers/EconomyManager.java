package io.github.henrique02w.displayauctionhouse.managers;

import io.github.henrique02w.displayauctionhouse.DisplayAuctionHouse;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private final DisplayAuctionHouse plugin;

    public EconomyManager(DisplayAuctionHouse plugin) {
        this.plugin = plugin;
    }

    /**
     * Procura o provedor de economia a cada uso, em vez de guardá-lo no enable.
     * Os plugins de economia (EssentialsX, CMI etc.) só registram o serviço no Vault durante o
     * próprio enable, que pode acontecer depois do enable deste plugin.
     */
    private Economy current() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) return null;
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        return provider == null ? null : provider.getProvider();
    }

    public boolean available() {
        return current() != null;
    }

    /** Registra no console se a economia está conectada, para facilitar o diagnóstico. */
    public void logStatus() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            plugin.getLogger().warning("Vault nao encontrado ou desativado. Compras ficam indisponiveis.");
            return;
        }
        Economy economy = current();
        if (economy == null) {
            plugin.getLogger().warning("Vault encontrado, mas nenhum plugin de economia registrou um provedor (ex.: EssentialsX).");
        } else {
            plugin.getLogger().info("Economia conectada via Vault: " + economy.getName());
        }
    }

    public boolean has(Player player, double amount) {
        Economy economy = current();
        return economy != null && economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        Economy economy = current();
        if (economy == null || !economy.has(player, amount)) return false;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        Economy economy = current();
        if (economy == null) return false;
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    public double getBalance(Player player) {
        Economy economy = current();
        return economy == null ? 0D : economy.getBalance(player);
    }

    public String format(double amount) {
        Economy economy = current();
        if (economy == null) return String.format("$%,.2f", amount);
        return economy.format(amount);
    }
}
