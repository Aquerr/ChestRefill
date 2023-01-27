package io.github.aquerr.chestrefill.storage.serializers;

import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class KitTypeSerializer implements TypeSerializer<Kit>
{
    @Override
    public Kit deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        String name = "";
        List<RefillableItem> items;
        try
        {
            name = node.node("name").getString();
            items = node.node("items").getList(TypeToken.get(RefillableItem.class), new ArrayList<>());
        }
        catch (final Exception exception)
        {
            throw new SerializationException("Could not deserialize the kit: " + name);
        }
        return new Kit(name, items);
    }

    @Override
    public void serialize(Type type, @Nullable Kit obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("name").set(obj.getName());
        node.node("items").set(ChestRefillTypeSerializers.REFILLABLE_ITEM_LIST_TYPE_TOKEN, obj.getItems());
    }
}
