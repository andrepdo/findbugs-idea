/*
 * Copyright 2008-2016 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.gui.tree;

import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.BugRankCategory;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.core.Bug;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BugInstanceComparator {

	private BugInstanceComparator() {
	}

	/**
	 * Compare BugInstance class names.
	 * This is useful for grouping bug instances by class.
	 * Note that all instances with the same class name will compare
	 * as equal.
	 */
	static class BugInstanceClassComparator implements Comparator<Bug> {
		@Override
		public int compare(@NotNull final Bug o1, @NotNull final Bug o2) {
			return getClassName(o1).compareTo(getClassName(o2));
		}

		static String getClassName(@NotNull final Bug bugInstance) {
			return bugInstance.getInstance().getPrimaryClass().getClassName().split("\\$")[0];
		}
	}

	/**
	 * The instance of BugInstanceClassComparator.
	 */
	private static final Comparator<Bug> _bugInstanceClassComparator = new BugInstanceClassComparator();

	/**
	 * Compare BugInstance package names.
	 * This is useful for grouping bug instances by package.
	 * Note that all instances with the same package name will compare
	 * as equal.
	 */
	static class BugInstancePackageComparator implements Comparator<Bug> {
		@Override
		public int compare(@NotNull final Bug o1, @NotNull final Bug o2) {
			return getPackageName(o1).compareTo(getPackageName(o2));
		}

		static String getPackageName(@NotNull final Bug bug) {
			return bug.getInstance().getPrimaryClass().getPackageName();
		}
	}

	/**
	 * The instance of BugInstancePackageComparator.
	 */
	private static final Comparator<Bug> _bugInstancePackageComparator = new BugInstancePackageComparator();

	/**
	 * Compare BugInstance bug types.
	 * This is useful for grouping bug instances by bug type.
	 * Note that all instances with the same bug type will compare
	 * as equal.
	 */
	static class BugInstanceTypeComparator implements Comparator<Bug> {
		@Override
		public int compare(@NotNull final Bug o1, @NotNull final Bug o2) {
			return getTypeDescription(o1).compareTo(getTypeDescription(o2));
		}

		static String getTypeDescription(@NotNull final Bug bug) {
			final BugPattern bugPattern = bug.getInstance().getBugPattern();
			return bugPattern.getAbbrev();
		}
	}

	/**
	 * The instance of BugInstanceTypeComparator.
	 */
	private static final Comparator<Bug> _bugInstanceTypeComparator = new BugInstanceTypeComparator();

	/**
	 * Compare BugInstance bug categories.
	 * This is useful for grouping bug instances by bug category.
	 * Note that all instances with the same bug category will compare
	 * as equal.
	 */
	static class BugInstanceCategoryComparator implements Comparator<Bug> {
		@Override
		public int compare(@NotNull final Bug o1, @NotNull final Bug o2) {
			return getCategory(o1).compareTo(getCategory(o2));
		}

		static String getCategory(@NotNull final Bug bugInstance) {
			final BugPattern bugPattern = bugInstance.getInstance().getBugPattern();
			return bugPattern.getCategory();
		}
	}

	/**
	 * The instance of BugInstanceCategoryComparator.
	 */
	private static final Comparator<Bug> _bugInstanceCategoryComparator = new BugInstanceCategoryComparator();

	/**
	 * Compare BugInstance bug short description.
	 * This is useful for grouping bug instances by bug short description.
	 * Note that all instances with the same bug short description will compare
	 * as equal.
	 */
	static class BugInstanceShortDescrComparator implements Comparator<Bug> {
		@Override
		public int compare(@NotNull final Bug o1, @NotNull final Bug o2) {
			return getShortDescr(o1).compareTo(getShortDescr(o2));
		}

		static String getShortDescr(@NotNull final Bug bug) {
			final BugPattern bugPattern = bug.getInstance().getBugPattern();
			return bugPattern.getShortDescription();
		}
	}

	/**
	 * The instance of BugInstanceCategoryComparator.
	 */
	private static final Comparator<Bug> _bugInstanceShortDescrComparator = new BugInstanceShortDescrComparator();

	static class BugInstancePriorityComparator implements Comparator<Bug> {
		@Override
		public int compare(@NotNull final Bug o1, @NotNull final Bug o2) {
			return getPriorityString(o1).compareTo(getPriorityString(o2));
		}

		static String getPriorityString(@NotNull final Bug bug) {
			return bug.getInstance().getPriorityString();
		}
	}

	/**
	 * The instance of BugInstancePriorityComparator.
	 */
	private static final Comparator<Bug> _bugInstancePriorityComparator = new BugInstancePriorityComparator();

	private static class BugInstanceBugRankComparator implements Comparator<Bug> {
		@Override
		public int compare(@NotNull final Bug o1, @NotNull final Bug o2) {
			return BugRankCategory.getRank(o1.getInstance().getBugRank()).compareTo(BugRankCategory.getRank(o2.getInstance().getBugRank()));
		}
	}

	/**
	 * The instance of BugInstancePriorityComparator.
	 */
	private static final Comparator<Bug> _bugInstanceBugRankComparator = new BugInstanceBugRankComparator();

	public static Comparator<Bug> getBugInstanceClassComparator() {
		return _bugInstanceClassComparator;
	}

	static Comparator<Bug> getBugInstancePackageComparator() {
		return _bugInstancePackageComparator;
	}

	static Comparator<Bug> getBugInstanceTypeComparator() {
		return _bugInstanceTypeComparator;
	}

	static Comparator<Bug> getBugInstanceShortDescrComparator() {
		return _bugInstanceShortDescrComparator;
	}

	static Comparator<Bug> getBugInstanceCategoryComparator() {
		return _bugInstanceCategoryComparator;
	}

	static Comparator<Bug> getBugInstancePriorityComparator() {
		return _bugInstancePriorityComparator;
	}

	private static Comparator<Bug> getBugInstanceBugRankComparator() {
		return _bugInstanceBugRankComparator;
	}

	public static Comparator<Bug> getBugInstanceComparator(@NotNull final GroupBy groupBy) {
		switch (groupBy) {
			case Class:
				return getBugInstanceClassComparator();
			case Package:
				return getBugInstancePackageComparator();
			case BugCategory:
				return getBugInstanceCategoryComparator();
			case BugType:
				return getBugInstanceTypeComparator();
			case BugShortDescription:
				return getBugInstanceShortDescrComparator();
			case Priority:
				return getBugInstancePriorityComparator();
			case BugRank:
				return getBugInstanceBugRankComparator();
			default:
				throw new IllegalArgumentException("Bad sort order: " + groupBy);
		}
	}

	public static List<Comparator<Bug>> getGroupComparators(@NotNull final GroupBy[] orderGroups) {
		final List<Comparator<Bug>> comp = new ArrayList<Comparator<Bug>>();
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

	public static class ComparatorChain implements Comparator<Bug> {

		private final int _compareDepth;
		private final GroupBy[] _groupBy;
		final transient List<Comparator<Bug>> _comparators;

		public ComparatorChain(final int compareDepth, final GroupBy[] groupBy) {
			_compareDepth = compareDepth;
			_groupBy = groupBy.clone();
			_comparators = getGroupComparators(groupBy);
		}

		@Override
		public int compare(@NotNull final Bug o1, @NotNull final Bug o2) {
			int ret = -1;
			for (int i = 0; i < _comparators.size(); i++) {
				final Comparator<Bug> comp = _comparators.get(i);
				ret = comp.compare(o1, o2);
				if (_compareDepth == i || ret != 0) {
					return ret;
				}
			}
			return ret;
		}
	}

	public static Comparator<Bug> getComparatorChain(final int depth, final GroupBy[] groupBy) {
		return new ComparatorChain(depth, groupBy);
	}
}
