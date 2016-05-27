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
package org.twodividedbyzero.idea.findbugs.core;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.actions.HelpAction;
import org.twodividedbyzero.idea.findbugs.common.util.ErrorUtil;
import org.twodividedbyzero.idea.findbugs.common.util.FindBugsUtil;
import org.twodividedbyzero.idea.findbugs.resources.ResourcesLoader;

import java.awt.Component;
import java.awt.datatransfer.StringSelection;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class ErrorReportSubmitterImpl extends ErrorReportSubmitter {

	@Override
	public String getReportActionText() {
		return StringUtil.capitalizeWords(ResourcesLoader.getString("error.submitReport.title"), true);
	}

	@SuppressWarnings("deprecation") // maximize compatibility
	@Override
	public SubmittedReportInfo submit(IdeaLoggingEvent[] events, Component parentComponent) {
		return submitImpl(events, null, parentComponent);
	}

	@SuppressWarnings("deprecation") // maximize compatibility
	@Override
	public boolean trySubmitAsync(IdeaLoggingEvent[] events, String additionalInfo, Component parentComponent, Consumer<SubmittedReportInfo> consumer) {
		consumer.consume(submitImpl(events, additionalInfo, parentComponent));
		return true;
	}

	@NotNull
	private SubmittedReportInfo submitImpl(
			@NotNull final IdeaLoggingEvent[] events,
			@Nullable final String additionalInfo,
			@NotNull final Component parentComponent
	) {

		final StringBuilder body = HelpAction.createProductInfo();
		if (!StringUtil.isEmptyOrSpaces(additionalInfo)) {
			body.append("\n\nAdditional Infos\n");
			body.append(additionalInfo);
		}

		boolean isFindBugsError = true;
		String title = null;
		for (final IdeaLoggingEvent event : events) {
			final String message = event.getMessage();
			final boolean hasMessage = !StringUtil.isEmptyOrSpaces(message);
			final String stack = event.getThrowableText();
			final boolean hasStack = !StringUtil.isEmptyOrSpaces(stack);
			if (title == null && hasMessage) {
				title = message;
			}
			if (hasMessage || hasStack) {
				body.append("\n\n");
				if (hasMessage) {
					body.append(message);
					if (hasStack) {
						body.append("\n");
					}
				}
				if (hasStack) {
					body.append(stack);
				}
			}
			if (!hasStack) {
				isFindBugsError = false;
			} else if (!FindBugsUtil.isFindBugsError(event.getThrowable())) {
				isFindBugsError = false;
			}
		}
		if (title == null) {
			title = "Analysis Error";
		}

		return openBrowser(
				isFindBugsError,
				title,
				body.toString(),
				parentComponent
		);
	}

	@NotNull
	private SubmittedReportInfo openBrowser(
			final boolean isFindBugsError,
			@NotNull final String title,
			@NotNull final String errorText,
			@SuppressWarnings("UnusedParameters") @NotNull final Component parentComponent
	) {

		final String baseUrl;
		if (isFindBugsError) {
			// http://sourceforge.net/p/findbugs/bugs/
			// is locked - assume we should report on github - 19.9.2015
			baseUrl = "https://github.com/findbugsproject/findbugs/issues";
		} else {
			baseUrl = "https://github.com/andrepdo/findbugs-idea/issues";
		}

		/**
		 * Note: set errorText as body does not work:
		 *   - can cause HTTP 414 Request URI too long
		 *   - if user is not yet logged in github login page will show an error
		 *     502 - "This page is taking way too long to load." (this will also occure with HTTP POST).
		 */
		final String body = "The error was copied to the clipboard. Press " + (SystemInfo.isMac ? "Command+V" : "Ctrl+V");
		final String newIssueUrl = baseUrl + "/new?title=" + encode(title) + "&body=" + encode(body);

		CopyPasteManager.getInstance().setContents(new StringSelection(errorText));
		BrowserUtil.browse(newIssueUrl);
		return new SubmittedReportInfo(baseUrl, "issue", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
	}

	@NotNull
	private static String encode(@NotNull final String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw ErrorUtil.toUnchecked(e); // all system support UTF-8
		}
	}
}
