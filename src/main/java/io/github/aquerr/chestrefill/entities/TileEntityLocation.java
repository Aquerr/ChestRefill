package io.github.aquerr.chestrefill.entities;

import com.flowpowered.math.vector.Vector3i;

import java.util.UUID;

/**
 * Created by Aquerr on 2018-02-14.
 */
public class TileEntityLocation
{
    private Vector3i blockPosition;
    private UUID worldUUID;

    public TileEntityLocation(Vector3i blockPosition, UUID worldUUID)
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
        if (!(obj instanceof TileEntityLocation))
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }

        return this.blockPosition.equals(((TileEntityLocation)obj).getBlockPosition()) && this.worldUUID.equals(((TileEntityLocation)obj).getWorldUUID());
    }

    @Override
    public int hashCode()
    {
        return this.blockPosition.toString().length();
    }
}
