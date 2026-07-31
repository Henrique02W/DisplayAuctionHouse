package com.displayah.managers;

import com.displayah.DisplayAuctionHouse;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class EconomyManager {

    private final Economy economy;

    public EconomyManager(DisplayAuctionHouse plugin, Economy economy) {
        this.economy = economy;
    }

    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (!economy.has(player, amount)) return false;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public String format(double amount) {
        return economy.format(amount);
    }
}
