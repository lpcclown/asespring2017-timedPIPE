package pipe.gui.handler;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import pipe.dataLayer.Place;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.action.ShowHideInfoAction;
import pipe.gui.undo.UndoManager;


/**
 * Class used to implement methods corresponding to mouse events on places.
 */
public class PlaceHandler 
        extends PlaceTransitionObjectHandler {
   
   
   public PlaceHandler(Container contentpane, Place obj) {
      super(contentpane, obj);
   }
   
   public void updateArcDataType()
   {
	   ((Place)myObject).updateArcDataType();
   }
   
   /** 
    * Creates the popup menu that the user will see when they right click on a 
    * component 
    */
   public JPopupMenu getPopup(MouseEvent e) {
      int index = 0;
      JPopupMenu popup = super.getPopup(e);      
     
      JMenuItem menuItem = new JMenuItem("Edit Place");      
      menuItem.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            ((Place)myObject).showEditor();
            updateArcDataType();
         }
      }); 
      popup.insert(menuItem, index++);
      
      menuItem = new JMenuItem("Define Type");      
      menuItem.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
            ((Place)myObject).showType();
         }
      }); 
      popup.insert(menuItem, index++);
      
      menuItem = new JMenuItem("Cut Points");      
      menuItem.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
        	 ((Place)myObject).CutPoints();
        	 
        	 
         }
      }); 
      //popup.insert(menuItem, index++);
      
 
      menuItem = new JMenuItem(new ShowHideInfoAction((Place)myObject));      
      if (((Place)myObject).getAttributesVisible() == true){
         menuItem.setText("Hide Attributes");
      } else {
         menuItem.setText("Show Attributes");
      }
      popup.insert(menuItem,index++);
      
      popup.insert(new JPopupMenu.Separator(),index);      

      return popup;
   }
   
   
   public void mouseClicked(MouseEvent e) {
      if (SwingUtilities.isLeftMouseButton(e)){
         if (e.getClickCount() == 2 &&
                 CreateGui.getApp().isEditionAllowed() &&
                 (CreateGui.getApp().getMode() == Pipe.PLACE || 
                 CreateGui.getApp().getMode() == Pipe.SELECT)) {
            ((Place)myObject).showEditor(); 
         } else {
            int currentMarking = ((Place)myObject).getCurrentMarking();
            UndoManager undoManager = CreateGui.getView().getUndoManager();
            
            switch(CreateGui.getApp().getMode()) {
               case Pipe.ADDTOKEN:
                  undoManager.addNewEdit(
                          ((Place)myObject).setCurrentMarking(++currentMarking));
                  break;
               case Pipe.DELTOKEN:
                  if (currentMarking > 0) {
                     undoManager.addNewEdit(
                             ((Place)myObject).setCurrentMarking(--currentMarking));
                  }
                  break;
               default:
                  break;
            }
         }
      }else if (SwingUtilities.isRightMouseButton(e)){
         if (CreateGui.getApp().isEditionAllowed() && enablePopup) { 
            JPopupMenu m = getPopup(e);
            if (m != null) {           
               int x = Zoomer.getZoomedValue(
                       ((Place)myObject).getNameOffsetXObject().intValue(),
                       myObject.getZoom());
               int y = Zoomer.getZoomedValue(
                       ((Place)myObject).getNameOffsetYObject().intValue(),
                       myObject.getZoom());
               m.show(myObject, x, y);
            }
         }
      }/* else if (SwingUtilities.isMiddleMouseButton(e)){
         ;
      } */
   }


   public void mouseWheelMoved(MouseWheelEvent e) {
      // 
      if (CreateGui.getApp().isEditionAllowed() == false || 
              e.isControlDown()) {
         return;
      }
      
      UndoManager undoManager = CreateGui.getView().getUndoManager();
      if (e.isShiftDown()) {
         int oldCapacity = ((Place)myObject).getCapacity();
         int oldMarking = ((Place)myObject).getCurrentMarking();
         
         int newCapacity = oldCapacity - e.getWheelRotation();
         if (newCapacity < 0) {
            newCapacity = 0;
         }
         
         undoManager.newEdit(); // new "transaction""
         if ((newCapacity > 0) && (oldMarking > newCapacity)){
            if (((Place)myObject).getMarkingParameter() != null) {
               undoManager.addEdit(((Place)myObject).clearMarkingParameter());
            }
            undoManager.addEdit(((Place)myObject).setCurrentMarking(newCapacity));
         }
         undoManager.addEdit(((Place)myObject).setCapacity(newCapacity));
      } else {
         int oldMarking = ((Place)myObject).getCurrentMarking();
         int newMarking = oldMarking - e.getWheelRotation();
         
         if (newMarking < 0) {
            newMarking = 0;
         }
         if (oldMarking != newMarking) {            
            undoManager.addNewEdit(((Place)myObject).setCurrentMarking(newMarking));
            if (((Place)myObject).getMarkingParameter() != null) {
               undoManager.addEdit(((Place)myObject).clearMarkingParameter());
            }            
         }         
      }
   }

}
