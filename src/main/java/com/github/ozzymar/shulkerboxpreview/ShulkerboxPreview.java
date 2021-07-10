package com.github.ozzymar.shulkerboxpreview;

import com.github.ozzymar.marsutils.api.commands.CommandManager;
import com.github.ozzymar.marsutils.api.config.YamlConfig;
import com.github.ozzymar.shulkerboxpreview.commands.CommandReload;
import com.github.ozzymar.shulkerboxpreview.commands.CommandToggle;
import com.github.ozzymar.shulkerboxpreview.database.SQLite;
import com.github.ozzymar.shulkerboxpreview.functionality.ShulkerBoxClickListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ShulkerboxPreview extends JavaPlugin {

    private CommandManager commandManager;
    private YamlConfig pluginConfig;

    @Override
    public void onEnable() {
        // Plugin startup logic
        SQLite.initializeDB(SQLite.getConnection());
        pluginConfig = new YamlConfig(this, "config.yml");
        commandManager = new CommandManager(
                this,
                new CommandReload("sbp-reload", this),
                new CommandToggle("sbp-use", this)
        );
        this.getServer().getPluginManager().registerEvents(new ShulkerBoxClickListener(this), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        commandManager = null;
        pluginConfig = null;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public YamlConfig getPluginConfig() {
        return pluginConfig;
    }
}
