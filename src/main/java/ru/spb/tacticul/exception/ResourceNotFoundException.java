package ru.spb.tacticul.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " с id " + id + " не найден");
    }
}
