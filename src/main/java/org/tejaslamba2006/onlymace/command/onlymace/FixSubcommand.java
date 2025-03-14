package org.tejaslamba2006.onlymace.command.onlymace;

import com.github.sirblobman.api.command.Command;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.tejaslamba2006.onlymace.OnlyMacePlugin;

import java.util.List;

public class FixSubcommand extends Command {
    private final OnlyMacePlugin plugin;

    public FixSubcommand(@NonNull OnlyMacePlugin plugin) {
        super(plugin, "fix");
        setPermissionName("onemace.fix");
        this.plugin = plugin;
    }

    @Override
    protected @NonNull List<String> onTabComplete(@NonNull CommandSender sender, String @NonNull [] args) {
        return List.of();
    }

    @Override
    protected boolean execute(@NonNull CommandSender sender, String @NonNull [] args) {
//        boolean fixed = plugin.fixDuplicateMaces(); // Implement this method in your plugin.
//        if (fixed) {
//            sendMessageWithPrefix(sender, "Duplicate Maces have been removed. Only one remains.");
//        } else {
//            sendMessageWithPrefix(sender, "No duplicate Maces were found.");
//        }
        return true;
    }
}