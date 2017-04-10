/*
 * placeRateEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.Transition;

/**
 *
 * @author corveau
 */
public class TransitionUpperBoundEdit extends UndoableEdit {

	Transition transition;
	int newUpperBound;
	int oldUpperBound;

	/** Creates a new instance of placeRateEdit */
	public TransitionUpperBoundEdit(Transition _transition, int _oldUpperBound, int _newUpperBound) {
		transition = _transition;
		oldUpperBound = _oldUpperBound;
		newUpperBound = _newUpperBound;
	}

	/** */
	public void undo() {
		transition.setUpperBound(oldUpperBound);
	}

	/** */
	public void redo() {
		transition.setUpperBound(newUpperBound);
	}

}
