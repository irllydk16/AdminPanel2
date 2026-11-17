package com.kyra.adminpanel2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class AdminPanel2 extends JavaPlugin implements Listener {

    private boolean maintenanceMode = false;
    private boolean disableFire = false;
    private boolean disableLeafDecay = false;
    private boolean disableEndermanGrief = false;
    private boolean disablePvp = false;
    private final Set<UUID> commandSpy = new HashSet<>();
    private final Set<UUID> oreAlert = new HashSet<>();

    private static final String[] PAGE_NAMES = {
            "Page 11: God Gear & Kits",
            "Page 12: Boss & Mob Spawner",
            "Page 13: Punishment Quick-Actions",
            "Page 14: Maintenance & Whitelist",
            "Page 15: World Protection",
            "Page 16: Health & XP Manager",
            "Page 17: GameRules Matrix",
            "Page 18: Staff Spy & Ore Alerts",
            "Page 19: Quick Builder Tools",
            "Page 20: Server Danger Zone"
    };

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.hasPermission("adminpanel.use")) {
            p.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }
        openPage(p, 11);
        return true;
    }

    private void openPage(Player p, int page) {
        int idx = page - 11;
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(PAGE_NAMES[idx], NamedTextColor.DARK_RED));

        switch (page) {
            case 11 -> { // God Gear
                inv.setItem(10, icon(Material.NETHERITE_SWORD, "Admin Sword (Sharpness X)", "Click to receive"));
                inv.setItem(12, icon(Material.STICK, "Knockback X Stick", "Click to receive"));
                inv.setItem(14, icon(Material.NETHERITE_PICKAXE, "Insta-Mine Pickaxe (Eff X)", "Click to receive"));
                inv.setItem(16, icon(Material.NETHERITE_CHESTPLATE, "Full God Armor (Prot X)", "Click to equip full set"));
                inv.setItem(22, icon(Material.ENCHANTED_GOLDEN_APPLE, "Full Heal & Feed", "Restores health and hunger"));
            }
            case 12 -> { // Mob Spawner
                inv.setItem(10, icon(Material.SCULK_SHRIEKER, "Spawn Warden", "Spawns at your location"));
                inv.setItem(12, icon(Material.WITHER_SKELETON_SKULL, "Spawn Wither", "Spawns at your location"));
                inv.setItem(14, icon(Material.DRAGON_HEAD, "Spawn Ender Dragon", "Spawns at your location"));
                inv.setItem(16, icon(Material.CREEPER_HEAD, "Spawn Charged Creeper", "Spawns at your location"));
                inv.setItem(22, icon(Material.TNT, "Nuke Mobs (30 Blocks)", "Removes non-player entities nearby"));
            }
            case 13 -> { // Quick Unban/Pardon
                inv.setItem(11, icon(Material.PAPER, "List Banned Players", "Prints ban list in chat"));
                inv.setItem(13, icon(Material.NAME_TAG, "Pardon Last Banned", "Unbans most recent profile ban"));
                inv.setItem(15, icon(Material.BARRIER, "Clear All IP Bans", "Removes all IP bans"));
            }
            case 14 -> { // Maintenance
                inv.setItem(11, icon(maintenanceMode ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK,
                        "Maintenance Mode: " + (maintenanceMode ? "ON" : "OFF"), "Kicks non-staff on join"));
                inv.setItem(13, icon(Bukkit.hasWhitelist() ? Material.LIME_DYE : Material.GRAY_DYE,
                        "Whitelist: " + (Bukkit.hasWhitelist() ? "ON" : "OFF"), "Click to toggle"));
                inv.setItem(15, icon(Material.BOOK, "Reload Whitelist", "Reloads whitelist.json"));
            }
            case 15 -> { // Protection
                inv.setItem(10, toggleIcon("Disable Fire Spread", disableFire));
                inv.setItem(12, toggleIcon("Disable Leaf Decay", disableLeafDecay));
                inv.setItem(14, toggleIcon("Disable Enderman Grief", disableEndermanGrief));
                inv.setItem(16, toggleIcon("Disable Global PvP", disablePvp));
            }
            case 16 -> { // Health & XP
                inv.setItem(10, icon(Material.EXPERIENCE_BOTTLE, "+10 XP Levels", "Adds 10 levels"));
                inv.setItem(12, icon(Material.EXPERIENCE_BOTTLE, "+50 XP Levels", "Adds 50 levels"));
                inv.setItem(14, icon(Material.GLASS_BOTTLE, "Reset XP to 0", "Clears levels"));
                inv.setItem(16, icon(Material.GOLDEN_APPLE, "Set Max Health: 40 HP", "20 Hearts"));
                inv.setItem(22, icon(Material.APPLE, "Reset Max Health: 20 HP", "10 Hearts"));
            }
            case 17 -> { // GameRules
                World w = p.getWorld();
                inv.setItem(10, toggleIcon("keepInventory", Boolean.TRUE.equals(w.getGameRuleValue(GameRule.KEEP_INVENTORY))));
                inv.setItem(12, toggleIcon("doDaylightCycle", Boolean.TRUE.equals(w.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))));
                inv.setItem(14, toggleIcon("doMobSpawning", Boolean.TRUE.equals(w.getGameRuleValue(GameRule.DO_MOB_SPAWNING))));
                inv.setItem(16, toggleIcon("fallDamage", Boolean.TRUE.equals(w.getGameRuleValue(GameRule.FALL_DAMAGE))));
                inv.setItem(22, toggleIcon("announceAdvancements", Boolean.TRUE.equals(w.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS))));
            }
            case 18 -> { // Staff Spy
                inv.setItem(12, toggleIcon("CommandSpy", commandSpy.contains(p.getUniqueId())));
                inv.setItem(14, toggleIcon("Diamond/Debris Ore Alert", oreAlert.contains(p.getUniqueId())));
            }
            case 19 -> { // Quick Builder
                inv.setItem(11, icon(Material.GLASS, "5x5x5 Hollow Glass Cage", "Builds around you"));
                inv.setItem(13, icon(Material.BARRIER, "Clear 10x10x10 Area", "Replaces blocks with air"));
                inv.setItem(15, icon(Material.BEDROCK, "3x3 Bedrock Platform", "Places under your feet"));
            }
            case 20 -> { // Server Control
                inv.setItem(11, icon(Material.WRITABLE_BOOK, "Save All Worlds", "Forces world save"));
                inv.setItem(13, icon(Material.BELL, "Broadcast Restart Warning", "Alerts all online players"));
                inv.setItem(15, icon(Material.REDSTONE_TORCH, "Shutdown Server", "Safely stops the server"));
            }
        }

        if (page > 11) inv.setItem(45, icon(Material.ARROW, "Previous Page (" + (page - 1) + ")", "Go back"));
        inv.setItem(49, icon(Material.NETHER_STAR, "Page " + page + " / 20", "AdminPanel2"));
        if (page < 20) inv.setItem(53, icon(Material.ARROW, "Next Page (" + (page + 1) + ")", "Go forward"));

        p.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        int page = -1;
        for (int i = 0; i < PAGE_NAMES.length; i++) {
            if (PAGE_NAMES[i].equals(title)) { page = i + 11; break; }
        }
        if (page == -1) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot == 45 && page > 11) { openPage(p, page - 1); return; }
        if (slot == 53 && page < 20) { openPage(p, page + 1); return; }

        switch (page) {
            case 11 -> {
                if (slot == 10) {
                    ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
                    sword.addUnsafeEnchantment(Enchantment.SHARPNESS, 10);
                    sword.addUnsafeEnchantment(Enchantment.UNBREAKING, 10);
                    p.getInventory().addItem(sword);
                } else if (slot == 12) {
                    ItemStack stick = new ItemStack(Material.STICK);
                    stick.addUnsafeEnchantment(Enchantment.KNOCKBACK, 10);
                    p.getInventory().addItem(stick);
                } else if (slot == 14) {
                    ItemStack pick = new ItemStack(Material.NETHERITE_PICKAXE);
                    pick.addUnsafeEnchantment(Enchantment.EFFICIENCY, 10);
                    pick.addUnsafeEnchantment(Enchantment.UNBREAKING, 10);
                    p.getInventory().addItem(pick);
                } else if (slot == 16) {
                    Material[] pieces = {Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS};
                    for (Material m : pieces) {
                        ItemStack armor = new ItemStack(m);
                        armor.addUnsafeEnchantment(Enchantment.PROTECTION, 10);
                        armor.addUnsafeEnchantment(Enchantment.UNBREAKING, 10);
                        p.getInventory().addItem(armor);
                    }
                } else if (slot == 22) {
                    p.setHealth(Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).getValue());
                    p.setFoodLevel(20);
                    p.setSaturation(20f);
                }
            }
            case 12 -> {
                Location loc = p.getLocation();
                if (slot == 10) p.getWorld().spawnEntity(loc, EntityType.WARDEN);
                else if (slot == 12) p.getWorld().spawnEntity(loc, EntityType.WITHER);
                else if (slot == 14) p.getWorld().spawnEntity(loc, EntityType.ENDER_DRAGON);
                else if (slot == 16) {
                    Creeper c = (Creeper) p.getWorld().spawnEntity(loc, EntityType.CREEPER);
                    c.setPowered(true);
                } else if (slot == 22) {
                    p.getNearbyEntities(30, 30, 30).stream().filter(ent -> !(ent instanceof Player)).forEach(Entity::remove);
                    p.sendMessage(Component.text("Cleared nearby entities.", NamedTextColor.GREEN));
                }
            }
            case 13 -> {
                if (slot == 11) {
                    p.sendMessage(Component.text("Banned count: " + Bukkit.getBanList(BanList.Type.PROFILE).getEntries().size(), NamedTextColor.YELLOW));
                } else if (slot == 13) {
                    var entries = Bukkit.getBanList(BanList.Type.PROFILE).getEntries();
                    if (!entries.isEmpty()) {
                        BanEntry<?> last = entries.iterator().next();
                        Bukkit.getBanList(BanList.Type.PROFILE).pardon(last.getTarget());
                        p.sendMessage(Component.text("Pardoned: " + last.getTarget(), NamedTextColor.GREEN));
                    }
                } else if (slot == 15) {
                    Bukkit.getIPBans().forEach(Bukkit::unbanIP);
                    p.sendMessage(Component.text("Cleared all IP bans.", NamedTextColor.GREEN));
                }
            }
            case 14 -> {
                if (slot == 11) maintenanceMode = !maintenanceMode;
                else if (slot == 13) Bukkit.setWhitelist(!Bukkit.hasWhitelist());
                else if (slot == 15) Bukkit.reloadWhitelist();
                openPage(p, 14);
            }
            case 15 -> {
                if (slot == 10) disableFire = !disableFire;
                else if (slot == 12) disableLeafDecay = !disableLeafDecay;
                else if (slot == 14) disableEndermanGrief = !disableEndermanGrief;
                else if (slot == 16) disablePvp = !disablePvp;
                openPage(p, 15);
            }
            case 16 -> {
                if (slot == 10) p.giveExpLevels(10);
                else if (slot == 12) p.giveExpLevels(50);
                else if (slot == 14) p.setLevel(0);
                else if (slot == 16) Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(40.0);
                else if (slot == 22) Objects.requireNonNull(p.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20.0);
            }
            case 17 -> {
                World w = p.getWorld();
                if (slot == 10) toggleRule(w, GameRule.KEEP_INVENTORY);
                else if (slot == 12) toggleRule(w, GameRule.DO_DAYLIGHT_CYCLE);
                else if (slot == 14) toggleRule(w, GameRule.DO_MOB_SPAWNING);
                else if (slot == 16) toggleRule(w, GameRule.FALL_DAMAGE);
                else if (slot == 22) toggleRule(w, GameRule.ANNOUNCE_ADVANCEMENTS);
                openPage(p, 17);
            }
            case 18 -> {
                if (slot == 12) toggleSet(commandSpy, p.getUniqueId());
                else if (slot == 14) toggleSet(oreAlert, p.getUniqueId());
                openPage(p, 18);
            }
            case 19 -> {
                Location c = p.getLocation().getBlock().getLocation();
                if (slot == 11) {
                    for (int x = -2; x <= 2; x++) for (int y = -1; y <= 3; y++) for (int z = -2; z <= 2; z++) {
                        if (Math.abs(x) == 2 || y == -1 || y == 3 || Math.abs(z) == 2)
                            c.clone().add(x, y, z).getBlock().setType(Material.GLASS);
                    }
                } else if (slot == 13) {
                    for (int x = -5; x <= 4; x++) for (int y = 0; y <= 9; y++) for (int z = -5; z <= 4; z++)
                        c.clone().add(x, y, z).getBlock().setType(Material.AIR);
                } else if (slot == 15) {
                    for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++)
                        c.clone().add(x, -1, z).getBlock().setType(Material.BEDROCK);
                }
            }
            case 20 -> {
                if (slot == 11) {
                    Bukkit.getWorlds().forEach(World::save);
                    p.sendMessage(Component.text("All worlds saved!", NamedTextColor.GREEN));
                } else if (slot == 13) {
                    Bukkit.broadcast(Component.text("[SERVER] Server restarting soon! Please find a safe spot.", NamedTextColor.RED));
                } else if (slot == 15) {
                    Bukkit.shutdown();
                }
            }
        }
    }

    private void toggleRule(World w, GameRule<Boolean> rule) {
        Boolean cur = w.getGameRuleValue(rule);
        w.setGameRule(rule, cur == null || !cur);
    }

    private void toggleSet(Set<UUID> set, UUID id) {
        if (!set.remove(id)) set.add(id);
    }

    private ItemStack icon(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.GOLD));
        meta.lore(List.of(Component.text(lore, NamedTextColor.GRAY)));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack toggleIcon(String name, boolean state) {
        return icon(state ? Material.LIME_CONCRETE : Material.RED_CONCRETE, name + ": " + (state ? "ENABLED" : "DISABLED"), "Click to toggle");
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        if (maintenanceMode) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(e.getUniqueId());
            if (!op.isOp()) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Server is currently in Maintenance Mode.", NamedTextColor.RED));
            }
        }
    }

    @EventHandler public void onBurn(BlockBurnEvent e) { if (disableFire) e.setCancelled(true); }
    @EventHandler public void onIgnite(BlockIgniteEvent e) { if (disableFire) e.setCancelled(true); }
    @EventHandler public void onDecay(LeavesDecayEvent e) { if (disableLeafDecay) e.setCancelled(true); }
    @EventHandler public void onGrief(EntityChangeBlockEvent e) { if (disableEndermanGrief && e.getEntityType() == EntityType.ENDERMAN) e.setCancelled(true); }
    @EventHandler public void onPvp(EntityDamageByEntityEvent e) { if (disablePvp && e.getEntity() instanceof Player && e.getDamager() instanceof Player) e.setCancelled(true); }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent e) {
        for (UUID id : commandSpy) {
            Player admin = Bukkit.getPlayer(id);
            if (admin != null && !admin.equals(e.getPlayer())) {
                admin.sendMessage(Component.text("[Spy] " + e.getPlayer().getName() + ": " + e.getMessage(), NamedTextColor.DARK_GRAY));
            }
        }
    }

    @EventHandler
    public void onMine(BlockBreakEvent e) {
        Material m = e.getBlock().getType();
        if (m == Material.DIAMOND_ORE || m == Material.DEEPSLATE_DIAMOND_ORE || m == Material.ANCIENT_DEBRIS) {
            for (UUID id : oreAlert) {
                Player admin = Bukkit.getPlayer(id);
                if (admin != null) {
                    admin.sendMessage(Component.text("[OreAlert] " + e.getPlayer().getName() + " mined " + m.name(), NamedTextColor.AQUA));
                }
            }
        }
    }
}
