package com.rogermiranda1000.portalgun;

import java.io.File;
import java.util.*;

import com.rogermiranda1000.portalgun.eventos.*;
import com.rogermiranda1000.portalgun.files.FileManager;
import com.rogermiranda1000.portalgun.files.Language;
import com.rogermiranda1000.portalgun.portals.Portal;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class PortalGun extends JavaPlugin
{
    public static PortalGun instancia;
    public static FileConfiguration config;
    public List<Entity> entidad_portal = new ArrayList<Entity>();

    int tasks;
    public ItemStack item;
    public ItemStack botas;
    boolean all_particles;
    boolean tp_log;
    int delay = 2;
    public static final String clearPrefix=ChatColor.GOLD+""+ChatColor.BOLD+"[PortalGun] ", errorPrefix=clearPrefix+ChatColor.RED;

    Particle Pportal1;
    Particle Pportal2;
    public boolean ROL;
    public boolean public_portals;
    public int max_length;
    public List<String> b;

    public void onEnable() {
        getLogger().info("Plugin activated.");

        instancia = this;

        FileManager.loadFiles();

        // TODO: config
      HashMap<String,String> c = new HashMap<String, String>();
      c.put("max_portal_length", "80");
      c.put("all_portal_particles", "false");
      c.put("teleport_log", "true");
      c.put("portalgun_material", "BLAZE_ROD");
      c.put("portal1_particle", "FLAME");
      c.put("portal2_particle", "VILLAGER_HAPPY");
      c.put("remove_on_leave", "true");
      c.put("keep_portals_on_stop", "false");
      c.put("delete_portals_on_death", "false");
      c.put("only_certain_blocks", "false");
      c.put("use_only_your_portals", "false");
      c.put("remove_portals_on_world_change", "false");
      c.put("blocks", "wool:0,quartz_block:0,quartz_block:1,quartz_block:2,concrete:0");
    config = getConfig();

    //Create/actualize config file
    try {
      if (!getDataFolder().exists()) getDataFolder().mkdir();
      File file = new File(getDataFolder(), "config.yml");
      boolean need = false;

      if (!file.exists()) {
          getLogger().info("Creating config.yml...");
          file.createNewFile();
          need = true;
      }

        for(Map.Entry<String, String> entry : c.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(!getConfig().isSet(key)) {
                if(value=="true") getConfig().set(key,Boolean.valueOf(true));
                else if(value=="false") getConfig().set(key,Boolean.valueOf(false));
                else if(value=="80") getConfig().set(key,Integer.valueOf(80));
                else getConfig().set(key,value);
                need = true;
            }
        }

        if(need) saveConfig();

    } catch (Exception e) {
      e.printStackTrace();
    }

    // TODO: Cargar portales
    /*if (config.getBoolean("keep_portals_on_stop")) {
      getLogger().info("Loading portals...");
      File file = new File(getDataFolder(), "portal.yml");
      if(file.exists()) {
          try {
              BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
              String l;
              while ((l=br.readLine())!=null) {
                  String[] args = l.split(">");
                  if(args.length!=3) continue;
                  portales.put(args[0], new LPortal(args[1].split(","),args[2].split(",")));
              }
              br.close();
          } catch (Exception e) { e.printStackTrace(); }

        cancelPortals(true);
      }
    }*/

      this.item = new ItemStack(Material.getMaterial(config.getString("portalgun_material")));
      ItemMeta meta = this.item.getItemMeta();
      meta.setDisplayName(ChatColor.GOLD+""+ChatColor.BOLD+"PortalGun");
      this.item.setItemMeta(meta);
      this.item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);

      botas = new ItemStack(Material.LEATHER_BOOTS);
      LeatherArmorMeta meta2 = (LeatherArmorMeta) botas.getItemMeta();
      meta2.setDisplayName(ChatColor.GREEN+"Portal Boots");
      meta2.setUnbreakable(true);
      meta2.setColor(Color.WHITE);
      botas.setItemMeta(meta2);
      botas.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
    
    max_length = config.getInt("max_portal_length");
    this.tp_log = config.getBoolean("teleport_log");
        Portal.allParticlesAtOnce = config.getBoolean("all_portal_particles");
        Portal.setParticle(Particle.valueOf(config.getString("portal1_particle")), true);
        Portal.setParticle(Particle.valueOf(config.getString("portal2_particle")), true);
    ROL = config.getBoolean("remove_on_leave");
        // TODO: public portals
      //public_portals = !config.getBoolean("use_only_your_portals");
    b = Arrays.asList(PortalGun.config.getString("blocks").replace(" ", "").toLowerCase().split(","));
    for(int x = 0; x<b.size(); x++) {
        if(!b.get(x).contains(":")) b.set(x, b.get(x)+":0");
    }


      getServer().getPluginManager().registerEvents(new onDead(), this);
      getServer().getPluginManager().registerEvents(new onLeave(), this);
      getServer().getPluginManager().registerEvents(new onMove(), this);
      getServer().getPluginManager().registerEvents(new onTab(), this);
      getServer().getPluginManager().registerEvents(new onUse(), this);
  }

  public static Location getGroundBlock(String look, Location loc) {
      //getLogger().info(look);
      Location last = loc.clone();
      if (look.equalsIgnoreCase("N")) {
          last.setX(last.getX() - 1.0D);
          return last;
      } else if (look.equalsIgnoreCase("S")) {
          last.setX(last.getX() + 1.0D);
          return last;
      } else if (look.equalsIgnoreCase("E")) {
          last.setZ(last.getZ() - 1.0D);
          return last;
      } else if (look.equalsIgnoreCase("W")) {
          last.setZ(last.getZ() + 1.0D);
          return last;
      }
      return null;
  }

  public void onDisable() {
	  getLogger().info("Plugin disabled.");
      // TODO: Guardar portales
	  /*if (config.getBoolean("keep_portals_on_stop")) {
		  getLogger().info("Saving portals...");
          try {
		        File file = new File(getDataFolder(), "portal.yml");
		        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                for (String n : portales.keySet()) {
                    bw.write(n + ">" + portales.get(n).Save());
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            } catch (IOException e) { e.printStackTrace(); }
	  }*/
  }
  
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	  Player player = (sender instanceof Player) ? (Player)sender : null;
	  if (!cmd.getName().equalsIgnoreCase("portalgun")) return false;

	  if (player == null) {
	      sender.sendMessage("Don't use this command in console.");
	      return true;
	  }

	  if (args.length == 0) {
	      // Get the PortalGun (and/or PortalBoots)
          if (player.hasPermission("portalgun.portalgun")) {
              player.getInventory().addItem(new ItemStack[] { this.item });
              if(player.hasPermission("portalgun.boots")) player.getInventory().addItem(new ItemStack[] { botas });
              player.sendMessage(clearPrefix + ChatColor.GREEN + Language.USER_GET.getText());
          } else {
              player.sendMessage(errorPrefix + Language.USER_NO_PERMISSIONS.getText());
          }

          return true;
      }

	  if (args[0].equalsIgnoreCase("remove")) {
          // Remove portals
          if (!player.hasPermission("portalgun.remove")) {
              player.sendMessage(errorPrefix + Language.USER_NO_PERMISSIONS.getText());

              return true;
          }

          if (args.length == 1) {
              if (Portal.removePortal(player)) player.sendMessage(clearPrefix + ChatColor.GREEN + Language.USER_REMOVE.getText());
              else player.sendMessage(errorPrefix + Language.USER_NO_PORTALS.getText());

              return true;
          }

          if (args.length == 2 && args[1].equalsIgnoreCase("all")) {
              if (!player.hasPermission("portalgun.remove.all")) {
                  player.sendMessage(errorPrefix + Language.USER_NO_PERMISSIONS.getText());

                  return true;
              }

              sender.sendMessage(ChatColor.RED + Language.HELP_REMOVE_ALL.getText());

              return true;
          }
      }

      // Help command
      player.sendMessage(clearPrefix);
      player.sendMessage(ChatColor.GOLD + "  /portalgun " + ChatColor.GREEN + "- " + Language.HELP_GET.getText());
      player.sendMessage(ChatColor.GOLD + "  /portalgun remove " + ChatColor.GREEN + "- " + Language.HELP_REMOVE.getText());
      player.sendMessage(ChatColor.GOLD + "  /portalgun remove all " + ChatColor.GREEN + "- " + Language.HELP_REMOVE_ALL.getText());

      return true;
  }

  // TODO: cancel portals (clock?)
  /*public void cancelPortals(boolean open) {
	  for (String ply : portales.keySet()) {
          LPortal p = portales.get(ply);
          if(p.reloj!=null){
              p.reloj.eliminar();
              p.reloj=null;
          }
		  
		  if (open) {
              Location loc1 = new Location(Bukkit.getServer().getWorld(p.world[0]), p.loc[0][0], p.loc[0][1], p.loc[0][2]);
              Location loc2 = new Location(Bukkit.getServer().getWorld(p.world[1]), p.loc[1][0], p.loc[1][1], p.loc[1][2]);
			  p.reloj=new PortalReloj(loc1, loc2, String.valueOf(p.dir[0]), String.valueOf(p.dir[1]), p.down[0], p.down[1], ply);
		  }
	  }
  }*/

  public static void playParticle(Location loc, int proceso, String look, boolean down, Integer color, String player) {
	  double x = loc.getBlockX();
	  double y = loc.getBlockY();
	  double z = loc.getBlockZ();
	  double grado=2*Math.PI*proceso/21;
      double suma = 0.45D*(1.1D+Math.cos(grado));
      double suma2 = 0.9D*(1.1D+Math.sin(grado));

	  if(!down) {
	      y+=suma2;

          if (look.equalsIgnoreCase("E") || look.equalsIgnoreCase("W")) {
              z += 0.1D;
              if (look.equalsIgnoreCase("W"))z += 0.8D;
              x += suma;
          } else {
              x += 0.15D;
              if (look.equalsIgnoreCase("S")) x += 0.75D;
              z += suma;
          }
      }
	  else {
	      y+=0.1D;
          if (look.equalsIgnoreCase("E") || look.equalsIgnoreCase("W")) {
              x+=suma;
              z+=suma2;
              if(look.equalsIgnoreCase("E")) z-=1;
          }
          else {
              x+=suma2;
              if(look.equalsIgnoreCase("N")) x-=1;
              z+=suma;
          }
      }
	  Particle particula = instancia.Pportal2;
	  if (color.intValue() == 0) particula = instancia.Pportal1;
      //getLogger().info(String.valueOf(loc.getWorld()));
	  if(instancia.public_portals) loc.getWorld().spawnParticle(particula, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
	  else {
	      Player ply = Bukkit.getPlayer(player);
	      for(Player a: Bukkit.getOnlinePlayers()) {
	        if(a.hasPermission("portalgun.overrideotherportals") || (ply!=null&&a==ply)) a.spawnParticle(particula, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
          }
      }
	}
  
  public void teletransporte(Location loc, Entity player, String look, String lastL, boolean down, boolean lastD) {
	  if (!this.entidad_portal.contains(player)) { this.entidad_portal.add(player); }
	  else { return; }
	  
	  if (loc.getWorld().getPlayers().size() == 0 && !(player instanceof Player)) return;
	  if (!player.getPassengers().isEmpty() && player.getPassengers().get(0) instanceof Player) player = (Entity)player.getPassengers().get(0);
	  if (this.tp_log) getLogger().info("Teleporting " + player.getName() + "...");

      Vector vel = player.getVelocity().clone();
      double predominante = 0D;
      if(lastD) predominante = vel.getY();
      else {
          if(lastL.equalsIgnoreCase("N") || lastL.equalsIgnoreCase("S")) predominante = vel.getX();
          else predominante = vel.getZ();
      }
      if(predominante<0) predominante *= -1;

      if(!down) {
          int yaw = 0;
          vel = new Vector(0D, 0D, predominante);
          if (look.equalsIgnoreCase("N")) {
              yaw = -90;
              vel = new Vector(predominante, 0D, 0D);
          }
          else if (look.equalsIgnoreCase("S")) {
              yaw = 90;
              vel = new Vector(-predominante, 0D, 0D);
          }
          else if (look.equalsIgnoreCase("W")) {
              yaw = 180;
              vel = new Vector(0D, 0D, -predominante);
          }
          loc.setYaw(yaw);
      }
      else {
          loc.setYaw(player.getLocation().getYaw());
          vel = new Vector(0D, predominante, 0D);
      }
	  loc.setPitch(player.getLocation().getPitch());
	  Entity ride = null;
	  for (Entity ent: loc.getChunk().getEntities()) {
		  if (!ent.getPassengers().isEmpty() && ent.getPassengers() == player) { ride = ent; break; }
	  }
	  if (ride != null) {
		  ride.removePassenger(player);
		  ride.teleport(new Location(loc.getWorld(), loc.getX() + 0.5D, loc.getY(), loc.getZ() + 0.5D, loc.getYaw(), loc.getPitch()));
		  if (!this.entidad_portal.contains(ride)) this.entidad_portal.add(ride);
	  } else {
		  player.teleport(new Location(loc.getWorld(), loc.getX() + 0.5D, loc.getY(), loc.getZ() + 0.5D, loc.getYaw(), loc.getPitch()));
	  }
	  if (player instanceof Player) {
		  ((Player)player).getPlayer().playSound(player.getLocation(), Sound.ENTITY_SHULKER_TELEPORT, 3.0F, 0.5F);
	  }

	  player.setVelocity(vel);
  }

}