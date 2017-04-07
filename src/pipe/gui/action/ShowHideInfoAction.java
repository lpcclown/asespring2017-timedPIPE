package pipe.gui.action;

import java.awt.event.ActionEvent;

import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.PlaceTransitionObject;


/**
 *
 */
public class ShowHideInfoAction 
        extends javax.swing.AbstractAction {
   
   private PetriNetObject pto;
   
   
   public ShowHideInfoAction(PlaceTransitionObject component) {
      pto = component;      
   }
   
   
   /**  */
   public void actionPerformed(ActionEvent e) {    
	   if(pto instanceof PlaceTransitionObject)
	   {
		   ((PlaceTransitionObject) pto).toggleAttributesVisible();
	   }
   }
   
}
