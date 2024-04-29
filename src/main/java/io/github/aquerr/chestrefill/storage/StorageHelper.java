package io.github.aquerr.chestrefill.storage;

import io.github.aquerr.chestrefill.caching.ContainerCache;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StorageHelper
{
    private final Queue<RefillableContainer> containersToSave;
    private final Storage containerStorage;

    private final ExecutorService executorService;

    public StorageHelper(Path configDir)
    {
        containersToSave = new LinkedList<>();
        this.executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this::startContainerSavingThread);
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
        containerStorage = new JSONStorage(configDir);
    }

    public void refreshCache()
    {
        //Load cache
        ContainerCache.loadCache(containerStorage.getRefillableContainers(), containerStorage.getKits());
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
        CompletableFuture.runAsync(() -> this.containerStorage.removeRefillableContainer(containerLocation));
        return ContainerCache.removeContainer(containerLocation);
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
        CompletableFuture.runAsync(() -> this.containerStorage.updateContainerTime(containerLocation, time));
        return ContainerCache.updateContainerTime(containerLocation, time);
    }

    public boolean changeContainerName(ContainerLocation containerLocation, String containerName)
    {
        CompletableFuture.runAsync(() -> this.containerStorage.changeContainerName(containerLocation, containerName));
        return ContainerCache.updateContainerName(containerLocation, containerName);
    }


    public Map<String, Kit> getKits()
    {
        return ContainerCache.getKitsCache();
    }

    public boolean createKit(Kit kit)
    {
        CompletableFuture.runAsync(() -> this.containerStorage.createKit(kit));
        return ContainerCache.addOrUpdateKitCache(kit);
    }

    public boolean removeKit(String kitName)
    {
        CompletableFuture.runAsync(() -> this.containerStorage.removeKit(kitName));
        return ContainerCache.removeKit(kitName);
    }

    //TODO: Remove this and replace with CompletableFuture
    private void startContainerSavingThread()
    {
        while(true)
        {
            if (this.executorService.isShutdown())
                break;

            synchronized(containersToSave)
            {
                if(!containersToSave.isEmpty())
                {
                    this.containerStorage.addOrUpdateContainer(this.containersToSave.poll());
                }
                else
                {
                    try
                    {
                        this.containersToSave.wait(5000);
                    }
                    catch(InterruptedException exception)
                    {
                        // ignored
                    }
                }
            }
        }
    }

    public void stopStorageThread()
    {
        this.executorService.shutdownNow();
    }
}
