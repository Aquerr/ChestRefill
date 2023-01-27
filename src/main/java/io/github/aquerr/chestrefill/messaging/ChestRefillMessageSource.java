package io.github.aquerr.chestrefill.messaging;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.messaging.locale.Localization;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.exception.CommandException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.PropertyResourceBundle;

public class ChestRefillMessageSource implements MessageSource
{
    private static final String LANG_FILE_BASE_PATH = "assets/chestrefill/lang/messages";
    private static final String DEFAULT_LANG_FILE_PATH = "assets/chestrefill/lang/messages.properties";


    private static class InstanceHolder {
        public static MessageSource INSTANCE = null;
    }

    public static MessageSource getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final Localization localization;

    private ChestRefillMessageSource(PropertyResourceBundle resourceBundle)
    {
        this.localization = Localization.forResourceBundle(resourceBundle);
    }

    public static void init(String langTag)
    {
        String jarLangFilePath = DEFAULT_LANG_FILE_PATH;
        if (!langTag.equals("en_US"))
        {
            jarLangFilePath = String.format(LANG_FILE_BASE_PATH + "_%s.properties", langTag);
        }

        Path langDir = ChestRefill.getInstance().getConfigDir().resolve("lang");
        Path fileLangFilePath = langDir.resolve(String.format("messages_%s.properties", langTag));

        try
        {
            Files.createDirectories(langDir);
            generateLangFile(jarLangFilePath, fileLangFilePath);
        }
        catch (IOException e)
        {
            try
            {
                jarLangFilePath = DEFAULT_LANG_FILE_PATH;
                fileLangFilePath = fileLangFilePath.resolveSibling("messages.properties");
                generateLangFile(jarLangFilePath, fileLangFilePath);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
                throw new IllegalStateException("Could not generate language file!");
            }
            e.printStackTrace();
        }

        PropertyResourceBundle propertyResourceBundle;

        try
        {
            propertyResourceBundle = new PropertyResourceBundle(Files.newInputStream(fileLangFilePath));
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }

        InstanceHolder.INSTANCE = new ChestRefillMessageSource(propertyResourceBundle);
    }

    private static void generateLangFile(String jarLangFilePath, Path fileLangFilePath) throws IOException
    {
        URI langFileUri = ChestRefill.getInstance().getPluginContainer().locateResource(URI.create(jarLangFilePath))
                .orElseThrow(() -> new RuntimeException("Could not locate language file!"));
        InputStream langFilePathStream = langFileUri.toURL().openStream();
        Files.copy(langFilePathStream, fileLangFilePath, StandardCopyOption.REPLACE_EXISTING);
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
