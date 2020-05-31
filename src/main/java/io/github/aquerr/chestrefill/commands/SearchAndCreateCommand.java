package io.github.aquerr.chestrefill.commands;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

        final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        final List<RefillableContainer> refillableContainers = forkJoinPool.invoke(new ScanForContainerTask(player.getWorld(), firstCorner, secondCorner));
        int createdCount = 0;

        //Register each container
        for (final RefillableContainer refillableContainer : refillableContainers)
        {
            refillableContainer.setRestoreTime(restoreTime);
            refillableContainer.setRequiredPermission(requiredPermission);
            super.getPlugin().getContainerManager().addRefillableContainer(refillableContainer);
            createdCount++;
        }

        player.sendMessage(PluginInfo.PLUGIN_PREFIX.concat(Text.of(TextColors.GREEN, "Successfully created " + createdCount + " containers.")));
    }

    public static class ScanForContainerTask extends RecursiveTask<List<RefillableContainer>>
    {
        private final World world;

        private final int startX;
        private final int startY;
        private final int startZ;

        private final int endX;
        private final int endY;
        private final int endZ;

        private final int lengthX;
        private final int lengthY;
        private final int lengthZ;

        private static final int THRESHOLD = 500;

        public ScanForContainerTask(final World world, final Vector3i firstCorner, final Vector3i secondCorner)
        {
            this.world = world;

            startX = Math.min(firstCorner.getX(), secondCorner.getX());
            startY = Math.min(firstCorner.getY(), secondCorner.getY());
            startZ = Math.min(firstCorner.getZ(), secondCorner.getZ());

            endX = Math.max(firstCorner.getX(), secondCorner.getX());
            endZ = Math.max(firstCorner.getZ(), secondCorner.getZ());
            endY = Math.max(firstCorner.getY(), secondCorner.getY());
            lengthX = endX - startX;
            lengthY = endY - startY;
            lengthZ = endZ - startZ;
        }

        @Override
        protected List<RefillableContainer> compute()
        {
            if (getBlockCountToScan() > THRESHOLD)
            {
                return ForkJoinTask.invokeAll(createSubTasks()).stream().map(ForkJoinTask::join).flatMap(Collection::stream).collect(Collectors.toList());
            }
            else return process();
        }

        private Collection<ScanForContainerTask> createSubTasks()
        {
            final List<ScanForContainerTask> tasks = new ArrayList<>();

            final Vector3i firstCorner = Vector3i.from(startX, startY, startZ);
            final Vector3i secondCorner = Vector3i.from(startX + (lengthX / 2), endY, startZ + (lengthZ / 2) - 1);
            tasks.add(new ScanForContainerTask(world, firstCorner,  secondCorner));

            final Vector3i firstCorner1 = Vector3i.from(endX - (lengthX / 2) + 1, startY, endZ - (lengthZ / 2));
            final Vector3i secondCorner1 = Vector3i.from(endX, endY, endZ);
            tasks.add(new ScanForContainerTask(world, firstCorner1, secondCorner1));

            final Vector3i firstCorner2 = Vector3i.from(startX + (lengthX / 2) + 1, startY, startZ + (lengthZ / 2) - 1);
            final Vector3i secondCorner2 = Vector3i.from(endX, endY, startZ);
            tasks.add(new ScanForContainerTask(world, firstCorner2, secondCorner2));

            final Vector3i firstCorner3 = Vector3i.from(endX - (lengthX / 2), startY, endZ - (lengthZ / 2));
            final Vector3i secondCorner3 = Vector3i.from(startX, endY, endZ);
            tasks.add(new ScanForContainerTask(world, firstCorner3, secondCorner3));

            return tasks;
        }

        private List<RefillableContainer> process()
        {
            final List<RefillableContainer> containerList = new ArrayList<>();
            final UUID worldUUID = world.getUniqueId();
            for (int y = startY; y <= endY; y++)
            {
                for (int x = startX; x <= endX; x++)
                {
                    for (int z = startZ; z <= endZ; z++)
                    {
                        world.spawnParticles(ParticleEffect.builder().quantity(10).type(ParticleTypes.END_ROD).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).build(), Vector3d.from(x + 0.5, y, z + 0.5));
                        final Optional<TileEntity> optionalTileEntity = world.getTileEntity(x, y, z);
                        if (!optionalTileEntity.isPresent())
                            continue;
                        final TileEntity tileEntity = optionalTileEntity.get();
                        if (!(tileEntity instanceof TileEntityCarrier))
                            continue;

                        final ContainerLocation containerLocation = new ContainerLocation(tileEntity.getLocatableBlock().getPosition(), worldUUID);
                        final Optional<RefillableContainer> optionalRefillableContainerAtLocation = ChestRefill.getInstance().getContainerManager().getRefillableContainerAtLocation(containerLocation);
                        if(optionalRefillableContainerAtLocation.isPresent())
                            continue;

                        final RefillableContainer refillableContainer = RefillableContainer.fromTileEntity(tileEntity, worldUUID);
                        containerList.add(refillableContainer);
                    }
                }
            }
            return containerList;
        }

        private int getBlockCountToScan()
        {
            return lengthX * lengthY * lengthZ;
        }
    }
}
