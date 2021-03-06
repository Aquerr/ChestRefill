package io.github.aquerr.chestrefill.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;

import java.util.ArrayList;
import java.util.List;

public class KitTypeSerializer implements TypeSerializer<Kit>
{
    @Nullable
    @Override
    public Kit deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        String name = "";
        List<RefillableItem> items;
        try
        {
            name = value.getNode("name").getString();
            items = value.getNode("items").getList(TypeToken.of(RefillableItem.class), new ArrayList<>());
        }
        catch (final Exception exception)
        {
            throw new ObjectMappingException("Could not deserialize the kit: " + name, exception);
        }
        return new Kit(name, items);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable Kit obj, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        if (obj == null)
            return;

        value.getNode("name").setValue(obj.getName());
        value.getNode("items").setValue(ChestRefillTypeSerializers.REFILLABLE_ITEM_LIST_TYPE_TOKEN, obj.getItems());
    }
}
