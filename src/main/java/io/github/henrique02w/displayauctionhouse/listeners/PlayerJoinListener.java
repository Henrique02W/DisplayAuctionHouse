package io.github.henrique02w.displayauctionhouse.listeners;

import io.github.henrique02w.displayauctionhouse.DisplayAuctionHouse;
import io.github.henrique02w.displayauctionhouse.utils.ColorUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final DisplayAuctionHouse plugin;

    public PlayerJoinListener(DisplayAuctionHouse plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getInboxManager().hasItems(player.getUniqueId())) {
                String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");
                player.sendMessage(ColorUtils.color(prefix + "&eVocê tem itens na sua caixa de entrada!"));
                player.sendMessage(ColorUtils.color(prefix + "&7Use &f/dah inbox &7para resgatar."));
            }
        }, 40L);
    }
}
