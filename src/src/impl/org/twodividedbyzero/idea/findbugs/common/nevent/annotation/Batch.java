/**
 * Copyright 2009 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.twodividedbyzero.idea.findbugs.common.nevent.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * A method marker(annotation) denoting that everything within it (including all
 * its calls to other methods) which might generate events, should be batched.
 * If a method which is annotated with Batch calls another method that is also
 * labelled Batch, the batch is nested, and is treated as one large batch, that
 * is notifications will only be sent once the outermost batch is completed.
 * This is very similar to the @Transactional annotation in Spring/Hibernate.
 * <p/>
 * For now assume only methods can be marked as Batch-able.
 * <p/>
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
@Target(
		{ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Batch {
	// a method marker indicating that all events generated
	// by calling the method will be batched
}
