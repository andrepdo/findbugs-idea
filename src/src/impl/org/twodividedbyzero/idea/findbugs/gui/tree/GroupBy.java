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

package org.twodividedbyzero.idea.findbugs.gui.tree;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugRankCategory;
import edu.umd.cs.findbugs.I18N;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator.BugInstanceCategoryComparator;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator.BugInstanceClassComparator;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator.BugInstancePackageComparator;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator.BugInstancePriorityComparator;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator.BugInstanceShortDescrComparator;
import org.twodividedbyzero.idea.findbugs.gui.tree.BugInstanceComparator.BugInstanceTypeComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.0.1
 */
public enum GroupBy {

	BugShortDescription("by bug short description"),
	BugCategory("by bug category"),
	BugType("by bug type"),
	Class("by class"),
	Package("by package"),
	Priority("by priority"),
	BugRank("by bug rank");


	private final String _description;


	GroupBy(final String description) {
		_description = description;
	}


	public String getDescription() {
		return _description;
	}


	public static List<GroupBy> getGroupByList() {
		final List<GroupBy> list = new ArrayList<GroupBy>();
		list.addAll(Arrays.asList(GroupBy.values()));

		return list;
	}


	public static String getBugCategory(final BugInstance bugInstance) {
		final BugInstanceCategoryComparator categoryComparator = (BugInstanceCategoryComparator) BugInstanceComparator.getBugInstanceCategoryComparator();
		return categoryComparator.getCategory(bugInstance);
	}


	public static String getGroupName(final GroupBy groupBy, final BugInstance bugInstance) {

		if (!Locale.ENGLISH.equals(Locale.getDefault())) {
			Locale.setDefault(Locale.ENGLISH);
		}

		final String groupName;
		switch (groupBy) {

			case BugCategory:
				final BugInstanceCategoryComparator categoryComparator = (BugInstanceCategoryComparator) BugInstanceComparator.getBugInstanceCategoryComparator();
				final String category = categoryComparator.getCategory(bugInstance);
				groupName = I18N.instance().getBugCategoryDescription(category);
				break;
			case BugShortDescription:
				final BugInstanceShortDescrComparator shortDescrComparator = (BugInstanceShortDescrComparator) BugInstanceComparator.getBugInstanceShortDescrComparator();
				groupName = shortDescrComparator.getShortDescr(bugInstance);
				break;
			case BugType:
				final BugInstanceTypeComparator typeComparator = (BugInstanceTypeComparator) BugInstanceComparator.getBugInstanceTypeComparator();
				final String type = typeComparator.getTypeDescription(bugInstance);
				groupName = I18N.instance().getBugTypeDescription(type);
				break;
			case Class:
				final BugInstanceClassComparator classComparator = (BugInstanceClassComparator) BugInstanceComparator.getBugInstanceClassComparator();
				groupName = classComparator.getClassName(bugInstance);
				break;
			case Package:
				final BugInstancePackageComparator packageComparator = (BugInstancePackageComparator) BugInstanceComparator.getBugInstancePackageComparator();
				groupName = packageComparator.getPackageName(bugInstance);
				break;
			case Priority:
				final BugInstancePriorityComparator priorityComparator = (BugInstancePriorityComparator) BugInstanceComparator.getBugInstancePriorityComparator();
				groupName = priorityComparator.getPriorityString(bugInstance);
				break;
			case BugRank:
				groupName = BugRankCategory.getRank(bugInstance.getBugRank()).toString();
				break;
			default:
				throw new IllegalStateException("Unknown group order: " + groupBy);
		}
		return groupName;
	}


	/**
	 * @param groupBy the primary group
	 * @return the specific sort order group
	 * @see org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy#BugCategory
	 * @see org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy#Class
	 * @see org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy#Package
	 * @see org.twodividedbyzero.idea.findbugs.gui.tree.GroupBy#Priority
	 */
	public static GroupBy[] getSortOrderGroup(final GroupBy groupBy) {

		switch (groupBy) {

			case BugCategory:
				return new GroupBy[] {BugCategory, BugType, BugShortDescription}; // todo: 2:Package, 3:Class, 4:Priority
			case Class:
				return new GroupBy[] {Class, BugCategory, BugType, BugShortDescription}; // todo: 1:Package, 3:Priority
			case Package:
				return new GroupBy[] {Package, BugCategory, BugType, BugShortDescription}; // todo: 2:Priority, 3:Class
			case Priority:
				return new GroupBy[] {Priority, BugCategory, BugType, BugShortDescription}; // todo: 2:Package, 3:Class
			case BugRank:
				return new GroupBy[] {BugRank, /*BugCategory,*/ BugType, BugShortDescription};
			default:
				throw new IllegalStateException("Unknown sort order group: " + groupBy);
		}
	}

	// todo: getAvailGroupsForPrimaryGroup ??? static !!??


	public static GroupBy[] getAvailableGroups(final GroupBy[] currentGroupBy) {
		final List<GroupBy> result = new ArrayList<GroupBy>();
		final List<GroupBy> list = Arrays.asList(currentGroupBy);

		for (final GroupBy groupBy : GroupBy.values()) {
			if (!list.contains(groupBy)) {
				result.add(groupBy);
			}
		}

		return result.toArray(new GroupBy[result.size()]);
	}
}
