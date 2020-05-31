package io.github.aquerr.chestrefill.commands;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by Aquerr & vvozny on 2018-02-09.
 */

public class ListCommand extends AbstractCommand implements CommandExecutor
{
    public ListCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        List<Text> helpList = Lists.newArrayList();

        for(RefillableContainer refillableContainer : super.getPlugin().getContainerManager().getRefillableContainers())
        {
            Text.Builder itemsToShow = Text.builder();

            itemsToShow.append(Text.of(TextColors.GREEN, "Container's name: ", TextColors.YELLOW, refillableContainer.getName() + "\n"));
            itemsToShow.append(Text.of(TextColors.GREEN, "Items in inventory: " + "\n"));
            refillableContainer.getItems().forEach(x-> itemsToShow.append(Text.of(TextColors.YELLOW, x.getItem().getType().getName(), TextColors.RESET, " x" + x.getItem().getCount() + "\n")));

            itemsToShow.append(Text.of("\n", TextColors.GREEN, "Kit: ", TextColors.WHITE, refillableContainer.getKitName(), "\n"));
            itemsToShow.append(Text.of(TextColors.GREEN, "One item at time: ", TextColors.WHITE,  refillableContainer.isOneItemAtTime(), "\n"));
            itemsToShow.append(Text.of(TextColors.GREEN, "Replace existing items: ", TextColors.WHITE, refillableContainer.shouldReplaceExistingItems(), "\n"));
            itemsToShow.append(Text.of(TextColors.GREEN, "Hidden if no items: ", TextColors.WHITE, refillableContainer.shouldBeHiddenIfNoItems(), "\n"));
            itemsToShow.append(Text.of(TextColors.GREEN, "Hiding block: ", TextColors.WHITE, refillableContainer.getHidingBlock(), "\n"));
            itemsToShow.append(Text.of(TextColors.GREEN, "Permission: ", TextColors.WHITE, refillableContainer.getRequiredPermission(), "\n"));
            itemsToShow.append(Text.of("\n", TextColors.BLUE, TextStyles.BOLD, "Container cooldown: ", refillableContainer.getRestoreTime(),"s"));
            itemsToShow.append(Text.of("\n", TextColors.RED, TextStyles.ITALIC, "Click to teleport..."));

            Text.Builder chestName = Text.builder();
            if(refillableContainer.getName().equals(""))
                chestName.append(Text.of("Not named container"));
            else
                chestName.append(Text.of("Container ", TextColors.YELLOW, refillableContainer.getName()));

            Text chestText = Text.builder()
                    .append(Text.of(TextColors.YELLOW, " - ", TextColors.DARK_GREEN, chestName.build(), " at location ", TextColors.YELLOW, refillableContainer.getContainerLocation().getBlockPosition().toString()))
                    .onHover(TextActions.showText(itemsToShow.build()))
                    .onClick(TextActions.executeCallback(new ChestTeleport(refillableContainer.getContainerLocation().getBlockPosition())))
                    .build();

            helpList.add(chestText);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GOLD, "List of Refilling Containers")).padding(Text.of(TextColors.DARK_GREEN, "-")).contents(helpList).linesPerPage(10);
        paginationBuilder.sendTo(source);


        return CommandResult.success();
    }

    private static class ChestTeleport implements Consumer<CommandSource>
    {
        private final Vector3i chestPosition;

        ChestTeleport(Vector3i chestPosition)
        {
            this.chestPosition = chestPosition;
        }

        @Override
        public void accept(CommandSource source)
        {
            //Do we need this check? Only in-game players can click on the chat...
            if (source instanceof Player)
            {
                final Player player = (Player)source;
                player.setLocation(new Location<>(player.getWorld(), this.chestPosition));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "You were teleported to the selected container!"));
            }
        }
    }
}
