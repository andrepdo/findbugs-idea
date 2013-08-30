/*
 * Copyright 2008-2013 Andre Pfeiler
 *
 * This file is part of FindBugs-IDEA.
 *
 * FindBugs-IDEA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FindBugs-IDEA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FindBugs-IDEA.  If not, see <http://www.gnu.org/licenses/>.
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

	String NAME = "EventManager";


	void fireEvent(@NotNull final Event event);


	<T extends Event> void addEventListener(@NotNull final EventFilter<T> eventFilter, @NotNull EventListener<T> listener);


	void removeEventListener(@NotNull final EventListener<?> listener);


	void removeEventListener(@NotNull final Project project);


	void removeAllListeners();


	Set<EventListener<?>> getListeners();


	boolean hasListeners(final Event event);


	boolean hasListeners(final EventType type);


	boolean hasListener(@NotNull final EventFilter<?> eventFilter, @NotNull final EventListener<?> listener);
}
