package pipe.gui;

import pipe.gui.widgets.ButtonBar;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeSupport;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class AnimationConfigUI {
  private static final String NUMBER_OF_STATES = "Number of Steps";
  private static final String FIND_REACHABLE_STATE = "Find reachable state";

  private final PropertyChangeSupport mPropertyChangeNotifier;

  private JPanel mRootComponent;
  private JTextField mNumberOfStepsField;
  private JComboBox<String> mStrategySelectorComboBox;
  private JPanel mStrategyConfigPanel;

  private ReachabilityConfigPanelUI mReachabilityConfigPanel;

  private ActionListener mCancelListener;
  private ActionListener mConfirmationListener;


  public AnimationConfigUI(final PropertyChangeSupport pPropertyChangeNotifier) {
    mPropertyChangeNotifier = pPropertyChangeNotifier;
    initialize();
  }

  private void initialize() {
    initModels();
//    initCommands();
    initComponents();
    initListeners();
    initLayout();
//    initState();

  }

  private void initListeners() {
    mStrategySelectorComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        if (mStrategyConfigPanel.getLayout() instanceof CardLayout) {
          CardLayout cardLayout = (CardLayout) mStrategyConfigPanel.getLayout();
          cardLayout.show(mStrategyConfigPanel, e.getItem().toString());
        }
      }
    });

    mConfirmationListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        AnimationStrategy animationStrategy;
        if (mStrategySelectorComboBox.getSelectedItem().equals(NUMBER_OF_STATES)) {
          animationStrategy = new FixedStepsAnimationStrategy(Integer.parseInt(mNumberOfStepsField.getText()));
          mPropertyChangeNotifier.firePropertyChange("config.done", null, animationStrategy);
        }
        else if (mStrategySelectorComboBox.getSelectedItem().equals(FIND_REACHABLE_STATE)) {
          animationStrategy = new ReachabilityTestingAnimationStrategy(mReachabilityConfigPanel.getPropertySpecificationText(), CreateGui.getModel());
          mPropertyChangeNotifier.firePropertyChange("config.done", null, animationStrategy);
        }
      }
    };

    mCancelListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        mPropertyChangeNotifier.firePropertyChange("config.cancel", "", null);
      }
    };
  }

  private void initLayout() {

    mStrategyConfigPanel.add(getNumberOfStepsConfigPanel(), NUMBER_OF_STATES);
    mStrategyConfigPanel.add(mReachabilityConfigPanel.getRootComponent(), FIND_REACHABLE_STATE);

    JPanel strategySelectorHolder = new JPanel();
    strategySelectorHolder.setLayout(new BoxLayout(strategySelectorHolder, BoxLayout.LINE_AXIS));
    strategySelectorHolder.add(new JLabel("Select termination strategy: "), BorderLayout.PAGE_START);
    strategySelectorHolder.add(mStrategySelectorComboBox, BorderLayout.PAGE_END);

    ButtonBar buttonBar = new ButtonBar(new String[]{"Ok", "Cancel"}, new ActionListener[]{mConfirmationListener, mCancelListener});

    mStrategyConfigPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0), ""));
    buttonBar.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    mRootComponent.setLayout(new BorderLayout());
    mRootComponent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    mRootComponent.add(strategySelectorHolder, BorderLayout.NORTH);
    mRootComponent.add(mStrategyConfigPanel, BorderLayout.CENTER);
    mRootComponent.add(buttonBar, BorderLayout.SOUTH);
  }

  private void initModels() {
//    mAnimationStrategies = new HashMap<>();
//    mAnimationStrategies.put(NUMBER_OF_STATES, new FixedStepsAnimationStrategy(5));
//    mAnimationStrategies.put(FIND_REACHABLE_STATE, new ReachabilityTestingAnimationStrategy());
  }

  private void initComponents() {
    mRootComponent = new JPanel();
    mNumberOfStepsField = new JTextField("5", 10);

    mStrategySelectorComboBox = new JComboBox<>(new String[]{NUMBER_OF_STATES, FIND_REACHABLE_STATE});
    mStrategyConfigPanel = new JPanel(new CardLayout());

    mReachabilityConfigPanel = new ReachabilityConfigPanelUI();
  }

  public JPanel getNumberOfStepsConfigPanel() {
    JPanel numberOfStepsConfigPanel = new JPanel();
    JLabel numberOfStepsLabel = new JLabel("Specify the number of steps: ");
    numberOfStepsConfigPanel.add(numberOfStepsLabel, BorderLayout.LINE_START);
    numberOfStepsConfigPanel.add(mNumberOfStepsField, BorderLayout.LINE_END);

    return numberOfStepsConfigPanel;
  }

  public JComponent getRootComponent() {
    return mRootComponent;
  }
}
