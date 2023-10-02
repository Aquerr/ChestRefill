package io.github.aquerr.chestrefill.entities;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;
import java.util.Map;

public class ModeExecutionParams
{
    private final RefillableContainer builtContainer;
    private final RefillableContainer refillableContainerAtLocation;
    private final ServerPlayer serverPlayer;
    private final Map<String, Object> extraData;

    public ModeExecutionParams(ServerPlayer serverPlayer, RefillableContainer builtContainer, RefillableContainer refillableContainerAtLocation)
    {
        this(serverPlayer, builtContainer, refillableContainerAtLocation, Collections.emptyMap());
    }

    public ModeExecutionParams(ServerPlayer serverPlayer, RefillableContainer builtContainer, RefillableContainer refillableContainerAtLocation, Map<String, Object> extraData)
    {
        this.serverPlayer = serverPlayer;
        this.builtContainer = builtContainer;
        this.refillableContainerAtLocation = refillableContainerAtLocation;
        this.extraData = extraData;
    }

    public RefillableContainer getRefillableContainerAtLocation()
    {
        return refillableContainerAtLocation;
    }

    public RefillableContainer getBuiltContainer()
    {
        return builtContainer;
    }

    public ServerPlayer getPlayer()
    {
        return serverPlayer;
    }

    public Map<String, Object> getExtraData()
    {
        return extraData;
    }
}