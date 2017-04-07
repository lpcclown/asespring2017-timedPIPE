/*
 * TransitionPriorityEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.Transition;


/**
 *
 * @author corveau
 */
public class TransitionFormulaEdit 
        extends UndoableEdit {
   
   Transition transition;
   String newPriority;
   String oldPriority;
   
   
   /** Creates a new instance of placePriorityEdit */
   public TransitionFormulaEdit(
           Transition _transition, String _oldPriority, String _newPriority) {
      transition = _transition;
      oldPriority = _oldPriority;      
      newPriority = _newPriority;
   }

   
   /** */
   public void undo() {
      transition.setFormula(oldPriority);
   }

   
   /** */
   public void redo() {
      transition.setFormula(newPriority);
   }
   
}
