package pipe.client.api.model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class AnimationHistoryItem extends PropertyChangeSupport {
  private final String mTitle;
  private final String mAnimationType;
  private final String mTerminationCriteria;
  private List<TransitionItem> mSequenceOfFiredTransition;
  private long mTimeTakenInNanos = -1;

  /**
   * Constructs a <code>PropertyChangeSupport</code> object.
   *
   * @param sourceBean           The bean to be given as the source for any events.
   * @param pAnimationType
   * @param pTerminationCriteria
   */
  public AnimationHistoryItem(final Object sourceBean, final String pTitle, final AnimationType pAnimationType, final String pTerminationCriteria) {
    super(sourceBean);
    mTitle = pTitle;
    mAnimationType = pAnimationType.getDisplayString();
    mTerminationCriteria = pTerminationCriteria;
    mSequenceOfFiredTransition = new ArrayList<>(AnimationType.SingleRandomFiring == pAnimationType ? 1 : 3);
  }

  public String getTitle() {
    return mTitle;
  }

  public String getAnimationType() {
    return mAnimationType;
  }

  public String getTerminationCriteria() {
    return mTerminationCriteria;
  }

  public List<TransitionItem> getSequenceOfFiredTransition() {
    return mSequenceOfFiredTransition;
  }

  public long getTimeTakenInSeconds() {
    return TimeUnit.NANOSECONDS.toSeconds(mTimeTakenInNanos);
  }

  public long getTimeTakenInMillis() {
    return TimeUnit.NANOSECONDS.toMillis(mTimeTakenInNanos);
  }

  public long getTimeTakenInNanos() {
    return mTimeTakenInNanos;
  }

  public void setTimeTakenInNanos(final long pTimeTakenInNanos) {
    long oldTime = mTimeTakenInNanos;
    mTimeTakenInNanos = pTimeTakenInNanos;

    firePropertyChange("run.totalTime", oldTime, mTimeTakenInNanos);
  }

  public void addFiredTransition(final TransitionItem pTransitionItem) {
    mSequenceOfFiredTransition.add(pTransitionItem);

    firePropertyChange("run.newtransition", null, pTransitionItem);
  }

  public static final class TransitionItem {
    private final String mTransitionName;
    private final List<String> mInputSymbols = new ArrayList<>(2);
    private final List<String> mOutputSymbols = new ArrayList<>(2);
    private final long mTimeTaken;

    public TransitionItem(final String pTransitionName, final long pTimeTaken, final List<String> pInputSymbols, final List<String> pOutputSymbols) {
      this(pTransitionName, pTimeTaken);
      mInputSymbols.addAll(pInputSymbols);
      mOutputSymbols.addAll(pOutputSymbols);
    }

    public TransitionItem(final String pTransitionName, final long pTimeTaken) {
      mTransitionName = pTransitionName;
      mTimeTaken = pTimeTaken;
    }

    public String getTransitionName() {
      return mTransitionName;
    }

    public List<String> getInputSymbols() {
      return mInputSymbols;
    }

    public List<String> getOutputSymbols() {
      return mOutputSymbols;
    }

    public long getTimeTaken() {
      return mTimeTaken;
    }

    public long getTimeTakenInSeconds() {
      return TimeUnit.NANOSECONDS.toSeconds(mTimeTaken);
    }

    public long getTimeTakenInMillis() {
      return TimeUnit.NANOSECONDS.toMillis(mTimeTaken);
    }
  }
}
