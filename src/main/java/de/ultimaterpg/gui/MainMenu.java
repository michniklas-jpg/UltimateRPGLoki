
package de.ultimaterpg.gui;

import de.ultimaterpg.UltimateRPG;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MainMenu implements Listener {

    private final UltimateRPG plugin;

    public MainMenu(UltimateRPG plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {

        Inventory inventory = Bukkit.createInventory(
                new MenuHolder(),
                54,
                ChatColor.DARK_PURPLE + "UltimateRPG"
        );

        // Hintergrund
        ItemStack background = createItem(
                Material.PURPLE_STAINED_GLASS_PANE,
                " ",
                List.of()
        );

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, background);
        }

        // Haupttitel
        inventory.setItem(4, createItem(
                Material.NETHER_STAR,
                ChatColor.LIGHT_PURPLE + "UltimateRPG",
                List.of(
                        ChatColor.GRAY + "Willkommen!",
                        ChatColor.YELLOW + "Dein RPG-Abenteuer beginnt."
                )
        ));

        // Boss und Waffen
        inventory.setItem(19, createItem(
                Material.NETHERITE_SWORD,
                ChatColor.RED + "Boss & Waffen",
                List.of("Klicke zum Öffnen")
        ));

        // Quests
        inventory.setItem(21, createItem(
                Material.WRITABLE_BOOK,
                ChatColor.GREEN + "Quests",
                List.of("Daily und Weekly Quests")
        ));

        // Battle Pass
        inventory.setItem(23, createItem(
                Material.GOLD_INGOT,
                ChatColor.GOLD + "Battle Pass",
                List.of("Sammle Belohnungen")
        ));

        // Rifts
        inventory.setItem(25, createItem(
                Material.END_PORTAL_FRAME,
                ChatColor.DARK_PURPLE + "Rifts",
                List.of("Entdecke Dungeons")
        ));

        // Meteoriten
        inventory.setItem(30, createItem(
                Material.MAGMA_BLOCK,
                ChatColor.GOLD + "Meteoriten",
                List.of("Entdecke seltene Ressourcen")
        ));

        // Skills
        inventory.setItem(32, createItem(
                Material.ENCHANTED_BOOK,
                ChatColor.AQUA + "Skill Tree",
                List.of("Verbessere deine Fähigkeiten")
        ));

        // Spielerprofil
        inventory.setItem(49, createItem(
                Material.PLAYER_HEAD,
                ChatColor.YELLOW + "Spielerprofil",
                List.of("Deine RPG-Statistiken")
        ));

        player.openInventory(inventory);
    }

    private ItemStack createItem(
            Material material,
            String name,
            List<String> lore
    ) {

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getView().getTopInventory().getHolder()
                instanceof MenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= 54) {
            return;
        }

        switch (slot) {

            case 19 -> openModule(player, "Boss & Waffen");
            case 21 -> openModule(player, "Quests");
            case 23 -> openModule(player, "Battle Pass");
            case 25 -> openModule(player, "Rifts");
            case 30 -> openModule(player, "Meteoriten");
            case 32 -> openModule(player, "Skill Tree");
            case 49 -> openModule(player, "Spielerprofil");

        }
    }

    private void openModule(Player player, String name) {

        player.closeInventory();

        player.sendMessage(
                ChatColor.DARK_PURPLE + "[UltimateRPG] "
                + ChatColor.GRAY + name
                + " wird vorbereitet."
        );
    }

    private static class MenuHolder implements InventoryHolder {

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
