package io.github.aquerr.chestrefill.entities;

import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class RefillableTileEntity
{
    private TileEntityLocation _tileEntityLocation;
    private List<ItemStack> items;
    private int restoreTimeInSeconds;

    public RefillableTileEntity(TileEntityLocation tileEntityLocation, List<ItemStack> itemsList)
    {
        this._tileEntityLocation = tileEntityLocation;
        this.items = itemsList;
        this.restoreTimeInSeconds = 120; //Default: 120 sec
    }

    public RefillableTileEntity(TileEntityLocation tileEntityLocation, List<ItemStack> itemsList, int time)
    {
        this._tileEntityLocation = tileEntityLocation;
        this.items = itemsList;
        this.restoreTimeInSeconds = time;
    }

    public TileEntityLocation getTileEntityLocation()
    {
        return _tileEntityLocation;
    }

    public List<ItemStack> getItems()
    {
        return items;
    }

    public int getRestoreTime() { return restoreTimeInSeconds; }

    public static RefillableTileEntity fromTileEntity(TileEntity tileEntity, UUID worldUUID)
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

        RefillableTileEntity refillableTileEntity = new RefillableTileEntity(new TileEntityLocation(tileEntity.getLocation().getBlockPosition(), worldUUID), items);

        return refillableTileEntity;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RefillableTileEntity))
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }


        //TODO: Refactor this code if it will be possible...
        //Compare chest location
        if (this._tileEntityLocation.equals(((RefillableTileEntity)obj).getTileEntityLocation()))
        {
            Inventory tempInventory = Inventory.builder().build(ChestRefill.getChestRefill());

            this.items.forEach(x-> {
                //Offer removes items from inventory so we need to build new temp items.
                ItemStack tempItemStack = ItemStack.builder().fromItemStack(x).build();
                tempInventory.offer(tempItemStack);
            });

            //Compare items
            for (ItemStack comparedItem : ((RefillableTileEntity) obj).getItems())
            {
                if (!tempInventory.contains(comparedItem))
                {
                    return false;
                }
            }

            //Compare restore time
            if (this.restoreTimeInSeconds == ((RefillableTileEntity)obj).getRestoreTime())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return _tileEntityLocation.toString().length();
    }
}
