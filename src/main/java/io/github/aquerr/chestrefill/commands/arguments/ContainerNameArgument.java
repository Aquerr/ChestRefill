package io.github.aquerr.chestrefill.commands.arguments;

import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class ContainerNameArgument extends CommandElement
{
    public ContainerNameArgument(Text key)
    {
        super(key);
    }

    @Nullable
    @Override
    protected String parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException
    {
        if(args.hasNext())
        {
            return args.next();
        }

        return null;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context)
    {
        List<RefillableContainer> refillableContainers = ContainerManager.getRefillableContainers();
        List<String> refillableContainersNames = refillableContainers.stream().map(RefillableContainer::getName).collect(Collectors.toList());

        if (args.hasNext())
        {
            String charSequence = args.nextIfPresent().get();
            return refillableContainersNames.stream().filter(x->x.contains(charSequence)).collect(Collectors.toList());
        }

        return refillableContainersNames;
    }
}
