package ru.cristalix.npcs.data;

import lombok.Data;

@Data
public class NpcData {

	private final int id;
	private final int type;
	private final String name;
	private final NpcBehaviour behaviour;
	private final double x;
	private final double y;
	private final double z;
	private final float pitch;
	private final float yaw;
	private final String skinUrl;
	private final String skinDigest;
	private final boolean slimArms;


}
