package org.jeasy.random;

import org.jeasy.random.api.Randomizer;
import org.jeasy.random.api.RandomizerContext;
import org.jeasy.random.validation.BeanValidationRandomizerHandlers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;

/**
 * Java Records Factory for easy-random, please refer https://github.com/j-easy/easy-random/issues/397
 *
 * @author benas
 * @author linux_china
 */
public class RecordFactory extends ObjenesisObjectFactory {

    private EasyRandom easyRandom;
    private BeanValidationRandomizerHandlers beanValidationHandlers;


    @Override
    public <T> T createInstance(Class<T> type, RandomizerContext context) {
        if (easyRandom == null) {
            easyRandom = new EasyRandom(context.getParameters());
            beanValidationHandlers = new BeanValidationRandomizerHandlers();
            beanValidationHandlers.init(context.getParameters());
        }
        if (type.isRecord()) {
            return createRandomRecord(type);
        } else {
            return super.createInstance(type, context);
        }
    }

    private <T> T createRandomRecord(Class<T> recordType) {
        // generate random values for record components
        RecordComponent[] recordComponents = recordType.getRecordComponents();
        Object[] randomValues = new Object[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            final RecordComponent recordComponent = recordComponents[i];
            Object componentValue = null;
            try {
                // check bean validation annotation
                final Field declaredField = recordType.getDeclaredField(recordComponent.getName());
                if (declaredField.getAnnotations().length >= 1) {
                    final Randomizer<?> randomizer = beanValidationHandlers.getRandomizer(declaredField);
                    if (randomizer != null) {
                        componentValue = randomizer.getRandomValue();
                    }
                }
            } catch (Exception ignore) {

            }
            if (componentValue == null) {
                componentValue = easyRandom.nextObject(recordComponent.getType());
            }
            randomValues[i] = componentValue;
        }
        // create a random instance with random values
        try {
            final Constructor<T> canonicalConstructor = getCanonicalConstructor(recordType);
            // change accessibility to public for nested Records
            canonicalConstructor.setAccessible(true);
            return canonicalConstructor.newInstance(randomValues);
        } catch (Exception e) {
            throw new ObjectCreationException("Unable to create a random instance of recordType " + recordType, e);
        }
    }

    private <T> Constructor<T> getCanonicalConstructor(Class<T> recordType) {
        RecordComponent[] recordComponents = recordType.getRecordComponents();
        Class<?>[] componentTypes = new Class<?>[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            // recordComponents are ordered, see javadoc:
            // "The components are returned in the same order that they are declared in the record header"
            componentTypes[i] = recordComponents[i].getType();
        }
        try {
            return recordType.getDeclaredConstructor(componentTypes);
        } catch (NoSuchMethodException e) {
            // should not happen, from Record javadoc:
            // "A record class has the following mandated members: a public canonical constructor ,
            // whose descriptor is the same as the record descriptor;"
            throw new RuntimeException("Invalid record definition", e);
        }
    }
}

