package pipe.client.api.model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class AnimationHistory extends PropertyChangeSupport {

  private List<AnimationHistoryItem> mItems = new ArrayList<>(5);
  private AnimationHistoryItem mCurrentItem;
  private String mTitle;

  public AnimationHistory(final String pTitle) {
    this("", pTitle);
  }

  /**
   * Constructs a <code>PropertyChangeSupport</code> object.
   *
   * @param sourceBean The bean to be given as the source for any events.
   */
  public AnimationHistory(final Object sourceBean, final String pTitle) {
    super(sourceBean);
    mTitle = pTitle;
  }


  public AnimationHistoryItem addNewItem(final AnimationType pAnimationType, final String pTerminationCriteria) {
    AnimationHistoryItem item = new AnimationHistoryItem(this, "Run" + (mItems.size() + 1), pAnimationType, pTerminationCriteria);
    mItems.add(item);
    mCurrentItem = item;
    firePropertyChange("run.newItem", null, mCurrentItem);
    return item;
  }

  public String getTitle() {
    return mTitle;
  }


  public AnimationHistoryItem getCurrentItem() {
    return mCurrentItem;
  }
}
