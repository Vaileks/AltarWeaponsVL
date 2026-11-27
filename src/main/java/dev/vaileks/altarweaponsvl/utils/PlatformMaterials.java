package dev.vaileks.altarweaponsvl.utils;

import org.bukkit.Material;

/** Names that changed between supported Minecraft releases. */
public final class PlatformMaterials {
    private PlatformMaterials() {}

    public static final Material CHAIN = resolve("IRON_CHAIN", "CHAIN");
    public static final Material COPPER_PICKAXE = resolve("COPPER_PICKAXE", "IRON_PICKAXE");

    private static Material resolve(String current, String legacy) {
        Material material = Material.matchMaterial(current);
        return material != null ? material : Material.valueOf(legacy);
    }
}
