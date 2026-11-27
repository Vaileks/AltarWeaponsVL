package dev.vaileks.altarweaponsvl.items.copperpickaxe.listeners;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.items.copperpickaxe.CopperPickaxeItem;
import dev.vaileks.altarweaponsvl.utils.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundGroup;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageAbortEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CopperPickaxeListener
implements Listener {
    private final AltarWeaponsVL plugin;
    private final Map<UUID, BlockFace> lastFace = new HashMap<UUID, BlockFace>();
    private final Map<UUID, List<Block>> previewBlocks = new HashMap<UUID, List<Block>>();
    private final Map<UUID, BukkitRunnable> previewTasks = new HashMap<UUID, BukkitRunnable>();
    private final Set<UUID> active3x3Breaks = new HashSet<UUID>();

    public CopperPickaxeListener(AltarWeaponsVL plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        boolean mainIsPickaxe = CopperPickaxeItem.isCopperPickaxe(mainHand);
        boolean offIsPickaxe = CopperPickaxeItem.isCopperPickaxe(offHand);
        if (!mainIsPickaxe && !offIsPickaxe) {
            return;
        }
        event.setCancelled(true);
        ItemStack pickaxe = mainIsPickaxe ? mainHand : offHand;
        CopperPickaxeItem.toggleMode(pickaxe);
        boolean enabled = CopperPickaxeItem.isModeEnabled(pickaxe);
        String status = enabled ? "&a" + this.plugin.tr("Включено", "Enabled") : "&c" + this.plugin.tr("Выключено", "Disabled");
        TextUtils.sendActionBar(player, TextUtils.legacy("&63x3 " + this.plugin.tr("Копание: ", "Mining: ") + status));
        player.playSound(player.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0f, 2.0f);
        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> ((Player)player).updateInventory());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        this.lastFace.put(event.getPlayer().getUniqueId(), event.getBlockFace());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.clearPreview(event.getPlayer());
        this.lastFace.remove(event.getPlayer().getUniqueId());
        this.active3x3Breaks.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!CopperPickaxeItem.isCopperPickaxe(tool) || !CopperPickaxeItem.isModeEnabled(tool)) {
            return;
        }
        Block origin = event.getBlock();
        if (origin.getType() == Material.BEDROCK || origin.getType().getHardness() < 0.0f) {
            return;
        }
        BlockFace face = this.lastFace.getOrDefault(player.getUniqueId(), BlockFace.SELF);
        List<Block> area = this.computeArea(origin, face);
        Set<Material> blocked = this.getBlockedMaterials();
        this.clearPreview(player);
        this.previewBlocks.put(player.getUniqueId(), area);
        this.startPreviewTask(player, blocked);
    }

    @EventHandler
    public void onBlockDamageAbort(BlockDamageAbortEvent event) {
        this.clearPreview(event.getPlayer());
    }

    
    @EventHandler(priority=org.bukkit.event.EventPriority.MONITOR, ignoreCancelled=true)
    public void onBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player player = event.getPlayer();
        if (this.active3x3Breaks.contains(player.getUniqueId())) {
            return;
        }
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!CopperPickaxeItem.isCopperPickaxe(tool) || !CopperPickaxeItem.isModeEnabled(tool)) {
            return;
        }
        Block origin = event.getBlock();
        if (origin.getType() == Material.BEDROCK || origin.getType().getHardness() < 0.0f) {
            event.setCancelled(true);
            return;
        }
        BlockFace face = this.lastFace.getOrDefault(player.getUniqueId(), BlockFace.SELF);
        List<Block> area = this.computeArea(origin, face);
        World world = origin.getWorld();
        Set<Material> blocked = this.getBlockedMaterials();
        this.clearPreview(player);
        this.active3x3Breaks.add(player.getUniqueId());
        try {
            for (Block block : area) {
                if (block.equals((Object)origin) || block.getType().isAir() || block.getType() == Material.BEDROCK || block.getType().getHardness() < 0.0f || blocked.contains(block.getType())) continue;
                SoundGroup soundGroup = block.getBlockSoundGroup();
                BlockData blockData = block.getBlockData();
                Location center = block.getLocation().add(0.5, 0.5, 0.5);
                boolean broken = player.breakBlock(block);
                if (!broken) continue;
                world.playSound(center, soundGroup.getBreakSound(), soundGroup.getVolume(), 1.1f);
                world.spawnParticle(Particle.BLOCK, center, 40, 0.3, 0.3, 0.3, 0.0, (Object)blockData);
            }
        }
        finally {
            this.active3x3Breaks.remove(player.getUniqueId());
        }
    }

    private Set<Material> getBlockedMaterials() {
        HashSet<Material> materials = new HashSet<Material>();
        materials.add(Material.BEDROCK);
        materials.add(Material.BARRIER);
        for (String entry : this.plugin.getItemsConfig().getStringList("copper-pickaxe.not-breakable-3x3")) {
            Material material = Material.matchMaterial((String)entry.toUpperCase(Locale.ROOT));
            if (material == null) continue;
            materials.add(material);
        }
        return materials;
    }

    private List<Block> computeArea(Block origin, BlockFace face) {
        int[] perp2;
        int[] perp1;
        ArrayList<Block> blocks = new ArrayList<Block>();
        if (face == null || face == BlockFace.SELF) {
            return blocks;
        }
        BlockFace digDirection = face.getOppositeFace();
        if (face.getModY() != 0) {
            perp1 = new int[]{1, 0, 0};
            perp2 = new int[]{0, 0, 1};
        } else if (face.getModX() != 0) {
            perp1 = new int[]{0, 1, 0};
            perp2 = new int[]{0, 0, 1};
        } else {
            perp1 = new int[]{1, 0, 0};
            perp2 = new int[]{0, 1, 0};
        }
        int dx = digDirection.getModX();
        int dy = digDirection.getModY();
        int dz = digDirection.getModZ();
        for (int depth = 0; depth <= 1; ++depth) {
            for (int a = -1; a <= 1; ++a) {
                for (int b = -1; b <= 1; ++b) {
                    if (depth == 0 && a == 0 && b == 0) continue;
                    int offX = perp1[0] * a + perp2[0] * b + dx * depth;
                    int offY = perp1[1] * a + perp2[1] * b + dy * depth;
                    int offZ = perp1[2] * a + perp2[2] * b + dz * depth;
                    blocks.add(origin.getRelative(offX, offY, offZ));
                }
            }
        }
        return blocks;
    }

    private void clearPreview(Player player) {
        this.cancelPreviewTask(player);
        List<Block> blocks = this.previewBlocks.remove(player.getUniqueId());
        if (blocks == null) {
            return;
        }
        for (Block block : blocks) {
            player.sendBlockDamage(block.getLocation(), 0.0f, this.blockSourceId(block));
        }
    }

    private void startPreviewTask(final Player player, final Set<Material> blocked) {
        this.cancelPreviewTask(player);
        final UUID uuid = player.getUniqueId();
        BukkitRunnable task = new BukkitRunnable(){
            private final float[] stages = new float[]{0.15f, 0.3f, 0.5f, 0.7f, 0.9f};
            private int step;

            public void run() {
                List<Block> blocks = CopperPickaxeListener.this.previewBlocks.get(uuid);
                if (blocks == null || blocks.isEmpty() || !player.isOnline()) {
                    CopperPickaxeListener.this.cancelPreviewTask(player);
                    this.cancel();
                    return;
                }
                ItemStack tool = player.getInventory().getItemInMainHand();
                if (!CopperPickaxeItem.isCopperPickaxe(tool) || !CopperPickaxeItem.isModeEnabled(tool)) {
                    CopperPickaxeListener.this.clearPreview(player);
                    this.cancel();
                    return;
                }
                float progress = this.stages[Math.min(this.step, this.stages.length - 1)];
                for (Block block : blocks) {
                    if (block.getType().isAir() || blocked.contains(block.getType())) continue;
                    player.sendBlockDamage(block.getLocation(), progress, CopperPickaxeListener.this.blockSourceId(block));
                }
                ++this.step;
            }
        };
        this.previewTasks.put(uuid, task);
        task.runTaskTimer(this.plugin, 0L, 2L);
    }

    private void cancelPreviewTask(Player player) {
        BukkitRunnable task = this.previewTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    public void cleanup() {
        for (Player player : Bukkit.getOnlinePlayers()) clearPreview(player);
        previewTasks.values().forEach(BukkitRunnable::cancel);
        previewTasks.clear(); previewBlocks.clear(); lastFace.clear(); active3x3Breaks.clear();
    }

    private int blockSourceId(Block block) {
        return Objects.hash(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }
}

