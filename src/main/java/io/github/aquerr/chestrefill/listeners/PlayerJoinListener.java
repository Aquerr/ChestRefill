package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.PluginPermissions;
import io.github.aquerr.chestrefill.version.VersionChecker;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.concurrent.CompletableFuture;

public class PlayerJoinListener extends AbstractListener
{
    public PlayerJoinListener(ChestRefill plugin)
    {
        super(plugin);
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player player)
    {
        CompletableFuture.runAsync(() -> checkVersionAndInform(player));
    }

    private void checkVersionAndInform(Player player)
    {
        if (!this.getPlugin().getConfiguration().getVersionConfig().shouldPerformVersionCheck())
            return;

        if (player.hasPermission(PluginPermissions.VERSION_NOTIFY) && !VersionChecker.getInstance().isLatest(PluginInfo.VERSION))
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, "There is a new version of ", TextColors.YELLOW, "Chest Refill", TextColors.WHITE, " available online!"));
        }
    }
}
