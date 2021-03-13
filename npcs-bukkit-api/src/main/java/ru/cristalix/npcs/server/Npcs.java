package ru.cristalix.npcs.server;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.player.PlayerUseUnknownEntityEvent;
import com.google.gson.Gson;
import dev.xdark.feder.NetUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.val;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import ru.cristalix.core.display.DisplayChannels;
import ru.cristalix.core.display.messages.Mod;
import ru.cristalix.npcs.data.NpcData;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class Npcs implements Listener {

	private static final Gson gson = new Gson();
	private static final Set<Npc> globalNpcs = new HashSet<>();

	public static final Set<Player> active = new HashSet<>();
	private static Plugin plugin;

	public static void init(Plugin plugin) {
		Npcs.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(new Npcs(), plugin);

		Bukkit.getMessenger().registerIncomingPluginChannel(plugin, "boards:loaded", (channel, player, data) -> {
			active.add(player);

			for (Npc npcs : globalNpcs) {
				if (npcs.getLocation().getWorld() == player.getWorld()) {
					show(npcs, player);
				}
			}
		});
	}

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
	public void onJoin(PlayerJoinEvent e) throws Exception {
		InputStream resource = plugin.getResource("npcs-client-mod.jar");
		byte[] serialize = Mod.serialize(new Mod(IOUtils.readFully(resource, resource.available())));
		ByteBuf buf = Unpooled.buffer();
		buf.writeBytes(serialize);
		PacketDataSerializer ds = new PacketDataSerializer(buf);
		PacketPlayOutCustomPayload packet = new PacketPlayOutCustomPayload(DisplayChannels.MOD_CHANNEL, ds);
		((CraftPlayer) e.getPlayer()).getHandle().playerConnection.sendPacket(packet);
	}

	@EventHandler
	public void handle(EntityAddToWorldEvent e) {
		if (e.getEntity() instanceof CraftPlayer) {
			for (Npc npcs : globalNpcs) {
				if (npcs.getLocation().getWorld() == e.entity.getWorld()) {
					show(npcs, (CraftPlayer) e.getEntity());
				}
			}
		}
	}

	@EventHandler
	public void handle(PlayerUseUnknownEntityEvent e) {
		if (e.getHand() == EquipmentSlot.OFF_HAND) return;
		e.getPlayer().sendMessage("Interacted at " + e.getEntityId());
	}

}
