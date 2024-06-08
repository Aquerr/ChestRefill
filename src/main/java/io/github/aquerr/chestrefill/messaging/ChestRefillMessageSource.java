package io.github.aquerr.chestrefill.messaging;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.messaging.locale.Localization;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.exception.CommandException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public class ChestRefillMessageSource implements MessageSource
{
    private static final String LANG_RESOURCE_FILE_BASE_PATH = "assets/chestrefill/lang/messages";


    private static class InstanceHolder {
        public static MessageSource INSTANCE = null;
    }

    public static MessageSource getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final Localization localization;

    private ChestRefillMessageSource(ResourceBundle resourceBundle)
    {
        this.localization = Localization.forResourceBundle(resourceBundle);
    }

    public static void init(String langTag)
    {
        Path langDir = ChestRefill.getInstance().getConfigDir().resolve("lang");

        ResourceBundle resourceBundle = null;
        try
        {
            if (Files.exists(langDir))
                resourceBundle = loadLangFile(langDir, langTag);
            else
                resourceBundle = loadResourceLangFile(langTag);
        }
        catch (Exception e)
        {
            try
            {
                resourceBundle = loadResourceLangFile(langTag);
            }
            catch (Exception exception)
            {
                throw new IllegalStateException("Could not load language file!", exception);
            }
        }

        InstanceHolder.INSTANCE = new ChestRefillMessageSource(resourceBundle);
    }

    private static ResourceBundle loadResourceLangFile(String langTag)
    {
        return ResourceBundle.getBundle(LANG_RESOURCE_FILE_BASE_PATH, Locale.forLanguageTag(langTag));
    }

    private static ResourceBundle loadLangFile(Path langFilesystemPath, String langTag) throws IOException
    {
        File file = langFilesystemPath.toFile();
        URL[] urls = {file.toURI().toURL()};
        ClassLoader classLoader = new URLClassLoader(urls);
        return ResourceBundle.getBundle("messages", Locale.forLanguageTag(langTag), classLoader);
    }

    @Override
    public Component resolveMessageWithPrefix(String messageKey)
    {
        return resolveMessageWithPrefix(messageKey, new Object[0]);
    }

    @Override
    public Component resolveMessageWithPrefix(String messageKey, Object... args)
    {
        return LinearComponents.linear(PluginInfo.PLUGIN_PREFIX, resolveComponentWithMessage(messageKey, args));
    }

    @Override
    public CommandException resolveExceptionWithMessage(String messageKey)
    {
        return resolveExceptionWithMessage(messageKey, new Object[0]);
    }

    @Override
    public CommandException resolveExceptionWithMessage(String messageKey, Object... args)
    {
        return new CommandException(LinearComponents.linear(PluginInfo.ERROR_PREFIX, resolveComponentWithMessage(messageKey, args)));
    }

    @Override
    public CommandException resolveExceptionWithMessageAndThrowable(String messageKey, Throwable throwable)
    {
        return new CommandException(LinearComponents.linear(PluginInfo.ERROR_PREFIX, resolveComponentWithMessage(messageKey)), throwable);
    }

    @Override
    public TextComponent resolveComponentWithMessage(String messageKey)
    {
        return resolveComponentWithMessage(messageKey, new Object[0]);
    }

    @Override
    public TextComponent resolveComponentWithMessage(String messageKey, Object... args)
    {
        args = Arrays.stream(args)
                .map(arg -> {
                    if (arg instanceof Component)
                    {
                        return LegacyComponentSerializer.legacyAmpersand().serialize((Component) arg);
                    }
                    return arg;
                }).toArray();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(resolveMessage(messageKey, args));
    }

    @Override
    public String resolveMessage(String messageKey)
    {
        return this.resolveMessage(messageKey, new Object[0]);
    }

    @Override
    public String resolveMessage(String messageKey, Object... args)
    {
        return MessageFormat.format(this.localization.getMessage(messageKey), args);
    }
}
