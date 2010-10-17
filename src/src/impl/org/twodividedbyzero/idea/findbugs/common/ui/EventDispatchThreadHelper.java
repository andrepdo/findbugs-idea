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

package org.twodividedbyzero.idea.findbugs.common.ui;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.twodividedbyzero.idea.findbugs.common.CallerStack;

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
		
		void onFailure(Throwable failure);
		
		void onSuccess();
		
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

}
