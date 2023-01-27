package io.github.aquerr.chestrefill.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;
import java.util.UUID;

public final class WorldUtils
{
    private WorldUtils()
    {
        throw new IllegalStateException("You should not instantiate this class!");
    }

    public static Optional<ServerWorld> getWorldByUUID(UUID worldUUID)
    {
        return Sponge.server().worldManager().worlds().stream()
                .filter(serverWorld -> serverWorld.uniqueId().equals(worldUUID))
                .findFirst();
    }
}
