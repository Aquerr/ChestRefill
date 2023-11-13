package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.PluginPermissions;
import io.github.aquerr.chestrefill.version.VersionChecker;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;

public class PlayerJoinListener extends AbstractListener
{
    public PlayerJoinListener(ChestRefill plugin)
    {
        super(plugin);
    }

    @Listener
    public void onPlayerJoin(ServerSideConnectionEvent.Join event, @Root ServerPlayer player)
    {
        CompletableFuture.runAsync(() -> checkVersionAndInform(player));
    }

    private void checkVersionAndInform(ServerPlayer player)
    {
        if (!this.getPlugin().getConfiguration().getVersionConfig().shouldPerformVersionCheck())
            return;

        if (player.hasPermission(PluginPermissions.VERSION_NOTIFY) && !VersionChecker.getInstance().isLatest(PluginInfo.VERSION))
        {
            player.sendMessage(linear(
                    PluginInfo.PLUGIN_PREFIX, text("There is a new version of "),
                    NamedTextColor.YELLOW, text("Chest Refill"),
                    NamedTextColor.WHITE, text(" available online!")));
        }
    }
}
