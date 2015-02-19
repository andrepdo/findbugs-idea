/*
 * Copyright 2008-2015 Andre Pfeiler
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
package org.twodividedbyzero.idea.findbugs.messages;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import edu.umd.cs.findbugs.BugCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.twodividedbyzero.idea.findbugs.common.EventDispatchThreadHelper;
import org.twodividedbyzero.idea.findbugs.common.util.New;
import org.twodividedbyzero.idea.findbugs.core.FindBugsProject;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;


/**
 * @author Reto Merz<reto.merz@gmail.com>
 * @version $Revision: 358 $
 * @since 0.9.995
 */
public final class MessageBusManager {


	private static final Map<Project, Map<Object, Pair<MessageBusConnection, Set<Topic<?>>>>> _connections = New.map();


	private MessageBusManager() {
	}


	public static <L extends AnalysisStateListener> void subscribeAnalysisState(@NotNull final Project project, @NotNull final Object subscriber, @NotNull final L handler) {
		subscribe(project, subscriber, AnalysisStartedListener.TOPIC, handler);
		subscribe(project, subscriber, AnalysisAbortedListener.TOPIC, handler);
		subscribe(project, subscriber, AnalysisFinishedListener.TOPIC, handler);
	}


	/**
	 * Note that you can only subscribe *one* {@code handler} per {@code topic}.
	 * If {@code subscriber} has subscribed the topic already, nothing is done.
	 *
	 * @param project ..
	 * @param subscriber ..
	 * @param topic ..
	 * @param handler ..
	 * @param <L>.
	 */
	public static <L> void subscribe(@NotNull final Project project, @NotNull final Object subscriber, @NotNull final Topic<L> topic, @NotNull final L handler) {
		EventDispatchThreadHelper.checkEDT();
		Map<Object, Pair<MessageBusConnection, Set<Topic<?>>>> perProject = _connections.get(project);
		if (perProject == null) {
			perProject = New.map();
			_connections.put(project, perProject);
		}
		final Pair<MessageBusConnection, Set<Topic<?>>> subscriberPair = perProject.get(subscriber);
		Set<Topic<?>> topics;
		if (subscriberPair == null) {
			final MessageBusConnection newConnection =  ApplicationManager.getApplication().getMessageBus().connect();
			topics = New.set();
			topics.add(topic);
			newConnection.subscribe(topic, handler);
			perProject.put(subscriber, Pair.create(newConnection, topics));
		} else {
			topics = subscriberPair.getSecond();
			if (!topics.contains(topic)) {
				topics.add(topic);
				subscriberPair.getFirst().subscribe(topic, handler);
			} // else do nothing ; topic already subscriber has already subscribed this topic
		}
	}


	public static void publishAnalysisStartedToEDT() {
		EventDispatchThreadHelper.checkNotEDT();
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			@Override
			public void run() {
				publish(AnalysisStartedListener.TOPIC).analysisStarted();
			}
		});
	}


	public static void publishAnalysisAbortedToEDT() {
		EventDispatchThreadHelper.checkNotEDT();
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			@Override
			public void run() {
				publish(AnalysisAbortedListener.TOPIC).analysisAborted();
			}
		});
	}


	public static void publishAnalysisFinishedToEDT(@NotNull final BugCollection bugCollection, @Nullable final FindBugsProject findBugsProject) {
		EventDispatchThreadHelper.checkNotEDT();
		/**
		 * Guarantee thread visibility *one* time.
		 */
		final AtomicReference<BugCollection> bugCollectionRef = New.atomicRef(bugCollection);
		final AtomicReference<FindBugsProject> findBugsProjectRef = New.atomicRef(findBugsProject);
		EventDispatchThreadHelper.invokeLater(new Runnable() {
			@Override
			public void run() {
				publish(AnalysisFinishedListener.TOPIC).analysisFinished(bugCollectionRef.get(), findBugsProjectRef.get());
			}
		});
	}


	@NotNull
	public static <L> L publish(@NotNull final Topic<L> topic) {
		EventDispatchThreadHelper.checkEDT();
		return ApplicationManager.getApplication().getMessageBus().syncPublisher(topic);
	}


	public static void dispose(@NotNull final Project project) {
		EventDispatchThreadHelper.checkEDT();
		final Map<Object, Pair<MessageBusConnection, Set<Topic<?>>>> perProject = _connections.remove(project);
		if (perProject != null) {
			for (Pair<MessageBusConnection, ?> subscriberPair : perProject.values()) {
				Disposer.dispose(subscriberPair.getFirst());
			}
		}
	}
}