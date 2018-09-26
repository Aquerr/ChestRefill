package io.github.aquerr.chestrefill.storage;

import io.github.aquerr.chestrefill.caching.ContainerCache;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;

import java.nio.file.Path;
import java.util.*;

public class StorageHelper
{
    private final Queue<RefillableContainer> containersToSave;
    private final Thread storageThread;
    private final Storage containerStorage;

    public StorageHelper(Path configDir)
    {
        containersToSave = new LinkedList<>();
        storageThread = new Thread(startContainerSavingThread());
        containerStorage = new JSONStorage(configDir);

        //Load cache
        ContainerCache.loadCache(containerStorage.getRefillableContainers());

        //Start new thread directly
        storageThread.start();
    }

    public Runnable startContainerSavingThread()
    {
        return () ->
        {
            int sleep = 1000;
            while(true)
            {
                if(containersToSave.size() > 0)
                {
                    synchronized(containersToSave)
                    {
                        this.containerStorage.addOrUpdateContainer(this.containersToSave.poll());
                        sleep = 1000;
                    }
                }
                else
                {
                    try
                    {
                        Thread.sleep(sleep);
                        if(sleep < 16000)
                        {
                            sleep *= 2;
                        }
                    }
                    catch(InterruptedException exception)
                    {
                        exception.printStackTrace();
                    }
                }
            }
        };
    }

    public boolean addOrUpdateContainer(RefillableContainer containerToSave)
    {
        ContainerCache.updateContainerCache(containerToSave);
        return this.containersToSave.add(containerToSave);
    }

    public boolean removeContainer(ContainerLocation containerLocation)
    {
        ContainerCache.removeContainer(containerLocation);
        return this.containerStorage.removeRefillableContainer(containerLocation);
    }

    public Collection<RefillableContainer> getRefillableContainers()
    {
        return ContainerCache.getContainersCache().values();
    }

    public Set<ContainerLocation> getContainerLocations()
    {
        return ContainerCache.getContainersCache().keySet();
    }

    public RefillableContainer getRefillableContainer(ContainerLocation containerLocation)
    {
        return ContainerCache.getContainersCache().get(containerLocation);
    }

    public boolean updateContainerTime(ContainerLocation containerLocation, int time)
    {
        //TODO: Rework so that the separate thread will take hand of it
        ContainerCache.updateContainerTime(containerLocation, time);
        return this.containerStorage.updateContainerTime(containerLocation, time);
    }

    public boolean changeContainerName(ContainerLocation containerLocation, String containerName)
    {
        //TODO: Rework so that the separate thread will take hand of it
        ContainerCache.updateContainerName(containerLocation, containerName);
        return this.containerStorage.changeContainerName(containerLocation, containerName);
    }
}
