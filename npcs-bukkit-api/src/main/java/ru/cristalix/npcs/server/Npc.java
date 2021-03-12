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
	private final EntityType type;
	private String name;
	private Location location;
	private NpcBehaviour behaviour;
	private Consumer<Player> onClick;
	private ByteBuf internalCachedData;

	public NpcData getData() {
		return new NpcData(id, type.typeId, name, location.x, location.y, location.z, location.pitch, location.yaw);
	}

}
