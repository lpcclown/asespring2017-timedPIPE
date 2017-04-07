package pipe.gui;

import pipe.client.api.model.AnimationType;
import pipe.dataLayer.Transition;

/**
 *
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public interface AnimationStrategy {
  AnimationType getAnimationType();

  String terminationCriteria();

  void startAnimation();

  void stopAnimation();

  boolean shouldContinueNextStep();

  void setFiredTransition(final Transition pTransition);
}
