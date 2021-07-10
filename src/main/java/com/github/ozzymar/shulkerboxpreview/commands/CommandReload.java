package com.github.ozzymar.shulkerboxpreview.commands;

import com.github.ozzymar.marsutils.api.colors.ColorFormatter;
import com.github.ozzymar.marsutils.api.commands.ACommand;
import com.github.ozzymar.shulkerboxpreview.ShulkerboxPreview;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CommandReload extends ACommand {

    private final ShulkerboxPreview plugin;

    public CommandReload(String name, ShulkerboxPreview plugin) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.hasPermission("sbp.reload")) return true;
        plugin.getPluginConfig().reload();
        player.sendMessage(ColorFormatter.format(plugin.getPluginConfig().getConfig().getString("language.reload-successful")));
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return Collections.emptyList();
    }
}
