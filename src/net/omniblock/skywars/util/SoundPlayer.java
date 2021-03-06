package net.omniblock.skywars.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SoundPlayer {

	/**
	 * 
	 * StopSound protocol with Packets
	 */

	public static void stopSound(Player player) {

		player.playSound(player.getLocation(), "MC|StopSound", 1, 1);
		return;
		
	}

	public static void stopSound(Player...player) {

		for(Player p : player) {
			
			p.playSound(p.getLocation(), "MC|StopSound", 1, 1);
			continue;
			
		}
		
		return;
		
	}
	
	/**
	 * 
	 * PlaySound protocol with Packets
	 */

	public static void sendSound(Location l, String soundname) {

		l.getWorld().playSound(l, soundname, 1, 1);
		return;

	}

	public static void sendSound(Location l, String soundname, int radius) {
		
		for (Player cache : Bukkit.getOnlinePlayers()) {
			
			if (cache.getLocation().distance(l) <= radius + 1) {
				
				stopSound(cache);
				cache.stopSound(soundname);
				
				l.getWorld().playSound(cache.getLocation(), soundname, 5, 1);
				
			}
			
		}

	}

	public static void sendSound(Player p, String soundname, boolean nearby, int radius) {
		
		Location l = p.getLocation();

		if (nearby) {
			
			for (Player cache : Bukkit.getOnlinePlayers()) {
				
				if (cache.getLocation().distance(l) <= radius + 1) {
					
					stopSound(cache);
					cache.stopSound(soundname);
					
					l.getWorld().playSound(cache.getLocation(), soundname, 5, 1);
					
				}
				
			}
			
		} else {
			
			l.getWorld().playSound(l, soundname, 5, 1);
			
		}
		
	}

	public static void sendSound(Player p, String soundname, int volume) {
		
		stopSound(p);
		p.stopSound(soundname);
		
		p.playSound(p.getLocation(), soundname, volume, 1);
		
	}

	public static void sendSound(Player p, String soundname, int volume, int pitch) {
		
		stopSound(p);
		p.stopSound(soundname);
		
		p.playSound(p.getLocation(), soundname, volume, pitch);
		
	}

}
