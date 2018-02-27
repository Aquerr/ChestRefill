package io.github.aquerr.chestrefill.storage;

import io.github.aquerr.chestrefill.entities.ChestLocation;
import io.github.aquerr.chestrefill.entities.RefillingChest;

import java.util.List;

/**
 * Created by Aquerr on 2018-02-15.
 */
public interface Storage
{
    boolean addOrUpdateChest(RefillingChest refillingChest);

    boolean removeChest(ChestLocation chestLocation);

    List<RefillingChest> getChests();

    RefillingChest getChest(ChestLocation chestLocation);

    boolean updateChestTime(ChestLocation chestLocation, int time);
}
