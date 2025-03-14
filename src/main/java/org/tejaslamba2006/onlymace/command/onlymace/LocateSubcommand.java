package org.tejaslamba2006.onlymace.command.onlymace;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.api.language.replacer.IntegerReplacer;
import com.github.sirblobman.api.language.replacer.StringReplacer;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.tejaslamba2006.onlymace.OnlyMacePlugin;

import java.util.List;
import java.util.Objects;

public class LocateSubcommand extends Command {
    private final OnlyMacePlugin plugin;

    public LocateSubcommand(@NonNull OnlyMacePlugin plugin) {
        super(plugin, "locate");
        setPermissionName("onemace.locate");
        this.plugin = plugin;
    }

    @Override
    protected @NonNull List<String> onTabComplete(@NonNull CommandSender sender, String @NonNull [] args) {
        return List.of();
    }

    @Override
    protected boolean execute(@NonNull CommandSender sender, String @NonNull [] args) {
        sendMessageWithPrefix(sender, "commands.locate.searching");
        Bukkit.getScheduler().runTask(this.plugin, () -> locateMace(sender));
        return true;
    }

    private void locateMace(CommandSender sender) {
        if (checkPlayers(sender)) return;
        if (checkEntities(sender)) return;
        if (checkChunks(sender)) return;
        if (checkOfflinePlayerData(sender)) return;

        sendMessageWithPrefix(sender, "commands.locate.not-found");
    }

    private boolean checkPlayers(CommandSender sender) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (checkInventory(sender, player.getInventory(), "commands.locate.found.inventory", player.getName())) return true;
            if (checkInventory(sender, player.getEnderChest(), "commands.locate.found.ender-chest", player.getName())) return true;
        }
        return false;
    }

    private boolean checkEntities(CommandSender sender) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof AbstractHorse horse && checkInventory(sender, horse.getInventory(), "commands.locate.found.storage-animal", horse.getLocation())) return true;
                if (entity instanceof Item item && checkItem(sender, item.getItemStack(), item.getLocation())) return true;
                if (entity instanceof StorageMinecart minecart && checkInventory(sender, minecart.getInventory(), "commands.locate.found.storage-minecart", minecart.getLocation())) return true;
                if (entity instanceof ChestBoat chestBoat && checkInventory(sender, chestBoat.getInventory(), "commands.locate.found.chest-boat", chestBoat.getLocation())) return true;
            }
        }
        return false;
    }

    private boolean checkChunks(CommandSender sender) {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof Container container && checkInventory(sender, container.getInventory(), "commands.locate.found.container", state.getLocation())) return true;
                }
            }
        }
        return false;
    }

    private boolean checkInventory(CommandSender sender, Inventory inventory, String messageKey, String playerName) {
        for (ItemStack item : inventory.getContents()) {
            if (plugin.isAnyMace(item) || plugin.isMaceInsideShulker(item)) {
                sendMessageWithPrefix(sender, messageKey, new StringReplacer("%player%", playerName));
                return true;
            }
        }
        return false;
    }

    private boolean checkInventory(CommandSender sender, Inventory inventory, String messageKey, Location location) {
        for (ItemStack item : inventory.getContents()) {
            if (plugin.isAnyMace(item) || plugin.isMaceInsideShulker(item)) {
                sendMessageWithPrefix(sender, messageKey, new IntegerReplacer("%x%", location.getBlockX()), new IntegerReplacer("%y%", location.getBlockY()), new IntegerReplacer("%z%", location.getBlockZ()), new StringReplacer("%world%", location.getWorld().getName()));
                return true;
            }
        }
        return false;
    }

    private boolean checkItem(CommandSender sender, ItemStack item, Location location) {
        if (plugin.isAnyMace(item) || plugin.isMaceInsideShulker(item)) {
            sendMessageWithPrefix(sender, "commands.locate.found.dropped-item", new IntegerReplacer("%x%", location.getBlockX()), new IntegerReplacer("%y%", location.getBlockY()), new IntegerReplacer("%z%", location.getBlockZ()), new StringReplacer("%world%", location.getWorld().getName()));
            return true;
        }
        return false;
    }

    private boolean checkOfflinePlayerData(CommandSender sender) {
        if (plugin.getConfig().isConfigurationSection("offlinePlayerData")) {
            for (String uuid : Objects.requireNonNull(plugin.getConfig().getConfigurationSection("offlinePlayerData")).getKeys(true)) {
                if (plugin.getConfig().getBoolean("offlinePlayerData." + uuid, true)) {
                    sendMessageWithPrefix(sender, "commands.locate.found.offline-player", new StringReplacer("%uuid%", uuid));
                    return true;
                }
            }
        }
        return false;
    }
}