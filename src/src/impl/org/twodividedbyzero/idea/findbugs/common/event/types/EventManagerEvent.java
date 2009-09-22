package org.twodividedbyzero.idea.findbugs.common.event.types;

import org.twodividedbyzero.idea.findbugs.common.event.Event;


/**
 * $Date$
 *
 * @version $Revision$
 */
public interface EventManagerEvent extends Event {

	public enum Operation {

		LISTENER_ADDED,
		LISTENER_REMOVED
	}


	public Operation getOperation();


}
