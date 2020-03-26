package io.github.aquerr.chestrefill.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStack;

public class RefillableItemTypeSerializer implements TypeSerializer<RefillableItem>
{
    @Nullable
    @Override
    public RefillableItem deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        final float chance = value.getNode("change").getFloat(1f);
        final int slot = value.getNode("slot").getInt();
        final ItemStack item = value.getNode("item").getValue(TypeToken.of(ItemStack.class));
        return new RefillableItem(item, slot, chance);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable RefillableItem obj, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        if (obj == null)
            return;

        value.getNode("chance").setValue(obj.getChance());
        value.getNode("slot").setValue(obj.getSlot());
        value.getNode("item").setValue(TypeToken.of(ItemStack.class), obj.getItem());
    }
}
