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
package org.twodividedbyzero.idea.findbugs.gui.tree;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.0.1
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings({"SE_TRANSIENT_FIELD_NOT_RESTORED"})
public class BugInstanceComparator {

	private BugInstanceComparator() {
	}


	/**
	 * Compare BugInstance class names.
	 * This is useful for grouping bug instances by class.
	 * Note that all instances with the same class name will compare
	 * as equal.
	 */
	public static class BugInstanceClassComparator implements Comparator<BugInstance>, Serializable {

		private static final long serialVersionUID = 0L;


		public int compare(final BugInstance lhs, final BugInstance rhs) {
			return getClassName(lhs).compareTo(getClassName(rhs));
		}


		@SuppressWarnings({"MethodMayBeStatic"})
		public String getClassName(final BugInstance bugInstance) {
			return bugInstance.getPrimaryClass().getClassName().split("\\$")[0];
		}
	}

	/** The instance of BugInstanceClassComparator. */
	public static final Comparator<BugInstance> _bugInstanceClassComparator = new BugInstanceClassComparator();


	/**
	 * Compare BugInstance package names.
	 * This is useful for grouping bug instances by package.
	 * Note that all instances with the same package name will compare
	 * as equal.
	 */
	public static class BugInstancePackageComparator implements Comparator<BugInstance>, Serializable {

		private static final long serialVersionUID = 0L;


		public int compare(final BugInstance lhs, final BugInstance rhs) {
			return getPackageName(lhs).compareTo(getPackageName(rhs));
		}


		@SuppressWarnings({"MethodMayBeStatic"})
		public String getPackageName(final BugInstance bugInstance) {
			return bugInstance.getPrimaryClass().getPackageName();
		}
	}

	/** The instance of BugInstancePackageComparator. */
	public static final Comparator<BugInstance> _bugInstancePackageComparator = new BugInstancePackageComparator();


	/**
	 * Compare BugInstance bug types.
	 * This is useful for grouping bug instances by bug type.
	 * Note that all instances with the same bug type will compare
	 * as equal.
	 */
	public static class BugInstanceTypeComparator implements Comparator<BugInstance>, Serializable {

		private static final long serialVersionUID = 0L;


		public int compare(final BugInstance lhs, final BugInstance rhs) {
			return getTypeDescription(lhs).compareTo(getTypeDescription(rhs));
		}


		@SuppressWarnings({"MethodMayBeStatic"})
		public String getTypeDescription(final BugInstance bugInstance) {
			final BugPattern bugPattern = bugInstance.getBugPattern();
			return bugPattern.getAbbrev();
		}
	}

	/** The instance of BugInstanceTypeComparator. */
	public static final Comparator<BugInstance> _bugInstanceTypeComparator = new BugInstanceTypeComparator();


	/**
	 * Compare BugInstance bug categories.
	 * This is useful for grouping bug instances by bug category.
	 * Note that all instances with the same bug category will compare
	 * as equal.
	 */
	public static class BugInstanceCategoryComparator implements Comparator<BugInstance>, Serializable {

		private static final long serialVersionUID = 0L;


		public int compare(final BugInstance lhs, final BugInstance rhs) {
			return getCategory(lhs).compareTo(getCategory(rhs));
		}


		@SuppressWarnings({"MethodMayBeStatic"})
		public String getCategory(final BugInstance bugInstance) {
			final BugPattern bugPattern = bugInstance.getBugPattern();
			return bugPattern.getCategory();
		}
	}

	/** The instance of BugInstanceCategoryComparator. */
	public static final Comparator<BugInstance> _bugInstanceCategoryComparator = new BugInstanceCategoryComparator();


	/**
	 * Compare BugInstance bug short description.
	 * This is useful for grouping bug instances by bug short description.
	 * Note that all instances with the same bug short description will compare
	 * as equal.
	 */
	public static class BugInstanceShortDescrComparator implements Comparator<BugInstance>, Serializable {

		private static final long serialVersionUID = 0L;


		public int compare(final BugInstance lhs, final BugInstance rhs) {
			return getShortDescr(lhs).compareTo(getShortDescr(rhs));
		}


		@SuppressWarnings({"MethodMayBeStatic"})
		public String getShortDescr(final BugInstance warning) {
			final BugPattern bugPattern = warning.getBugPattern();
			return bugPattern.getShortDescription();
		}
	}


	/** The instance of BugInstanceCategoryComparator. */
	public static final Comparator<BugInstance> _bugInstanceShortDescrComparator = new BugInstanceShortDescrComparator();


	public static class BugInstancePriorityComparator implements Comparator<BugInstance>, Serializable {

		private static final long serialVersionUID = 0L;


		public int compare(final BugInstance lhs, final BugInstance rhs) {
			return getPriorityString(lhs).compareTo(getPriorityString(rhs));
		}


