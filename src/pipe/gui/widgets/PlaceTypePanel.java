package pipe.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.taskdefs.JikesOutputParser;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.DataType;
import pipe.gui.GuiView;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class PlaceTypePanel extends JPanel {
  private static final java.lang.String DELIM = ", ";

  Place place;
  DataType dataType;
  DataLayer pnmlData;
  GuiView view;
  JRootPane rootPane;
  Vector<DataType> group;
  DefaultListModel mDatatypePoolModel;

  /**
   * Creates new form PlaceEditor
   */
  public PlaceTypePanel(JRootPane _rootPane, Place _place, Vector<DataType> _group,
                        DataLayer _pnmlData, GuiView _view) {

    place = _place;
    dataType = _place.getDataType();
    pnmlData = _pnmlData;
    view = _view;
    group = _group;

    rootPane = _rootPane;

    initialize();
    rootPane.setDefaultButton(okButton);
  }

  private void initialize() {
    initComponents();
    initListeners();
    initModel();
    initState();
  }

  private void initState() {
    if (dataType != null) {
      showDefinedDataType();
    }

    lBraketButton.setEnabled(dataType == null);
    stringButton.setEnabled(false);
    intButton.setEnabled(false);
    rBraketButton.setEnabled(false);
  }

  private void initModel() {
    mDatatypePoolModel = new DefaultListModel();
    mDatatypePoolUI.setModel(mDatatypePoolModel);
    for (int i = 0; i < group.size(); i++) {
      mDatatypePoolModel.addElement(group.get(i).getName());
    }
  }

  private void initListeners() {
    isTypeCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        isTypecheckboxHandler(evt);
      }
    });

    lBraketButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        lBraketButtonHandler(evt);
      }
    });

    stringButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        stringButtonHandler(evt);
      }
    });

    intButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        intButtonHandler(evt);
      }
    });
    rBraketButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        rBraketButtonHandler(evt);
      }

    });

    chooseType.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (mDatatypePoolUI.isSelectionEmpty()) {
          return;
        }
        insertSelectedDataTypeToDefinition();
      }

    });

    mDatatypePoolUI.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          insertSelectedDataTypeToDefinition();
        }
      }
    });

    loadButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        loadButtonHandler(evt);
      }
    });

    removeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        removeButtonHandler(evt);
      }

    });

    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        okButtonHandler(evt);
      }
    });

    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonHandler(evt);
      }
    });
  }

  private void showDefinedDataType() {
    if (dataType == null) {
      JOptionPane.showMessageDialog(this.getParent(), "The datatype is undefined");
      return;
    }
    if (dataType.getDef()) {
      this.nameTextField.setText(dataType.getName());
      this.powcheckbox.setSelected(dataType.getPow());
      this.TypeTextField.setText(dataType.getStringRepresentation());
      isTypeCheckBox.setSelected(place.getDataType() == null || this.dataType.getName().equals(place.getDataType().getName().trim()));
    }
  }


  private void SetValue(String name) {
    DataType dataTypeFromPool;
    for (int i = 0; i < group.size(); i++) {
      if ((dataTypeFromPool = group.get(i)).getName().equals(name)) {
        showMessage(dataTypeFromPool.getStringRepresentation());
        dataType = dataTypeFromPool;
        showDefinedDataType();
        break;
      }
    }
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    placeTypePanel = new JPanel();
    nameLabel = new JLabel();
    dataTypePoolLabel = new JLabel();
    nameTextField = new JTextField();
    TypeTextField = new JTextArea();


    buttonPanel = new JPanel();
    buttonPanel1 = new JPanel();
    removeButton = new JButton();
    okButton = new JButton();
    cancelButton = new JButton();
    lBraketButton = new JButton();
    rBraketButton = new JButton();
    createButton = new JButton();
    stringButton = new JButton();
    intButton = new JButton();

    loadButton = new JButton();

    TextLabel = new JLabel();

    TypelistPanel = new JPanel();

    mDatatypePoolUI = new JList();
    chooseType = new JButton();

    namePanel = new JPanel();
    TypefieldPanel = new JPanel();

    isTypeCheckBox = new JCheckBox();

    powcheckbox = new JCheckBox();
    setLayout(new java.awt.GridBagLayout());

    placeTypePanel.setLayout(new java.awt.GridBagLayout());

    placeTypePanel.setBorder(BorderFactory.createTitledBorder("Token Type"));
    namePanel.setLayout(new java.awt.FlowLayout());
    nameLabel.setText("Name:");
    namePanel.add(nameLabel);
//	      gridBagConstraints = new java.awt.GridBagConstraints();
//	      gridBagConstraints.gridx = 1;
//	      gridBagConstraints.gridy = 0;
//	      gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
//	      gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
//	      placeTypePanel.add(nameLabel, gridBagConstraints);

    nameTextField.setMaximumSize(new java.awt.Dimension(180, 20));
    nameTextField.setMinimumSize(new java.awt.Dimension(180, 20));
    nameTextField.setPreferredSize(new java.awt.Dimension(180, 20));
    namePanel.add(nameTextField);


    isTypeCheckBox.setText("setPlaceType");
    isTypeCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    isTypeCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

    namePanel.add(isTypeCheckBox);

    gridBagConstraints = new java.awt.GridBagConstraints();
//	      gridBagConstraints.gridwidth = 2;
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    placeTypePanel.add(namePanel, gridBagConstraints);

    buttonPanel1.setLayout(new java.awt.FlowLayout());

    lBraketButton.setText("<");
    lBraketButton.setMaximumSize(new java.awt.Dimension(50, 25));
    lBraketButton.setMinimumSize(new java.awt.Dimension(50, 25));
    lBraketButton.setPreferredSize(new java.awt.Dimension(50, 25));
    lBraketButton.setEnabled(false);

    buttonPanel1.add(lBraketButton);

    Dimension buttonDimension = new java.awt.Dimension(100, 25);
    stringButton.setText("String");
    stringButton.setMaximumSize(buttonDimension);
    stringButton.setMinimumSize(buttonDimension);
    stringButton.setPreferredSize(buttonDimension);

    buttonPanel1.add(stringButton);

    intButton.setText("Number");
    intButton.setMaximumSize(buttonDimension);
    intButton.setMinimumSize(buttonDimension);
    intButton.setPreferredSize(buttonDimension);

    buttonPanel1.add(intButton);

    rBraketButton.setText(">");
    rBraketButton.setMaximumSize(buttonDimension);
    rBraketButton.setMinimumSize(buttonDimension);
    rBraketButton.setPreferredSize(buttonDimension);

    buttonPanel1.add(rBraketButton);

    powcheckbox.setText("Is Power Set");
    powcheckbox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    powcheckbox.setMargin(new java.awt.Insets(0, 0, 0, 0));
    powcheckbox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        powcheckboxHandler(evt);
      }
    });

    buttonPanel1.add(powcheckbox);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    placeTypePanel.add(buttonPanel1, gridBagConstraints);


    showMessage("(Type Description)");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(3, 10, 3, 3);
    placeTypePanel.add(TextLabel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(3, 10, 3, 3);
    this.dataTypePoolLabel.setText("DataType Pool");
    placeTypePanel.add(dataTypePoolLabel, gridBagConstraints);


    TypelistPanel.setLayout(new java.awt.GridBagLayout());

    TypeTextField.setLineWrap(true);
    TypeTextField.setRows(8);
    TypeTextField.setEditable(false);
    TypeTextField.setMaximumSize(new java.awt.Dimension(230, 150));
    TypeTextField.setMinimumSize(new java.awt.Dimension(230, 150));
    TypeTextField.setPreferredSize(new java.awt.Dimension(230, 150));
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
//	      gridBagConstraints.gridwidth = 2;
//	      gridBagConstraints.gridheight = 10;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    TypelistPanel.add(TypeTextField, gridBagConstraints);


    TypefieldPanel.setLayout(new java.awt.GridBagLayout());

    mDatatypePoolUI.setBackground(Color.WHITE);
    mDatatypePoolUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mDatatypePoolUI.setMaximumSize(new java.awt.Dimension(100, 134));
    mDatatypePoolUI.setMinimumSize(new java.awt.Dimension(100, 134));
    mDatatypePoolUI.setPreferredSize(new java.awt.Dimension(100, 134));
//	      mDatatypePoolUI.setSize(TypeTextField.getWidth() ,TypeTextField.getHeight());
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    TypefieldPanel.add(mDatatypePoolUI, gridBagConstraints);

    chooseType.setText("Select");
    chooseType.setMaximumSize(new java.awt.Dimension(75, 25));
    chooseType.setMinimumSize(new java.awt.Dimension(75, 25));
    chooseType.setPreferredSize(new java.awt.Dimension(75, 25));

    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    TypefieldPanel.add(chooseType, gridBagConstraints);

    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
    TypelistPanel.add(TypefieldPanel, gridBagConstraints);


    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);

    placeTypePanel.add(TypelistPanel, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(5, 8, 5, 8);
    add(placeTypePanel, gridBagConstraints);

    buttonPanel.setLayout(new java.awt.FlowLayout());

    loadButton.setText("Load");
    loadButton.setMaximumSize(new java.awt.Dimension(75, 25));
    loadButton.setMinimumSize(new java.awt.Dimension(75, 25));
    loadButton.setPreferredSize(new java.awt.Dimension(75, 25));


    buttonPanel.add(loadButton);

    removeButton.setText("Clear");
    removeButton.setMaximumSize(new java.awt.Dimension(75, 25));
    removeButton.setMinimumSize(new java.awt.Dimension(75, 25));
    removeButton.setPreferredSize(new java.awt.Dimension(75, 25));

    buttonPanel.add(removeButton);

    okButton.setText("OK");
    okButton.setMaximumSize(new java.awt.Dimension(75, 25));
    okButton.setMinimumSize(new java.awt.Dimension(75, 25));
    okButton.setPreferredSize(new java.awt.Dimension(75, 25));

    buttonPanel.add(okButton);

    cancelButton.setText("Cancel");
    cancelButton.setMaximumSize(new java.awt.Dimension(75, 25));
    cancelButton.setMinimumSize(new java.awt.Dimension(75, 25));
    cancelButton.setPreferredSize(new java.awt.Dimension(75, 25));

    buttonPanel.add(cancelButton);

    createButton.setText("Create");
    createButton.setMaximumSize(new java.awt.Dimension(75, 25));
    createButton.setMinimumSize(new java.awt.Dimension(75, 25));
    createButton.setPreferredSize(new java.awt.Dimension(75, 25));
    createButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        createButtonHandler(evt);
      }
    });

    buttonPanel.add(createButton);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    add(buttonPanel, gridBagConstraints);

    if (dataType != null) {
      int index = group.indexOf(dataType);
      mDatatypePoolUI.setSelectedIndex(index);
      showDefinedDataType();
    }
  }

  private void insertSelectedDataTypeToDefinition() {
    int selectedIndex = mDatatypePoolUI.getSelectedIndex();
    DataType selectedDataType = group.get(selectedIndex);
    if (nameTextField.getText().trim().equals(selectedDataType.getName())) {
      showWarning("Cannot include itself");
      return;
    }

    if (selectedDataType.getPow()) {
      showWarning("Power Set cannot be included");
      return;
    }

    includeIntermediateDefinitionParts(selectedDataType.getName());
  }


  ChangeListener changeListener = new ChangeListener() {
    public void stateChanged(ChangeEvent evt) {
      JSpinner spinner = (JSpinner) evt.getSource();
      JSpinner.NumberEditor numberEditor =
          ((JSpinner.NumberEditor) spinner.getEditor());
      numberEditor.getTextField().setBackground(new Color(255, 255, 255));
      spinner.removeChangeListener(this);
    }
  };


