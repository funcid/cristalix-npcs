package ru.cristalix.npcs.server.example;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.cristalix.npcs.data.NpcBehaviour;
import ru.cristalix.npcs.server.Npc;

public class NpcsExample extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void handle(BlockPlaceEvent e) {
		if (e.getBlockPlaced().getType() == Material.DIAMOND_BLOCK) {
			Npc.builder().location(e.getBlockPlaced().getLocation().add(0.5, 1.5, 0.5))
					.name("§bХранитель алмазного блока")
					.onClick(player -> player.sendMessage("§bХранитель алмазного блока§7: §fПриветствую."))
					.type(EntityType.PLAYER)
					.behaviour(NpcBehaviour.STARE_AND_LOOK_AROUND)
					.build();
		}
	}

}