		@SuppressWarnings({"MethodMayBeStatic"})
		public String getPriorityString(final BugInstance bugInstance) {
			return bugInstance.getPriorityString();
		}
	}


	/** The instance of BugInstancePriorityComparator. */
	public static final Comparator<BugInstance> _bugInstancePriorityComparator = new BugInstancePriorityComparator();


	public static class BugInstanceBugRankComparator implements Comparator<BugInstance>, Serializable {

		private static final long serialVersionUID = 0L;


		public int compare(final BugInstance lhs, final BugInstance rhs) {
			return getBugRank(lhs).compareTo(getBugRank(rhs));
		}


		@SuppressWarnings({"MethodMayBeStatic"})
		public Integer getBugRank(final BugInstance bugInstance) {
			return bugInstance.getBugRank();
		}
	}

	/** The instance of BugInstancePriorityComparator. */
	public static final Comparator<BugInstance> _bugInstanceBugRankComparator = new BugInstanceBugRankComparator();


	public static Comparator<BugInstance> getBugInstanceClassComparator() {
		return _bugInstanceClassComparator;
	}


	public static Comparator<BugInstance> getBugInstancePackageComparator() {
		return _bugInstancePackageComparator;
	}


	public static Comparator<BugInstance> getBugInstanceTypeComparator() {
		return _bugInstanceTypeComparator;
	}


	public static Comparator<BugInstance> getBugInstanceShortDescrComparator() {
		return _bugInstanceShortDescrComparator;
	}


	public static Comparator<BugInstance> getBugInstanceCategoryComparator() {
		return _bugInstanceCategoryComparator;
	}


	public static Comparator<BugInstance> getBugInstancePriorityComparator() {
		return _bugInstancePriorityComparator;
	}


	public static Comparator<BugInstance> getBugInstanceBugRankComparator() {
		return _bugInstanceBugRankComparator;
	}


	public static Comparator<BugInstance> getBugInstanceComparator(final GroupBy groupBy) {
		if (groupBy.equals(GroupBy.Class)) {
			return getBugInstanceClassComparator();
		} else if (groupBy.equals(GroupBy.Package)) {
			return getBugInstancePackageComparator();
		} else if (groupBy.equals(GroupBy.BugCategory)) {
			return getBugInstanceCategoryComparator();
		} else if (groupBy.equals(GroupBy.BugType)) {
			return getBugInstanceTypeComparator();
		} else if (groupBy.equals(GroupBy.BugShortDescription)) {
			return getBugInstanceShortDescrComparator();
		} else if (groupBy.equals(GroupBy.Priority)) {
			return getBugInstancePriorityComparator();
		} else if (groupBy.equals(GroupBy.BugRank)) {
			return getBugInstanceBugRankComparator();
		} else {
			throw new IllegalArgumentException("Bad sort order: " + groupBy);
		}
	}


	public static List<Comparator<BugInstance>> getGroupComparators(final GroupBy[] orderGroups) {
		final List<Comparator<BugInstance>> comp = new ArrayList<Comparator<BugInstance>>();

		for (final GroupBy groupBy : orderGroups) {

			switch (groupBy) {

				case BugCategory:
					comp.add(getBugInstanceCategoryComparator());
					break;
				case BugShortDescription:
					comp.add(getBugInstanceShortDescrComparator());
					break;
				case BugType:
					comp.add(getBugInstanceTypeComparator());
					break;
				case Class:
					comp.add(getBugInstanceClassComparator());
					break;
				case Package:
					comp.add(getBugInstancePackageComparator());
					break;
				case Priority:
					comp.add(getBugInstancePriorityComparator());
					break;
				case BugRank:
					comp.add(getBugInstanceBugRankComparator());
					break;
				default:
					throw new IllegalArgumentException("Bad group order: " + groupBy);
			}
		}

		return comp;
	}


	public static class ComparatorChain implements Comparator<BugInstance>, Serializable {

		private static final long serialVersionUID = 0L;
		private final int _compareDepth;
		private final GroupBy[] _groupBy;
		final transient List<Comparator<BugInstance>> _comparators;


		public ComparatorChain(final int compareDepth, final GroupBy[] groupBy) {
			_compareDepth = compareDepth;
			_groupBy = groupBy.clone();
			_comparators = getGroupComparators(groupBy);
		}


		public int compare(final BugInstance lhs, final BugInstance rhs) {
			int retval = -1;
			for (int i = 0; i < _comparators.size(); i++) {
				final Comparator<BugInstance> comp = _comparators.get(i);
				retval = comp.compare(lhs, rhs);

				if (_compareDepth == i || retval != 0) {
					return retval;
				}

			}

			return retval;
		}
	}


	public static Comparator<BugInstance> getComparatorChain(final int depth, final GroupBy[] groupBy) {
		return new ComparatorChain(depth, groupBy);
	}

}
