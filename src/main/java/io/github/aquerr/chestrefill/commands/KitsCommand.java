package io.github.aquerr.chestrefill.commands;

import com.google.common.collect.Lists;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.Kit;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class KitsCommand extends AbstractCommand implements CommandExecutor
{
    public KitsCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        List<Text> helpList = Lists.newArrayList();

        for(Kit kit : super.getPlugin().getContainerManager().getKits().values())
        {
            Text.Builder itemsToShow = Text.builder();

            itemsToShow.append(Text.of(TextColors.GREEN, "Kit's name: ", TextColors.YELLOW, kit.getName() + "\n"));
            itemsToShow.append(Text.of(TextColors.GREEN, "Items in kit: " + "\n"));
            kit.getItems().forEach(x -> itemsToShow.append(Text.of(TextColors.YELLOW, x.getItem().getType().getTranslation().get(), TextColors.RESET, " x" + x.getItem().getCount() + "\n")));

            Text kitText = Text.builder()
                    .append(Text.of(TextColors.YELLOW, " - ", TextColors.YELLOW, kit.getName()))
                    .onHover(TextActions.showText(itemsToShow.build()))
                    .build();

            helpList.add(kitText);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GOLD, "List of Kits")).padding(Text.of(TextColors.DARK_GREEN, "-")).contents(helpList).linesPerPage(10);
        paginationBuilder.sendTo(source);


        return CommandResult.success();
    }
}
