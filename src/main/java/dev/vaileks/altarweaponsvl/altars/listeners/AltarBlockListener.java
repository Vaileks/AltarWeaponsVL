package dev.vaileks.altarweaponsvl.altars.listeners;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class AltarBlockListener
implements Listener {
    private final AltarWeaponsVL plugin;

    public AltarBlockListener(AltarWeaponsVL plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location loc = event.getBlockPlaced().getLocation();
        for (int y = 0; y <= 3; ++y) {
            Location checkLoc = loc.clone().subtract(0.0, (double)y, 0.0);
            if (this.plugin.getAltarManager().getAltarAt(checkLoc) == null) continue;
            event.setCancelled(true);
            return;
        }
    }
}

