package io.github.aquerr.chestrefill.storage;

import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;

import java.util.List;

/**
 * Created by Aquerr on 2018-02-15.
 */
public interface Storage
{
    boolean addOrUpdateContainer(RefillableContainer refillableContainer);

    boolean removeRefillableContainers(ContainerLocation containerLocation);

    List<RefillableContainer> getRefillableContainers();

    List<ContainerLocation> getContainerLocations();

    RefillableContainer getRefillableContainer(ContainerLocation containerLocation);

    boolean updateContainerTime(ContainerLocation containerLocation, int time);
}
