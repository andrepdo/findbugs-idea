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
package org.twodividedbyzero.idea.findbugs.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@Target(value = {TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, PACKAGE})
@Retention(value = CLASS)
public @interface SuppressWarnings {

	/**
	 * The set of warnings that are to be suppressed by the compiler in the
	 * annotated element.
	 * <p/>
	 * Duplicate names are permitted.  The second and
	 * successive occurrences of a name are ignored.  The presence of
	 * unrecognized warning names is <i>not</i> an error: Compilers must
	 * ignore any warning names they do not recognize.  They are, however,
	 * free to emit a warning if an annotation contains an unrecognized
	 * warning name.
	 * <p/>
	 * <p>Compiler vendors should document the warning names they support in
	 * conjunction with this annotation type. They are encouraged to cooperate
	 * to ensure that the same names work across multiple compilers.
	 * @return
	 */
	String[] value() default {};

	String justification() default "";

}
