package org.jeasy.random;

import com.github.javafaker.Faker;
import net.datafaker.providers.base.BaseProviders;
import org.jeasy.random.api.Randomizer;
import org.jeasy.random.validation.BeanValidationRandomizerHandlers;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

/**
 * The easy random extension provides a test with randomly generated objects, including:
 *
 * <ul>
 *   <li>JDK types
 *   <li>Custom types
 *   <li>Collections
 *   <li>Generic collections
 *   <li>Partial population
 *   <li>Java Validation API
 * </ul>
 *
 * <p>Usage examples:
 *
 * <p>Injecting random values as fields:
 *
 * <pre>
 * &#064;ExtendWith(EasyRandomExtension.class)
 * public class MyTest {
 *
 *     &#064;Random
 *     private String anyString;
 *
 *     &#064;Random(excluded = {"name", "value"})
 *     private List&lt;DomainObject&gt; anyPartiallyPopulatedDomainObject;
 *
 *     &#064;Random(type = DomainObject.class)
 *     private List&lt;DomainObject&gt; anyDomainObjects;
 *
 *     &#064;Test
 *     public void testUsingRandomString() {
 *         // use the injected anyString
 *         // ...
 *     }
 *
 *     &#064;Test
 *     public void testUsingRandomDomainObjects() {
 *         // use the injected anyDomainObjects
 *         // the anyDomainObjects will contain _N_ fully populated random instances of DomainObject
 *         // ...
 *     }
 *
 *     &#064;Test
 *     public void testUsingPartiallyPopulatedDomainObject() {
 *         // use the injected anyPartiallyPopulatedDomainObject
 *         // this object's "name" and "value" members will not be populated since this has been declared with
 *         //     excluded = {"name", "value"}
 *         // ...
 *     }
 * }
 * </pre>
 *
 * <p>Injecting random values as parameters:
 *
 * <pre>
 * &#064;ExtendWith(EasyRandomExtension.class)
 * public class MyTest {
 *
 *     &#064;Test
 *     &#064;ExtendWith(EasyRandomExtension.class)
 *     public void testUsingRandomString(&#064;Random &#064;Email String anyString) {
 *         // use the provided anyString
 *         // ...
 *     }
 *
 *     &#064;Test
 *     &#064;ExtendWith(EasyRandomExtension.class)
 *     public void testUsingRandomDomainObjects(&#064;Random List&lt;DomainObject&gt; anyDomainObjects) {
 *         // use the injected anyDomainObjects
 *         // the anyDomainObjects will contain _N_ fully populated random instances of DomainObject
 *         // ...
 *     }
 * }
 * </pre>
 *
 * @author Random Beans
 * @author linux_china
 */
public class EasyRandomExtension implements TestInstancePostProcessor, ParameterResolver {

    private final EasyRandom easyRandom;
    private final BeanValidationRandomizerHandlers beanValidationHandlers;
    /**
     * faker type handlers
     */
    private final Map<Class<?>, Method> fakerTypeHandlers = new HashMap<>();
    /**
     * i18n faker map
     */
    private final Map<String, Faker> fakerI18nMap = new HashMap<>();
    private final Map<String, net.datafaker.Faker> dataFakerI18nMap = new HashMap<>();

    public EasyRandomExtension() {
        EasyRandomParameters parameters = new EasyRandomParameters().objectFactory(new RecordFactory());
        easyRandom = new EasyRandom(parameters);
        beanValidationHandlers = new BeanValidationRandomizerHandlers();
        beanValidationHandlers.init(parameters);
        // init Faker
        for (Method method : Faker.class.getMethods()) {
            final Package typePackage = method.getReturnType().getPackage();
            if (typePackage != null
                    && typePackage.getName().equals("com.github.javafaker")
                    && method.getParameterCount() == 0
            ) {
                fakerTypeHandlers.put(method.getReturnType(), method);
            }
        }
        for (Method method : BaseProviders.class.getDeclaredMethods()) {
            if (method.getParameterCount() == 0) {
                fakerTypeHandlers.put(method.getReturnType(), method);
            }
        }
    }

    public EasyRandomExtension(EasyRandomParameters parameters) {
        this.easyRandom = new EasyRandom(parameters);
        this.beanValidationHandlers = new BeanValidationRandomizerHandlers();
        this.beanValidationHandlers.init(parameters);
    }

    /**
     * Does this extension support injection for parameters of the type described by the given {@code
     * parameterContext}?
     *
     * @param parameterContext the context for the parameter for which an argument should be resolved
     * @param extensionContext the extension context for the {@code Executable} about to be invoked
     * @return true if the given {@code parameterContext} is annotated with {@link Random}, false
     * otherwise
     * @throws ParameterResolutionException exception
     */
    @Override
    public boolean supportsParameter(ParameterContext parameterContext,
                                     ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getAnnotation(Random.class) != null;
    }

