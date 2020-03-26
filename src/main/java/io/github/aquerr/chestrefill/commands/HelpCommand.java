package io.github.aquerr.chestrefill.commands;

import com.google.common.collect.Lists;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Map;

/**
 * Created by Aquerr on 2018-02-10.
 */

public class HelpCommand extends AbstractCommand implements CommandExecutor
{
    public HelpCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Map<List<String>, CommandSpec> commands = ChestRefill.SUBCOMMANDS;
        List<Text> helpList = Lists.newArrayList();

        for (List<String> aliases: commands.keySet())
        {
            CommandSpec commandSpec = commands.get(aliases);

            //This code prevents displaying commands which player has no permissions for.

//            if(source instanceof Player)
//            {
//                Player player = (Player)source;
//
//                if(!commandSpec.testPermission(player))
//                {
//                    continue;
//                }
//            }

            Text commandHelp = Text.builder()
                    .append(Text.builder()
                            .append(Text.of(TextColors.GOLD, "/cr " + aliases.toString().replace("[","").replace("]","")))
                            .build())
                    .append(Text.builder()
                            .append(Text.of(TextColors.WHITE, " - " + commandSpec.getShortDescription(source).get().toPlain() + "\n"))
                            .build())
                    .append(Text.builder()
                            .append(Text.of(TextColors.GRAY, "Usage: /cr " + aliases.toString().replace("[","").replace("]","") + " " + commandSpec.getUsage(source).toPlain()))
                            .build())
                    .build();

            helpList.add(commandHelp);
        }

        //Sort commands alphabetically.
        helpList.sort(Text::compareTo);

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GOLD, "Chest Refill v" + PluginInfo.VERSION)).padding(Text.of(TextColors.DARK_GREEN, "-")).contents(helpList).linesPerPage(14);
        paginationBuilder.sendTo(source);

        return CommandResult.success();
    }
}
