package io.github.aquerr.chestrefill.commands.arguments;

import com.google.common.base.Strings;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RefillableContainerArgument
{
    private RefillableContainerArgument()
    {

    }

    public static class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<RefillableContainer>
    {
        private final ContainerManager containerManager;

        public ValueParser(ContainerManager containerManager)
        {
            this.containerManager = containerManager;
        }

        @Override
        public Optional<RefillableContainer> parseValue(Parameter.Key<? super RefillableContainer> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
        {
            if(!reader.canRead())
                throw reader.createException(Component.text("Argument is not a valid container name!"));
            final String containerName = reader.parseUnquotedString();
            if (Strings.isNullOrEmpty(containerName))
                throw reader.createException(Component.text("Argument is not a valid container name!"));
            final RefillableContainer refillableContainer = this.containerManager.getRefillableContainer(containerName)
                    .orElseThrow(() -> reader.createException(Component.text("Argument is not a valid container name!")));
            return Optional.ofNullable(refillableContainer);
        }
    }

    public static class Completer implements ValueCompleter
    {
        private final ContainerManager containerManager;

        public Completer(ContainerManager containerManager)
        {
            this.containerManager = containerManager;
        }

        @Override
        public List<CommandCompletion> complete(CommandContext context, String currentInput)
        {
            Collection<RefillableContainer> refillableContainers = this.containerManager.getRefillableContainers();
            List<String> refillableContainersNames = refillableContainers.stream()
                    .map(RefillableContainer::getName)
                    .sorted()
                    .collect(Collectors.toList());

            String charSequence = currentInput.toLowerCase();

            return refillableContainersNames.stream()
                    .filter(name -> name.contains(charSequence))
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        }
    }
}
