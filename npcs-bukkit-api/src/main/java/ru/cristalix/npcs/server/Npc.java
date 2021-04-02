package ru.cristalix.npcs.server;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import net.minecraft.server.v1_12_R1.Entity;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import ru.cristalix.npcs.data.NpcBehaviour;
import ru.cristalix.npcs.data.NpcData;

import java.util.function.Consumer;

@Data
@AllArgsConstructor
@Builder
public class Npc {

	private final int id = Entity.entityCount++;

	@Builder.Default
	private final EntityType type = EntityType.PLAYER;

	private String name;
	private Location location;

	@Builder.Default
	private NpcBehaviour behaviour = NpcBehaviour.NONE;

	private String skinUrl;
	private String skinDigest;
	private boolean slimArms;
	private Consumer<Player> onClick;
	private ByteBuf internalCachedData;
	
	@Builder.Default
	private boolean unload = true;

	public NpcData getData() {
		return new NpcData(id, type == EntityType.PLAYER ? 1000 : type.typeId, name, behaviour, location.x, location.y, location.z, location.pitch, location.yaw, skinUrl, skinDigest, slimArms, unload);
	}

}
