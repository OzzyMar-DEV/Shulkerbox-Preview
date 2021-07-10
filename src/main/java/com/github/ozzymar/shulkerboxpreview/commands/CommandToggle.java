package com.github.ozzymar.shulkerboxpreview.commands;

import com.github.ozzymar.marsutils.api.colors.ColorFormatter;
import com.github.ozzymar.marsutils.api.commands.ACommand;
import com.github.ozzymar.shulkerboxpreview.ShulkerboxPreview;
import com.github.ozzymar.shulkerboxpreview.database.SQLite;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandToggle extends ACommand {

    private final ShulkerboxPreview plugin;

    public CommandToggle(String name, ShulkerboxPreview plugin) {
        super(name);
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        if (!player.hasPermission("sbp.toggle")) return true;
        if (args.length == 0 || args[0] == null || args[0].isEmpty() || !isTrueOrFalse(args[0])) {
            player.sendMessage(ColorFormatter.format(plugin.getPluginConfig().getConfig().getString("language.toggle-feedback-error")));
            return true;
        }
        Connection connection;
        PreparedStatement preparedStatement;
        try {
            if (!SQLite.isPlayerInDatabase(player)) {
                connection = SQLite.getConnection();
                preparedStatement = connection.prepareStatement("INSERT INTO EnabledSBP (UUID, isEnabled) VALUES (?,?);");
                preparedStatement.setString(1, player.getUniqueId().toString());
                preparedStatement.setString(2, args[0]);
                preparedStatement.execute();
                preparedStatement.close();
                connection.close();
            } else {
                connection = SQLite.getConnection();
                preparedStatement = connection.prepareStatement(
                        "UPDATE EnabledSBP SET isEnabled = ? WHERE UUID = ?;");
                preparedStatement.setString(1, args[0]);
                preparedStatement.setString(2, player.getUniqueId().toString());
                preparedStatement.execute();
                preparedStatement.close();
                connection.close();
            }
            player.sendMessage(ColorFormatter.format(plugin.getPluginConfig().getConfig().getString("language.toggle-feedback-successful")));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            list.add("true");
            list.add("false");
            List<String> result = new ArrayList<>();
            for (String str : list) if (str.toLowerCase().startsWith(args[0].toLowerCase())) result.add(str);
            return result;
        }
        return Collections.emptyList();
    }

    private boolean isTrueOrFalse(String str) {
        return str.equalsIgnoreCase("true") || str.equalsIgnoreCase("false");
    }
}
