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
