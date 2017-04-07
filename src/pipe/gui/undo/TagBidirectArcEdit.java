/*
 * TagArcEdit.java
 */
package pipe.gui.undo;

import pipe.dataLayer.BidirectionalArc;


public class TagBidirectArcEdit 
        extends UndoableEdit {
   
   BidirectionalArc arc;
   
   
   /** Creates a new instance of TagArcEdit */
   public TagBidirectArcEdit(BidirectionalArc _arc) {
      arc = _arc;
   }

   
   /** */
   public void undo() {
      arc.setTagged(!arc.isTagged());
   }

   
   /** */
   public void redo() {
      arc.setTagged(!arc.isTagged());
   }
   
}
