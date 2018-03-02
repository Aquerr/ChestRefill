package io.github.aquerr.chestrefill.storage;

import io.github.aquerr.chestrefill.entities.TileEntityLocation;
import io.github.aquerr.chestrefill.entities.RefillableTileEntity;

import java.util.List;

/**
 * Created by Aquerr on 2018-02-15.
 */
public interface Storage
{
    boolean addOrUpdateRefillableEntity(RefillableTileEntity refillableTileEntity);

    boolean removeRefillableEntity(TileEntityLocation tileEntityLocation);

    List<RefillableTileEntity> getRefillableEntities();

    RefillableTileEntity getRefillableEntity(TileEntityLocation tileEntityLocation);

    boolean updateEntityTime(TileEntityLocation tileEntityLocation, int time);
}
