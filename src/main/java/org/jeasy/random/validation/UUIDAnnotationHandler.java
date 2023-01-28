package org.jeasy.random.validation;

import org.jeasy.random.api.Randomizer;

import java.lang.reflect.Field;
import java.util.UUID;

public class UUIDAnnotationHandler implements BeanValidationAnnotationHandler {

    @Override
    public Randomizer<?> getRandomizer(Field field) {
        return (Randomizer<Object>) () -> UUID.randomUUID().toString();
    }
}
