/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.scheduling.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a method to be scheduled. Exactly one of the
 * <code>cron</code>, <code>fixedDelay</code>, or <code>fixedRate</code>
 * attributes must be provided.
 *
 * <p>The annotated method must expect no arguments and have a
 * <code>void</code> return type.
 *
 * @author Mark Fisher
 * @author Dave Syer
 * @since 3.0
 * @see ScheduledAnnotationBeanPostProcessor
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Scheduled {

	/**
	 * A cron-like expression, extending the usual UN*X definition to include
	 * triggers on the second as well as minute, hour, day of month, month
	 * and day of week.  e.g. <code>"0 * * * * MON-FRI"</code> means once 
	 * per minute on weekdays (at the top of the minute - the 0th second).
	 * @return an expression that can be parsed to a cron schedule
	 */
	String cron() default "";

	/**
	 * Execute the annotated method with a fixed period between the end
	 * of the last invocation and the start of the next.
	 * @return the delay in milliseconds
	 */
	long fixedDelay() default -1;

	/**
	 * Execute the annotated method with a fixed period between invocations.
	 * @return the period in milliseconds
	 */
	long fixedRate() default -1;

}
