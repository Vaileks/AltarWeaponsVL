package dev.vaileks.altarweaponsvl.altars.listeners;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/** Displays are transient; the saved altar is the source of truth across chunk loads. */
public final class AltarChunkListener implements Listener {
    private final AltarWeaponsVL plugin;

    public AltarChunkListener(AltarWeaponsVL plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void unload(ChunkUnloadEvent event) { update(event.getChunk(), false); }

    @EventHandler(priority = EventPriority.MONITOR)
    public void load(ChunkLoadEvent event) { update(event.getChunk(), true); }

    private void update(Chunk chunk, boolean spawn) {
        for (int id : plugin.getAltarManager().getAltarsIds()) {
            var altar = plugin.getAltarManager().getAltar(id);
            var at = altar.getLocation();
            if (at.getWorld() != chunk.getWorld() || (at.getBlockX() >> 4) != chunk.getX() || (at.getBlockZ() >> 4) != chunk.getZ()) continue;
            if (spawn) altar.spawnEntities(); else altar.removeAllEntities();
        }
    }
}
