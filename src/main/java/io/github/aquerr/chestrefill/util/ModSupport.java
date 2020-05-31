package io.github.aquerr.chestrefill.util;

import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.item.inventory.Inventory;

import java.lang.reflect.Field;

public class ModSupport
{
    public static boolean isStorageUnitFromActuallyAdditions(final TileEntity tileEntity)
    {
        final Class clazz = tileEntity.getClass();
        return clazz.getName().contains("actuallyadditions") && clazz.getName().contains("GiantChest");
    }

    public static Inventory getInventoryFromActuallyAdditions(final TileEntity tileEntity)
    {
        final Class clazz = tileEntity.getClass();
        try
        {
            final Field invField = clazz.getField("inv");
            final Object invObject = invField.get(tileEntity);
            return (Inventory) invObject;
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
            return null;
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
