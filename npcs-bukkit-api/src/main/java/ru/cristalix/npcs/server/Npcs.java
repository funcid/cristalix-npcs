package ru.cristalix.npcs.server;

import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import com.google.gson.Gson;
import dev.xdark.feder.NetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.val;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import ru.cristalix.npcs.data.NpcData;

import java.util.HashSet;
import java.util.Set;

public class Npcs implements Listener {

	private static final Gson gson = new Gson();
	public static void init(Plugin plugin) {
		Bukkit.getPluginManager().registerEvents(new Npcs(), plugin);
	}

	private static final Set<Npc> globalNpcs = new HashSet<>();

	public static void spawn(Npc npc) {
		if (!globalNpcs.add(npc)) return;
		for (Player player : npc.getLocation().getWorld().getPlayers()) {
			show(npc, player);
		}
	}

	public static void show(Npc npc, Player player) {

		ByteBuf internalCachedData = npc.getInternalCachedData();
		if (internalCachedData == null) {
			NpcData data = npc.getData();
			String json = gson.toJson(data);
			ByteBuf buffer = Unpooled.buffer();
			NetUtil.writeUtf8(json, buffer);
			npc.setInternalCachedData(buffer);
		}

		val packet = new PacketPlayOutCustomPayload("npcs", new PacketDataSerializer(npc.getInternalCachedData().retainedSlice()));

		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	@EventHandler
	public void handle(PlayerChangedWorldEvent e) {
		System.out.println("Player " + e.getPlayer().getName() + " changed world to " + e.getFrom());
	}

	@EventHandler
	public void handle(PlayerUseUnknownEntityEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND) return;
		e.getPlayer().sendMessage("Interacted at " + e.getEntityId());
	}

}
