package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class SearchAndCreateCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public SearchAndCreateCommand(final ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer serverPlayer = requirePlayerSource(context);
        final int restoreTime = context.one(Parameter.integerNumber().key("restore_time").build()).orElse(120);
        final String requiredPermission = context.one(Parameter.string().key("required_permission").build()).orElse("");

        final SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(serverPlayer.uniqueId());

        if (selectionPoints == null || selectionPoints.getFirstPoint() == null || selectionPoints.getSecondPoint() == null)
            throw messageSource.resolveExceptionWithMessage("command.searchandcreate.error.you-need-to-select-two-points");

        serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.searchandcreate.searching"));

        //To not freeze game, we will run all calculations in separate thread.
        CompletableFuture.runAsync(() -> scanAndCreateRefillableContainers(serverPlayer, selectionPoints, restoreTime, requiredPermission));
        return CommandResult.success();
    }

    private void scanAndCreateRefillableContainers(final ServerPlayer player, final SelectionPoints selectionPoints, final int restoreTime, final String requiredPermission)
    {
        final Vector3i firstCorner = selectionPoints.getFirstPoint();
        final Vector3i secondCorner = selectionPoints.getSecondPoint();

        final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        final List<RefillableContainer> refillableContainers = forkJoinPool.invoke(new ScanForContainerTask(player.world(), firstCorner, secondCorner));
        int createdCount = 0;

        //Register each container
        for (final RefillableContainer refillableContainer : refillableContainers)
        {
            refillableContainer.setRestoreTime(restoreTime);
            refillableContainer.setRequiredPermission(requiredPermission);
            super.getPlugin().getContainerManager().addRefillableContainer(refillableContainer);
            createdCount++;
        }

        player.sendMessage(messageSource.resolveMessageWithPrefix("command.searchandcreate.success", createdCount));
    }

    public static class ScanForContainerTask extends RecursiveTask<List<RefillableContainer>>
    {
        private final ServerWorld world;

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

        public ScanForContainerTask(final ServerWorld world, final Vector3i firstCorner, final Vector3i secondCorner)
        {
            this.world = world;

            startX = Math.min(firstCorner.x(), secondCorner.x());
            startY = Math.min(firstCorner.y(), secondCorner.y());
            startZ = Math.min(firstCorner.z(), secondCorner.z());

            endX = Math.max(firstCorner.x(), secondCorner.x());
            endZ = Math.max(firstCorner.z(), secondCorner.z());
            endY = Math.max(firstCorner.y(), secondCorner.y());
            lengthX = endX - startX;
            lengthY = endY - startY;
            lengthZ = endZ - startZ;
        }

        @Override
        protected List<RefillableContainer> compute()
        {
            if (getBlockCountToScan() > THRESHOLD)
            {
                return ForkJoinTask.invokeAll(createSubTasks()).stream()
                        .map(ForkJoinTask::join)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
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
            final UUID worldUUID = world.uniqueId();
            for (int y = startY; y <= endY; y++)
            {
                for (int x = startX; x <= endX; x++)
                {
                    for (int z = startZ; z <= endZ; z++)
                    {
                        world.spawnParticles(ParticleEffect.builder().quantity(10).type(ParticleTypes.END_ROD).option(ParticleOptions.VELOCITY, new Vector3d(0, 0.15, 0)).build(), Vector3d.from(x + 0.5, y, z + 0.5));
                        final Optional<? extends BlockEntity> optionalBlockEntity = world.blockEntity(x, y, z);
                        if (!optionalBlockEntity.isPresent())
                            continue;
                        final BlockEntity blockEntity = optionalBlockEntity.get();
                        if (!(blockEntity instanceof CarrierBlockEntity))
                            continue;

                        final ContainerLocation containerLocation = new ContainerLocation(blockEntity.locatableBlock().blockPosition(), worldUUID);
                        final Optional<RefillableContainer> optionalRefillableContainerAtLocation = ChestRefill.getInstance().getContainerManager().getRefillableContainerAtLocation(containerLocation);
                        if(optionalRefillableContainerAtLocation.isPresent())
                            continue;

                        final RefillableContainer refillableContainer = RefillableContainer.fromBlockEntity((CarrierBlockEntity) blockEntity, worldUUID);
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
