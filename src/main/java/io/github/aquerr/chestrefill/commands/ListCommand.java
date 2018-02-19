package io.github.aquerr.chestrefill.commands;

import com.flowpowered.math.vector.Vector3i;
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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.function.Consumer;

public class ListCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        List<Text> helpList = Lists.newArrayList();

        for(RefillingChest refillingChest : ChestManager.getChests())
        {
            Text.Builder itemsToShow = Text.builder();

            itemsToShow.append(Text.of(TextColors.GREEN, "Items in chest: " + "\n"));
            refillingChest.getItems().forEach(x-> itemsToShow.append(Text.of(TextColors.YELLOW, x.getType().getName(), TextColors.RESET, " x" + x.getQuantity() + "\n")));
            itemsToShow.append(Text.of("\n", TextColors.RED, TextStyles.ITALIC, "Click to teleport..."));

            Text chestText = Text.builder()
                    .append(Text.of(TextColors.DARK_GREEN, "Chest at ", TextColors.YELLOW, refillingChest.getChestLocation().getBlockPosition().toString()))
                    .onHover(TextActions.showText(itemsToShow.build()))
                    .onClick(TextActions.executeCallback(teleportToChest(source, refillingChest.getChestLocation().getBlockPosition())))
                    .build();

            helpList.add(chestText);
        }

        PaginationService paginationService = Sponge.getServiceManager().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder().title(Text.of(TextColors.GOLD, "List of Refilling Chests")).padding(Text.of(TextColors.DARK_GREEN, "-")).contents(helpList).linesPerPage(10);
        paginationBuilder.sendTo(source);


        return CommandResult.success();
    }

    private Consumer<CommandSource> teleportToChest(CommandSource source, Vector3i blockPosition)
    {
        return consumer ->
        {
            //Do we need this check? Only in-game players can click on the chat...
            if (source instanceof Player)
            {
                Player player = (Player)source;

                player.setLocation(new Location<World>(player.getWorld(), blockPosition));
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "You were teleported to the chosen chest!"));
            }
        };
    }
}
