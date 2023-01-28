package org.jeasy.random.validation;

import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.RegularExpressionRandomizer;

import java.lang.reflect.Field;

public class EANAnnotationHandler implements BeanValidationAnnotationHandler {
    private final long seed;

    public EANAnnotationHandler(long seed) {
        this.seed = seed;
    }

    @Override
    public Randomizer<?> getRandomizer(Field field) {
        return new RegularExpressionRandomizer("[0-9]{13}", seed);
    }
}
