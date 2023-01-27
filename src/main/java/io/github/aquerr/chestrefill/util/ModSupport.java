package io.github.aquerr.chestrefill.util;

import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.item.inventory.Inventory;

import java.lang.reflect.Field;

public final class ModSupport
{
    private ModSupport()
    {

    }

    public static boolean isStorageUnitFromActuallyAdditions(final BlockEntity blockEntity)
    {
        final Class clazz = blockEntity.getClass();
        return clazz.getName().contains("actuallyadditions") && clazz.getName().contains("GiantChest");
    }

    public static Inventory getInventoryFromActuallyAdditions(final BlockEntity blockEntity)
    {
        final Class clazz = blockEntity.getClass();
        try
        {
            final Field invField = clazz.getField("inv");
            final Object invObject = invField.get(blockEntity);
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
