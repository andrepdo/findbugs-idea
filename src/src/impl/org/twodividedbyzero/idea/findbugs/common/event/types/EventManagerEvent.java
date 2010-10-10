package org.twodividedbyzero.idea.findbugs.common.event.types;

import org.twodividedbyzero.idea.findbugs.common.event.Event;


/**
 * $Date$
 *
 * @version $Revision$
 */
public interface EventManagerEvent extends Event {

	enum Operation {

		LISTENER_ADDED,
		LISTENER_REMOVED
	}


	Operation getOperation();


}