    /**
     * Provides a value for any parameter context which has passed the {@link
     * #supportsParameter(ParameterContext, ExtensionContext)} gate.
     *
     * @param parameterContext the context for the parameter for which an argument should be resolved
     * @param extensionContext the <em>context</em> in which the current test or container is being
     *                         executed
     * @return a randomly generated object
     * @throws ParameterResolutionException exception
     */
    @Override
    public Object resolveParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext) throws ParameterResolutionException {
        final Parameter parameter = parameterContext.getParameter();
        if (fakerTypeHandlers.containsKey(parameter.getType())) {
            return fakeValue(parameter.getAnnotation(Random.class), parameter.getType());
        }
        if (parameter.getAnnotations().length > 1) {
            final Randomizer<?> randomizer = beanValidationHandlers.getRandomizer(parameter);
            if (randomizer != null) {
                return randomizer.getRandomValue();
            }
        }
        return resolve(parameter.getParameterizedType(), parameter.getType(), parameter.getAnnotation(Random.class));
    }

    /**
     * Inject random values into any fields which are annotated with {@link Random}
     *
     * @param testInstance     the instance to post-process
     * @param extensionContext the current extension context
     * @throws Exception exception
     */
    @Override
    public void postProcessTestInstance(Object testInstance,
                                        ExtensionContext extensionContext) throws Exception {
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (isAnnotated(field, Random.class)) {
                Random annotation = field.getAnnotation(Random.class);
                Object randomObject;
                if (fakerTypeHandlers.containsKey(field.getType())) {
                    randomObject = fakeValue(field.getAnnotation(Random.class), field.getType());
                } else {
                    final Randomizer<?> randomizer = beanValidationHandlers.getRandomizer(field);
                    if (randomizer != null) {
                        randomObject = randomizer.getRandomValue();
                    } else {
                        randomObject = resolve(field.getGenericType(), field.getType(), annotation);
                    }
                }
                field.setAccessible(true);
                field.set(testInstance, randomObject);
            }
        }
    }

    /**
     * Maps the 'random requirements' expressed by the given {@code annotation} to invocations on
     * {@link #easyRandom}.
     *
     * @param targetType the type to be provided
     * @param annotation an instance of {@link Random} which describes how the user wishes to
     *                   configure the 'random generation'
     * @return a randomly generated instance of {@code targetType}
     */
    private Object resolve(Type targetType, Class<?> targetClass, Random annotation) {
        if (targetClass.isAssignableFrom(List.class) || targetClass.isAssignableFrom(Collection.class)) {
            return easyRandom
                    .objects(parseInferredClass(targetType, annotation.type()), annotation.size())
                    .collect(Collectors.toList());
        } else if (targetClass.isAssignableFrom(Set.class)) {
            return easyRandom
                    .objects(parseInferredClass(targetType, annotation.type()), annotation.size())
                    .collect(Collectors.toSet());
        } else if (targetClass.isAssignableFrom(Stream.class)) {
            return easyRandom.objects(parseInferredClass(targetType, annotation.type()), annotation.size());
        } else if (fakerTypeHandlers.containsKey(targetClass)) {
            return fakeValue(annotation, targetClass);
        } else {
            return easyRandom.nextObject(targetClass);
        }
    }

    public static Class<?> parseInferredClass(Type genericType, Class<?> defaultClass) {
        if (defaultClass != Object.class) {
            return defaultClass;
        }
        Class<?> inferredClass = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) genericType;
            Type[] typeArguments = type.getActualTypeArguments();
            if (typeArguments.length > 0) {
                final Type typeArgument = typeArguments[0];
                if (typeArgument instanceof ParameterizedType) {
                    inferredClass = (Class<?>) ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                } else if (typeArgument instanceof Class) {
                    inferredClass = (Class<?>) typeArgument;
                } else {
                    String typeName = typeArgument.getTypeName();
                    if (typeName.contains(" ")) {
                        typeName = typeName.substring(typeName.lastIndexOf(" ") + 1);
                    }
                    if (typeName.contains("<")) {
                        typeName = typeName.substring(0, typeName.indexOf("<"));
                    }
                    try {
                        inferredClass = Class.forName(typeName);
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
        }
        if (inferredClass == null && genericType instanceof Class) {
            inferredClass = (Class<?>) genericType;
        }
        return inferredClass == null ? defaultClass : inferredClass;
    }

    public Object fakeValue(Random random, Class<?> fakeType) {
        if (fakeType.getCanonicalName().startsWith("net.datafaker.")) {
            return dataFakerValue(random, fakeType);
        } else {
            return javaFakerValue(random, fakeType);
        }
    }

    public Object javaFakerValue(Random random, Class<?> fakeType) {
        // faker checker
        final Method handler = fakerTypeHandlers.get(fakeType);
        try {
            String locale = random.locale();
            if (!fakerI18nMap.containsKey(locale)) {
                final String[] parts = locale.split("[_\\-]+");
                Locale temp = parts.length > 1 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
                fakerI18nMap.put(locale, new Faker(temp));
            }
            return handler.invoke(fakerI18nMap.get(locale));
        } catch (Exception e) {
            return null;
        }
    }

    private Object dataFakerValue(Random random, Class<?> fakeType) {
        // faker checker
        final Method handler = fakerTypeHandlers.get(fakeType);
        try {
            String locale = random.locale();
            if (!dataFakerI18nMap.containsKey(locale)) {
                final String[] parts = locale.split("[_\\-]+");
                Locale temp = parts.length > 1 ? new Locale(parts[0], parts[1]) : new Locale(parts[0]);
                dataFakerI18nMap.put(locale, new net.datafaker.Faker(temp));
            }
            return handler.invoke(dataFakerI18nMap.get(locale));
        } catch (Exception e) {
            return null;
        }
    }
}
