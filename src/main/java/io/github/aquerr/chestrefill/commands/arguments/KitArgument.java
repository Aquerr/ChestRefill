package io.github.aquerr.chestrefill.commands.arguments;

import com.google.common.base.Strings;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class KitArgument
{
	private KitArgument()
	{

	}

	public static final class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<Kit>
	{
		private final ContainerManager containerManager;

		public ValueParser(ContainerManager containerManager)
		{
			this.containerManager = containerManager;
		}


		@Override
		public Optional<? extends Kit> parseValue(Parameter.Key<? super Kit> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
		{
			if(!reader.canRead())
				throw reader.createException(Component.text("Argument is not a valid kit"));
			final String kitName = reader.parseUnquotedString();
			if (Strings.isNullOrEmpty(kitName))
				throw reader.createException(Component.text("Argument is not a valid kit"));
			final Kit kit = this.containerManager.getKit(kitName);
			return Optional.ofNullable(kit);
		}
	}

	public static final class Completer implements ValueCompleter
	{
		private final ContainerManager containerManager;

		public Completer(final ContainerManager containerManager)
		{
			this.containerManager = containerManager;
		}

		@Override
		public List<CommandCompletion> complete(CommandContext context, String currentInput)
		{
			final List<String> kits = new ArrayList<>(this.containerManager.getKits().keySet());

			String charSequence = currentInput.toLowerCase();

			return kits.stream()
					.map(String::toLowerCase)
					.filter(kitName -> kitName.contains(charSequence))
					.map(CommandCompletion::of)
					.collect(Collectors.toList());
		}
	}
}
