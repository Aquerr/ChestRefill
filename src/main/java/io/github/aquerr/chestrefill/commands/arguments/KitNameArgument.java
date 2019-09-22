package io.github.aquerr.chestrefill.commands.arguments;

import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitNameArgument extends CommandElement
{
	public KitNameArgument(Text key)
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
		final List<String> kits = new ArrayList<>(ChestRefill.getInstance().getContainerManager().getKits().keySet());
		if (args.hasNext())
		{
			String charSequence = args.nextIfPresent().get();
			return kits.stream().filter(x->x.contains(charSequence)).collect(Collectors.toList());
		}

		return kits;
	}
}
