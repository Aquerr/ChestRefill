package io.github.aquerr.chestrefill.util;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class LootTableHelper
{
    private static final Pattern LOOT_TABLE_NAME_PATTERN = Pattern.compile("^([^/:;'\\]\\[,.#$%&*()!?@^]*):(.*)$");

    private final LootTableLoader lootTableLoader;

    public LootTableHelper(ChestRefill plugin)
    {
        this.lootTableLoader = new LootTableLoader(plugin.getConfigDir());
    }

    public List<RefillableItem> getItemsFromLootTable(String lootTableName, ServerWorld serverWorld)
    {
        ResourceLocation resourceLocation = new ResourceLocation(lootTableName);
        if (!LOOT_TABLE_NAME_PATTERN.matcher(lootTableName).matches() || !(serverWorld instanceof net.minecraft.world.server.ServerWorld))
            return Collections.emptyList();

        LootTable lootTable = ServerLifecycleHooks.getCurrentServer().getLootTables().get(resourceLocation);
        if (lootTable != LootTable.EMPTY)
        {
            return getItemsFromLootTable(lootTable, (net.minecraft.world.server.ServerWorld)serverWorld);
        }
        else
        {
            return getItemsFromChestRefillLootTable(resourceLocation, (net.minecraft.world.server.ServerWorld)serverWorld);
        }
    }

    private List<RefillableItem> getItemsFromLootTable(LootTable lootTable, net.minecraft.world.server.ServerWorld serverWorld)
    {
        List<ItemStack> itemStacks = lootTable.getRandomItems(new LootContext.Builder(serverWorld).create(new LootParameterSet.Builder().build()));

        List<RefillableItem> refillableItems = new ArrayList<>();
        int slot = 0;
        for (final ItemStack itemStack : itemStacks)
        {
            org.spongepowered.api.item.inventory.ItemStack spongeItemStack = org.spongepowered.api.item.inventory.ItemStack.class.cast(itemStack);
            refillableItems.add(new RefillableItem(spongeItemStack.createSnapshot(), slot++, 1f));
        }
        return refillableItems;
    }

    private List<RefillableItem> getItemsFromChestRefillLootTable(ResourceLocation resourceLocation, net.minecraft.world.server.ServerWorld serverWorld)
    {
        String path = resourceLocation.getPath();
        LootTable lootTable = lootTableLoader.loadLootTable(path);
        if (lootTable == null)
            return Collections.emptyList();

        return getItemsFromLootTable(lootTable, serverWorld);
    }

    public Collection<String> getAllChestRefillLootTablesNames()
    {
        return this.lootTableLoader.findAllLootTablesNames();
    }
}
