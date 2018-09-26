package io.github.aquerr.chestrefill.caching;

import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerCache
{
    private static Map<ContainerLocation, RefillableContainer> refillableContainerListCache = new HashMap();

    public static boolean loadCache(List<RefillableContainer> refillableContainerList)
    {
        try
        {
            refillableContainerList.clear();
            for(RefillableContainer refillableContainer : refillableContainerList)
            {
                refillableContainerListCache.put(refillableContainer.getContainerLocation(), refillableContainer);
            }
        }
        catch(NullPointerException exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean updateContainerCache(RefillableContainer refillableContainer)
    {
        try
        {
            refillableContainerListCache.replace(refillableContainer.getContainerLocation(), refillableContainer);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static Map<ContainerLocation, RefillableContainer> getContainersCache()
    {
        return refillableContainerListCache;
    }

    public static boolean removeContainer(ContainerLocation containerLocation)
    {
        try
        {
            refillableContainerListCache.remove(containerLocation);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean updateContainerTime(ContainerLocation containerLocation, int time)
    {
        try
        {
            refillableContainerListCache.get(containerLocation).setRestoreTime(time);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean updateContainerName(ContainerLocation containerLocation, String name)
    {
        try
        {
            refillableContainerListCache.get(containerLocation).setName(name);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }
}
