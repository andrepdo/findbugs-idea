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
package org.twodividedbyzero.idea.findbugs.gui.tree.model;

import com.intellij.openapi.diagnostic.Logger;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.I18N;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
public class Grouper<T> {

	public interface GrouperCallback<E> {

		public void startGroup(E firstMember, int depth);

		/**
		 * Start a new sub group.
		 *
		 * @param depth  insert the sub group at index (depth)
		 * @param member the member to compare at index (depth)
		 * @param parent
		 */
		public void startSubGroup(int depth, E member, E parent);

		public void addToGroup(int depth, E member, E parent);

		public List<E> availableGroups(int depth, E member);

		public Comparator<E> currentGroupComparatorChain(int depth);
	}

	private static final Logger LOGGER = Logger.getInstance(Grouper.class.getName());

	private final GrouperCallback<T> _callback;


	/**
	 * Creates a new instance of Grouper.
	 *
	 * @param callback the callback which receives the groups and elements
	 */
	public Grouper(final GrouperCallback<T> callback) {
		_callback = callback;
	}


	/**
	 * Group elements of given collection according to given
	 * compartor's test for equality.  The groups are specified by
	 * calls to the Grouper's callback object.
	 *
	 * @param collection the natural SORTED collection
	 * @param comparator the comparator
	 */
	public void group(final Collection<T> collection, final Comparator<T> comparator) {
		final Iterator<T> i = collection.iterator();
		T last = null;
		while (i.hasNext()) {
			final T current = i.next();
			if (last != null && comparator.compare(last, current) == 0) {
				// Same group as before
				//_callback.addToGroup(current);
			} else {
				// Start of a new group
				//_callback.startGroup(current);
			}

			last = current;
		}
	}


