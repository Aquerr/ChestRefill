package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.ItemProvider;
import io.github.aquerr.chestrefill.entities.ItemProviderType;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.scheduling.ScanForEmptyContainersTask;
import io.github.aquerr.chestrefill.storage.StorageHelper;
import io.github.aquerr.chestrefill.util.LootTableHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class ContainerManager
{
    private final ChestRefill plugin;
    private final StorageHelper storageHelper;

    private final ContainerRefiller containerRefiller;
    private final LootTableHelper lootTableHelper;

    public ContainerManager(ChestRefill plugin, Path configDir, LootTableHelper lootTableHelper)
    {
        this.plugin = plugin;

        if (!Files.isDirectory(configDir))
        {
            try
            {
                Files.createDirectory(configDir);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        storageHelper = new StorageHelper(configDir);
        this.lootTableHelper = lootTableHelper;
        this.containerRefiller = new ContainerRefiller(plugin, this, lootTableHelper);
    }

    public boolean addRefillableContainer(RefillableContainer refillableContainer)
    {
        if (storageHelper.addOrUpdateContainer(refillableContainer))
        {
            return startRefillingContainer(refillableContainer.getContainerLocation(), refillableContainer.getRestoreTime());
        }

        return false;
    }

    public boolean updateRefillableContainer(RefillableContainer refillableContainer)
    {
        //We do not need to restart the refill scheduler task. New chest content will be loaded from the storage by existing scheduler.
        return storageHelper.addOrUpdateContainer(refillableContainer);
    }

    public Optional<RefillableContainer> getRefillableContainer(String containerName)
    {
        return getRefillableContainers().stream()
                .filter(refillableContainer -> refillableContainer.getName().equals(containerName))
                .findFirst();
    }

    public Collection<RefillableContainer> getRefillableContainers()
    {
        return storageHelper.getRefillableContainers();
    }

    public Set<ContainerLocation> getContainerLocations()
    {
        return storageHelper.getContainerLocations();
    }

    public boolean removeRefillableContainer(ContainerLocation containerLocation)
    {
        final boolean successfullyStopped = stopRefillingContainer(containerLocation);
        final boolean successfullyRemoved = storageHelper.removeContainer(containerLocation);

        return successfullyStopped && successfullyRemoved;
    }

    private boolean stopRefillingContainer(ContainerLocation containerLocation)
    {
        try
        {
            this.plugin.getContainerScheduler().cancelTask("Chest Refill " + containerLocation.getBlockPosition().toString()
                    + "|" + containerLocation.getWorldUUID().toString());
            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return false;
    }

    @Nullable
    private RefillableContainer getRefillableContainer(ContainerLocation containerLocation)
    {
        return storageHelper.getRefillableContainer(containerLocation);
    }

    private boolean startRefillingContainer(ContainerLocation containerLocation, int time)
    {
        try
        {
            String name = "Chest Refill " + containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID().toString();
            this.plugin.getContainerScheduler().scheduleWithInterval(name, time, TimeUnit.SECONDS, runRefillContainer(containerLocation));

//            final Optional<World> optionalWorld = Sponge.getServer().getWorld(containerLocation.getWorldUUID());
//            if(optionalWorld.isPresent())
//            {
//                this.plugin.getContainerScheduler().scheduleWithInterval(name + "_particle", 50, TimeUnit.MILLISECONDS, startParticleEffect(containerLocation, optionalWorld.get()));
//            }

            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

//    private Runnable startParticleEffect(final ContainerLocation containerLocation, final World world)
//    {
//        return new Runnable()
//        {
//            private World chestWorld = world;
//            private int maxHeight = 2;
//            private Vector3d chestLocation = new Vector3d(containerLocation.getBlockPosition().getX(), containerLocation.getBlockPosition().getY(), containerLocation.getBlockPosition().getZ());
//            private Vector3d lastParticleLocation = new Vector3d(chestLocation);
//
//            private final double d = Math.PI - 2 * Math.acos(0.1 / (2 * 6));
//            private final double l = (2 * Math.PI) / d;
//            private int i = 0;
//
//            @Override
//            public void run()
//            {
//                if(i > l)
//                    i = 0;
//
//                double x = chestLocation.getX() + 0.5;
//                double z = chestLocation.getZ() + 0.5;
//
//                double xOffset = 2 * Math.cos(i * d);
//                double zOffset = 2 * Math.sin(i * d);
//
//                double xOffset2 = 2 * Math.cos((i - 50) * d);
//                double zOffset2 = 2 * Math.sin((i - 50) * d);
//
//                final ParticleEffect.Builder particleEffectBuilder = ParticleEffect.builder();
//                particleEffectBuilder.type(ParticleTypes.REDSTONE_DUST)
//                        .option(ParticleOptions.COLOR, Color.CYAN)
//                        .quantity(10);
//                final ParticleEffect.Builder particleEffectBuilder2 = ParticleEffect.builder();
//                particleEffectBuilder2.type(ParticleTypes.REDSTONE_DUST)
//                        .option(ParticleOptions.COLOR, Color.RED)
//                        .quantity(10);
//                chestWorld.spawnParticles(particleEffectBuilder.build(), new Vector3d(x + xOffset, chestLocation.getY(), z + zOffset));
//                chestWorld.spawnParticles(particleEffectBuilder2.build(), new Vector3d(x + xOffset2, chestLocation.getY(), z + zOffset2));
//                i++;
//
////                double x = lastParticleLocation.getX();
////                double y = lastParticleLocation.getY();
////                double z = lastParticleLocation.getZ();
////
////                double newY = 0.5 * Math.sin(20*x) + chestLocation.getY() + 0.5;
////                double newX = x + 0.1;
////
////                if(newX > chestLocation.getX() + 1)
////                    newX = chestLocation.getX();
////
////                y = newY;
////                x = newX;
////
////                final Vector3d particleLocation = new Vector3d(x, y, z);
////                final ParticleEffect.Builder particleEffectBuilder = ParticleEffect.builder();
////                particleEffectBuilder.type(ParticleTypes.REDSTONE_DUST)
////                        .quantity(1)
////                        .offset(new Vector3d())
////                        .velocity(new Vector3d(0, 0, 0));
////                chestWorld.spawnParticles(particleEffectBuilder.build(), particleLocation);
////                lastParticleLocation = particleLocation;
//            }
//        };
//    }

    public boolean refillContainer(final ContainerLocation containerLocation)
    {
        final RefillableContainer refillableContainer = getRefillableContainer(containerLocation);
        if (refillableContainer == null)
        {
            this.plugin.getServer().sendMessage(linear(RED, text("Could not find container at location: " + containerLocation)));
            return false;
        }

        try
        {
            this.containerRefiller.refillContainer(refillableContainer);
            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            this.plugin.getServer().sendMessage(linear(RED, text("Couldn't refill : " + refillableContainer.getName())));
            this.plugin.getServer().sendMessage(linear(RED, text("Container block type : " + refillableContainer.getContainerBlockType())));
            this.plugin.getServer().sendMessage(linear(RED, text("Container block position : " + refillableContainer.getContainerLocation().getBlockPosition() + "|" + refillableContainer.getContainerLocation().getWorldUUID().toString())));
            this.plugin.getServer().sendMessage(linear(RED, text("Container items : " + refillableContainer.getItems())));
            this.plugin.getServer().sendMessage(linear(RED, text("Suggestion: Remove this container from the containers.json file and restart the server.")));
            return false;
        }
    }

    public Runnable runRefillContainer(ContainerLocation containerLocation)
    {
        return () -> refillContainer(containerLocation);
    }

    public void restoreRefilling()
    {
        for (RefillableContainer refillableContainer : getRefillableContainers())
        {
            startRefillingContainer(refillableContainer.getContainerLocation(), refillableContainer.getRestoreTime());
        }
    }

    public boolean updateRefillingTime(ContainerLocation containerLocation, int time)
    {
        if (stopRefillingContainer(containerLocation)
            && storageHelper.updateContainerTime(containerLocation, time)
            && startRefillingContainer(containerLocation, time)) return true;

        return false;
    }

    public boolean renameRefillableContainer(ContainerLocation containerLocation, String containerName)
    {
        return this.storageHelper.changeContainerName(containerLocation, containerName);
    }

    public Map<String, Kit> getKits()
    {
        return this.storageHelper.getKits();
    }

    public boolean createKit(Kit kit)
    {
        return this.storageHelper.createKit(kit);
    }

    public boolean removeKit(String kitName)
    {
        return this.storageHelper.removeKit(kitName);
    }

    public boolean assignKit(ContainerLocation containerLocation, String kitName)
    {
        try
        {
            final RefillableContainer refillableContainer = getRefillableContainer(containerLocation);
            if (refillableContainer != null)
                refillableContainer.setItemProvider(new ItemProvider(ItemProviderType.KIT, kitName));
            return this.storageHelper.addOrUpdateContainer(refillableContainer);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean assignLootTable(ContainerLocation containerLocation, String lootTableName)
    {
        try
        {
            final RefillableContainer refillableContainer = getRefillableContainer(containerLocation);
            if (refillableContainer != null)
                refillableContainer.setItemProvider(new ItemProvider(ItemProviderType.LOOT_TABLE, lootTableName));
            return this.storageHelper.addOrUpdateContainer(refillableContainer);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    public Optional<RefillableContainer> getRefillableContainerAtLocation(ContainerLocation containerLocation)
    {
        final RefillableContainer refillableContainer = this.storageHelper.getRefillableContainer(containerLocation);
        if(refillableContainer == null)
        {
            return Optional.empty();
        }
        else
        {
            return Optional.of(refillableContainer);
        }
    }

    public void startLookingForEmptyContainers()
    {
        this.plugin.getContainerScheduler().scheduleWithInterval("Chest Refill - Scanning for empty containers", 5L, TimeUnit.SECONDS, new ScanForEmptyContainersTask(this));
    }

    public Kit getKit(final String name)
    {
        return getKits().get(name);
    }

    public void refreshCache()
    {
        this.storageHelper.refreshCache();
    }

    public LootTableHelper getLootTableHelper()
    {
        return this.lootTableHelper;
    }
}
