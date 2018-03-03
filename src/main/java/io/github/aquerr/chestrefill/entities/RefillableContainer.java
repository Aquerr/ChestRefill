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
    private ContainerLocation _containerLocation;
    private List<ItemStack> items;
    private int restoreTimeInSeconds;

    public RefillableContainer(ContainerLocation containerLocation, List<ItemStack> itemsList)
    {
        this._containerLocation = containerLocation;
        this.items = itemsList;
        this.restoreTimeInSeconds = 120; //Default: 120 sec
    }

    public RefillableContainer(ContainerLocation containerLocation, List<ItemStack> itemsList, int time)
    {
        this._containerLocation = containerLocation;
        this.items = itemsList;
        this.restoreTimeInSeconds = time;
    }

    public ContainerLocation getContainerLocation()
    {
        return _containerLocation;
    }

    public List<ItemStack> getItems()
    {
        return items;
    }

    public int getRestoreTime() { return restoreTimeInSeconds; }

    public static RefillableContainer fromTileEntity(TileEntity tileEntity, UUID worldUUID)
    {
        TileEntityCarrier carrier = (TileEntityCarrier) tileEntity;
        List<ItemStack> items = new ArrayList<>();

        carrier.getInventory().slots().forEach(x->
        {
            if (x.peek().isPresent())
            {
                items.add(x.peek().get());
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
        if (this._containerLocation.equals(((RefillableContainer)obj).getContainerLocation()))
        {
            Inventory tempInventory = Inventory.builder().build(ChestRefill.getChestRefill());

            this.items.forEach(x-> {
                //Offer removes items from inventory so we need to build new temp items.
                ItemStack tempItemStack = ItemStack.builder().fromItemStack(x).build();
                tempInventory.offer(tempItemStack);
            });

            //Compare items
            for (ItemStack comparedItem : ((RefillableContainer) obj).getItems())
            {
                if (!tempInventory.contains(comparedItem))
                {
                    return false;
                }
            }

            //Compare restore time
            if (this.restoreTimeInSeconds == ((RefillableContainer)obj).getRestoreTime())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return _containerLocation.toString().length();
    }
}
