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
package org.twodividedbyzero.idea.findbugs.common.nevent.types;

import org.twodividedbyzero.idea.findbugs.common.nevent.Event;


/**
 * See the Event interface.
 * <p/>
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public abstract class AbstractEvent<T> implements Event<T> {
	//private static final long serialVersionUID = 1L;

	private final String _description;

	private T _result;

	//private Utils utils_ = new Utils();


	public AbstractEvent(final String description) {
		_description = description;
	}


	public String getDescription() {
		return _description;
	}


	public T getResult() {
		return _result;
	}


	public void setResult(final T result) {
		_result = result;
	}


	@Override
	public String toString() {
		return "AbstractEvent{" + "_description='" + _description + '\'' + ", _result=" + _result + '}';
	}
}
