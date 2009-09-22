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
 * The event filter annotation can be applied to listeners so they can indicate
 * which events they are interested in. The annotation can have one or several
 * event types.
 * <p/>
 * See the EventManagerImpl for motivations behind event filtering.
 * <p/>
 * single event
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @EventFilter (SomeEvent.class) multiple events in an array
 * @EventFilter ({SomeEvent.class,SomeOtherEvent.class,MyEvent.class})
 * <p/>
 * $Date$
 * @since 0.9.7-dev
 */
@Target(
		{ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EventFilter {

	Class<?>[] value();
}
