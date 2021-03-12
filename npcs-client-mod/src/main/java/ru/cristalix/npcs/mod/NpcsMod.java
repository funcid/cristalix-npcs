package ru.cristalix.npcs.mod;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import dev.xdark.clientapi.ClientApi;
import dev.xdark.clientapi.entity.*;
import dev.xdark.clientapi.entry.ModMain;
import dev.xdark.clientapi.event.Listener;
import dev.xdark.clientapi.event.lifecycle.GameLoop;
import dev.xdark.clientapi.network.NetworkPlayerInfo;
import dev.xdark.clientapi.util.EnumHand;

import java.util.UUID;
import java.util.function.Consumer;

public class NpcsMod implements ModMain {

	private Listener listener;

	@Override
	public void load(ClientApi clientApi) {

		AbstractClientPlayer npc = (AbstractClientPlayer) clientApi.entityProvider().newEntity(EntityProvider.PLAYER, clientApi.minecraft().getWorld());
		UUID id = UUID.randomUUID();
		npc.setUniqueId(id);

		GameProfile profile = new GameProfile(id, "Helloworld");
		profile.getProperties().put("skinURL", new Property("skinURL", "https://webdata.c7x.dev/textures/skin/307264a1-2c69-11e8-b5ea-1cb72caa35fd"));
		profile.getProperties().put("skinDigest", new Property("skinDigest", "307264a1-2c69-11e8-b5ea-1cb72caa35fd"));

		npc.setGameProfile(profile);

		NetworkPlayerInfo info = clientApi.clientConnection().newPlayerInfo(profile);

//		info.setResponseTime(-2);

		clientApi.clientConnection().addPlayerInfo(info);

		npc.setAlwaysRenderNameTag(true);
		npc.setCustomNameTag("test");

//		npc.setWearing(PlayerModelPart.CAPE);
//		npc.setWearing(PlayerModelPart.HAT);
//		npc.setWearing(PlayerModelPart.JACKET);
//		npc.setWearing(PlayerModelPart.LEFT_PANTS_LEG);
//		npc.setWearing(PlayerModelPart.LEFT_SLEEVE);
//		npc.setWearing(PlayerModelPart.RIGHT_PANTS_LEG);
//		npc.setWearing(PlayerModelPart.RIGHT_SLEEVE);

		EntityPlayerSP player = clientApi.minecraft().getPlayer();
		npc.teleport(player.getX(), player.getY(), player.getZ());

		clientApi.minecraft().getWorld().spawnEntity(npc);

		npc.setNoGravity(false);

		this.listener = clientApi.eventBus().createListener();

		int[] ticks = {0};

		double[] from = {0, 0, 0};
		double[] to = {0, 0, 0};

		clientApi.eventBus().register(listener, GameLoop.class, new Consumer<GameLoop>() {
			@Override
			public void accept(GameLoop gameLoop) {

				int tick = ticks[0]++ % 600;

				if (tick == 500 || tick == 510) npc.swingArm(EnumHand.MAIN_HAND);
				float dyaw = 0;
				if (tick > 40 && tick < 70) dyaw = -40;
				else if (tick > 75 && tick < 130) dyaw = +40;

				boolean resetPitch = tick > 40 && tick < 130;

				boolean sneak = tick > 400 && tick < 415 || tick > 430 && tick < 445;

				double dx = player.getX() - npc.getX();
				double dy = player.getY() - npc.getY();
				double dz = player.getZ() - npc.getZ();

				boolean active = dx*dx+dy*dy+dz*dz < 36;

				npc.setSneaking(sneak || !active);

				dy /= Math.sqrt(dx*dx+dz*dz);
				float yaw = active ? (float) (Math.atan2(-dx, dz) / Math.PI * 180) : 30;
				yaw += dyaw;
				npc.setRotationYawHead(yaw);
				npc.setYaw(yaw);
				npc.setPitch(!active || resetPitch ? 0 : (float) (Math.atan(-dy) / Math.PI * 180));
			}
		}, 1);

//		npc.setCustomNameTag(npc.getEntityId() + "");
	}

	@Override
	public void unload() {

	}

}
