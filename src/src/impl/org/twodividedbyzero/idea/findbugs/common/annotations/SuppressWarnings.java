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
package org.twodividedbyzero.idea.findbugs.common.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Target;


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
	 */
	String[] value() default {};

	String justification() default "";

}
