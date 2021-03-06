package net.omniblock.skywars.patch.managers.chest.defaults.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

import net.omniblock.network.library.helpers.effectlib.effect.ExplodeEffect;
import net.omniblock.skywars.Skywars;
import net.omniblock.skywars.games.solo.events.SoloPlayerBattleListener;
import net.omniblock.skywars.games.solo.events.SoloPlayerBattleListener.DamageCauseZ;
import net.omniblock.skywars.games.solo.managers.SoloPlayerManager;
import net.omniblock.skywars.games.teams.events.TeamPlayerBattleListener;
import net.omniblock.skywars.games.teams.managers.TeamPlayerManager;
import net.omniblock.skywars.patch.managers.CustomProtocolManager;
import net.omniblock.skywars.patch.types.SkywarsType;
import net.omniblock.skywars.util.CameraUtil;
import net.omniblock.skywars.util.NumberUtil;

public class Meteoro implements Listener {

	public static Map<Entity, Player> METEORO_OWNER = new HashMap<Entity, Player>();

	@SuppressWarnings("deprecation")
	@EventHandler
	public void launchMeteoro(PlayerInteractEvent event) {

		Player player = event.getPlayer();

		if (SoloPlayerManager.getPlayersInGameList().contains(player)
				|| TeamPlayerManager.getPlayersInGameList().contains(player)
						&& player.getGameMode() == GameMode.SURVIVAL) {

			if (event.getPlayer().getItemInHand().hasItemMeta()) {
				if (event.getPlayer().getItemInHand().getItemMeta().hasDisplayName()) {
					if (event.getPlayer().getItemInHand().getType() == Material.getMaterial(2256)) {
						if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK
								|| event.getAction() == Action.LEFT_CLICK_AIR
								|| event.getAction() == Action.LEFT_CLICK_BLOCK) {

							if (event.getClickedBlock() != null) {
								if (event.getClickedBlock().getType() == Material.CHEST
										|| event.getClickedBlock().getType() == Material.TRAPPED_CHEST
										|| event.getClickedBlock().getType() == Material.JUKEBOX) {

									return;

								}
							}

							if(!Skywars.ingame)
								return;
							
							player.getInventory().setItemInHand(null);

							Block targetblock = event.getPlayer().getTargetBlock((Set<Material>) null, 200);
							Player targetplayer = null;

							for (Player p : player.getWorld().getEntitiesByClass(Player.class)) {
								if (SoloPlayerManager.getPlayersInGameList().contains(p)) {
									if (CameraUtil.getLookingAt(player, p)) {
										if(player.hasLineOfSight(p))
											targetplayer = p;
									}
								}
							}

							Location location = targetplayer != null ? targetplayer.getLocation()
									: targetblock.getLocation();
							genMeteoro(location, player);

						}
					}
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void explode(EntityChangeBlockEvent e) {
		if (e.getEntity() instanceof FallingBlock) {

			FallingBlock fb = (FallingBlock) e.getEntity();

			if (fb.hasMetadata("METEORO")) {

				Player damager = null;

				e.setCancelled(true);
				fb.getWorld().playEffect(fb.getLocation(), Effect.EXPLOSION, 4);
				fb.getWorld().playSound(fb.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 10);

				if (METEORO_OWNER.containsKey(fb)) {
					damager = METEORO_OWNER.get(fb);
					METEORO_OWNER.remove(fb);
				}

				List<Entity> entities = fb.getNearbyEntities(3, 3, 3);
				for (Entity entity : entities) {

					if (entity.getType() == EntityType.PLAYER) {
						Player p = (Player) entity;

						if ((SoloPlayerManager.getPlayersInGameList().contains(p)
								|| TeamPlayerManager.getPlayersInGameList().contains(p))) {

							if (Skywars.currentMatchType == SkywarsType.SW_NORMAL_TEAMS
									|| Skywars.currentMatchType == SkywarsType.SW_INSANE_TEAMS
									|| Skywars.currentMatchType == SkywarsType.SW_Z_TEAMS) {

								TeamPlayerBattleListener.makeZDamage(p, damager, 4,
										net.omniblock.skywars.games.teams.events.TeamPlayerBattleListener.DamageCauseZ.METEORO);
								continue;

							}

							SoloPlayerBattleListener.makeZDamage(p, damager, 4, DamageCauseZ.METEORO);
							continue;

						}
					}

				}

				List<Block> cube = circle(fb.getLocation(), 2, 1, false, true, -1);

				for (Block b : cube) {
					if (b != null) {
						if (!CustomProtocolManager.PROTECTED_BLOCK_LIST.contains(b)) {
							if (NumberUtil.getRandomInt(1, 10) == 5) {

								bounceBlock(b, (float) (0.5));
								continue;

							} else {

								b.setType(Material.AIR);
								continue;

							}
						}
					}
				}

				fb.remove();
			}

		}
	}

	public static void genNaturallyMeteoro(Location loc) {
		genMeteoro(loc, null);
	}

	@SuppressWarnings("deprecation")
	public static void genMeteoro(Location loc, Player player) {

		Location spawnloc = loc.clone().add(0, 80, 0);
		List<FallingBlock> meteoro = Lists.newArrayList();

		Map<Block, ItemStack> blocks = new HashMap<Block, ItemStack>();
		List<Block> cube = circle(spawnloc, 2, 1, false, true, -1);

		for (Block b : cube) {
			if (!CustomProtocolManager.PROTECTED_BLOCK_LIST.contains(b)) {
				b.getWorld().playEffect(b.getLocation(), Effect.LAVA_POP, 4);
				int r = NumberUtil.getRandomInt(1, 3);
				if (r == 1) {
					b.setType(Material.STAINED_GLASS);
					b.setData((byte) 14);
				} else if (r == 2) {
					b.setType(Material.STAINED_GLASS);
					b.setData((byte) 4);
				} else {
					b.setType(Material.STAINED_GLASS);
					b.setData((byte) 1);
				}
				blocks.put(b, new ItemStack(b.getType(), 1, b.getData()));
			} else {

				for (Map.Entry<Block, ItemStack> r : blocks.entrySet()) {
					r.getKey().setType(r.getValue().getType());
					r.getKey().setData((byte) r.getValue().getDurability());
				}

				genMeteoro(spawnloc.clone().add(0, 10, 0), player);
				return;
			}
		}

		for (Block b : cube) {

			Material m = b.getType();
			byte bx = b.getData();
			b.setType(Material.AIR);

			final FallingBlock meteorocomponent = b.getWorld().spawnFallingBlock(b.getLocation(), m, bx);
			meteorocomponent.setMetadata("METEORO", new FixedMetadataValue(Skywars.getInstance(), "dummy"));
			meteoro.add(meteorocomponent);

			if (player != null) {
				METEORO_OWNER.put(meteorocomponent, player);
			}

		}

		new BukkitRunnable() {

			int power = 2;

			boolean unintegred_sound = false;

			List<Block> destructor = Lists.newArrayList();

			@Override
			public void run() {

				if (power <= 0) {

					cancel();

					boolean soundend = false;

					for (FallingBlock exterminator : meteoro) {

						if (exterminator.isDead()) {
							continue;
						}

						Location loc = exterminator.getLocation();
						exterminator.remove();

						if (soundend == false) {

							soundend = true;

							loc.getWorld().playSound(loc.getBlock().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 15, -5);
							loc.getWorld().playSound(loc.getBlock().getLocation(), Sound.BLOCK_PISTON_CONTRACT, 15, -15);

							ExplodeEffect ef = new ExplodeEffect(Skywars.effectmanager);
							ef.visibleRange = 300;
							ef.setLocation(loc);
							ef.start();

						}

					}

				} else {

					for (FallingBlock exterminator : meteoro) {

						if (exterminator.isDead()) {
							continue;
						}

						if (!unintegred_sound) {

							unintegred_sound = true;

							exterminator.getLocation().getWorld().playEffect(exterminator.getLocation(),
									Effect.LAVA_POP, 4);
							exterminator.getLocation().getWorld().playEffect(exterminator.getLocation(),
									Effect.FIREWORKS_SPARK, 4);

							exterminator.getLocation().getWorld().playSound(exterminator.getLocation(),
									Sound.BLOCK_PISTON_EXTEND, 10, -15);
							exterminator.getLocation().getWorld().playSound(exterminator.getLocation(), Sound.ENTITY_BLAZE_HURT,
									10, -15);
							exterminator.getLocation().getWorld().playSound(exterminator.getLocation(), Sound.BLOCK_LAVA_POP,
									10, -15);
							exterminator.getLocation().getWorld().playSound(exterminator.getLocation(), Sound.ENTITY_GENERIC_EXPLODE,
									10, 10);

						}

						List<Block> explosion = circle(exterminator.getLocation(), power, 1, false, true, -1);
						for (Block b : explosion) {
							if (b.getType() != Material.AIR) {
								if (b.getType() != Material.BEDROCK && b.getType() != Material.WATER
										&& b.getType() != Material.OBSIDIAN) {

									power--;

									destructor.add(b);

									exterminator.getLocation().getWorld().playSound(exterminator.getLocation(),
											Sound.ENTITY_COW_STEP, 15, -15);
									exterminator.getLocation().getWorld().playSound(exterminator.getLocation(),
											Sound.ENTITY_GENERIC_EXPLODE, 15, -25);

									b.setType(Material.AIR);

									continue;

								} else {

									exterminator.remove();

									exterminator.getLocation().getWorld().playSound(exterminator.getLocation(),
											Sound.ENTITY_CREEPER_PRIMED, 15, -5);
									exterminator.getLocation().getWorld().playSound(exterminator.getLocation(),
											Sound.ENTITY_GENERIC_EXPLODE, 15, 15);

									continue;

								}
							}
						}

					}

					unintegred_sound = false;

				}
			}
		}.runTaskTimer(Skywars.getInstance(), 10L, 10L);

	}

	public static List<Block> circle(Location loc, Integer r, Integer h, Boolean hollow, Boolean sphere, int plus_y) {
		List<Block> circleblocks = new ArrayList<Block>();
		int cx = loc.getBlockX();
		int cy = loc.getBlockY();
		int cz = loc.getBlockZ();
		for (int x = cx - r; x <= cx + r; x++)
			for (int z = cz - r; z <= cz + r; z++)
				for (int y = (sphere ? cy - r : cy); y < (sphere ? cy + r : cy + h); y++) {
					double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
					if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
						Location l = new Location(loc.getWorld(), x, y + plus_y, z);
						circleblocks.add(l.getBlock());
					}
				}

		return circleblocks;
	}

	@SuppressWarnings("deprecation")
	public static void bounceBlock(Block b, float y_speed) {

		if (b == null)
			return;

		FallingBlock fb = b.getWorld().spawnFallingBlock(b.getLocation(), b.getType(), b.getData());

		fb.setDropItem(false);
		b.setType(Material.AIR);

		float x = (float) -0.2 + (float) (Math.random() * ((0.2 - -0.2) + 0.2));
		float y = y_speed;
		float z = (float) -0.2 + (float) (Math.random() * ((0.2 - -0.2) + 0.2));

		fb.setVelocity(new Vector(x, y, z));
	}

}
