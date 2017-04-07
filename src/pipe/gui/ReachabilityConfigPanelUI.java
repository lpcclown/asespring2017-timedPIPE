package pipe.gui;

import pipe.dataLayer.Place;
import pipe.gui.widgets.ButtonBar;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by Maks on 2/8/2016.
 */
public class ReachabilityConfigPanelUI {


  private static final String CONJUCTION = "\u2227";
  private static final String DISJUNCTION = "\u2228";
  private static final String NEGATION = "\u00AC";
  private static final String BRACES = "()";
  private JComponent mRootComponent;
  private JComboBox<Place> mPlaceSelectorComboBox;
  private JTextArea mPropertySpecificationTextArea;
  private JLabel mPlaceDatatypeIndicator;
  private ActionListener mLogicalClauseAction;

  public ReachabilityConfigPanelUI() {

    initialize();
  }

  private void initialize() {
//    initModels();
//    initCommands();
    initComponents();
    initListeners();
    initLayout();
    initState();

  }

  private void initState() {
    Place place = mPlaceSelectorComboBox.getItemAt(0);
    mPlaceDatatypeIndicator.setText(String.format("%s[%s]", place.getName(), place.getDataType().getJoinedTypes()));
  }

  private void initListeners() {
    mLogicalClauseAction = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        String string = String.format("%s", ((JButton) e.getSource()).getText());
        int caretOffset = 0;
        if (string.equals(BRACES)) {
          caretOffset = 1;
        }
        else {
          caretOffset = string.length();
        }
        appendTextToPropertySpecifier(string, caretOffset);
      }
    };

    mPlaceSelectorComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        String string = String.format("%s()", ((Place) mPlaceSelectorComboBox.getSelectedItem()).getId());
        appendTextToPropertySpecifier(string, string.length() - 1);
      }
    });

    mPlaceSelectorComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(final ItemEvent e) {
        Place place = mPlaceSelectorComboBox.getItemAt(mPlaceSelectorComboBox.getSelectedIndex());
        mPlaceDatatypeIndicator.setText(String.format("%s[%s]", place.getId(), place.getDataType().getJoinedTypes()));
      }
    });

  }

  private void appendTextToPropertySpecifier(final String pString, final int pCaretPositionOffset) {
    int currentCaretPosition = mPropertySpecificationTextArea.getCaretPosition();
    mPropertySpecificationTextArea.insert(pString, currentCaretPosition);
    int nextCaretPosition = currentCaretPosition + pCaretPositionOffset;
    System.out.println(String.format("STRING:: %s, %d:: next caret: %d, %d", pString, pString.length(), currentCaretPosition, nextCaretPosition));
    mPropertySpecificationTextArea.setCaretPosition(nextCaretPosition);
    mPropertySpecificationTextArea.requestFocus();
  }

  private void initComponents() {
    mRootComponent = new JPanel();
    mPlaceSelectorComboBox = new JComboBox<>(CreateGui.getModel().getPlaces());
    mPropertySpecificationTextArea = new JTextArea();
    mPlaceDatatypeIndicator = new JLabel(" ");
  }

  private void initLayout() {
    mRootComponent.setLayout(new BoxLayout(mRootComponent, BoxLayout.PAGE_AXIS));

    Border verticalSpacer = BorderFactory.createEmptyBorder(0, 0, 0, 10);
    JLabel label1 = new JLabel("Add property specification here: ");
    label1.setBorder(verticalSpacer);
    label1.setAlignmentX(Component.LEFT_ALIGNMENT);
    mRootComponent.add(label1);

    JScrollPane scrollPane = new JScrollPane(mPropertySpecificationTextArea);
    scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
    scrollPane.setPreferredSize(new Dimension(500, 100));
    scrollPane.setMaximumSize(new Dimension(500, 100));
    mRootComponent.add(scrollPane);

    mPlaceDatatypeIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);
    mPlaceDatatypeIndicator.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    mRootComponent.add(mPlaceDatatypeIndicator);

    JPanel placeHolderPanel = new JPanel();
    placeHolderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    placeHolderPanel.setBorder(verticalSpacer);

    JLabel placeHolderLabel = new JLabel("Select a place to insert: ");
    placeHolderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    placeHolderLabel.setLabelFor(mPlaceSelectorComboBox);

    placeHolderPanel.add(placeHolderLabel, BorderLayout.EAST);
    placeHolderPanel.add(mPlaceSelectorComboBox, BorderLayout.CENTER);


    mRootComponent.add(placeHolderPanel);
//    mRootComponent.add(Box.createRigidArea(verticaleSeparatorDimension));

    JPanel clauseHolder = new JPanel();
    clauseHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
    clauseHolder.setBorder(verticalSpacer);
    clauseHolder.add(new JLabel("Add connectors from here: "), BorderLayout.EAST);
    clauseHolder.add(new ButtonBar(new String[]{CONJUCTION, DISJUNCTION, NEGATION, BRACES}, new ActionListener[]{mLogicalClauseAction, mLogicalClauseAction, mLogicalClauseAction, mLogicalClauseAction}), BorderLayout.CENTER);
    mRootComponent.add(clauseHolder);
  }

  public JComponent getRootComponent() {
    return mRootComponent;
  }

  public String getPropertySpecificationText() {
    return mPropertySpecificationTextArea.getText();
  }
}
