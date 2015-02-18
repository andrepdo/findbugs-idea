package org.twodividedbyzero.idea.findbugs.messages;

import com.intellij.util.messages.Topic;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.ProjectStats;
import org.jetbrains.annotations.NotNull;

public interface NewBugInstanceListener {
	Topic<NewBugInstanceListener> TOPIC = Topic.create("New Bug Instance", NewBugInstanceListener.class);

	/**
	 * Invoked by EDT.
	 *
	 * @param bugInstance ..
	 * @param projectStats ..
	 */
	void newBugInstance(@NotNull final BugInstance bugInstance, @NotNull final ProjectStats projectStats);

}