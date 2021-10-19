package org.jeasy.random;

import org.jeasy.random.api.RandomizerContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;

/**
 * Java Records Factory for easy-random, please refer https://github.com/j-easy/easy-random/issues/397
 *
 * @author benas
 * @author linux_china
 */
public class RecordFactory extends ObjenesisObjectFactory {

    private EasyRandom easyRandom;

    @Override
    public <T> T createInstance(Class<T> type, RandomizerContext context) {
        if (easyRandom == null) {
            easyRandom = new EasyRandom(context.getParameters());
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
            randomValues[i] = easyRandom.nextObject(recordComponents[i].getType());
        }
        // create a random instance with random values
        try {
            return getCanonicalConstructor(recordType).newInstance(randomValues);
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

