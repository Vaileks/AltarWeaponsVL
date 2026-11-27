package dev.vaileks.altarweaponsvl.altars.commands;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.altars.Altar;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditAltarCommand
implements CommandExecutor,
TabCompleter {
    private static final List<String> SUBCOMMANDS = List.of("help", "give", "set", "remove", "save", "load", "delete", "create", "reload", "restart", "teleport", "teleporthere");
    private final AltarWeaponsVL plugin;

    public EditAltarCommand(AltarWeaponsVL plugin) {
        this.plugin = plugin;
    }

    private boolean hasAdminPermission(CommandSender sender) {
        return sender.hasPermission("runesaltarv.admin") || sender.hasPermission("runesaltarv.admin");
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return this.executeSubcommand(sender, args);
    }

    public boolean executeSubcommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!this.hasAdminPermission(sender)) {
            this.send(sender, "command.no_permission", new String[0]);
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            this.showHelp(sender);
            return true;
        }
        String subCommand = args[0].toLowerCase(java.util.Locale.ROOT);
        if (subCommand.equals("reload")) {
            this.plugin.reloadCrafting();
            for (Integer id : this.plugin.getAltarManager().getAltarsIds()) {
                Altar altar = this.plugin.getAltarManager().getAltar(id);
                if (altar == null) continue;
                altar.spawnEntities();
            }
            this.send(sender, "command.reload_success", new String[0]);
            return true;
        }
        if (subCommand.equals("restart")) {
            this.plugin.getAltarManager().cleanupEntities();
            for (Integer id : this.plugin.getAltarManager().getAltarsIds()) {
                Altar altar = this.plugin.getAltarManager().getAltar(id);
                if (altar == null) continue;
                altar.spawnEntities();
            }
            this.send(sender, "command.restart_success", new String[0]);
            return true;
        }
        if (subCommand.equals("give")) {
            if (args.length < 2) {
                this.send(sender, "command.give_usage", new String[0]);
                return true;
            }
            Player target = Bukkit.getPlayerExact((String)args[1]);
            if (target == null) {
                this.send(sender, "command.player_not_found", "player", args[1]);
                return true;
            }
            ItemStack altarItem = this.plugin.getAltarManager().createAltarItem();
            target.getInventory().addItem(altarItem).values().forEach(item -> target.getWorld().dropItemNaturally(target.getLocation(), item));
            this.send(sender, "command.give_success", "player", target.getName());
            return true;
        }
        if (!(sender instanceof Player)) {
            this.send(sender, "command.player_only", new String[0]);
            return true;
        }
        Player player = (Player)sender;
        if (subCommand.equals("teleport") || subCommand.equals("tp")) {
            if (args.length < 2) {
                this.send(player, "command.teleport_usage", new String[0]);
                return true;
            }
            int id = this.tryParse(args[1]);
            Altar altar = this.getAltar(player, id);
            if (altar == null) {
                return true;
            }
            player.teleport(altar.getLocation().clone().add(0.5, 1.0, 0.5));
            this.send(player, "command.teleport_success", "id", String.valueOf(id));
            return true;
        }
        if (subCommand.equals("teleporthere") || subCommand.equals("tphere")) {
            if (args.length < 2) {
                this.send(player, "command.teleporthere_usage", new String[0]);
                return true;
            }
            int id = this.tryParse(args[1]);
            Altar altar = this.getAltar(player, id);
            if (altar == null) {
                return true;
            }
            Location newLocation = player.getLocation().getBlock().getLocation();
            if (!newLocation.getBlock().getType().isAir() || this.plugin.getAltarManager().getAltarAt(newLocation) != null) {
                this.send(player, "error.altar_already_here", new String[0]);
                return true;
            }
            altar.getLocation().getBlock().setType(Material.AIR);
            altar.removeAllEntities();
            altar.setLocation(newLocation);
            newLocation.getBlock().setType(Material.BARRIER);
            altar.spawnEntities();
            this.plugin.getAltarManager().saveAltars();
            this.send(player, "command.teleporthere_success", "id", String.valueOf(id));
            return true;
        }
        if (subCommand.equals("create")) {
            Location location = player.getLocation().getBlock().getLocation();
            if (!location.getBlock().getType().isAir() || this.plugin.getAltarManager().getAltarAt(location) != null) {
                this.send(player, "error.altar_already_here", new String[0]);
                return true;
            }
            location.getBlock().setType(Material.BARRIER);
            Altar altar = this.plugin.getAltarManager().createAltar(location);
            altar.spawnEntities();
            this.send(player, "command.create_success", "id", String.valueOf(altar.getId()));
            return true;
        }
        if (subCommand.equals("delete")) {
            this.handleDelete(player, args);
            return true;
        }
        if (subCommand.equals("remove")) {
            this.handleRemove(player, args);
            return true;
        }
        if (subCommand.equals("set")) {
            this.handleSet(player, args);
            return true;
        }
        if (subCommand.equals("save")) {
            this.handleSave(player, args);
            return true;
        }
        if (subCommand.equals("load")) {
            this.handleLoad(player, args);
            return true;
        }
        this.send(sender, "command.unknown", new String[0]);
        return true;
    }

    private void handleDelete(Player player, String[] args) {
        if (args.length < 2) {
            this.send(player, "command.delete_usage", new String[0]);
            return;
        }
        if (args[1].equalsIgnoreCase("all")) {
            ArrayList<Integer> ids = new ArrayList<Integer>(this.plugin.getAltarManager().getAltarsIds());
            if (ids.isEmpty()) {
                this.send(player, "command.delete_none", new String[0]);
                return;
            }
            Iterator iterator = ids.iterator();
            while (iterator.hasNext()) {
                int id = (Integer)iterator.next();
                Altar altar = this.plugin.getAltarManager().getAltar(id);
                if (altar == null) continue;
                altar.getLocation().getBlock().setType(Material.AIR);
                this.plugin.getAltarManager().removeAltar(id);
            }
            this.send(player, "command.delete_all_success", "amount", String.valueOf(ids.size()));
            return;
        }
        int id = this.tryParse(args[1]);
        Altar altar = this.getAltar(player, id);
        if (altar == null) {
            return;
        }
        altar.getLocation().getBlock().setType(Material.AIR);
        this.plugin.getAltarManager().removeAltar(id);
        this.send(player, "command.delete_success", "id", String.valueOf(id));
    }

    private void handleRemove(Player player, String[] args) {
        if (args.length < 2) {
            this.send(player, "command.remove_usage", new String[0]);
            return;
        }
        String action = args[1].toLowerCase();
        if (action.equals("item")) {
            if (args.length < 3) {
                this.send(player, "command.remove_item_usage", new String[0]);
                return;
            }
            int id = this.tryParse(args[2]);
            Altar altar = this.getAltar(player, id);
            if (altar == null) {
                return;
            }
            altar.setResultItem(null);
            altar.setRecipe(new ArrayList<ItemStack>());
            altar.setCrafted(false);
            altar.spawnEntities();
            this.plugin.getAltarManager().saveAltars();
            this.send(player, "command.remove_item_success", "id", String.valueOf(id));
            return;
        }
        if (action.equals("recipe")) {
            if (args.length < 4) {
                this.send(player, "command.remove_recipe_usage", new String[0]);
                return;
            }
            int id = this.tryParse(args[2]);
            Altar altar = this.getAltar(player, id);
            if (altar == null) {
                return;
            }
            int recipeSize = altar.getRecipe().size();
            if (recipeSize == 0) {
                this.send(player, "error.recipe_empty", "id", String.valueOf(id));
                return;
            }
            int position = this.tryParse(args[3]);
            if (position < 1 || position > recipeSize) {
                this.send(player, "error.recipe_position", "max", String.valueOf(recipeSize));
                return;
            }
            altar.getRecipe().remove(position - 1);
            altar.spawnEntities();
            this.plugin.getAltarManager().saveAltars();
            this.send(player, "command.remove_recipe_success", "position", String.valueOf(position), "id", String.valueOf(id));
            return;
        }
        this.send(player, "command.remove_usage", new String[0]);
    }

    private void handleSet(Player player, String[] args) {
        if (args.length < 3) {
            this.send(player, "command.set_usage", new String[0]);
            return;
        }
        String action = args[1].toLowerCase();
        if (action.equals("use")) {
            if (args.length < 4) {
                this.send(player, "command.set_use_usage", new String[0]);
                return;
            }
            int id = this.tryParse(args[2]);
            Altar altar = this.getAltar(player, id);
            if (altar == null) {
                return;
            }
            if (args[3].equalsIgnoreCase("inf")) {
                altar.setMaxUses(-1);
                this.send(player, "command.set_use_infinite", "id", String.valueOf(id));
            } else {
                int uses = this.tryParse(args[3]);
                if (uses <= 0) {
                    this.send(player, "error.positive_integer", new String[0]);
                    return;
                }
                altar.setMaxUses(uses);
                this.send(player, "command.set_use_success", "id", String.valueOf(id), "amount", String.valueOf(uses));
            }
            altar.spawnEntities();
            this.plugin.getAltarManager().saveAltars();
            return;
        }
        if (action.equals("cooldown")) {
            if (args.length < 4) {
                this.send(player, "command.set_cooldown_usage", new String[0]);
                return;
            }
            int id = this.tryParse(args[2]);
            Altar altar = this.getAltar(player, id);
            if (altar == null) {
                return;
            }
            int cooldown = this.tryParse(args[3]);
            if (cooldown < 0) {
                this.send(player, "error.non_negative_integer", new String[0]);
                return;
            }
            altar.setCooldownSeconds(cooldown);
            this.plugin.getAltarManager().saveAltars();
            this.send(player, "command.set_cooldown_success", "id", String.valueOf(id), "seconds", String.valueOf(cooldown));
            return;
        }
        if (action.equals("item") || action.equals("recipe")) {
            if (args.length >= 5 && action.equals("recipe") && args[3].equalsIgnoreCase("head")) {
                int id = this.tryParse(args[2]);
                Altar altar = this.getAltar(player, id);
                if (altar == null) {
                    return;
                }
                int amount = this.tryParse(args[4]);
                ItemStack head = new ItemStack(Material.PLAYER_HEAD, amount <= 0 ? 1 : amount);
                SkullMeta meta = (SkullMeta)head.getItemMeta();
                if (meta != null) {
                    if (args.length >= 6) {
                        String playerName = args[5];
                        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer((String)playerName);
                        meta.setOwningPlayer(targetPlayer);
                        meta.displayName((Component)Component.text(playerName + "'s Head"));
                    } else {
                        meta.displayName((Component)Component.text("Player's Head"));
                    }
                    head.setItemMeta(meta);
                }
                altar.addRecipeItem(head);
                altar.spawnEntities();
                this.plugin.getAltarManager().saveAltars();
                this.send(player, "command.set_recipe_success", "position", String.valueOf(altar.getRecipe().size()), "id", String.valueOf(id));
                return;
            }
            int id = this.tryParse(args[2]);
            Altar altar = this.getAltar(player, id);
            if (altar == null) {
                return;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (hand.getType().isAir()) {
                this.send(player, "error.empty_hand", new String[0]);
                return;
            }
            if (action.equals("item")) {
                ItemStack result = hand.clone();
                result.setAmount(1);
                altar.setResultItem(result);
                altar.getRecipe().clear();
                altar.setCrafted(false);
                altar.spawnEntities();
                this.plugin.getAltarManager().saveAltars();
                this.send(player, "command.set_item_success", "id", String.valueOf(id));
            } else {
                altar.addRecipeItem(hand.clone());
                altar.spawnEntities();
                this.plugin.getAltarManager().saveAltars();
                this.send(player, "command.set_recipe_success", "position", String.valueOf(altar.getRecipe().size()), "id", String.valueOf(id));
            }
            return;
        }
        if (action.equals("scale")) {
            this.handleDisplaySetting(player, args);
            return;
        }
        this.send(player, "command.set_usage", new String[0]);
    }

    private void handleDisplaySetting(Player player, String[] args) {
        double value;
        if (args.length < 5) {
            this.send(player, "command.set_display_usage", "action", "scale");
            return;
        }
        String type = args[2].toLowerCase();
        if (!List.of("title", "recipe", "item").contains(type)) {
            this.send(player, "command.set_display_invalid_type", new String[0]);
            return;
        }
        try {
            value = Double.parseDouble(args[4]);
        }
        catch (NumberFormatException exception) {
            this.send(player, "error.number", new String[0]);
            return;
        }
        if (!Double.isFinite(value) || value <= 0 || value > 16) { this.send(player, "error.number"); return; }
        String target = args[3].toLowerCase();
        String localizedType = this.plugin.getAltarLanguageManager().plain("display_type." + type, new String[0]);
        if (target.equals("all") || target.equals("def")) {
            this.setDisplayConfigDefault(type, value);
            this.send(player, "command.set_default_scale_success", "type", localizedType, "value", String.valueOf(value));
            if (target.equals("all")) {
                for (Integer id : this.plugin.getAltarManager().getAltarsIds()) {
                    Altar a = this.plugin.getAltarManager().getAltar(id);
                    if (a == null) continue;
                    a.setCustomScale(type, "x", (float)value);
                    a.setCustomScale(type, "y", (float)value);
                    a.setCustomScale(type, "z", (float)value);
                }
                this.plugin.getAltarManager().saveAltars();
                this.send(player, "command.set_applied_all", new String[0]);
            }
        } else {
            int id = this.tryParse(target);
            Altar altar = this.plugin.getAltarManager().getAltar(id);
            if (altar == null) {
                this.send(player, "error.altar_id_not_found", "id", String.valueOf(id));
                return;
            }
            altar.setCustomScale(type, "x", (float)value);
            altar.setCustomScale(type, "y", (float)value);
            altar.setCustomScale(type, "z", (float)value);
            this.plugin.getAltarManager().saveAltars();
            this.send(player, "command.set_scale_success", "type", localizedType, "id", String.valueOf(id), "value", String.valueOf(value));
        }
        for (Integer id : this.plugin.getAltarManager().getAltarsIds()) {
            Altar altar = this.plugin.getAltarManager().getAltar(id);
            if (altar == null) continue;
            altar.spawnEntities();
        }
    }

    private void setDisplayConfigDefault(String type, double value) {
        String path = "settings.display." + type;
        this.plugin.getAltarsConfig().set(path + ".scale.x", (Object)value);
        this.plugin.getAltarsConfig().set(path + ".scale.y", (Object)value);
        this.plugin.getAltarsConfig().set(path + ".scale.z", (Object)value);
        this.plugin.saveAltarsConfig();
    }

    private void handleSave(Player player, String[] args) {
        if (args.length < 3) {
            this.send(player, "command.save_usage", new String[0]);
            return;
        }
        int id = this.tryParse(args[1]);
        String name = args[2].toLowerCase();
        Altar altar = this.getAltar(player, id);
        if (altar == null) {
            return;
        }
        String basePath = "templates." + name;
        this.plugin.getAltarManager().getDataConfig().set(basePath + ".result", (Object)altar.getResultItem());
        this.plugin.getAltarManager().getDataConfig().set(basePath + ".recipe", altar.getRecipe());
        for (String type : List.of("title", "recipe", "item")) {
            double defaultY = type.equals("title") ? 3.2 : (type.equals("recipe") ? 3.5 : 1.5);
            double yOffset = altar.getOffset(type, "y", defaultY);
            this.plugin.getAltarManager().getDataConfig().set(basePath + ".display." + type + ".offset.y", (Object)yOffset);
            float scale = altar.getScale(type, "x", 1.0);
            this.plugin.getAltarManager().getDataConfig().set(basePath + ".display." + type + ".scale", (Object)Float.valueOf(scale));
        }
        this.plugin.getAltarManager().saveDataConfig();
        this.send(player, "command.save_success", "id", String.valueOf(id), "name", name);
    }

    private void handleLoad(Player player, String[] args) {
        if (args.length < 2) {
            this.send(player, "command.load_usage", new String[0]);
            return;
        }
        String name = args[1].toLowerCase();
        String basePath = "templates." + name;
        if (!this.plugin.getAltarManager().getDataConfig().contains(basePath)) {
            this.send(player, "template_not_found", "name", name);
            return;
        }
        Location location = player.getLocation().getBlock().getLocation();
        if (!location.getBlock().getType().isAir() || this.plugin.getAltarManager().getAltarAt(location) != null) {
            this.send(player, "error.altar_already_here", new String[0]);
            return;
        }
        location.getBlock().setType(Material.BARRIER);
        Altar altar = this.plugin.getAltarManager().createAltar(location);
        int newId = altar.getId();
        altar.setResultItem(this.plugin.getAltarManager().getDataConfig().getItemStack(basePath + ".result"));
        altar.setRecipe(this.plugin.getAltarManager().getDataConfig().getList(basePath + ".recipe", List.of()).stream().filter(ItemStack.class::isInstance).map(ItemStack.class::cast).toList());
        for (String type : List.of("title", "recipe", "item")) {
            if (this.plugin.getAltarManager().getDataConfig().contains(basePath + ".display." + type + ".offset.y")) {
                double offset = this.plugin.getAltarManager().getDataConfig().getDouble(basePath + ".display." + type + ".offset.y");
                altar.setCustomOffset(type, "y", offset);
            }
            if (!this.plugin.getAltarManager().getDataConfig().contains(basePath + ".display." + type + ".scale")) continue;
            float scale = (float)this.plugin.getAltarManager().getDataConfig().getDouble(basePath + ".display." + type + ".scale");
            altar.setCustomScale(type, "x", scale);
            altar.setCustomScale(type, "y", scale);
            altar.setCustomScale(type, "z", scale);
        }
        altar.spawnEntities();
        this.plugin.getAltarManager().saveAltars();
        this.send(player, "command.load_success", "name", name, "id", String.valueOf(newId));
    }

    private void showHelp(CommandSender sender) {
        for (Component line : this.plugin.getAltarLanguageManager().components("help.lines", new String[0])) {
            TextUtils.sendMessage(sender, line);
        }
    }

    private Altar getAltar(CommandSender sender, int id) {
        Altar altar = this.plugin.getAltarManager().getAltar(id);
        if (altar == null) {
            this.send(sender, "error.altar_not_found", new String[0]);
        }
        return altar;
    }

    private void send(CommandSender sender, String key, String ... replacements) {
        TextUtils.sendMessage(sender, this.plugin.getAltarLanguageManager().component(key, replacements));
    }

    private int tryParse(String value) {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException exception) {
            return -1;
        }
    }

    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return this.completeSubcommand(sender, args);
    }

    @NotNull
    public List<String> completeSubcommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!this.hasAdminPermission(sender)) {
            return List.of();
        }
        ArrayList<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            completions.addAll(SUBCOMMANDS);
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase(java.util.Locale.ROOT);
            if (subCommand.equals("give")) {
                completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            } else if (subCommand.equals("set")) {
                completions.addAll(List.of("item", "recipe", "scale", "use", "cooldown"));
            } else if (subCommand.equals("remove")) {
                completions.addAll(List.of("recipe", "item"));
            } else if (List.of("save", "teleport", "teleporthere", "tp", "tphere").contains(subCommand)) {
                completions.addAll(this.altarIds());
            } else if (subCommand.equals("delete")) {
                completions.add("all");
                completions.addAll(this.altarIds());
            } else if (subCommand.equals("load") && this.plugin.getAltarManager().getDataConfig().getConfigurationSection("templates") != null) {
                completions.addAll(this.plugin.getAltarManager().getDataConfig().getConfigurationSection("templates").getKeys(false));
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase(java.util.Locale.ROOT);
            String action = args[1].toLowerCase();
            if (subCommand.equals("set")) {
                if (List.of("item", "recipe", "use", "cooldown").contains(action)) {
                    completions.addAll(this.altarIds());
                } else if (action.equals("scale")) {
                    completions.addAll(List.of("title", "recipe", "item"));
                }
            } else if (subCommand.equals("remove") && List.of("recipe", "item").contains(action)) {
                completions.addAll(this.altarIds());
            }
        } else if (args.length == 4) {
            Altar altar;
            String subCommand = args[0].toLowerCase(java.util.Locale.ROOT);
            String action = args[1].toLowerCase();
            if (subCommand.equals("set")) {
                if (action.equals("scale")) {
                    completions.addAll(List.of("all", "def"));
                    completions.addAll(this.altarIds());
                } else if (action.equals("use")) {
                    completions.addAll(List.of("inf", "1", "5", "10"));
                } else if (action.equals("cooldown")) {
                    completions.addAll(List.of("0", "10", "30", "60"));
                } else if (action.equals("recipe")) {
                    completions.add("head");
                }
            } else if (subCommand.equals("remove") && action.equals("recipe") && (altar = this.plugin.getAltarManager().getAltar(this.tryParse(args[2]))) != null) {
                completions.addAll(IntStream.rangeClosed(1, altar.getRecipe().size()).mapToObj(String::valueOf).toList());
            }
        } else if (args.length == 5) {
            if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("recipe") && args[3].equalsIgnoreCase("head")) {
                completions.addAll(List.of("1", "2", "4", "8", "16", "32", "64"));
            } else if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("scale")) {
                completions.addAll(List.of("0.5", "1.0", "1.5", "2.0", "2.4", "3.0", "3.2", "3.5"));
            }
        } else if (args.length == 6 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("recipe") && args[3].equalsIgnoreCase("head")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }
        String lastWord = args[args.length - 1].toLowerCase();
        return completions.stream().filter(value -> value.toLowerCase().startsWith(lastWord)).collect(Collectors.toList());
    }

    private List<String> altarIds() {
        return this.plugin.getAltarManager().getAltarsIds().stream().sorted().map(String::valueOf).toList();
    }
}

