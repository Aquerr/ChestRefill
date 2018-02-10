package io.github.aquerr.chestrefill;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;


/**
 * Created by Aquerr on 2018-02-09.
 */

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description, authors = PluginInfo.Authors, url = PluginInfo.Url)
public class ChestRefill
{

    @Inject
    private Logger _logger;

    public Logger getLogger() {return _logger;}







    @Listener
    public void onGameInitialization(GameInitializationEvent event)
    {
        Sponge.getServer().getConsole().sendMessage(Text.of(TextColors.YELLOW, "Chest Refill is loading...! :D"));
    }
}
