package io.github.aquerr.chestrefill.commands.arguments;

import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import org.spongepowered.api.command.parameter.Parameter;

public class ChestRefillCommandParameters
{
    private static Parameter.Value<RefillableContainer> REFILLABLE_CONTAINER;
    private static Parameter.Value<Kit> KIT;

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
    }

    public static Parameter.Value<RefillableContainer> refillableContainer()
    {
        return REFILLABLE_CONTAINER;
    }

    public static Parameter.Value<Kit> kit()
    {
        return KIT;
    }
}
