package pipe.gui.handler;

import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;


/**
 * This class handles mouse clicks by the user. 
 * 
 * @author unknown 
 * @author David Patterson
 * 
 * Change by David Patterson was to fire the selected 
 * transition in the DataLayer, and then record the firing
 * in the animator.
 * 
 * @author Pere Bonet reverted the above change.
 */
public class AnimationHandler 
        extends javax.swing.event.MouseInputAdapter {
   
   
   public void mouseClicked(MouseEvent e){      
      if (e.getComponent() instanceof Transition) {
         Transition transition = (Transition)e.getComponent();
         
         if (SwingUtilities.isLeftMouseButton(e)
                 && (transition.isEnabled(true))) {
//            CreateGui.getAnimationHistory().clearStepsForward();
            CreateGui.getAnimator().fireLowLevelTransition(transition);
            CreateGui.getApp().setRandomAnimationMode(false);
         }
      }
   }
   
/**
 * Replaced by Su Liu
 */
//	 public void mouseClicked(MouseEvent e){
//		 if (e.getComponent() instanceof Transition){
//			 Transition transition = (Transition)e.getComponent();
//			 if(SwingUtilities.isLeftMouseButton(e)){
//				 CreateGui.getAnimator().fireTransition(transition);
//				 CreateGui.getApp().setRandomAnimationMode(false);
//			 }
//		 }
//	 }
   
   
}
