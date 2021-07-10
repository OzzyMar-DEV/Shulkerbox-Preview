package com.github.ozzymar.shulkerboxpreview.functionality;

import com.github.ozzymar.marsutils.api.colors.ColorFormatter;
import com.github.ozzymar.shulkerboxpreview.ShulkerboxPreview;
import com.github.ozzymar.shulkerboxpreview.database.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShulkerboxFunc implements Listener {

    Map<UUID, ItemStack> getClickedItem = new HashMap<>();

    private final ShulkerboxPreview plugin;

    public ShulkerboxFunc(ShulkerboxPreview plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerClickShulker(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals(ColorFormatter.format(plugin.getPluginConfig().getConfig().getString("preview.title")))) {
            if (!plugin.getPluginConfig().getConfig().getBoolean("preview.is-backpack")) {
                event.setCancelled(true);
            } else {
                return;
            }
        }
        if (event.getClick() == ClickType.MIDDLE) {

            if (!player.getGameMode().equals(GameMode.SURVIVAL)) return;

            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;
            if (!ShulkerManager.boxMaterials.contains(event.getCurrentItem().getType())) return;

            if (event.getClickedInventory() == null) return;
            if (!event.getClickedInventory().getType().equals(InventoryType.PLAYER)) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (!(clickedItem.getItemMeta() instanceof BlockStateMeta)) return;

            BlockStateMeta blockStateMeta = (BlockStateMeta) clickedItem.getItemMeta();
            if (!(blockStateMeta.getBlockState() instanceof ShulkerBox)) return;
            ShulkerBox shulkerBox = (ShulkerBox) blockStateMeta.getBlockState();

            Inventory shulkerPreview = Bukkit.createInventory(null, 27, ColorFormatter.format(
                    plugin.getPluginConfig().getConfig().getString("preview.title")));
            shulkerPreview.setContents(shulkerBox.getInventory().getContents());

            if (!player.hasPermission("sbp.open-shulker")) return;
            if (!plugin.getPluginConfig().getConfig()
                    .getStringList("enabled-worlds").contains(player.getWorld().getName())) return;

            try {
                if (!SQLite.isPlayerInDatabase(player)) {
                    Connection connection = SQLite.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement(
                            "INSERT INTO EnabledSBP (UUID, isEnabled) VALUES (?,?);"
                    );
                    preparedStatement.setString(1, player.getUniqueId().toString());
                    preparedStatement.setString(2, "false");
                    preparedStatement.execute();
                    preparedStatement.close();
                    connection.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            if (!SQLite.doesPLayerHavePreviewEnabled(player).equalsIgnoreCase("true")) {
                return;
            }

            player.openInventory(shulkerPreview);
            player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_OPEN, 1.0F, 1.0F);

            blockStateMeta.setBlockState(shulkerBox);
            clickedItem.setItemMeta(blockStateMeta);


            if (plugin.getPluginConfig().getConfig().getBoolean("preview.is-backpack")) {
                this.getClickedItem.put(player.getUniqueId(), clickedItem);
            }
        }
    }

    @EventHandler
    public void checkForShulkerNesting(InventoryClickEvent event) {
        if (!plugin.getPluginConfig().getConfig().getBoolean("preview.is-backpack")) return;
        if (event.getView().getTitle()
                .equals(ColorFormatter.format(plugin.getPluginConfig().getConfig().getString("preview.title")))) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().equals(this.getClickedItem.get(player.getUniqueId()))) event.setCancelled(true);
            if (ShulkerManager.boxMaterials.contains(event.getCurrentItem().getType())) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        if (event.getView().getTitle()
                .equals(ColorFormatter.format(plugin.getPluginConfig().getConfig().getString("preview.title")))) {
            if (!plugin.getPluginConfig().getConfig().getBoolean("preview.is-backpack")) {
                player.playSound(player.getLocation(), Sound.BLOCK_SHULKER_BOX_CLOSE, 1.0F, 1.0F);
            } else {
                ItemStack shulkerBox = getClickedItem.get(player.getUniqueId());
                BlockStateMeta im = (BlockStateMeta) shulkerBox.getItemMeta();
                assert im != null;
                ShulkerBox shulker = (ShulkerBox) im.getBlockState();
                shulker.getInventory().setContents(event.getInventory().getContents());
                im.setBlockState(shulker);
                shulkerBox.setItemMeta(im);

                player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_CLOSE, 1, 1);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.getPluginConfig().getConfig().getBoolean("preview.is-backpack")) return;
        // STOP PLAYER FROM PLACING BOX IF BACKPACK IS ENABLED
        if (ShulkerManager.boxMaterials.contains(event.getPlayer().getInventory().getItemInMainHand().getType()))
            event.setCancelled(true);
        if (ShulkerManager.boxMaterials.contains(event.getPlayer().getInventory().getItemInOffHand().getType()))
            event.setCancelled(true);
    }
}
