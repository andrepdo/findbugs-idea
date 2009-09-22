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
package org.twodividedbyzero.idea.findbugs.common.nevent;

/**
 * Should events always have a result? Maybe, but the result is just
 * an easy way for now to indicate a context. This interface is really
 * just some basic scaffolding for now. I am making the assumption
 * that the return value will contain sufficient information for a context;
 * that is, the method being annotated must return a result that has
 * sufficient information that a listener will need.
 * <p/>
 * T is the result of the method being annotated should it have a return
 * value.
 * <p/>
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.7-dev
 */
public interface Event<T> {

	String getDescription();

	T getResult();

	void setResult(T r);


}
