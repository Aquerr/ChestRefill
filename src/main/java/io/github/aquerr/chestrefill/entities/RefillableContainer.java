package io.github.aquerr.chestrefill.entities;

import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
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
    private BlockType containerBlockType;

    private int restoreTimeInSeconds;
    private boolean oneItemAtTime;
    private boolean replaceExistingItems;

    private boolean hiddenIfNoItems;
    private BlockType hidingBlock;

    private RefillableContainer(ContainerLocation containerLocation, BlockType containerBlockType, List<RefillableItem> refillableItemList)
    {
        this(containerLocation, containerBlockType, refillableItemList, 120, false, true, false, BlockTypes.DIRT);
    }

    public RefillableContainer(ContainerLocation containerLocation, BlockType containerBlockType, List<RefillableItem> refillableItemList, int time, boolean oneItemAtTime, boolean replaceExistingItems, boolean hiddenIfNoItems, BlockType hidingBlock)
    {
        this.containerLocation = containerLocation;
        this.restoreTimeInSeconds = time;
        this.items = refillableItemList;
        this.oneItemAtTime = oneItemAtTime;
        this.replaceExistingItems = replaceExistingItems;
        this.hiddenIfNoItems = hiddenIfNoItems;
        this.hidingBlock = hidingBlock;
        this.containerBlockType = containerBlockType;
    }

    public ContainerLocation getContainerLocation()
    {
        return this.containerLocation;
    }

    public List<RefillableItem> getItems()
    {
        return this.items;
    }

    public BlockType getContainerBlockType()
    {
        return this.containerBlockType;
    }

    public int getRestoreTime() { return this.restoreTimeInSeconds; }

    public boolean isOneItemAtTime()
    {
        return this.oneItemAtTime;
    }

    public boolean shouldReplaceExistingItems()
    {
        return this.replaceExistingItems;
    }

    public boolean shouldBeHiddenIfNoItems()
    {
        return this.hiddenIfNoItems;
    }

    public BlockType getHidingBlock()
    {
        return this.hidingBlock;
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

        RefillableContainer refillableContainer = new RefillableContainer(new ContainerLocation(tileEntity.getLocation().getBlockPosition(), worldUUID), tileEntity.getBlock().getType(), items);

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
