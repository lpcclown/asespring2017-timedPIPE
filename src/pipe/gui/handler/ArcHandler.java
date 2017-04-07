package pipe.gui.handler;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import pipe.dataLayer.Arc;
import pipe.dataLayer.BidirectionalArc;
import pipe.dataLayer.InhibitorArc;
import pipe.dataLayer.NormalArc;
import pipe.dataLayer.Place;
import pipe.gui.CreateGui;
import pipe.gui.action.EditTaggedAction;
import pipe.gui.Grid;
import pipe.gui.GuiView;
import pipe.gui.Pipe;
import pipe.gui.action.EditWeightAction;
import pipe.gui.action.InsertPointAction;
import pipe.gui.action.SplitArcAction;
import pipe.gui.undo.UndoManager;


/**
 * Class used to implement methods corresponding to mouse events on arcs.
 */
public class ArcHandler 
        extends PetriNetObjectHandler {

   
   public ArcHandler(Container contentpane, Arc obj) {
      super(contentpane, obj);
      enablePopup = true;
   }

   
   /** 
    * Creates the popup menu that the user will see when they right click on a 
    * component 
    */
   public JPopupMenu getPopup(MouseEvent e) {
      int popupIndex = 0;
      JMenuItem menuItem;
      JPopupMenu popup = super.getPopup(e);
      
      menuItem = new JMenuItem("Edit Variable");      
      menuItem.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            ((Arc)myObject).showEditor();
         }
      }); 
      popup.insert(menuItem, popupIndex++);
      
      
      if (myObject instanceof InhibitorArc) {
         menuItem = new JMenuItem(new EditWeightAction(contentPane,
                 (Arc)myObject));
         menuItem.setText("Edit Weight");
         popup.insert(menuItem, popupIndex++);

         menuItem = new JMenuItem(new SplitArcAction((Arc)myObject, 
                                                      e.getPoint()));
         menuItem.setText("Split Arc Segment");
         popup.insert(menuItem, popupIndex++);

         popup.insert(new JPopupMenu.Separator(), popupIndex++);         
      } else if(myObject instanceof BidirectionalArc) {
          menuItem = new JMenuItem(new EditWeightAction(contentPane,
                  (Arc)myObject));
          menuItem.setText("Edit Weight");
          popup.insert(menuItem, popupIndex++);

          menuItem = new JMenuItem(new SplitArcAction((Arc)myObject, 
                                                       e.getPoint()));
          menuItem.setText("Split Arc Segment");
          popup.insert(menuItem, popupIndex++);

          popup.insert(new JPopupMenu.Separator(), popupIndex++);         
      } else if (myObject instanceof NormalArc) {
         if (((NormalArc)myObject).isJoined()){
            NormalArc PTArc;
            NormalArc TPArc;
            
            if (((NormalArc)myObject).getSource() instanceof Place){
               PTArc = (NormalArc)myObject;
               TPArc = ((NormalArc)myObject).getInverse();
            }  else {
               PTArc = ((NormalArc)myObject).getInverse();
               TPArc = (NormalArc)myObject;               
            }

//            if (!PTArc.isTagged()) { //pendentnou
               menuItem = new JMenuItem(new EditWeightAction(contentPane, PTArc));
               menuItem.setText("Edit Weight (PT Arc)");
               popup.insert(menuItem, popupIndex++);
//               menuItem = new JMenuItem(
//                       new EditTaggedAction(contentPane, PTArc));
//               menuItem.setText("Make Tagged (PT Arc)");               
//               popup.insert(menuItem, popupIndex++);
//            } else {
//               menuItem = new JMenuItem(
//                       new EditTaggedAction(contentPane, PTArc));
//               menuItem.setText("Make Non-Tagged (PT Arc)");               
//               popup.insert(menuItem, popupIndex++);               
//            }
            popup.insert(new JPopupMenu.Separator(), popupIndex++);
            
//            if (!TPArc.isTagged()) {
               menuItem = new JMenuItem(new EditWeightAction(contentPane, TPArc));
               menuItem.setText("Edit Weight (TP Arc)");
               popup.insert(menuItem, popupIndex++);  
//               menuItem = new JMenuItem(
//                       new EditTaggedAction(contentPane, TPArc));
//               menuItem.setText("Make Tagged (TP Arc)");               
//               popup.insert(menuItem, popupIndex++);               
//            } else {
//               menuItem = new JMenuItem(
//                       new EditTaggedAction(contentPane, TPArc));
//               menuItem.setText("Make Non-Tagged (TP Arc)");               
//               popup.insert(menuItem, popupIndex++);  
//            }

            popup.insert(new JPopupMenu.Separator(), popupIndex++);
            
            menuItem = new JMenuItem(new InsertPointAction((Arc)myObject, 
                                                         e.getPoint()));            
            menuItem.setText("Insert Point");
            /*                        
            menuItem = new JMenuItem(new SplitArcAction((Arc)myObject, 
                                                         e.getPoint()));
            menuItem.setText("Split Arc Segment");
             */
            popup.insert(menuItem, popupIndex++);
            
            menuItem = new JMenuItem(
                    new SplitArcsAction((NormalArc)myObject, true));
            menuItem.setText("Split Arcs (PT / TP)");
            popup.insert(menuItem, popupIndex++);            

            popup.insert(new JPopupMenu.Separator(), popupIndex++);   
            
            menuItem = new JMenuItem(new DeleteInverseArcAction(PTArc));
            menuItem.setText("Delete (PT Arc)");
            popup.insert(menuItem, popupIndex++);  
            
            menuItem = new JMenuItem(new DeleteInverseArcAction(TPArc));
            menuItem.setText("Delete (TP Arc)");
            popup.insert(menuItem, popupIndex++);
            /*
            menuItem = new JMenuItem(new DeleteBothAction((NormalArc)myObject));
            menuItem.setText("Delete Both");
            popup.insert(menuItem, 8);                                    
            */
         } else {
//            if(!((NormalArc)myObject).isTagged()) {
               menuItem = new JMenuItem(new EditWeightAction(contentPane, 
                                                          (Arc)myObject));
               menuItem.setText("Edit Weight");
               popup.insert(menuItem, popupIndex++);
//            }
            
//            menuItem = new JMenuItem(
//                    new EditTaggedAction(contentPane,(NormalArc)myObject));
//            if (((NormalArc)myObject).isTagged()) {
//               menuItem.setText("Make Non-Tagged");
//            } else { 
//               menuItem.setText("Make Tagged");
//            }
//            popup.insert(menuItem, popupIndex++);            

            //menuItem = new JMenuItem(new SplitArcAction((Arc)myObject, 
            //                                             e.getPoint()));
            //menuItem.setText("Split Arc Segment");
            
            
            menuItem = new JMenuItem(new SplitArcAction((Arc)myObject, 
                                                         e.getPoint()));            
            menuItem.setText("Insert Point");
            popup.insert(menuItem, popupIndex++);

            if (((NormalArc)myObject).hasInverse()){
               menuItem = new JMenuItem(
                        new SplitArcsAction((NormalArc)myObject, false));
               
               menuItem.setText("Join Arcs (PT / TP)");
               popup.insert(menuItem, popupIndex++);            
            }
            popup.insert(new JPopupMenu.Separator(), popupIndex);
         }
      }
      return popup;
   }

   
   public void mousePressed(MouseEvent e) {
      super.mousePressed(e);
      if (CreateGui.getApp().isEditionAllowed() == false){
         return;
      }      
//      if (e.getClickCount() == 2){
//         Arc arc = (Arc)myObject;
//         if (e.isControlDown()){
//            CreateGui.getView().getUndoManager().addNewEdit(
//                    arc.getArcPath().insertPoint(
//                            new Point2D.Float(arc.getX() + e.getX(), 
//                            arc.getY() + e.getY()), e.isAltDown()));
//         } else {
//            arc.getSource().select();
//            arc.getTarget().select();
//            justSelected = true;
//         }
//         
//      }
      if(e.getClickCount() == 2 &&
              CreateGui.getApp().isEditionAllowed() &&
              (CreateGui.getApp().getMode() == Pipe.ARC ||
              CreateGui.getApp().getMode() == Pipe.BIDIRECTARC ||
              CreateGui.getApp().getMode() == Pipe.SELECT))
              {
    	  ((Arc)myObject).showEditor(); 
              }
   }

   
   public void mouseDragged(MouseEvent e) {
      switch (CreateGui.getApp().getMode()) {
         case Pipe.SELECT:
            if (!isDragging){
               break;
            }
            Arc currentObject = (Arc)myObject;
            Point oldLocation = currentObject.getLocation();
            // Calculate translation in mouse
            int transX = (int)(Grid.getModifiedX(e.getX() - dragInit.x));
            int transY = (int)(Grid.getModifiedY(e.getY() - dragInit.y));
            ((GuiView)contentPane).getSelectionObject().translateSelection(
                     transX, transY);
            dragInit.translate(
                     -(currentObject.getLocation().x - oldLocation.x - transX),
                     -(currentObject.getLocation().y - oldLocation.y - transY));
      }
   }

   
   public void mouseWheelMoved (MouseWheelEvent e) {
      
      if (CreateGui.getApp().isEditionAllowed() == false){
         return;
      }
      
      Arc arc = ((Arc)myObject);
      if (arc instanceof NormalArc){
         
//         if (e.isControlDown()) {
//            if (arc.getWeight() == 1) {
//               if (((NormalArc)arc).hasInvisibleInverse()) {
//                  if (arc.getSource() instanceof Place){
//                     if (e.isShiftDown()) {
//                        arc = ((NormalArc)arc).getInverse();
//                     }
//                  } else {
//                     if (!e.isShiftDown()) {
//                        arc = ((NormalArc)arc).getInverse();
//                     }
//                  }
//               }
//               CreateGui.getView().getUndoManager().addNewEdit(
//                       ((NormalArc)arc).setTagged(!((NormalArc)arc).isTagged()));
//            }
//            return;
//         }
         
         if (((NormalArc)arc).hasInvisibleInverse()) {
            if (arc.getSource() instanceof Place){
               if (e.isShiftDown()) {
                  arc = ((NormalArc)arc).getInverse();                  
               }
            } else {
               if (!e.isShiftDown()) {
                  arc = ((NormalArc)arc).getInverse();                  
               }
            }
         }
      }      
      
      int oldWeight = arc.getWeight();
      int newWeight = oldWeight - e.getWheelRotation();
      if (newWeight < 1) {
         newWeight = 1;
      }
      if (newWeight != oldWeight) {
         CreateGui.getView().getUndoManager().addNewEdit(
               arc.setWeight(newWeight));
         arc.repaint();
      }
   }
   
   class SplitArcsAction extends AbstractAction {

      NormalArc arc;
      boolean joined;
      
      
      public SplitArcsAction(NormalArc _arc, boolean _joined){
         arc = _arc;
         joined = _joined;
      }
      
      public void actionPerformed(ActionEvent e) {
         if (joined) {
            CreateGui.getView().getUndoManager().addNewEdit(
                    arc.split());
         } else {         
            CreateGui.getView().getUndoManager().addNewEdit(
                    arc.join());
         }
      }
      
   }
      

   
   class DeleteInverseArcAction extends AbstractAction {
      
      NormalArc arc, inverse;
      boolean switchArcs;
      
      
      public DeleteInverseArcAction(NormalArc _arc){
         arc = _arc;
         inverse = arc.getInverse();
         switchArcs = arc.inView();
      }
      
      
      public void actionPerformed(ActionEvent e) {
         UndoManager undoManager = CreateGui.getView().getUndoManager();
         
         if (switchArcs) {
            undoManager.addNewEdit(arc.split());
         } else {
            undoManager.addNewEdit(inverse.split());
         }
         undoManager.deleteSelection(arc);

         arc.delete();   
      }
   }   
   
}
