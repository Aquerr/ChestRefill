package io.github.aquerr.chestrefill.storage;

import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;

import java.util.List;

public interface Storage
{
    boolean addOrUpdateContainer(RefillableContainer refillableContainer);

    boolean removeRefillableContainer(ContainerLocation containerLocation);

    List<RefillableContainer> getRefillableContainers();

    List<ContainerLocation> getContainerLocations();

//    RefillableContainer getRefillableContainer(ContainerLocation containerLocation);

    boolean updateContainerTime(ContainerLocation containerLocation, int time);

    boolean changeContainerName(ContainerLocation containerLocation, String containerName);

    List<Kit> getKits();

    boolean createKit(Kit kit);

    boolean removeKit(String kitName);
}
