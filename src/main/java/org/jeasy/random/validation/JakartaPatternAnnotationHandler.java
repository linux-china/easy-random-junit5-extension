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

import org.jeasy.random.api.Randomizer;
import org.jeasy.random.randomizers.RegularExpressionRandomizer;
import org.jeasy.random.util.ReflectionUtils;

import jakarta.validation.constraints.Pattern;

import java.lang.reflect.Field;
import java.util.Random;

class JakartaPatternAnnotationHandler implements BeanValidationAnnotationHandler {

    private final Random random;

    JakartaPatternAnnotationHandler(long seed) {
        random = new Random(seed);
    }

    @Override
    public Randomizer<?> getRandomizer(Field field) {
        Class<?> fieldType = field.getType();
        Pattern patternAnnotation = ReflectionUtils
                .getAnnotation(field, Pattern.class);

        final String regex = patternAnnotation.regexp();
        if (fieldType.equals(String.class)) {
            return new RegularExpressionRandomizer(regex, random.nextLong());
        }
        return null;
    }
}
