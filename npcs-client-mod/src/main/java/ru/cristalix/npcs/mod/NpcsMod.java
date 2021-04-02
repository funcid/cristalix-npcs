package ru.cristalix.npcs.mod;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.xdark.clientapi.ClientApi;
import dev.xdark.clientapi.entity.*;
import dev.xdark.clientapi.entry.ModMain;
import dev.xdark.clientapi.event.Listener;
import dev.xdark.clientapi.event.chunk.ChunkLoad;
import dev.xdark.clientapi.event.chunk.ChunkUnload;
import dev.xdark.clientapi.event.lifecycle.GameLoop;
import dev.xdark.clientapi.event.network.PluginMessage;
import dev.xdark.clientapi.network.NetworkPlayerInfo;
import dev.xdark.clientapi.util.EnumHand;
import dev.xdark.clientapi.world.World;
import dev.xdark.clientapi.world.chunk.Chunk;
import dev.xdark.feder.NetUtil;
import io.netty.buffer.Unpooled;
import lombok.val;
import ru.cristalix.npcs.data.NpcBehaviour;
import ru.cristalix.npcs.data.NpcData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("Convert2Lambda")
public class NpcsMod implements ModMain {

	private Listener listener;
	private final Gson gson = new Gson();
	private final List<Npc> npcs = new ArrayList<>();
	private ClientApi clientApi;

	@Override
	public void load(ClientApi clientApi) {

		this.clientApi = clientApi;

		clientApi.messageBus().register(clientApi.messageBus().createListener(), PluginMessage.class, new Consumer<PluginMessage>() {
			@Override
			public void accept(PluginMessage pluginMessage) {
				if (pluginMessage.getChannel().equals("npcs")) {
					String json = NetUtil.readUtf8(pluginMessage.getData());
					NpcData npcData = gson.fromJson(json, NpcData.class);
					Npc npc = createNpc(npcData);
					npcs.add(npc);
					int chunkX = ((int) npcData.getX()) >> 4;
					int chunkZ = ((int) npcData.getZ()) >> 4;
					World world = clientApi.minecraft().getWorld();
					Chunk chunk = world.getChunkProvider().getLoadedChunk(chunkX, chunkZ);
					if (chunk != null) {
						world.spawnEntity(npc.getEntity());
					}
				}
			}
		}, 1);


		EntityPlayerSP player = this.clientApi.minecraft().getPlayer();

		this.listener = clientApi.eventBus().createListener();

		clientApi.eventBus().register(listener, ChunkLoad.class, new Consumer<ChunkLoad>() {
			@Override
			public void accept(ChunkLoad event) {
				Chunk chunk = event.getChunk();
				for (Npc npc : npcs) {
					int chunkX = ((int) npc.getEntity().getX()) >> 4;
					int chunkZ = ((int) npc.getEntity().getZ()) >> 4;
					if (chunkX == chunk.getX() && chunkZ == chunk.getZ())
						event.getChunk().getWorld().spawnEntity(npc.getEntity());
				}
			}
		}, 1);
		clientApi.eventBus().register(listener, ChunkUnload.class, new Consumer<ChunkUnload>() {
			@Override
			public void accept(ChunkUnload event) {
				Chunk chunk = event.getChunk();
				for (Npc npc : npcs) {
					if (!npc.getData().isUnload())
						continue;
					int chunkX = ((int) npc.getEntity().getX()) >> 4;
					int chunkZ = ((int) npc.getEntity().getZ()) >> 4;
					if (chunkX == chunk.getX() && chunkZ == chunk.getZ())
						event.getChunk().getWorld().removeEntity(npc.getEntity());
				}
			}
		}, 1);

		int[] ticks = {0};

		clientApi.eventBus().register(listener, GameLoop.class, new Consumer<GameLoop>() {
			@Override
			public void accept(GameLoop gameLoop) {

				int tick = ticks[0]++ % 600;

				for (Npc npc : npcs) {

					if (npc.getData().getBehaviour() == NpcBehaviour.NONE) continue;
					val lookAround = npc.getData().getBehaviour() == NpcBehaviour.STARE_AND_LOOK_AROUND;

					EntityLivingBase entity = npc.getEntity();

					if (lookAround && (tick == 500 || tick == 510)) entity.swingArm(EnumHand.MAIN_HAND);
					float dyaw = 0;
					if (tick > 40 && tick < 70) dyaw = -40;
					else if (tick > 75 && tick < 130) dyaw = +40;

					boolean resetPitch = tick > 40 && tick < 130;

					boolean sneak = lookAround && (tick > 400 && tick < 415 || tick > 430 && tick < 445);

					double dx = player.getX() - entity.getX();
					double dy = player.getY() - entity.getY();
					double dz = player.getZ() - entity.getZ();

					boolean active = dx * dx + dy * dy + dz * dz < 36;

//					entity.setSneaking(sneak || !active);

					dy /= Math.sqrt(dx * dx + dz * dz);
					float yaw = active ? (float) (Math.atan2(-dx, dz) / Math.PI * 180) : npc.getData().getYaw();
					if (lookAround) yaw += dyaw;
					entity.setRotationYawHead(yaw);
					entity.setYaw(yaw);
					if (!active || (resetPitch && lookAround)) entity.setPitch(0);
					else entity.setPitch((float) (Math.atan(-dy) / Math.PI * 180));

				}

			}
		}, 1);

		clientApi.clientConnection().sendPayload("npcs:loaded", Unpooled.EMPTY_BUFFER);

//		npc.setCustomNameTag(npc.getEntityId() + "");
	}

