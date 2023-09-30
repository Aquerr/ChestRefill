package io.github.aquerr.chestrefill.caching;

import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.ItemProvider;
import io.github.aquerr.chestrefill.entities.ItemProviderType;
import io.github.aquerr.chestrefill.entities.RefillableContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ContainerCache
{
    private ContainerCache()
    {
        throw new UnsupportedOperationException();
    }

    private static Map<ContainerLocation, RefillableContainer> refillableContainersCache = new HashMap<>();
    private static Map<String, Kit> kitsCache = new HashMap<>();

    public static boolean loadCache(List<RefillableContainer> refillableContainerList, List<Kit> kits)
    {
        try
        {
            for(final RefillableContainer refillableContainer : refillableContainerList)
            {
                refillableContainersCache.put(refillableContainer.getContainerLocation(), refillableContainer);
            }
            for(final Kit kit : kits)
            {
                kitsCache.put(kit.getName().toLowerCase(), kit);
            }
        }
        catch(NullPointerException exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static Map<String, Kit> getKitsCache()
    {
        return kitsCache;
    }

    public static boolean addOrUpdateKitCache(Kit kit)
    {
        try
        {
            kitsCache.put(kit.getName().toLowerCase(), kit);
        }
        catch(final Exception e)
        {
            return false;
        }
        return true;
    }

    public static boolean removeKit(final String name)
    {
        try
        {
            kitsCache.remove(name.toLowerCase());
            for(final RefillableContainer refillableContainer : refillableContainersCache.values())
            {
                if (refillableContainer.getItemProvider().getType() == ItemProviderType.KIT
                    && refillableContainer.getItemProvider().getLocation().equalsIgnoreCase(name))
                {
                    refillableContainer.setItemProvider(new ItemProvider(ItemProviderType.SELF, null));
                }
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean addOrUpdateContainerCache(RefillableContainer refillableContainer)
    {
        try
        {
            if(refillableContainersCache.containsKey(refillableContainer.getContainerLocation()))
            {
                refillableContainersCache.replace(refillableContainer.getContainerLocation(), refillableContainer);
            }
            else
            {
                refillableContainersCache.put(refillableContainer.getContainerLocation(), refillableContainer);
            }
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
        return refillableContainersCache;
    }

    public static boolean removeContainer(ContainerLocation containerLocation)
    {
        try
        {
            refillableContainersCache.remove(containerLocation);
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
            refillableContainersCache.get(containerLocation).setRestoreTime(time);
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
            refillableContainersCache.get(containerLocation).setName(name);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean assignKit(ContainerLocation containerLocation, String kitName)
    {
        final RefillableContainer refillableContainer = refillableContainersCache.get(containerLocation);
        refillableContainer.setItemProvider(new ItemProvider(ItemProviderType.KIT, kitName));
        return refillableContainer.getItemProvider().getLocation().equalsIgnoreCase(kitName);
    }
}
