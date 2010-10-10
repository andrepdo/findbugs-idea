package org.twodividedbyzero.idea.findbugs.common.event.types;

import org.twodividedbyzero.idea.findbugs.common.event.EventImpl;


/**
 * $Date$
 *
 * @version $Revision$
 */
public class EventManagerEventImpl extends EventImpl implements EventManagerEvent {

	private static final long serialVersionUID = 0L;

	private final Operation _operation;


	public EventManagerEventImpl(final Operation operation) {
		super(EventType.EVENT_MANAGER);
		_operation = operation;
	}


	public Operation getOperation() {
		return _operation;
	}


	@Override
	public String toString() {
		return "EventManagerEventImpl{" + "_operation=" + _operation + '}';
	}
}
