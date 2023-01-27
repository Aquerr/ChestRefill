package io.github.aquerr.chestrefill.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.command.exception.CommandException;

public interface MessageSource
{

    Component resolveMessageWithPrefix(String messageKey);

    Component resolveMessageWithPrefix(String messageKey, Object... args);

    CommandException resolveExceptionWithMessage(String messageKey);

    CommandException resolveExceptionWithMessage(String messageKey, Object... args);

    CommandException resolveExceptionWithMessageAndThrowable(String messageKey, Throwable throwable);

    TextComponent resolveComponentWithMessage(String messageKey);

    TextComponent resolveComponentWithMessage(String messageKey, Object... args);

    String resolveMessage(String messageKey);

    String resolveMessage(String messageKey, Object... args);
}
