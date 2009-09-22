/**
 * Copyright 2008 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.common.event;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.event.Event.EventType;
import org.twodividedbyzero.idea.findbugs.common.event.filters.EventFilter;

import java.util.Set;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public interface EventManager {

	public static final String NAME = "EventManager";


	public void fireEvent(@NotNull final Event event);


	public <T extends Event> void addEventListener(@NotNull final EventFilter<T> eventFilter, @NotNull EventListener<T> listener);


	public void removeEventListener(@NotNull final EventListener<?> listener);


	public void removeEventListener(@NotNull final Project project);


	public void removeAllListeners();


	public Set<EventListener<?>> getListeners();


	public boolean hasListeners(final Event event);


	public boolean hasListeners(final EventType type);


	public boolean hasListener(@NotNull final EventFilter<?> eventFilter, @NotNull final EventListener<?> listener);
}
