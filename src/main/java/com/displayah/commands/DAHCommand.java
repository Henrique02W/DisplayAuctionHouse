package com.displayah.commands;

import com.displayah.DisplayAuctionHouse;
import com.displayah.models.AuctionDisplay;
import com.displayah.models.AuctionListing;
import com.displayah.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DAHCommand implements CommandExecutor, TabCompleter {

    private final DisplayAuctionHouse plugin;

    public DAHCommand(DisplayAuctionHouse plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6AH&8] &r");

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "inbox" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                if (!player.hasPermission("dah.inbox")) {
                    player.sendMessage(ColorUtils.color(prefix + plugin.getConfig().getString("messages.no-permission")));
                    return true;
                }
                plugin.getInboxManager().openInbox(player);
            }

            case "adddisplay", "createdisplay", "newdisplay" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                if (!player.hasPermission("dah.admin")) {
                    player.sendMessage(ColorUtils.color(prefix + plugin.getConfig().getString("messages.no-permission")));
                    return true;
                }
                AuctionDisplay display = plugin.getDisplayManager().createDisplay(player.getLocation());
                String msg = plugin.getConfig().getString("messages.display-created", "&aDisplay criado! ID: &e{id}")
                        .replace("{id}", display.getId());
                player.sendMessage(ColorUtils.color(prefix + msg));
            }

            case "removedisplay", "deldisplay" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                if (!player.hasPermission("dah.admin")) {
                    player.sendMessage(ColorUtils.color(prefix + plugin.getConfig().getString("messages.no-permission")));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.color(prefix + "&cUso: /dah removedisplay <id>"));
                    return true;
                }
                String id = args[1];
                if (plugin.getDisplayManager().removeDisplay(id)) {
                    player.sendMessage(ColorUtils.color(prefix + plugin.getConfig().getString("messages.display-removed")));
                } else {
                    player.sendMessage(ColorUtils.color(prefix + plugin.getConfig().getString("messages.display-not-found")));
                }
            }

            case "listdisplays", "displays" -> {
                if (!sender.hasPermission("dah.admin")) {
                    sender.sendMessage(ColorUtils.color(prefix + plugin.getConfig().getString("messages.no-permission")));
                    return true;
                }
                Collection<AuctionDisplay> displays = plugin.getDisplayManager().getAllDisplays();
                sender.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
                sender.sendMessage(ColorUtils.color("&6&l  Displays Registrados &8(" + displays.size() + ")"));
                sender.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
                for (AuctionDisplay d : displays) {
                    String loc = d.getLocation().getWorld().getName() + " " +
                            (int) d.getLocation().getX() + "/" +
                            (int) d.getLocation().getY() + "/" +
                            (int) d.getLocation().getZ();
                    String status = d.isOccupied() ? "&a[Ocupado]" : "&7[Vazio]";
                    sender.sendMessage(ColorUtils.color("  &e" + d.getId() + " &8— &f" + loc + " " + status));
                }
                sender.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
            }

            case "listings", "list" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                Collection<AuctionListing> listings = plugin.getListingManager().getAllListings();
                List<AuctionListing> myListings = new ArrayList<>();
                for (AuctionListing l : listings) {
                    if (l.getSellerUUID().equals(player.getUniqueId())) myListings.add(l);
                }
                player.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
                player.sendMessage(ColorUtils.color("&6&l  Seus Itens à Venda &8(" + myListings.size() + ")"));
                player.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
                if (myListings.isEmpty()) {
                    player.sendMessage(ColorUtils.color("  &7Você não tem itens à venda."));
                }
                for (AuctionListing l : myListings) {
                    player.sendMessage(ColorUtils.color(
                            "  &f" + l.getItem().getType().name().replace("_", " ") +
                                    " &8— &e$" + String.format("%.2f", l.getPrice()) +
                                    " &8| &7Expira: &f" + l.getTimeRemaining() +
                                    " &8| &7ID: &e" + l.getId()
                    ));
                }
                player.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
            }

            case "remove", "cancel" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(ColorUtils.color(prefix + "&cUso: /dah remove <listing-id>"));
                    return true;
                }
                AuctionListing listing = plugin.getListingManager().getListingById(args[1]);
                if (listing == null) {
                    player.sendMessage(ColorUtils.color(prefix + "&cListing não encontrado."));
                    return true;
                }
                if (!listing.getSellerUUID().equals(player.getUniqueId()) && !player.hasPermission("dah.admin")) {
                    player.sendMessage(ColorUtils.color(prefix + plugin.getConfig().getString("messages.no-permission")));
                    return true;
                }
                plugin.getListingManager().removeListing(args[1], true);
            }

            case "reload" -> {
                if (!sender.hasPermission("dah.admin")) {
                    sender.sendMessage(ColorUtils.color(prefix + plugin.getConfig().getString("messages.no-permission")));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getDisplayManager().reapplyItemDisplaySettings();
                plugin.getDisplayManager().refreshSigns();
                sender.sendMessage(ColorUtils.color(prefix + "&aConfig recarregada! Displays atualizados."));
            }

            case "help" -> sendHelp(sender);

            default -> sender.sendMessage(ColorUtils.color(prefix + "&cComando desconhecido. Use &f/dah help"));
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
        sender.sendMessage(ColorUtils.color("&6&l  DisplayAuctionHouse &8— Ajuda"));
        sender.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
        sender.sendMessage(ColorUtils.color("  &e/dah inbox &8— &7Abrir sua caixa de entrada"));
        sender.sendMessage(ColorUtils.color("  &e/dah listings &8— &7Ver seus itens à venda"));
        sender.sendMessage(ColorUtils.color("  &e/dah remove <id> &8— &7Remover seu listing"));
        if (sender.hasPermission("dah.admin")) {
            sender.sendMessage(ColorUtils.color("  &c/dah adddisplay &8— &7Criar display na sua posição"));
            sender.sendMessage(ColorUtils.color("  &c/dah removedisplay <id> &8— &7Remover display"));
            sender.sendMessage(ColorUtils.color("  &c/dah listdisplays &8— &7Listar todos displays"));
            sender.sendMessage(ColorUtils.color("  &c/dah reload &8— &7Recarregar config"));
        }
        sender.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
        sender.sendMessage(ColorUtils.color("&7Para vender: &fSHIFT+DIREITO &7num display vazio"));
        sender.sendMessage(ColorUtils.color("&7Para comprar: &fDIREITO &7num display com item"));
        sender.sendMessage(ColorUtils.color("&8&m" + "─".repeat(40)));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>(List.of("inbox", "listings", "remove", "help"));
            if (sender.hasPermission("dah.admin")) {
                subs.addAll(List.of("adddisplay", "removedisplay", "listdisplays", "reload"));
            }
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
        }
        return completions;
    }
}
