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

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;

public class JakartaDecimalMinMaxAnnotationHandler extends AbstractNumberBaseAnnotationHandler {

    JakartaDecimalMinMaxAnnotationHandler(long seed) {
        super(seed);
    }

    @Override
    public Randomizer<?> getRandomizer(Field field) {
        Class<?> fieldType = field.getType();
        DecimalMax decimalMaxAnnotation = ReflectionUtils
                .getAnnotation(field, DecimalMax.class);
        DecimalMin decimalMinAnnotation = ReflectionUtils
                .getAnnotation(field, DecimalMin.class);

        BigDecimal maxValue = null;
        BigDecimal minValue = null;

        if (decimalMaxAnnotation != null) {
            maxValue = new BigDecimal(decimalMaxAnnotation.value());
        }

        if (decimalMinAnnotation != null) {
            minValue = new BigDecimal(decimalMinAnnotation.value());
        }
        return getRandomizer(fieldType, minValue, maxValue);
    }
}
