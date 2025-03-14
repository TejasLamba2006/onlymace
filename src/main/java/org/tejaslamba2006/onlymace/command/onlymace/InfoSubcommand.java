package org.tejaslamba2006.onlymace.command.onlymace;

import com.github.sirblobman.api.command.Command;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.tejaslamba2006.onlymace.OnlyMacePlugin;

import java.util.List;

public class InfoSubcommand extends Command {
    private final OnlyMacePlugin plugin;

    public InfoSubcommand(@NonNull OnlyMacePlugin plugin) {
        super(plugin, "info");
        this.plugin = plugin;
    }

    @Override
    protected @NonNull List<String> onTabComplete(@NonNull CommandSender sender, String @NonNull [] args) {
        return List.of();
    }

    @Override
    protected boolean execute(@NonNull CommandSender sender, String @NonNull [] args) {
        sendMessageWithPrefix(sender, "OnlyMace Plugin v" + plugin.getDescription().getVersion());
        sendMessageWithPrefix(sender, "Developed by: " + String.join(", ", plugin.getDescription().getAuthors()));
        sendMessageWithPrefix(sender, "This plugin manages the unique Mace on your server.");
        return true;
    }
}