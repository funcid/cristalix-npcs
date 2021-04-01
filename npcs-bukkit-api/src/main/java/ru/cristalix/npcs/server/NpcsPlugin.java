package ru.cristalix.npcs.server;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ru.cristalix.npcs.data.NpcBehaviour;

public class NpcsPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Npcs.init(this);
        Npcs.spawn(Npc.builder()
                .location(new Location(Bukkit.getWorlds().get(1), 100, 100, 100))
                .skinUrl("https://webdata.c7x.dev/textures/skin/30719b68-2c69-11e8-b5ea-1cb72caa35fd")
                .skinDigest("30719b68-2c6911e8b5ea1cb72caa35fd")
                .name("__xDark")
                .build()
        );
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        boolean slim = Math.random() > 0.5;
        NpcBehaviour behaviour = NpcBehaviour.values()[(int) (Math.random() * NpcBehaviour.values().length)];
        Npcs.spawn(Npc.builder()
                .name((slim ? "alex" : "steve") + " " + behaviour.name().toLowerCase())
                .location(e.getPlayer().getLocation())
                .slimArms(slim)
                .behaviour(behaviour)
                .build());
    }


}
