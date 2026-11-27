package test;

import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.altars.Altar;
import dev.vaileks.altarweaponsvl.altars.listeners.AltarListener;
import dev.vaileks.altarweaponsvl.commands.AltarLegendaryCommand;
import dev.vaileks.altarweaponsvl.integration.CommonItems;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class AltarCraftingTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    public static void run(AltarWeaponsVL plugin) throws Exception {
        var listener = new AltarListener(plugin);
        for (boolean english : new boolean[]{true, false}) {
            plugin.getConfig().set("lang", english ? "en_US" : "ru_RU");
            plugin.reloadCrafting();
            for (var entry : CommonItems.ITEMS) {
                var item = entry.factory().get();
                check(!item.getType().isAir() && item.hasItemMeta() && item.getItemMeta().hasLore(), "ordinary item: " + entry.id());
                check(entry.id().equals(CommonItems.id(item)), "stable common identity");
            }
        }
        plugin.getConfig().set("lang", "en_US");
        plugin.reloadCrafting();
        check(new AltarLegendaryCommand.Menu(plugin, true).getInventory().getItem(7) != null, "8 common menu entries");
        for (String name : List.of("weapons_handle", "illusion_core", "vulcan_skull"))
            check(Bukkit.getRecipe(new NamespacedKey("runesaltarv", name)) != null, "workbench recipe: " + name);
        ItemStack[] contents = new ItemStack[]{new ItemStack(Material.DIAMOND, 3)};
        check(listener.planIngredients(contents, List.of(new ItemStack(Material.DIAMOND, 2),new ItemStack(Material.DIAMOND, 2))) == null, "overlapping ingredients cannot craft with insufficient total");
        check(contents[0].getAmount() == 3, "rejected plan leaves inventory unchanged");
        contents[0].setAmount(5);
        var plan = listener.planIngredients(contents, List.of(new ItemStack(Material.DIAMOND, 2),new ItemStack(Material.DIAMOND, 2)));
        check(plan != null && plan[0].getAmount() == 1 && contents[0].getAmount() == 5, "complete atomic deduction on cloned inventory");
        var skull = CommonItems.ITEMS.get(1);
        check(listener.planIngredients(new ItemStack[]{skull.factory().get()}, List.of(skull.factory().get())) != null, "independently crafted Vulcan skulls match despite random UUIDs");
        plugin.getConfig().set("lang", "ru_RU");
        var russian = skull.factory().get();
        plugin.getConfig().set("lang", "en_US");
        check(listener.planIngredients(new ItemStack[]{russian}, List.of(skull.factory().get())) != null, "language change preserves material recognition");
        check(listener.planIngredients(new ItemStack[]{new ItemStack(Material.FEATHER)},List.of(CommonItems.ITEMS.getFirst().factory().get())) == null,"vanilla feather cannot replace a Warden Heart");

        var world = Bukkit.getWorlds().getFirst();
        var at = world.getSpawnLocation().clone().add(8, 5, 8).getBlock().getLocation();
        at.getChunk().load();
        at.getBlock().setType(Material.BARRIER);
        var manager = plugin.getAltarManager();
        for (int oldId : manager.getAltarsIds()) if (manager.getAltar(oldId).getLocation().equals(at)) manager.removeAltar(oldId);
        Altar altar = manager.createAltar(at);
        int id = altar.getId();
        altar.setResultItem(new ItemStack(Material.EMERALD));
        altar.setRecipe(List.of(new ItemStack(Material.DIAMOND,2), new ItemStack(Material.DIAMOND,2)));
        altar.setMaxUses(2);
        altar.setCooldownSeconds(60);
        altar.spawnEntities();
        check(world.getNearbyEntities(at, 2, 5, 2).stream().filter(ItemDisplay.class::isInstance).count() == 1, "one actual ItemDisplay over pedestal");
        altar.spawnEntities();
        check(world.getNearbyEntities(at, 2, 5, 2).stream().filter(ItemDisplay.class::isInstance).count() == 1, "refresh does not duplicate floating item");
        ItemStack[] storage = new ItemStack[36];
        storage[0] = new ItemStack(Material.DIAMOND,3);
        PlayerInventory inventory = (PlayerInventory) Proxy.newProxyInstance(PlayerInventory.class.getClassLoader(), new Class[]{PlayerInventory.class}, (proxy,method,args) -> {
            return switch(method.getName()) {
                case "getStorageContents" -> Arrays.stream(storage).map(i->i==null?null:i.clone()).toArray(ItemStack[]::new);
                case "setStorageContents" -> { System.arraycopy((ItemStack[])args[0],0,storage,0,36); yield null; }
                case "addItem" -> {
                    for (var item : (ItemStack[])args[0]) for (int i=0;i<storage.length;i++) if(storage[i]==null) { storage[i]=item.clone(); break; }
                    yield new java.util.HashMap<Integer,ItemStack>();
                }
                default -> defaultValue(method.getReturnType());
            };
        });
        Player player = (Player)Proxy.newProxyInstance(Player.class.getClassLoader(),new Class[]{Player.class},(proxy,method,args)->switch(method.getName()){
            case "getInventory" -> inventory;
            case "getName" -> "AltarTest";
            case "getLocation" -> at.clone().add(0.5,1,0.5);
            case "getWorld" -> world;
            default -> defaultValue(method.getReturnType());
        });
        listener.onInteract(interact(player, at.getBlock()));
        check(storage[0].getAmount()==3 && altar.getCurrentUses()==2,"insufficient craft consumes nothing and preserves uses");
        storage[0].setAmount(5);
        listener.onInteract(interact(player, at.getBlock()));
        check(storage[0].getAmount()==1 && Arrays.stream(storage).anyMatch(i->i!=null&&i.getType()==Material.EMERALD),"craft deducts all four ingredients and grants result");
        check(altar.getCurrentUses()==1 && altar.getCooldownRemaining()>0,"successful craft consumes one use and begins cooldown");
        int before=Arrays.stream(storage).filter(i->i!=null&&i.getType()==Material.EMERALD).mapToInt(ItemStack::getAmount).sum();
        listener.onInteract(interact(player, at.getBlock()));
        check(altar.getCurrentUses()==1 && before==Arrays.stream(storage).filter(i->i!=null&&i.getType()==Material.EMERALD).mapToInt(ItemStack::getAmount).sum(),"cooldown prevents second reward");
        manager.saveAltars(); manager.loadAltars(); altar=manager.getAltar(id);
        check(altar!=null&&altar.getCurrentUses()==1&&altar.getCooldownRemaining()>0&&altar.getRecipe().size()==2,"uses, recipe and remaining cooldown survive serialized reload");
        altar.setCooldownSeconds(0); altar.setLastUsedTime(0);
        storage[0]=new ItemStack(Material.DIAMOND,8);
        listener.onInteract(interact(player, at.getBlock()));
        check(altar.getCurrentUses()==0&&altar.isCrafted(),"last use marks altar exhausted");
        listener.onInteract(interact(player, at.getBlock()));
        check(storage[0].getAmount()==4,"exhausted altar consumes no ingredients");
        altar.setMaxUses(-1);
        listener.onInteract(interact(player, at.getBlock()));
        check(altar.getMaxUses()==-1&&Arrays.stream(storage).noneMatch(i->i!=null&&i.getType()==Material.DIAMOND),"unlimited setting re-enables an exhausted altar");
        manager.removeAltar(id); at.getBlock().setType(Material.AIR);
        check(world.getNearbyEntities(at,2,5,2).stream().noneMatch(ItemDisplay.class::isInstance),"delete removes floating result");
        plugin.getLogger().info("ALTAR_CRAFTING_PASS checks="+checks);
    }

    private static PlayerInteractEvent interact(Player player, org.bukkit.block.Block block) {
        return new PlayerInteractEvent(player,Action.RIGHT_CLICK_BLOCK,null,block,BlockFace.UP,EquipmentSlot.HAND);
    }
    private static Object defaultValue(Class<?> type) {
        if(type==boolean.class)return false;
        if(type==int.class)return 0;
        if(type==long.class)return 0L;
        if(type==double.class)return 0d;
        if(type==float.class)return 0f;
        if(type==byte.class)return (byte)0;
        return null;
    }
}
