/*
 * placeRateEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.Transition;

/**
 *
 * @author corveau
 */
public class TransitionLowerBoundEdit extends UndoableEdit {

	Transition transition;
	int newLowerBound;
	int oldLowerBound;

	/** Creates a new instance of placeRateEdit */
	public TransitionLowerBoundEdit(Transition _transition, int _oldLowerBound, int _newLowerBound) {
		transition = _transition;
		oldLowerBound = _oldLowerBound;
		newLowerBound = _newLowerBound;
	}

	/** */
	public void undo() {
		transition.setLowerBound(oldLowerBound);
	}

	/** */
	public void redo() {
		transition.setLowerBound(newLowerBound);
	}

}
