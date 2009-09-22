/*
 * Copyright 2009 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.common.util;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ClassAnnotation;
import edu.umd.cs.findbugs.FieldAnnotation;
import edu.umd.cs.findbugs.I18N;
import edu.umd.cs.findbugs.MethodAnnotation;
import edu.umd.cs.findbugs.SourceLineAnnotation;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrepdo@dev.java.net>
 * @version $Revision$
 * @since 0.9.84-dev
 */
public class BugInstanceUtil {

	private BugInstanceUtil() {
		throw new UnsupportedOperationException();
	}


	public static MethodAnnotation getPrimaryMethod(final BugInstance bugInstance) {
		return bugInstance.getPrimaryMethod();
	}


	public static FieldAnnotation getPrimaryField(final BugInstance bugInstance) {
		return bugInstance.getPrimaryField();
	}


	public static String getAbridgedMessage(final BugInstance bugInstance) {
		return bugInstance.getAbridgedMessage();
	}


	public static ClassAnnotation getPrimaryClass(final BugInstance bugInstance) {
		return bugInstance.getPrimaryClass();
	}


	public static String getSimpleClassName(final BugInstance bugInstance) {
		return bugInstance.getPrimaryClass().getSourceLines().getSimpleClassName();
	}


	public static String getPackageName(final BugInstance bugInstance) {
		return bugInstance.getPrimaryClass().getSourceLines().getPackageName();
	}


	public static String getJavaSourceMethodName(final BugInstance bugInstance) {
		return getPrimaryMethod(bugInstance).getJavaSourceMethodName();
	}


	public static String getFullMethod(final BugInstance bugInstance) {
		return getPrimaryMethod(bugInstance).getFullMethod(getPrimaryClass(bugInstance));
	}


	public static String getMethodName(final BugInstance bugInstance) {
		return getPrimaryMethod(bugInstance).getMethodName();
	}


	public static String getFieldName(final BugInstance bugInstance) {
		return getPrimaryField(bugInstance).getFieldName();
	}


	public static String getPriorityString(final BugInstance bugInstance) {
		return bugInstance.getPriorityString();
	}


	public static String getPriorityTypeString(final BugInstance bugInstance) {
		return bugInstance.getPriorityTypeString();
	}


	public static String getBugCategoryDescription(final BugInstance bugInstance) {
		return I18N.instance().getBugCategoryDescription(bugInstance.getBugPattern().getCategory());
	}


	public static String getBugTypeDescription(final BugInstance bugInstance) {
		return I18N.instance().getBugTypeDescription(bugInstance.getBugPattern().getAbbrev());
	}


	public static String getBugPatternShortDescription(final BugInstance bugInstance) {
		return bugInstance.getBugPattern().getShortDescription();
	}


	public static String getDetailHtml(final BugInstance bugInstance) {
		final String bugDetailsKey = bugInstance.getType();
		return I18N.instance().getDetailHTML(bugDetailsKey);
	}


	public static String getDetailText(final BugInstance bugInstance) {
		return bugInstance.getBugPattern().getDetailText();
	}


	public static int[] getSourceLines(final BugInstance bugInstance) {
		final int[] lines = new int[2];
		final SourceLineAnnotation annotation = bugInstance.getPrimarySourceLineAnnotation();

		if (annotation != null) {
			lines[0] = annotation.getStartLine();
			lines[1] = annotation.getEndLine();
		} else {
			lines[0] = 1;
			lines[1] = 1;
		}

		return lines;
	}


	public static String getBugType(final BugInstance bugInstance) {
		return bugInstance.getBugPattern().getType();
	}
}
