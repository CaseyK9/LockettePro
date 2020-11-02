package com.craftermc.lockettepro;

import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Team;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyPermission.ActionType;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.milkbowl.vault.permission.Permission;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.ClanPlayer;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import net.sacredlabyrinth.phaed.simpleclans.managers.ClanManager;

@SuppressWarnings("deprecation")
public class Dependency {
	
	protected static WorldGuardPlugin worldguard = null;
	protected static Plugin residence = null;
	protected static Plugin towny = null;
	protected static Plugin factions = null;
	protected static Plugin vault = null;
	protected static Permission permission = null;
	protected static Plugin askyblock = null;
	protected static Plugin plotsquared = null;
	protected static Plugin simpleclans = null;
	protected static ClanManager clanmanager = null;
	protected static Plugin griefprevention = null;
	
	public Dependency(Plugin plugin){
		// WorldGuard
		Plugin worldguardplugin = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
	    if (worldguardplugin == null || !(worldguardplugin instanceof WorldGuardPlugin)) {
	    	worldguard = null;
	    } else {
	    	worldguard = (WorldGuardPlugin)worldguardplugin;
	    }
	    // Residence
	    residence = plugin.getServer().getPluginManager().getPlugin("Residence");
	    // Towny
	    towny = plugin.getServer().getPluginManager().getPlugin("Towny");
		// Factions
	    factions = plugin.getServer().getPluginManager().getPlugin("Factions");
		// Vault
	    vault = plugin.getServer().getPluginManager().getPlugin("Vault");
	    if (vault != null){
	    	RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
	        permission = rsp.getProvider();
	    }
	    // ASkyblock
	    askyblock = plugin.getServer().getPluginManager().getPlugin("ASkyblock");
	    // SimpleClans
	    simpleclans = plugin.getServer().getPluginManager().getPlugin("SimpleClans");
	    if (simpleclans != null){
	    	clanmanager = ((SimpleClans)simpleclans).getClanManager();
	    }
	    // GreifPrevention
	    griefprevention = plugin.getServer().getPluginManager().getPlugin("GriefPrevention");
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isProtectedFrom(Block block, Player player){
		if (factions != null){
			try {
				Faction faction = BoardColl.get().getFactionAt(PS.valueOf(block));
				if (faction != null && !faction.isNone()){
					MPlayer mplayer = MPlayer.get(player);
					if (mplayer != null && !mplayer.isOverriding()){
						Faction playerfaction = mplayer.getFaction();
						if (faction != playerfaction){
							return true;
						}
					}
				}
			} catch (Exception e){}
		}
		if (askyblock != null){
			try {
				Island island = ASkyBlockAPI.getInstance().getIslandAt(block.getLocation());
				if (island != null) {
					for (UUID memberuuid : island.getMembers()){
						if (memberuuid.equals(player.getUniqueId())) return false;
					}
					return true;
				}
			} catch (Exception e){}
		}
		if (simpleclans != null){
			// TODO or not todo
		}
		return false;
	}
	
	public static boolean isTownyTownOrNationOf(String line, Player player){
		if (towny != null){
			String name = player.getName();
			try {
				Resident resident = TownyUniverse.getDataSource().getResident(name);
				Town town = resident.getTown();
				if (line.equals("[" + town.getName() + "]")) return true;
				Nation nation = town.getNation();
				if (line.equals("[" + nation.getName() + "]")) return true;
			} catch (Exception e) {}
		}
		if (factions != null){
			try {
				MPlayer mplayer = MPlayer.get(player);
				if (mplayer != null){
					Faction faction = mplayer.getFaction();
					if (faction != null){
						if (line.equals("[" + faction.getName() + "]")) return true;
					}
				}
			} catch (Exception e) {}
		}
		return false;
	}
	
	public static boolean isPermissionGroupOf(String line, Player player){
		if (vault != null){
			try {
				String[] groups = permission.getPlayerGroups(player);
				for (String group : groups){
					if (line.equals("[" + group + "]")) return true;
				}
			} catch (Exception e){}
		}
		return false;
	}
	
	public static boolean isScoreboardTeamOf(String line, Player player){
		Team team = Bukkit.getScoreboardManager().getMainScoreboard().getEntryTeam(player.getName());
		if (team != null){
			if (line.equals("[" + team.getName() + "]")) return true;
		}
		return false;
	}
	
	public static boolean isSimpleClanOf(String line, Player player){
		if (simpleclans == null) return false;
		try {
			ClanPlayer clanplayer = ((SimpleClans)simpleclans).getClanManager().getClanPlayer(player);
			if (clanplayer != null){
				Clan clan = clanplayer.getClan();
				if (clan != null){
					if (line.equals("[" + clan.getTag() + "]")) return true;
				}
			}
		} catch (Exception e){}
		return false;
	}

}