	/**
	 * Group elements of given collection obtained by calling {@link GrouperCallback#availableGroups(int, Object)}
	 * according to given compartor's and depth test for equality.  The groups are specified by
	 * calls to the Grouper's callback object.
	 *
	 * @param comparable  the comparable to compare with the obtained group(s) comparables
	 * @param comparators the comparator's according to the comparing depth
	 * @see org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy
	 * @see org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator
	 * @see Grouper.GrouperCallback
	 */
	public synchronized void group(final T comparable, final List<Comparator<T>> comparators) {

		int depth = -1;
		int parentIndex = -1;
		int index = -1;
		int j = 0;

		for (int i = 0; i < comparators.size(); i++) {
			depth = -1;
			j = i;

			final List<T> groups = _callback.availableGroups(i, comparable);

			final Comparator<T> tComparator = comparators.get(i);
			Collections.sort(groups, tComparator);
			index = Collections.binarySearch(groups, comparable, tComparator);

			// todo: make dynamic
			if (i == 0 && index < 0) { // if top 1st level group does not exists break iteration
				depth = i;
				break;
			} else if (i == 1 && index < 0) { // if 2nd level group does not exists break iteration
				depth = i;
				break;
			} else if (i == 2 && index < 0) { // if 3rd level group does not exists break iteration
				depth = i;
				break;
			} else if (i == 3 && index < 0) { // if 4th level group does not exists break iteration
				depth = i;
				break;
			} else if (i == 4 && index < 0) { // if 5th level group does not exists break iteration
				depth = i;
				break;
			} else { // addToGroup
				//parentIndex = index;
				depth = i;
			}
		}


		/*System.err.println("\n===OOOPS \nindex: " + index + " level: " + level + " i: " + j);
					System.err.println("Classname: " + ((BugInstance) comparable).getPrimaryClass().getClassName().split("\\$")[0]);
					System.err.println("Category: " + I18N.instance().getBugCategoryDescription(((BugInstance) comparable).getBugPattern().getCategory()));
					System.err.println("Type: " + I18N.instance().getBugTypeDescription(((BugInstance) comparable).getBugPattern().getAbbrev()));
					System.err.println("Short: " + ((BugInstance) comparable).getBugPattern().getShortDescription());*/

		if (index < 0 && depth == 0) { // top level group ##  && level == -1
			//System.err.println("\n= StartGroup =\nindex: " + index + " depth: " + depth + " i: " + j);
			//System.err.println("Classname: " + ((BugInstance) comparable).getPrimaryClass().getClassName().split("\\$")[0]);
			/*System.err.println("Category: " + I18N.instance().getBugCategoryDescription(((BugInstance) comparable).getBugPattern().getCategory()));
			System.err.println("Type: " + I18N.instance().getBugTypeDescription(((BugInstance) comparable).getBugPattern().getAbbrev()));
			System.err.println("Short: " + ((BugInstance) comparable).getBugPattern().getShortDescription());*/
			//final T parent = groups.get(index);
			_callback.startGroup(comparable, depth);

		} else if (index < 0 && depth > 0) { // x level group

/*
			final Comparator<T> c = comparators.get(0);// depth-1
			Collections.sort(groups, c);
			parentIndex = Collections.binarySearch(groups, comparable, c);

			// todo: multilevel comparator:  by depth-comparators.size(); for (int y = depth; y < comparators.size(); y++) {
			final T parent = groups.get(parentIndex);
			System.err.println("Parnet: " + parent);
			_callback.startSubGroup(depth, comparable, parent);*/
			final Comparator<T> c = _callback.currentGroupComparatorChain(depth - 1);

			final List<T> groups = _callback.availableGroups(depth - 1, comparable);
			Collections.sort(groups, c); // todo: comparators.get(depth-1) ???
			parentIndex = Collections.binarySearch(groups, comparable, comparators.get(depth - 1)); // todo: comparators.get(depth-1) ???

			if (LOGGER.isDebugEnabled()) {
				if (parentIndex < 0) {
					//System.out.println("############################### < 0");
				}

				System.err.println("\n== StartSubGroup ==\nindex: " + index + " depth: " + depth + " i: " + j);
				System.err.println("Classname: " + ((BugInstance) comparable).getPrimaryClass().getClassName().split("\\$")[0]);
				System.err.println("PAckage: " + ((BugInstance) comparable).getPrimaryClass().getPackageName());
				System.err.println("Message: " + ((BugInstance) comparable).getMessage());
				System.err.println("Category: " + I18N.instance().getBugCategoryDescription(((BugInstance) comparable).getBugPattern().getCategory()));
				System.err.println("Type: " + I18N.instance().getBugTypeDescription(((BugInstance) comparable).getBugPattern().getAbbrev()));
				System.err.println("Short: " + ((BugInstance) comparable).getBugPattern().getShortDescription());
			}

			final T parent = groups.get(parentIndex);


			if (LOGGER.isDebugEnabled()) {
				System.err.println("ParentIndex: " + parentIndex);
				System.err.println("ParentClassname: " + ((BugInstance) parent).getPrimaryClass().getClassName().split("\\$")[0]);
				System.err.println("ParentCategory: " + I18N.instance().getBugCategoryDescription(((BugInstance) parent).getBugPattern().getCategory()));
				System.err.println("ParentType: " + I18N.instance().getBugTypeDescription(((BugInstance) parent).getBugPattern().getAbbrev()));
				System.err.println("PArentShort: " + ((BugInstance) parent).getBugPattern().getShortDescription());
			}


			_callback.startSubGroup(depth, comparable, parent);
		} else if (index >= 0) {
			if (LOGGER.isDebugEnabled()) {
				System.err.println("\n#### AddToGroup");
				System.err.println("=== index: " + index + " depth: " + depth + " i: " + j);
				System.err.println("Classname: " + ((BugInstance) comparable).getPrimaryClass().getClassName().split("\\$")[0]);
				System.err.println("Classname: " + ((BugInstance) comparable).getPrimaryClass().getClassName());
				System.err.println("Category: " + I18N.instance().getBugCategoryDescription(((BugInstance) comparable).getBugPattern().getCategory()));
				System.err.println("Type: " + I18N.instance().getBugTypeDescription(((BugInstance) comparable).getBugPattern().getAbbrev()));
				System.err.println("Short: " + ((BugInstance) comparable).getBugPattern().getShortDescription());
				System.err.println("PriorityAbb: " + ((BugInstance) comparable).getPriorityAbbreviation());
				System.err.println("PriorityString: " + ((BugInstance) comparable).getPriorityString());
				System.err.println("PriorityTypeString: " + ((BugInstance) comparable).getPriorityTypeString());
			}

			final Comparator<T> c = _callback.currentGroupComparatorChain(depth); // todo: -1 ???

			final List<T> groups = _callback.availableGroups(depth, comparable); // todo: -1 ???
			Collections.sort(groups, c); // todo: -1 ???
			parentIndex = Collections.binarySearch(groups, comparable, c); // todo: -1 ???
			final T parent = groups.get(parentIndex);

			if (LOGGER.isDebugEnabled()) {
				System.err.println("ParentIndex: " + parentIndex);
				System.err.println("ParentClassname: " + ((BugInstance) parent).getPrimaryClass().getClassName().split("\\$")[0]);
				System.err.println("ParentCategory: " + I18N.instance().getBugCategoryDescription(((BugInstance) parent).getBugPattern().getCategory()));
				System.err.println("ParentType: " + I18N.instance().getBugTypeDescription(((BugInstance) parent).getBugPattern().getAbbrev()));
				System.err.println("PArentShort: " + ((BugInstance) parent).getBugPattern().getShortDescription());
			}

			_callback.addToGroup(depth, comparable, parent);
		}


	}

}
