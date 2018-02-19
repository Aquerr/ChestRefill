package io.github.aquerr.chestrefill.commands;

import com.google.common.collect.Lists;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import io.github.aquerr.chestrefill.managers.ChestManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class ListCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        List<Text> helpList = Lists.newArrayList();


        for(RefillingChest refillingChest : ChestManager.getChests())
        {
            Text chestText = Text.builder()
                    .append(Text.of(TextColors.YELLOW, "Chest at " + refillingChest.getChestLocation().getBlockPosition().toString()))
                    .build();

            helpList.add(chestText);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GOLD, "List of Refilling Chests")).padding(Text.of(TextColors.DARK_GREEN, "-")).contents(helpList).linesPerPage(10);
        paginationBuilder.sendTo(source);


        return CommandResult.success();
    }
}
