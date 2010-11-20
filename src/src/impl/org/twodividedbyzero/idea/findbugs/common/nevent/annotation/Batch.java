/*
 * Copyright 2010 Andre Pfeiler
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