//	   private void markingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_markingComboBoxActionPerformed
//	      Integer index = markingComboBox.getSelectedIndex();
//
//	      if (index > 0){
//	         Integer value = ((MarkingParameter)markingComboBox.getItemAt(index)).getValue();
//	         markingSpinner.setValue(value);
//	      } 
//	   }//GEN-LAST:event_markingComboBoxActionPerformed


  private void okButtonKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_okButtonKeyPressed
    if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
      // doOK();
    }
  }//GEN-LAST:event_okButtonKeyPressed

  private void showWarning(String message) {
    TextLabel.setForeground(Color.RED);
    TextLabel.setText(message);
  }

  private void showMessage(String message) {
    TextLabel.setForeground(Color.BLACK);
    TextLabel.setText(message);
  }

  private void doOK() {
    int userResponse = -1;
    String type = TypeTextField.getText().trim();
    String name = nameTextField.getText().trim();
    if ((StringUtils.isNotBlank(type) || StringUtils.isNotBlank(name)) && dataType == null || //no datatype has been created so far, but the texts in the UI are not empty, means that someone started to create one but is going to leave unfinished
        dataType != null && (!dataType.getStringRepresentation().equals(type) || !dataType.getName().equals(name))) {
      userResponse = JOptionPane.showConfirmDialog(this.getParent(), "You have changed/created the datatype. Do you want to apply the changes?", "", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    boolean isSuccessful = true; //by default true. Will be false only if the user chooses to save but failed due validation check.
    if (userResponse == JOptionPane.YES_OPTION) {
      isSuccessful = createOrUpdateDataType(dataType==null);
    }
        if (!isSuccessful) {
          return;
        }
        if (isTypeCheckBox.isSelected()) {
          place.setDataType(dataType);
        }
    exit();
  }

  private void loadButtonHandler(ActionEvent evt) {
    if (loadButton.getText().equals("Load")) {
      if (!this.mDatatypePoolUI.isSelectionEmpty()) {
        dataType = group.get(mDatatypePoolUI.getSelectedIndex());
        this.loadButton.setText("UnLoad");
        this.nameTextField.setEditable(false);
//			 			this.mDatatypePoolUI.setEnabled(false);
        this.createButton.setText("Modify");
        showDefinedDataType();
        this.repaint();
      }
    }

    else if (loadButton.getText().equals("UnLoad")) {
      this.nameTextField.setText("");
      this.nameTextField.setEditable(true);
      this.isTypeCheckBox.setSelected(false);
//			   this.mDatatypePoolUI.setEnabled(true);
      this.powcheckbox.setSelected(false);
      this.TypeTextField.setText("");
      showMessage("(Type Description)");
      this.loadButton.setText("Load");
      this.createButton.setText("Create");
      this.repaint();
      return;
    }

  }

  private void createButtonHandler(ActionEvent evt) {
    String dataTypeName = nameTextField.getText().trim();
    if (dataTypeName.equals("")) {
      showWarning("Name cannot be empty");
      return;
    }

    if (createOrUpdateDataType(createButton.getText().equalsIgnoreCase("create"))) {
      showDefinedDataType();
      showMessage("");
    }
  }

  private boolean createOrUpdateDataType(final boolean pShouldCreate) {
    String[] types = buildTypes();
    if (types == null) {
      return false;
    }

    String dataTypeName = nameTextField.getText().trim();
    if (pShouldCreate) {
      if (StringUtils.isBlank(dataTypeName)) {
        showWarning("Name cannot be empty");
        return false;
      }

      if (indexOfDataTypeOfName(dataTypeName) != -1) {
        showWarning("Name already exists");
        return false;
      }

      DataType dataType = new DataType(dataTypeName, types, powcheckbox.isSelected(), group);
      group.add(dataType);
      mDatatypePoolModel.addElement(dataType.getName());
      this.dataType = dataType;
    }
    else {
      dataType.defineType(types);
      dataType.setPow(powcheckbox.isSelected());
    }

    return true;
  }

  private String[] buildTypes() {
    String definition = TypeTextField.getText().trim();
    int startIndex = definition.indexOf('<');
    int endIndex = definition.indexOf('>');
    if (startIndex == -1 || endIndex == -1) {
      showWarning("Data type definition is not complete");
      return null;
    }

    definition = definition.substring(startIndex+1, endIndex);
    if (StringUtils.isBlank(definition)) {
      showWarning("No type component is present");
      return null;
    }

    return definition.split(DELIM);
  }

  private int indexOfDataTypeOfName(final String pDataTypeName) {
    for (int index = 0; index < group.size(); index++) {
      if (group.get(index).getName().equals(pDataTypeName)) {
        return index;
      }
    }

    return -1;
  }


  private void lBraketButtonHandler(ActionEvent evt) {
    if (evt.getSource() == lBraketButton) {
      doString("<", true);
      lBraketButton.setEnabled(false);
      setEnabledTypesButtons(true);
    }
  }

  private void stringButtonHandler(ActionEvent evt) {
    includeIntermediateDefinitionParts("string");

  }

  private void intButtonHandler(ActionEvent evt) {
    includeIntermediateDefinitionParts("number");
  }

  private void includeIntermediateDefinitionParts(final String pTerm) {
      doString(pTerm, false);

  }

  private void rBraketButtonHandler(ActionEvent evt) {
      doString(">", true);
    setEnabledTypesButtons(false);
  }

  private void doString(final String pTerm, final boolean pIsTerminal) {
    String stringToAppend = pTerm;
    if (!pIsTerminal && TypeTextField.getText().length() > 2) {
        stringToAppend = String.format("%s%s", DELIM, pTerm);
      }

    TypeTextField.append(stringToAppend);
  }

  private void isTypecheckboxHandler(ActionEvent evt) {

  }


  private void powcheckboxHandler(ActionEvent evt) {
    if (powcheckbox.isSelected()) {
      String str = TypeTextField.getText().trim();
      if (str.startsWith("<") && str.endsWith(">")) {
        TypeTextField.setText("P( " + str + " )");
      }
      else {
        powcheckbox.setSelected(false);
        powcheckbox.repaint();
      }
    }
    else {
      String str = TypeTextField.getText().trim();
      if (str.startsWith("P(") && str.endsWith(")")) {
        TypeTextField.setText(str.substring(2, str.length() - 2).trim());
      }
    }
  }

  private void removeButtonHandler(ActionEvent evt) {
    TypeTextField.setText("");
    powcheckbox.setSelected(false);
    lBraketButton.setEnabled(true);
    repaint();
  }

  private void okButtonHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonHandler
    doOK();
  }//GEN-LAST:event_okButtonHandler


  private void exit() {
    rootPane.getParent().setVisible(false);
  }


  private void cancelButtonHandler(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonHandler
    exit();
  }//GEN-LAST:event_cancelButtonHandler

  public DataType getDataType() {
    return dataType;
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables

  private JPanel buttonPanel;
  private JPanel buttonPanel1;
  private JButton removeButton;
  private JButton cancelButton;
  private JButton lBraketButton;
  private JButton rBraketButton;
  private JButton stringButton;
  private JButton intButton;
  private JCheckBox powcheckbox;
  private JLabel nameLabel;
  private JTextField nameTextField;
  private JTextArea TypeTextField;
  private JButton okButton;
  private JPanel placeTypePanel;
  private JList mDatatypePoolUI;
  private JLabel TextLabel;
  private JLabel dataTypePoolLabel;
  private JPanel TypelistPanel;
  private JButton chooseType;
  private JPanel namePanel;
  private JPanel TypefieldPanel;
  private JCheckBox isTypeCheckBox;
  private JButton createButton;
  private JButton loadButton;

  public void setEnabledTypesButtons(final boolean pEnabledTypesButtons) {
    stringButton.setEnabled(pEnabledTypesButtons);
    intButton.setEnabled(pEnabledTypesButtons);
    rBraketButton.setEnabled(pEnabledTypesButtons);
  }
}