	private Npc createNpc(NpcData npcData) {

		Entity npc1 = clientApi.entityProvider().newEntity(npcData.getType(), clientApi.minecraft().getWorld());
		npc1.setEntityId(npcData.getId());
		EntityLivingBase npc = (EntityLivingBase) npc1;
		int skinType = npcData.isSlimArms() ? 1 : 0;
		UUID id;
		do {
			id = UUID.randomUUID();
		} while (id.hashCode() % 2 != skinType);

//		System.out.println(npcData.getId() + " " + npcData.isSlimArms() + " " + id + " " + npc1);

		npc.setUniqueId(id);

		if (npcData.getType() == EntityProvider.PLAYER) {
			AbstractClientPlayer player = (AbstractClientPlayer) npc;
			GameProfile profile = new GameProfile(id, npcData.getName());
			profile.getProperties().put("skinURL", new Property("skinURL", npcData.getSkinUrl()));
			profile.getProperties().put("skinDigest", new Property("skinDigest", npcData.getSkinDigest()));
			player.setGameProfile(profile);

			NetworkPlayerInfo info = clientApi.clientConnection().newPlayerInfo(profile);

			info.setResponseTime(-2);

			player.setWearing(PlayerModelPart.CAPE);
			player.setWearing(PlayerModelPart.HAT);
			player.setWearing(PlayerModelPart.JACKET);
			player.setWearing(PlayerModelPart.LEFT_PANTS_LEG);
			player.setWearing(PlayerModelPart.LEFT_SLEEVE);
			player.setWearing(PlayerModelPart.RIGHT_PANTS_LEG);
			player.setWearing(PlayerModelPart.RIGHT_SLEEVE);

			if (npcData.isSlimArms()) info.setSkinType("SLIM");
			else info.setSkinType("DEFAULT");

			clientApi.clientConnection().addPlayerInfo(info);


		}

		npc.setAlwaysRenderNameTag(true);
		npc.setCustomNameTag(npcData.getName());

		npc.teleport(npcData.getX(), npcData.getY(), npcData.getZ());
		npc.setYaw(npcData.getYaw());
		npc.setPitch(npcData.getPitch());

//		clientApi.minecraft().getWorld().spawnEntity(npc);

		npc.setNoGravity(false);

		return new Npc(npc, npcData);
	}

	@Override
	public void unload() {

	}

}
