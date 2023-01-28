package org.jeasy.random.validation;

import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.RegularExpressionRandomizer;

import java.lang.reflect.Field;

public class IsbnAnnotationHandler implements BeanValidationAnnotationHandler {
    private final long seed;

    public IsbnAnnotationHandler(long seed) {
        this.seed = seed;
    }

    @Override
    public Randomizer<?> getRandomizer(Field field) {
        return new RegularExpressionRandomizer("978-[0-9]{3}-[0-9]{3}-[0-9]{3}-[0-9]", seed);
    }
}
