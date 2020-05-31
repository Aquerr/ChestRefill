package io.github.aquerr.chestrefill.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableItem;

import java.util.List;

public class ChestRefillTypeSerializers
{
    public static final TypeToken<Kit> KIT_TYPE_TOKEN = TypeToken.of(Kit.class);
    public static final TypeToken<RefillableItem> REFILLABLE_ITEM_TYPE_TOKEN = TypeToken.of(RefillableItem.class);
    public static final TypeToken<List<Kit>> KIT_LIST_TYPE_TOKEN = new TypeToken<List<Kit>>(){};
    public static final TypeToken<List<RefillableItem>> REFILLABLE_ITEM_LIST_TYPE_TOKEN = new TypeToken<List<RefillableItem>>(){};
}
