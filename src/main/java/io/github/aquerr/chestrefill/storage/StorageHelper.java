package io.github.aquerr.chestrefill.storage;

import io.github.aquerr.chestrefill.caching.ContainerCache;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageHelper
{
    private final Queue<RefillableContainer> containersToSave;
    private final Storage containerStorage;
    private final ExecutorService executorService;
//    private Thread storageThread;

    public StorageHelper(Path configDir)
    {
        containersToSave = new LinkedList<>();
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(startContainerSavingThread());
        containerStorage = new JSONStorage(configDir);

        //Load cache
        ContainerCache.loadCache(containerStorage.getRefillableContainers());

//        storageThread = new Thread(startContainerSavingThread());
//        storageThread.start();
    }

    public boolean addOrUpdateContainer(RefillableContainer containerToSave)
    {
        final boolean didSucceed = ContainerCache.addOrUpdateContainerCache(containerToSave);
        synchronized(this.containersToSave)
        {
            this.containersToSave.add(containerToSave);
            this.containersToSave.notify();
        }
        return didSucceed;
    }

    public boolean removeContainer(ContainerLocation containerLocation)
    {
        ContainerCache.removeContainer(containerLocation);
//        synchronized(this.containersToSave)
//        {
//            this.containersToSave.add(containerToSave);
//            this.containersToSave.notify();
//        }
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


    public List<Kit> getKits()
    {
        return this.containerStorage.getKits();
    }

    public boolean createKit(Kit kit)
    {
        return this.containerStorage.createKit(kit);
    }

    public boolean removeKit(String kitName)
    {
        return this.containerStorage.removeKit(kitName);
    }

    public boolean assignKit(ContainerLocation containerLocation, String kitName)
    {
        return this.containerStorage.assignKit(containerLocation, kitName);
    }

    private Runnable startContainerSavingThread()
    {
        return () ->
        {
            while(true)
            {
                synchronized(containersToSave)
                {
                    if(containersToSave.size() > 0)
                    {
                        this.containerStorage.addOrUpdateContainer(this.containersToSave.poll());
                    }
                    else
                    {
                        try
                        {
                            this.containersToSave.wait();
                        }
                        catch(InterruptedException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                }
            }
        };
    }
}
