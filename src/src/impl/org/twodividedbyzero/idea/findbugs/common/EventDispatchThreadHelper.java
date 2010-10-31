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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;


/**
 * $Date$
 *
 * @author Andre Pfeiler<andrep@twodividedbyzero.org>
 * @version $Revision$
 * @since 0.9.96-dev
 */
public final class EventDispatchThreadHelper {

	private static final Logger LOGGER = Logger.getInstance(EventDispatchThreadHelper.class.getName());


	public static void invokeAndWait(@NotNull final Operation operation) {
		assert operation != null : "Operation must not be null!";
		if (EventQueue.isDispatchThread()) {
			operation.run();
			operation.onSuccess();
		} else {
			try {
				EventQueue.invokeAndWait(operation);
				operation.onSuccess();
			} catch (final Exception e) {
				operation.onFailure(e);
			}
		}
	}


	@SuppressWarnings({"ThrowableInstanceNeverThrown"})
	public static void assertInEDT(final String message) {
		if (!EventQueue.isDispatchThread()) {
			final CallerStack caller = new CallerStack();
			final Throwable e = new NotInEDTViolation(message);
			CallerStack.initCallerStack(e, caller);
			LOGGER.debug(e);
		}
	}


	@SuppressWarnings({"ThrowableInstanceNeverThrown"})
	public static void assertInADT(final String message) {
		if (!ApplicationManager.getApplication().isDispatchThread()) {
			final CallerStack caller = new CallerStack();
			final Throwable e = new NotInADTViolation(message);
			CallerStack.initCallerStack(e, caller);
			LOGGER.debug(e);
		}
	}

	public static void assertInEDTorADT() {
		if (!EventQueue.isDispatchThread() && !ApplicationManager.getApplication().isDispatchThread()) {
			final CallerStack caller = new CallerStack();
			final Throwable e = new NotInEDTViolation("Should run in EventDispatchThread or ApplcationtDispatchThread.");
			CallerStack.initCallerStack(e, caller);
			LOGGER.debug(e);
		}
	}


	public static void assertInEDT() {
		assertInEDT("Should run in EventDispatchThread.");
	}


	public static void assertInADT() {
		assertInEDT("Should run in ApplcationtDispatchThread.");
	}


	public static boolean checkEDTViolation() {
		for (final String propertyName : new String[] { "checkedtviolation"}) {
			final String value = System.getProperty(propertyName);
			if ((value != null) && ("1".equals(value) || "yes".equalsIgnoreCase(value) || Boolean.valueOf(value))) {
				return true;
			}
		}
		return false;
	}


	public static void invokeLater(final Runnable runnable) {
		if (!EventQueue.isDispatchThread()) {
			@SuppressWarnings({"ThrowableInstanceNeverThrown"})
			final CallerStack caller = new CallerStack();
			final Runnable wrapper = new Runnable() {
				public void run() {
					try {
						runnable.run();
					} catch (RuntimeException e) {
						CallerStack.initCallerStack(e, caller);
						throw e;
					} catch (Error e) {
						CallerStack.initCallerStack(e, caller);
						throw e;
					}
				}
			};
			EventQueue.invokeLater(wrapper);
		} else {
			// we are already in EventDispatcherThread.. Runnable can be called inline
			runnable.run();
		}
	}


	public interface Operation extends Runnable {

		public void onFailure(Throwable failure);

		public void onSuccess();

	}


	public abstract static class OperationAdapter implements Operation {

		public void onFailure(final Throwable failure) {
			if (failure instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			} else if (failure instanceof InvocationTargetException) {
				final Throwable target = ((InvocationTargetException) failure).getTargetException();
				final StackTraceElement[] stackTrace = target.getStackTrace();
				final StringBuilder message = new StringBuilder("Operation failed. ");
				if (stackTrace.length > 2) {
					final StackTraceElement stackTraceElement = stackTrace[1];
					message.append(stackTraceElement.getClassName()).append('.').append(stackTraceElement.getMethodName()).append("() - ");
				}
				message.append(target);
				LOGGER.error(message.toString(), target);
			}
		}

		public void onSuccess() {
			// stub
		}

	}


	private EventDispatchThreadHelper() {
		// utility
	}


	private static class NotInEDTViolation extends Exception {

		private static final long serialVersionUID = 1L;


		NotInEDTViolation(final String message) {
			super(message);
		}
	}


	private static class NotInADTViolation extends Exception {

		private static final long serialVersionUID = 1L;


		NotInADTViolation(final String message) {
			super(message);
		}
	}


	private static class NotInEDTorADTViolation extends Exception {

		private static final long serialVersionUID = 1L;


		NotInEDTorADTViolation(final String message) {
			super(message);
		}
	}

}
