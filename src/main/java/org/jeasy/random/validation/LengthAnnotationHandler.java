/*
 * The MIT License
 *
 *   Copyright (c) 2020, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */
package org.jeasy.random.validation;

import org.hibernate.validator.constraints.Length;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.range.IntegerRangeRandomizer;
import org.jeasy.random.randomizers.text.StringRandomizer;
import org.jeasy.random.util.ReflectionUtils;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static org.jeasy.random.util.ReflectionUtils.*;

public class LengthAnnotationHandler implements BeanValidationAnnotationHandler {

    private EasyRandom easyRandom;
    private final EasyRandomParameters parameters;

    public LengthAnnotationHandler(EasyRandomParameters parameters) {
        this.parameters = parameters.copy();
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Randomizer<?> getRandomizer(Field field) {
        Class<?> fieldType = field.getType();
        Length lengthAnnotation = ReflectionUtils
                .getAnnotation(field, Length.class);

        final int min = lengthAnnotation.min();
        final int max = lengthAnnotation.max() == Integer.MAX_VALUE ? 255 : lengthAnnotation.max();
        if (easyRandom == null) {
            parameters.setCollectionSizeRange(new EasyRandomParameters.Range<>(min, max));
            parameters.setStringLengthRange(new EasyRandomParameters.Range<>(min, max));
            easyRandom = new EasyRandom(parameters);
        }

        if (fieldType.equals(String.class)) {
            return new StringRandomizer(parameters.getCharset(), min, max, easyRandom.nextLong());
        }

        // FIXME: There should be away to reuse code from ArrayPopulator/CollectionPopulator/MapPopulator *without* making them public

        if (isArrayType(fieldType)) {
            return (Randomizer<Object>) () -> {
                int randomSize = new IntegerRangeRandomizer(min, max, parameters.getSeed()).getRandomValue();
                Object result = Array.newInstance(field.getType().getComponentType(), randomSize);
                for (int i = 0; i < randomSize; i++) {
                    Object randomElement = easyRandom.nextObject(fieldType.getComponentType());
                    Array.set(result, i, randomElement);
                }
                return result;
            };
        }

        if (isCollectionType(fieldType)) {
            return (Randomizer<Object>) () -> {
                int randomSize = new IntegerRangeRandomizer(min, max, parameters.getSeed()).getRandomValue();
                Type fieldGenericType = field.getGenericType();
                Collection collection;

                if (isInterface(fieldType)) {
                    collection = getEmptyImplementationForCollectionInterface(fieldType);
                } else {
                    collection = createEmptyCollectionForType(fieldType, randomSize);
                }
                if (isParameterizedType(fieldGenericType)) { // populate only parameterized types, raw types will be empty
                    ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
                    Type type = parameterizedType.getActualTypeArguments()[0];
                    if (isPopulatable(type)) {
                        for (int i = 0; i < randomSize; i++) {
                            Object item = easyRandom.nextObject((Class<?>) type);
                            collection.add(item);
                        }

                    }
                }
                return collection;
            };
        }
        if (isMapType(fieldType)) {
            return (Randomizer<Object>) () -> {
                int randomSize = new IntegerRangeRandomizer(min, max, parameters.getSeed()).getRandomValue();
                Type fieldGenericType = field.getGenericType();
                Map<Object, Object> map;

                if (isInterface(fieldType)) {
                    map = (Map<Object, Object>) getEmptyImplementationForMapInterface(fieldType);
                } else {
                    try {
                        map = (Map<Object, Object>) fieldType.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                             InvocationTargetException e) {
                        if (fieldType.isAssignableFrom(EnumMap.class)) {
                            if (isParameterizedType(fieldGenericType)) {
                                Type type = ((ParameterizedType) fieldGenericType).getActualTypeArguments()[0];
                                map = new EnumMap((Class<?>) type);
                            } else {
                                return null;
                            }
                        } else {
                            map = (Map<Object, Object>) new ObjenesisStd().newInstance(fieldType);
                        }
                    }
                }

                if (isParameterizedType(fieldGenericType)) { // populate only parameterized types, raw types will be empty
                    ParameterizedType parameterizedType = (ParameterizedType) fieldGenericType;
                    Type keyType = parameterizedType.getActualTypeArguments()[0];
                    Type valueType = parameterizedType.getActualTypeArguments()[1];
                    if (isPopulatable(keyType) && isPopulatable(valueType)) {
                        for (int index = 0; index < randomSize; index++) {
                            Object randomKey = easyRandom.nextObject((Class<?>) keyType);
                            Object randomValue = easyRandom.nextObject((Class<?>) valueType);
                            if (randomKey != null) {
                                map.put(randomKey, randomValue);
                            }
                        }
                    }
                }
                return map;
            };
        }
        return null;
    }
}
