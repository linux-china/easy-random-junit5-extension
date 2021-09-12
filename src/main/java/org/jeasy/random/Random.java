package org.jeasy.random;

import java.lang.annotation.*;

/**
 * Allows the caller to customise the random generation of a given type.
 *
 * <p>Usage example:
 *
 * <pre>
 *  // create a random instance of String
 *  &#064;Random String anyString;
 *  // create a random email
 *  &#064;Random &#064;Email String email;
 *
 *  // create a random, fully populated instance of MyDomainObject
 *  &#064;Random private DomainObject fullyPopulatedDomainObject;
 *
 *  // create a List containing the default size of randomly generated instances of String
 *  &#064;Random List&lt;String&gt; anyStrings;
 *
 *  // create a Stream containing two randomly generated instances of MyDomainObject
 *  &#064;Random(size = 2) Stream&lt;MyDomainObject&gt; anyStrings;
 * </pre>
 *
 * @author Random Beans
 * @author linux_china
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Documented
public @interface Random {
    /**
     * When generating a collection of random type you may want to limit its size.
     *
     * @return the desired size of any collections within the randomly generated type
     */
    int size() default 10;

    /**
     * When generating a collection of random type you'll want to tell the generator what that type
     * <b>is</b>.
     *
     * @return the type of a randomly generated generic collection
     */
    Class<?> type() default Object.class;

    String locale() default "en_US";
}