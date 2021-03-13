package ru.cristalix.npcs.mod;


import dev.xdark.clientapi.entity.Entity;
import dev.xdark.clientapi.entity.EntityLiving;
import lombok.Data;
import ru.cristalix.npcs.data.NpcData;

@Data
public class Npc {

    private final EntityLiving entity;
    private final NpcData data;


}
