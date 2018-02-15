package io.github.aquerr.chestrefill.entities;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;

/**
 * Created by Aquerr on 2018-02-14.
 */
public class ChestLocation
{
    private Vector3i blockPosition;
    private UUID worldUUID;

    public ChestLocation(Vector3i blockPosition, UUID worldUUID)
    {
        this.blockPosition = blockPosition;
        this.worldUUID = worldUUID;
    }

    public Vector3i getBlockPosition()
    {
        return this.blockPosition;
    }

    public UUID getWorldUUID()
    {
        return this.worldUUID;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ChestLocation))
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }

        return this.blockPosition.equals(((ChestLocation)obj).getBlockPosition()) && this.worldUUID.equals(((ChestLocation)obj).getWorldUUID());
    }

    @Override
    public int hashCode()
    {
        return this.blockPosition.toString().length();
    }
}
