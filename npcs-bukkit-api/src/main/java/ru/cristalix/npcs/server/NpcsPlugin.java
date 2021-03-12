package ru.cristalix.npcs.server;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.cristalix.npcs.data.NpcBehaviour;
import ru.cristalix.npcs.data.NpcData;

public class NpcsPlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		Npcs.init(this);

	}


}
