package io.github.aquerr.chestrefill.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SearchAndCreateCommand extends AbstractCommand
{
    public SearchAndCreateCommand(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
    {
        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Only in-game players can use this command!"));

        final int restoreTime = args.<Integer>getOne(Text.of("restoreTime")).orElse(120);
        final String requiredPermission = args.<String>getOne(Text.of("requiredPermission")).orElse("");

        final Player player = (Player) source;
        final SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(player.getUniqueId());

        if (selectionPoints == null || selectionPoints.getFirstPoint() == null || selectionPoints.getSecondPoint() == null)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You need to select two points with wand before using this command! Use \"/cr wand\" to get the wand and select two corners with it."));

        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.YELLOW, "Searching for containers...")));
        //To not freeze game, we will run all calculations in separate thread.
        CompletableFuture.runAsync(() -> scanAndCreateRefillableContainers(player, selectionPoints, restoreTime, requiredPermission));
        return CommandResult.success();
    }

    private void scanAndCreateRefillableContainers(final Player player, final SelectionPoints selectionPoints, final int restoreTime, final String requiredPermission)
    {
        final Vector3i firstCorner = selectionPoints.getFirstPoint();
        final Vector3i secondCorner = selectionPoints.getSecondPoint();

        final World world = player.getWorld();
        final UUID worldUUID = world.getUniqueId();

        final int startX = Math.min(firstCorner.getX(), secondCorner.getX());
        final int startY = Math.min(firstCorner.getY(), secondCorner.getY());
        final int startZ = Math.min(firstCorner.getZ(), secondCorner.getZ());
        final int endX = Math.max(firstCorner.getX(), secondCorner.getX());
        final int endZ = Math.max(firstCorner.getZ(), secondCorner.getZ());
        final int endY = Math.max(firstCorner.getY(), secondCorner.getY());

        int createdCount = 0;
        final List<RefillableContainer> containerList = new ArrayList<>();

        for (int y = startY; y < endY; y++)
        {
            for (int x = startX; x < endX; x++)
            {
                for (int z = startZ; z < endZ; z++)
                {
                    final BlockState blockState = world.getBlock(x, y ,z);
                    final Optional<TileEntity> optionalTileEntity = world.getTileEntity(x, y, z);
                    if (!optionalTileEntity.isPresent())
                        continue;
                    if (blockState.getType() == BlockTypes.AIR)
                        continue;
                    final TileEntity tileEntity = optionalTileEntity.get();
                    if (!(tileEntity instanceof TileEntityCarrier))
                        continue;
                    final ContainerLocation containerLocation = new ContainerLocation(tileEntity.getLocatableBlock().getPosition(), player.getWorld().getUniqueId());
                    final Optional<RefillableContainer> optionalRefillableContainerAtLocation = super.getPlugin().getContainerManager().getRefillableContainerAtLocation(containerLocation);
                    if(optionalRefillableContainerAtLocation.isPresent())
                        continue;

                    final RefillableContainer refillableContainer = RefillableContainer.fromTileEntity(tileEntity, worldUUID);
                    containerList.add(refillableContainer);
                }
            }
        }

        //Register each container
        for (final RefillableContainer refillableContainer : containerList)
        {
            refillableContainer.setRestoreTime(restoreTime);
            refillableContainer.setRequiredPermission(requiredPermission);
            super.getPlugin().getContainerManager().addRefillableContainer(refillableContainer);
            createdCount++;
        }

        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GREEN, "Successfully created " + createdCount + " containers.")));
    }
}
