package org.tejaslamba2006.onlymace.command.onlymace;

import com.github.sirblobman.api.command.Command;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.tejaslamba2006.onlymace.OnlyMacePlugin;

import java.util.List;

public class ReloadSubcommand extends Command {
    private final OnlyMacePlugin plugin;
    public ReloadSubcommand(@NonNull OnlyMacePlugin plugin) {
        super(plugin, "reload");
        this.plugin = plugin;
    }

    @Override
    protected @NonNull List<String> onTabComplete(@NonNull CommandSender commandSender, String @NonNull [] strings) {
        return List.of();
    }

    @Override
    protected boolean execute(@NonNull CommandSender commandSender, String @NonNull [] strings) {
        plugin.reload();
        sendMessageWithPrefix(commandSender, "commands.reload");
        return false;
    }
}
