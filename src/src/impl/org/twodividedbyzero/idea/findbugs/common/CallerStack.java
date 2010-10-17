/*
 * Copyright 2010 Andre Pfeiler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.twodividedbyzero.idea.findbugs.common;

/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.96-dev
 */
public class CallerStack extends Throwable {

	private static final long serialVersionUID = 1L;


	public CallerStack() {
		super("called from");
	}


	public CallerStack(final CallerStack cause) {
		super("called from", cause);
	}


	public static void initCallerStack(final Throwable throwable, final CallerStack callerStack) {
		Throwable lastCause = throwable;
		while (lastCause.getCause() != null) {
			lastCause = lastCause.getCause();
		}
		try {
			lastCause.initCause(callerStack);
		} catch (final IllegalStateException ignored) {
			// some exceptions may override getCause(), but not initCause()
			// => getCause() can be null, but cause is alreay set
		}
	}


}
