package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class SetHidingBlockCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public SetHidingBlockCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final BlockState blockState = context.requireOne(Parameter.blockState().key("hiding_block").build());
        final BlockType blockType = blockState.type();
        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(blockType), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.hidingblock.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.hidingblock.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams(BlockType blockType)
    {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("HIDING_BLOCK", blockType);
        return new SelectionParams(SelectionMode.HIDING_BLOCK, this::setHidingBlock, extraData);
    }

    private void setHidingBlock(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final BlockType blockType = (BlockType) params.getExtraData().get("HIDING_BLOCK");
        RefillableContainer refillableContainer = params.getRefillableContainerAtLocation();
        refillableContainer.setHidingBlock(blockType);
        final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);
        if(didSucceed)
        {
            player.sendMessage(messageSource.resolveMessageWithPrefix("command.successful-refillable-container-update"));
        }
        else
        {
            player.sendMessage(messageSource.resolveMessageWithPrefix("error.command.something-went-wrong"));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
