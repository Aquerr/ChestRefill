package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class DeselectCommand extends AbstractCommand
{
    public DeselectCommand(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
    {
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Only in-game players can use this command!"));

        final Player player = (Player) source;
        final SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(player.getUniqueId());
        if (selectionPoints != null)
        {
            selectionPoints.setFirstPoint(null);
            selectionPoints.setSecondPoint(null);
        }
        ChestRefill.PLAYER_SELECTION_POINTS.remove(player.getUniqueId());
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GREEN, "Selections points have been cleared out!")));
        return CommandResult.success();
    }
}
