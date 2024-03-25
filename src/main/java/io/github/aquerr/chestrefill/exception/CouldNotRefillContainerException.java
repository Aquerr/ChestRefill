package io.github.aquerr.chestrefill.exception;

public class CouldNotRefillContainerException extends Exception
{
    public CouldNotRefillContainerException(String message)
    {
        super(message);
    }

    public CouldNotRefillContainerException(Throwable cause)
    {
        super(cause);
    }
}
