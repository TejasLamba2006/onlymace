package org.tejaslamba2006.onlymace.command.onlymace;

import com.github.sirblobman.api.command.Command;
import com.github.sirblobman.api.language.replacer.StringReplacer;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.tejaslamba2006.onlymace.OnlyMacePlugin;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OnlyMaceCommand extends Command {
    public OnlyMaceCommand(@NonNull OnlyMacePlugin plugin) {
        super(plugin, "onlymace");
        setPermissionName("onlymace.command");
        addSubCommand(new ReloadSubcommand(plugin));
        addSubCommand(new InfoSubcommand(plugin));
        addSubCommand(new FixSubcommand(plugin));
        addSubCommand(new LocateSubcommand(plugin));
    }
    @Override
    protected @NonNull List<String> onTabComplete(@NonNull CommandSender sender, String @NonNull [] args) {
        return Collections.emptyList();
    }

    @Override
    protected boolean execute(@NonNull CommandSender commandSender, String @NonNull [] strings) {
        Objects.requireNonNull(getLanguageManager()).sendMessage(commandSender, "help-title", new StringReplacer("%command%", "/onlymace"));
        Objects.requireNonNull(getLanguageManager()).sendMessage(commandSender, "commands.help");
        return true;
    }
}
