package io.github.aquerr.chestrefill.storage;

import io.github.aquerr.chestrefill.entities.RefillingChest;

import java.util.List;

/**
 * Created by Aquerr on 2018-02-15.
 */
public interface Storage
{
    boolean addChest(RefillingChest refillingChest);

    boolean removeChest(RefillingChest refillingChest);

    List<RefillingChest> getChests();

}
