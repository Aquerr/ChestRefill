package io.github.aquerr.chestrefill.entities;

import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class RefillableContainer
{
    private ContainerLocation containerLocation;
    private List<RefillableItem> items;
    private int restoreTimeInSeconds;
    private boolean oneItemAtTime;
    private boolean replaceExistingItems;

    private RefillableContainer(ContainerLocation containerLocation, List<RefillableItem> refillableItemList)
    {
        this(containerLocation, refillableItemList, 120, false, true);
    }

    public RefillableContainer(ContainerLocation containerLocation, List<RefillableItem> refillableItemList, int time, boolean oneItemAtTime, boolean replaceExistingItems)
    {
        this.containerLocation = containerLocation;
        this.restoreTimeInSeconds = time;
        this.items = refillableItemList;
        this.oneItemAtTime = oneItemAtTime;
        this.replaceExistingItems = replaceExistingItems;
    }

    public ContainerLocation getContainerLocation()
    {
        return containerLocation;
    }

    public List<RefillableItem> getItems()
    {
        return items;
    }

    public int getRestoreTime() { return restoreTimeInSeconds; }

    public boolean isOneItemAtTime()
    {
        return oneItemAtTime;
    }

    public boolean shouldReplaceExistingItems()
    {
        return replaceExistingItems;
    }

    public static RefillableContainer fromTileEntity(TileEntity tileEntity, UUID worldUUID)
    {
        TileEntityCarrier carrier = (TileEntityCarrier) tileEntity;
        List<RefillableItem> items = new ArrayList<>();

        carrier.getInventory().slots().forEach(x->
        {
            if (x.peek().isPresent())
            {
                items.add(new RefillableItem(x.peek().get(), 1f));
            }
        });

        RefillableContainer refillableContainer = new RefillableContainer(new ContainerLocation(tileEntity.getLocation().getBlockPosition(), worldUUID), items);

        return refillableContainer;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RefillableContainer))
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }


        //TODO: Refactor this code if it will be possible...
        //Compare container location
        if (this.containerLocation.equals(((RefillableContainer)obj).getContainerLocation()))
        {
            Inventory tempInventory = Inventory.builder().build(ChestRefill.getChestRefill());

            this.items.forEach(x-> {
                //Offer removes items from inventory so we need to build new temp items.
                ItemStack tempItemStack = ItemStack.builder().fromItemStack(x.getItem()).build();
                tempInventory.offer(tempItemStack);
            });

            //Compare items
            for (RefillableItem comparedItem : ((RefillableContainer) obj).getItems())
            {
                if (!tempInventory.contains(comparedItem.getItem()))
                {
                    return false;
                }
            }

            //Compare restore time
            if (this.restoreTimeInSeconds != ((RefillableContainer)obj).getRestoreTime())
            {
                return false;
            }

            //Check if randomize is turned on
            if (this.oneItemAtTime != ((RefillableContainer)obj).oneItemAtTime)
            {
                return false;
            }

            //Check equality of replaceExistingItems property
            if (this.replaceExistingItems == ((RefillableContainer)obj).replaceExistingItems)
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return containerLocation.toString().length();
    }
}
