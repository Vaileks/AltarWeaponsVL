package test;
import java.util.*;import java.nio.file.*;import org.bukkit.*;import org.bukkit.plugin.java.JavaPlugin;import org.bukkit.attribute.Attribute;import org.bukkit.inventory.*;import net.kyori.adventure.text.format.*;
import dev.vaileks.altarweaponsvl.integration.*;import dev.vaileks.altarweaponsvl.AltarWeaponsVL;
import dev.vaileks.altarweaponsvl.commands.AltarLegendaryCommand;
public class AltarSmoke extends JavaPlugin {
 public void onEnable(){Bukkit.getScheduler().runTaskLater(this,()->{try{check();getLogger().info("ALTAR_SMOKE_PASS");}catch(Throwable e){getLogger().severe("ALTAR_SMOKE_FAIL: "+e);e.printStackTrace();}finally{Bukkit.shutdown();}},40);}
 void assertTrue(boolean value,String message){if(!value)throw new AssertionError(message);}
 void check()throws Exception{
  var plugin=(AltarWeaponsVL)Bukkit.getPluginManager().getPlugin("AltarWeaponsVL");assertTrue(plugin!=null&&plugin.isEnabled(),"standalone enabled");
  assertTrue(Bukkit.getPluginManager().getPlugins().length==2,"only the standalone and its test helper are installed");
  assertTrue(plugin.getDescription().getDepend().isEmpty(),"no required dependencies");
  assertTrue(plugin.getDescription().getAuthors().equals(List.of("Vaileks")),"correct author");
  var pickaxe = dev.vaileks.altarweaponsvl.items.copperpickaxe.CopperPickaxeItem.create();
  var oldPickaxe = pickaxe.clone(); oldPickaxe.setType(Material.IRON_PICKAXE);
  assertTrue(dev.vaileks.altarweaponsvl.items.copperpickaxe.CopperPickaxeItem.isCopperPickaxe(oldPickaxe), "older-server pickaxe remains usable after upgrade");
  dev.vaileks.altarweaponsvl.items.copperpickaxe.CopperPickaxeItem.toggleMode(oldPickaxe);
  assertTrue(dev.vaileks.altarweaponsvl.items.copperpickaxe.CopperPickaxeItem.isModeEnabled(oldPickaxe), "older-server pickaxe retains mining toggle");
  assertTrue(!dev.vaileks.altarweaponsvl.items.copperpickaxe.CopperPickaxeItem.isCopperPickaxe(new ItemStack(Material.IRON_PICKAXE)), "ordinary iron pickaxe has no custom ability");
  Set<String> ids=new HashSet<>();
  for(boolean english:new boolean[]{true,false}){
   plugin.getConfig().set("lang",english?"en_US":"ru_RU");
   for(var entry:AltarCatalog.WEAPONS){
    ids.add(entry.id());ItemStack item=entry.factory().get();assertTrue(!item.getType().isAir(),"nonempty item");assertTrue(AltarCatalog.find(item)==entry,"own item identity: "+entry.id());
    var meta=item.getItemMeta();assertTrue(meta.isUnbreakable(),"no durability loss");assertTrue(meta.hasLore()&&meta.lore().size()>5,"styled lore: "+entry.id());
    var lore=meta.lore();String text=dev.vaileks.altarweaponsvl.utils.TextUtils.toPlain(lore.get(lore.size()-2));
    assertTrue(text.contains(english?"Attack Damage":"Урон при атаке"),"localized damage line");
    assertTrue(lore.get(lore.size()-2).color().equals(NamedTextColor.DARK_GREEN)&&lore.getLast().color().equals(NamedTextColor.DARK_GREEN),"green damage/speed lines");
    var old=item.clone();var oldMeta=old.getItemMeta();oldMeta.getPersistentDataContainer().remove(new NamespacedKey("runesaltarv","altar_weapon"));old.setItemMeta(oldMeta);assertTrue(AltarCatalog.find(old)==null,"model alone cannot claim another plugin's item");
   }
  }
  plugin.getConfig().set("lang","en_US");assertTrue(ids.size()==16,"16 weapons");
  var commands=new AltarLegendaryCommand(plugin);
  var listed=commands.onTabComplete(Bukkit.getConsoleSender(),plugin.getCommand("rav"),"rav",new String[]{"give","Player",""});
  assertTrue(listed.size()==24 && listed.containsAll(ids),"all give-command IDs available");
  assertTrue(commands.onCommand(Bukkit.getConsoleSender(),plugin.getCommand("rav"),"rav",new String[]{"give","DefinitelyNotOnline","bone_blade"}),"missing player handled");
  assertTrue(commands.onCommand(Bukkit.getConsoleSender(),plugin.getCommand("rav"),"rav",new String[]{"reload"}),"reload command succeeds");
  var menu=new AltarLegendaryCommand.Menu(plugin).getInventory();assertTrue(menu.getSize()==27,"menu capacity");int shown=0;for(var item:menu.getContents())if(item!=null){assertTrue(AltarCatalog.find(item)!=null,"weapons only in menu");shown++;}assertTrue(shown==16,"all weapons in menu");
  for(String name:List.of("weapons.yml","models.yml","items.yml"))assertTrue(!Files.exists(plugin.getDataFolder().toPath().resolve(name)),"no duplicate or unused configuration: "+name);
  assertTrue(plugin.getCommand("rav")!=null,"own command available");
  var world=Bukkit.getWorlds().getFirst();var at=world.getSpawnLocation().clone().add(0,4,0);at.getChunk().load();
  var zombie=world.spawn(at,org.bukkit.entity.Zombie.class,z->{z.setAI(false);z.setSilent(true);});
  zombie.getAttribute(Attribute.MAX_ABSORPTION).setBaseValue(4);zombie.setAbsorptionAmount(4);double before=zombie.getHealth();getLogger().info("PURE_BEFORE health="+before+" absorption="+zombie.getAbsorptionAmount());plugin.getCleanDamageManager().apply(zombie,6);getLogger().info("PURE_AFTER health="+zombie.getHealth()+" absorption="+zombie.getAbsorptionAmount());
  assertTrue(Math.abs(zombie.getHealth()-(before-2))<.001&&zombie.getAbsorptionAmount()==0,"pure damage includes absorption");
  var cancelled=world.spawn(at.clone().add(2,0,0),org.bukkit.entity.Zombie.class,z->{z.setAI(false);z.setSilent(true);});
  Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener(){@org.bukkit.event.EventHandler(priority=org.bukkit.event.EventPriority.LOWEST)public void deny(org.bukkit.event.entity.EntityDamageEvent e){if(e.getEntity()==cancelled)e.setCancelled(true);}},this);
  before=cancelled.getHealth();plugin.getCleanDamageManager().apply(cancelled,10);assertTrue(cancelled.getHealth()==before,"cancelled damage remains cancelled");
  var saved=world.spawn(at.clone().add(4,0,0),org.bukkit.entity.Zombie.class,z->{z.setAI(false);z.setSilent(true);z.getEquipment().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));});
  plugin.getCleanDamageManager().apply(saved,99);assertTrue(!saved.isDead()&&saved.getHealth()>0&&saved.getEquipment().getItemInOffHand().getType().isAir(),"native totem survives pure damage");
  plugin.getCleanDamageManager().apply(saved,99);assertTrue(!saved.isDead(),"follow-up hit cannot bypass popped totem");
  zombie.remove();cancelled.remove();saved.remove();

  dev.vaileks.altarweaponsvl.utils.AltarActionBarTest.run();
  AltarCraftingTest.run(plugin);
  AltarCooldownAudit.run(plugin);AltarBalanceTest.run(plugin);AltarConfigurationTest.run(plugin);
  getLogger().info("ALTAR_COUNTS weapons="+ids.size()+" languages=2");
 }
}
