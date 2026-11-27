package dev.vaileks.altarweaponsvl.altars.listeners;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.altars.Altar;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public class AltarListener
implements Listener {
    private final AltarWeaponsVL plugin;

    public AltarListener(AltarWeaponsVL plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (event.getBlockAgainst().getType() == Material.BARRIER && this.plugin.getAltarManager().getAltarAt(event.getBlockAgainst().getLocation()) != null) {
            event.setCancelled(true);
            return;
        }
        if (item.getType() != Material.STRUCTURE_BLOCK || !item.hasItemMeta()) {
            return;
        }
        if (!item.getItemMeta().getPersistentDataContainer().has(this.plugin.getAltarManager().getAltarKey(), PersistentDataType.BYTE)) {
            return;
        }
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (event.isCancelled() || event.getBlockPlaced().getType() != Material.STRUCTURE_BLOCK) return;
            event.getBlockPlaced().setType(Material.BARRIER, false);
            Altar altar = this.plugin.getAltarManager().createAltar(event.getBlockPlaced().getLocation());
            altar.spawnEntities();
            TextUtils.sendMessage((CommandSender)event.getPlayer(), this.plugin.getAltarLanguageManager().component("altar.placed", "id", String.valueOf(altar.getId())));
        });
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BARRIER) {
            return;
        }
        Altar altar = this.plugin.getAltarManager().getAltarAt(block.getLocation());
        if (altar == null || event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);
        Player player = event.getPlayer();
        if ((player.isOp() || player.hasPermission("runesaltarv.admin")) && player.isSneaking()) {
            String uses = altar.getMaxUses() == -1 ? this.plugin.getAltarLanguageManager().plain("altar.infinite_uses", new String[0]) : altar.getCurrentUses() + "/" + altar.getMaxUses();
            TextUtils.sendMessage(player, this.plugin.getAltarLanguageManager().component("altar.admin_info", "id", String.valueOf(altar.getId()), "uses", uses));
            return;
        }
        if (altar.getCooldownRemaining() > 0L) {
            TextUtils.sendMessage(player, this.plugin.getAltarLanguageManager().component("altar.still_on_cooldown", new String[0]));
            return;
        }
        if (altar.isCrafted() && altar.getMaxUses() != -1 && altar.getCurrentUses() <= 0) {
            return;
        }
        if (altar.getResultItem() == null) {
            return;
        }
        this.processCraft(player, altar);
    }

    private void processCraft(Player player, Altar altar) {
        List<ItemStack> recipe = mergeRequirements(altar.getRecipe());
        if (recipe.isEmpty()) {
            return;
        }
        ItemStack[] planned = planIngredients(player.getInventory().getStorageContents(), recipe);
        if (planned == null) {
            TextUtils.sendMessage(player, this.plugin.getAltarLanguageManager().component("craft.missing_header"));
            return;
        }
        player.getInventory().setStorageContents(planned);
        ItemStack craftedResult = altar.getResultItem().clone();
        Location location = altar.getLocation().clone().add(0.5, 1.0, 0.5);
        location.getWorld().strikeLightningEffect(location);
        altar.setLastUsedTime(System.currentTimeMillis());
        if (altar.getMaxUses() != -1) {
            altar.setCurrentUses(altar.getCurrentUses() - 1);
            if (altar.getCurrentUses() <= 0) {
                altar.setCrafted(true);
                altar.removeHolograms();
            }
        }
        if (altar.getCurrentUses() > 0 || altar.getMaxUses() == -1) {
            altar.spawnEntities();
        }
        this.plugin.getAltarManager().saveAltars();
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(craftedResult);
        for (ItemStack dropItem : leftover.values()) {
            Item drop = location.getWorld().dropItem(location.clone().add(0, 1.5, 0), dropItem);
            drop.setPickupDelay(0);
            drop.setVelocity(new Vector(0, 0.1, 0));
        }
            Component playerName = MiniMessage.miniMessage().deserialize("<gradient:#FFAA00:#FFFFFF:#FFAA00>" + player.getName() + "</gradient>").decoration(TextDecoration.ITALIC, false);
            Component itemHoverComp = ((TextComponent)((TextComponent)((TextComponent)Component.text("[").append(Altar.getItemDisplayName(craftedResult))).append(Component.text("]"))).color(NamedTextColor.GOLD)).hoverEvent((HoverEventSource)craftedResult.asHoverEvent());
            Component broadcastMessage = playerName.append(this.plugin.getAltarLanguageManager().component("craft.broadcast_prefix", new String[0])).append(itemHoverComp).append(this.plugin.getAltarLanguageManager().component("craft.broadcast_suffix", new String[0]));
            Bukkit.broadcast(broadcastMessage);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }

    }

    public static List<ItemStack> mergeRequirements(List<ItemStack> recipe) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack item : recipe) {
            if (item == null || item.getType().isAir() || item.getAmount() <= 0) continue;
            ItemStack same = merged.stream().filter(i -> i.isSimilar(item)).findFirst().orElse(null);
            if (same == null) merged.add(item.clone()); else same.setAmount(Math.addExact(same.getAmount(), item.getAmount()));
        }
        return merged;
    }

    public ItemStack[] planIngredients(ItemStack[] inventory, List<ItemStack> recipe) {
        ItemStack[] planned = java.util.Arrays.stream(inventory).map(i -> i == null ? null : i.clone()).toArray(ItemStack[]::new);
        List<ItemStack> ordered = new ArrayList<>(mergeRequirements(recipe));
        ordered.sort(java.util.Comparator.comparingInt(i -> i.getType() == Material.PLAYER_HEAD && ((SkullMeta)i.getItemMeta()).hasOwner() ? 0 : 1));
        for (ItemStack required : ordered) {
            int remaining = required.getAmount();
            for (int slot = 0; slot < planned.length && remaining > 0; slot++) {
                ItemStack item = planned[slot];
                if (item == null || !matchesRequirement(item, required)) continue;
                int used = Math.min(item.getAmount(), remaining);
                remaining -= used;
                if (item.getAmount() == used) planned[slot] = null; else item.setAmount(item.getAmount() - used);
            }
            if (remaining > 0) return null;
        }
        return planned;
    }

    private boolean matchesRequirement(ItemStack item, ItemStack requirement) {
        String common = dev.vaileks.altarweaponsvl.integration.CommonItems.id(requirement);
        if (common != null) return item.getType() == requirement.getType() && common.equals(dev.vaileks.altarweaponsvl.integration.CommonItems.id(item));
        if (requirement.getType() == Material.PLAYER_HEAD && item.getType() == Material.PLAYER_HEAD) {
            SkullMeta reqMeta = (SkullMeta)requirement.getItemMeta();
            SkullMeta itemMeta = (SkullMeta)item.getItemMeta();
            if (reqMeta != null && reqMeta.hasOwner()) {
                if (itemMeta != null && itemMeta.hasOwner()) {
                    OfflinePlayer reqOwner = reqMeta.getOwningPlayer();
                    OfflinePlayer itemOwner = itemMeta.getOwningPlayer();
                    return reqOwner != null && itemOwner != null && reqOwner.getUniqueId().equals(itemOwner.getUniqueId());
                }
                return false;
            }
            return true;
        }
        return item.isSimilar(requirement);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        boolean enabled = this.plugin.getConfig().getBoolean("player-heads.enabled", this.plugin.getConfig().getBoolean("player_heads.enabled", this.plugin.getAltarsConfig().getBoolean("player_heads.enabled", true)));
        if (!enabled) {
            return;
        }
        Player victim = event.getEntity();
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta)head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer((OfflinePlayer)victim);
            String headName = this.plugin.getAltarLanguageManager().plain("head.name", "player", victim.getName());
            meta.displayName((Component)LegacyComponentSerializer.legacyAmpersand().deserialize(headName));
            Component deathComponent = event.deathMessage();
            String deathCause = deathComponent != null ? PlainTextComponentSerializer.plainText().serialize(deathComponent) : "Умер";
            ArrayList<Component> lore = new ArrayList<Component>();
            lore.add(this.plugin.getAltarLanguageManager().component("head.lore_cause", "cause", deathCause));
            meta.lore(lore);
            head.setItemMeta(meta);
        }
        event.getDrops().add(head);
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.BARRIER) {
            return;
        }
        Altar altar = this.plugin.getAltarManager().getAltarAt(block.getLocation());
        if (altar == null) {
            return;
        }
        if (!event.getPlayer().hasPermission("runesaltarv.admin")) { event.setCancelled(true); return; }
        event.setDropItems(false);
        altar.removeAllEntities();
        this.plugin.getAltarManager().removeAltar(altar.getId());
        TextUtils.sendMessage((CommandSender)event.getPlayer(), this.plugin.getAltarLanguageManager().component("altar.removed", "id", String.valueOf(altar.getId())));
    }
}

