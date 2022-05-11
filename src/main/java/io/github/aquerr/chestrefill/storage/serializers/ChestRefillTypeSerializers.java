package io.github.aquerr.chestrefill.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;

import java.util.List;

public class ChestRefillTypeSerializers
{
    public static TypeSerializerCollection TYPE_SERIALIZER_COLLECTION = TypeSerializerCollection.defaults().newChild();

    static {
        TYPE_SERIALIZER_COLLECTION.register(TypeToken.of(RefillableItem.class), new RefillableItemTypeSerializer());
        TYPE_SERIALIZER_COLLECTION.register(TypeToken.of(Kit.class), new KitTypeSerializer());
        TYPE_SERIALIZER_COLLECTION.register(new TypeToken<List<RefillableItem>>(){}, new RefillableItemListTypeSerializer());
    }

    public static final TypeToken<Kit> KIT_TYPE_TOKEN = TypeToken.of(Kit.class);
    public static final TypeToken<RefillableItem> REFILLABLE_ITEM_TYPE_TOKEN = TypeToken.of(RefillableItem.class);
    public static final TypeToken<List<Kit>> KIT_LIST_TYPE_TOKEN = new TypeToken<List<Kit>>(){};
    public static final TypeToken<List<RefillableItem>> REFILLABLE_ITEM_LIST_TYPE_TOKEN = new TypeToken<List<RefillableItem>>(){};
}
