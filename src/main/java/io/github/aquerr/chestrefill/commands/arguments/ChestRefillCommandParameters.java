package io.github.aquerr.chestrefill.commands.arguments;

import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import org.spongepowered.api.command.parameter.Parameter;

public final class ChestRefillCommandParameters
{
    private ChestRefillCommandParameters()
    {
        throw new UnsupportedOperationException();
    }

    private static Parameter.Value<RefillableContainer> REFILLABLE_CONTAINER;
    private static Parameter.Value<Kit> KIT;

    private static Parameter.Value<String> LOOT_TABLE;

    public static void init(ContainerManager containerManager)
    {
        REFILLABLE_CONTAINER = Parameter.builder(RefillableContainer.class)
                .key("container")
                .addParser(new RefillableContainerArgument.ValueParser(containerManager))
                .completer(new RefillableContainerArgument.Completer(containerManager))
                .build();

        KIT = Parameter.builder(Kit.class)
                .key("kit")
                .addParser(new KitArgument.ValueParser(containerManager))
                .completer(new KitArgument.Completer(containerManager))
                .build();

        LOOT_TABLE = Parameter.builder(String.class)
                .key("loot_table")
                .addParser(new LootTableArgument.ValueParser(containerManager.getLootTableHelper()))
                .completer(new LootTableArgument.Completer(containerManager.getLootTableHelper()))
                .build();
    }

    public static Parameter.Value<RefillableContainer> refillableContainer()
    {
        return REFILLABLE_CONTAINER;
    }

    public static Parameter.Value<Kit> kit()
    {
        return KIT;
    }

    public static Parameter.Value<String> lootTable()
    {
        return LOOT_TABLE;
    }
}
