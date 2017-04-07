package pipe.gui.handler;

import hlpn2smt.HLPNModelToZ3Converter;
import hlpn2smt.Property;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Place;
import pipe.dataLayer.Token;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.ResultsTxtPane;
import analysis.myPromela;

public class BoundedModelCheckingModule extends AbstractAction {
	
	JButton btn;
	private static final String MODULE_NAME = "Bounded Model Checking";
	private EscapableDialog guiDialog;
	private JPanel leftPanel;
	private JPanel rightPanel;
	
	private ResultsTxtPane results;
	private JTextField steptext;
	private JLabel PreDefineSteps;
	//property definition
	private JLabel PropertySpec;
	private JLabel PropertyPlace;
	private JComboBox propertyPlaceComboBox;
	private String propertyPlaceComboBoxSelectionString = "";
//	private JTextField PropertyPlaceText;
	private JLabel PropertyToken;
	private JTextField PropertyTokenText;
	private JLabel PropertyRelationType;
	private JComboBox relationTypeComboBox;
	private String relationTypeString = "CONJUNCTION";
	private JLabel PropertyOperator;
	private JComboBox operatorComboBox;
	private String operatorTypeString = "EQ";
	
	private JList propertyList;
	private DefaultListModel<String> propertyListStrings;
	private ButtonBar runZ3CheckButton;
	
	ArrayList<Property> propertyBuilderList = new ArrayList<Property>();
	DataLayer sourceDataLayer = CreateGui.getModel();
	Place curPlace = null;
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton){
			btn = (JButton) e.getSource();
			if(btn.getName().compareTo("btnModelToPromela") == 0){
				boundedModelCheckingWindow();
			}
		}
		
	}
	
	public void buildLeftPanel(){
		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));	
	    //Add results textField
		leftPanel.add(new JLabel("Checking Result"));
		results = new ResultsTxtPane("Checking Results Shown Here: ");
		JScrollPane scrollPane = new JScrollPane(results);
		leftPanel.add(scrollPane); //SUTODO: the null parameter in ResultsTxtPane to be reconsider.
	}
	
	/**
	 * building property panel
	 */
	public void buildRightPanel(){
		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));	
		// add property
		PropertySpec = new JLabel("----------------Property SPEC----------------\n");
		PropertySpec.setAlignmentX(Component.CENTER_ALIGNMENT);
		rightPanel.add(PropertySpec);
		
		//property place text area
		PropertyPlace = new JLabel("Property Place: ");
		PropertyPlace.setAlignmentX(Component.CENTER_ALIGNMENT);
		rightPanel.add(PropertyPlace);
//		rightPanel.add(this.PropertyPlaceText = new JTextField());
		
		//property place combo box
		Place[] places = this.sourceDataLayer.getPlaces();
		String[] placesStr = new String[places.length];
		for(int i=0;i<places.length;i++){
			placesStr[i] = places[i].getId();  //changed to getID() 11/11/15
		}
		this.PropertyTokenText = new JTextField();
		
		this.propertyPlaceComboBox = new JComboBox(placesStr);
		if(places.length!=0){
			propertyPlaceComboBox.setSelectedIndex(0);
			propertyPlaceComboBoxSelectionString = (String)propertyPlaceComboBox.getSelectedItem();
		}else 
			propertyPlaceComboBox.setSelectedIndex(-1);
		
		if(!propertyPlaceComboBoxSelectionString.equals("")){
			curPlace =  sourceDataLayer.getPlaceByName(propertyPlaceComboBoxSelectionString);
		}
		initPropertyTokenTypeText(true, curPlace);
		
		propertyPlaceComboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				propertyPlaceComboBoxSelectionString = (String)cb.getSelectedItem();
				curPlace =  sourceDataLayer.getPlaceByName(propertyPlaceComboBoxSelectionString);
				initPropertyTokenTypeText(true, curPlace);
			}
	      });
	      rightPanel.add(propertyPlaceComboBox);
		
		//property token editing
		PropertyToken = new JLabel("Property Token: ");
		PropertyToken.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		PropertyTokenText.addFocusListener(new java.awt.event.FocusListener(){
	    	  public void focusGained(FocusEvent e) {
//  		  		if(PropertyTokenText.getText().trim().equals(initPropertyTokenTypeText(false, curPlace)))
  		  		PropertyTokenText.setText("");
  		  		PropertyTokenText.setForeground(Color.black);
	    	  }

	    	  public void focusLost(FocusEvent e) {
  	    	  	if(PropertyTokenText.getText().trim().equals(""))
  	    	  		initPropertyTokenTypeText(true, curPlace);
	    	  }
    });
		rightPanel.add(PropertyToken);
		rightPanel.add(PropertyTokenText);
		PropertyRelationType = new JLabel("Property RelationType: ");
		PropertyRelationType.setAlignmentX(Component.CENTER_ALIGNMENT);
		rightPanel.add(PropertyRelationType);
	    String[] relationTypeStrings = { "CONJUNCTION", "DISJUNCTION" };
	      relationTypeComboBox = new JComboBox(relationTypeStrings);
	      relationTypeComboBox.setSelectedIndex(0);
	      //listen to combobox relation type
	      relationTypeComboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox)e.getSource();
				relationTypeString = (String)cb.getSelectedItem();
			}
	      });
	      rightPanel.add(relationTypeComboBox);
	      
	      PropertyOperator = new JLabel("Property Operator: ");
	      PropertyOperator.setAlignmentX(Component.CENTER_ALIGNMENT);
	      rightPanel.add(PropertyOperator);
	      String[] operatorStrings = { "EQ", "NEQ" };
	      operatorComboBox = new JComboBox(operatorStrings);
	      operatorComboBox.setSelectedIndex(0);
	      //listen to combobox operator type
	      operatorComboBox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox opCB = (JComboBox)e.getSource();
				operatorTypeString = (String)opCB.getSelectedItem();
			}
	      });
	      rightPanel.add(operatorComboBox);
	      
	      /**
	       * layout buttons in a row
	       */
	      JPanel buttonPanel = new JPanel();
	      
	      //add update and delete buttons to property formula
	      buttonPanel.add(new ButtonBar("Add Clause", new ActionListener(){
	    	  public void actionPerformed(ActionEvent e){
	    		  //add the property name to JList
	    		  String curRel = "";
	    		  if(!propertyListStrings.isEmpty()){
	    			  if(relationTypeString.equals("CONJUNCTION"))
	    				  curRel = "\u2227";
	    			  else curRel = "\u2228";
	    		  }
	    		  String curOpStr = "";
	    		  if(operatorTypeString.equals("EQ")){
	    			  curOpStr = "=";
	    		  }else{
	    			  curOpStr = "\u2260";
	    		  }
	    		  propertyListStrings.addElement(curRel+" "+propertyPlaceComboBoxSelectionString + 
	    				  curOpStr + PropertyTokenText.getText());
	    		  //add the property to propertyList
	    		  addClauseToPropertyFormulaFromGUI();
	    	  }
	      }, guiDialog.getRootPane()));
	      
	      buttonPanel.add(new ButtonBar("Update Clause", new ActionListener(){
	    	  public void actionPerformed(ActionEvent e){
	    		  int index = propertyList.getSelectedIndex();
	    		  String curRel = "";
	    		  if(index!=0){
	    			  if(relationTypeString.equals("CONJUNCTION"))
	    				  curRel = "\u2227";
	    			  else curRel = "\u2228";
	    		  }
	    		  String curOpStr = "";
	    		  if(operatorTypeString.equals("EQ")){
	    			  curOpStr = "=";
	    		  }else{
	    			  curOpStr = "\u2260";
	    		  }
	    		  propertyListStrings.set(index, curRel+" "+propertyPlaceComboBoxSelectionString + 
	    				  curOpStr + PropertyTokenText.getText());
	    		  
	    		  //add the property to propertyList
	    		  updateClauseToPropertyFormulaFromGUI(index);
	    	  }
	      }, guiDialog.getRootPane()));
	      
	      buttonPanel.add(new ButtonBar("Delete Clause", new ActionListener(){
	    	  public void actionPerformed(ActionEvent e){
	    		  //delete the property name from JList
	    		  int index = propertyList.getSelectedIndex();
	    		  propertyListStrings.remove(index);
	    		  if(index == 0 && !propertyListStrings.isEmpty()){
	    			  String propertyListHead = propertyListStrings.get(0);
	    			  propertyListStrings.set(0, propertyListHead.substring(2));
	    		  }
	    		  //delete the property to propertyList
	    		  deleteClauseToPropertyFormulaFromGUI(index);
	    	  }
	      }, guiDialog.getRootPane()));
	      
	      rightPanel.add(buttonPanel);
	      
	      //display added property formula
	      this.propertyListStrings = new DefaultListModel<String>();//init as null, but need to init with previous defined property
	      this.propertyList = new JList(propertyListStrings);
	      
	      propertyList.setBackground(Color.WHITE);
	      propertyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION );
	      propertyList.addListSelectionListener(propertySelectionListener);
//	      propertyList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
	      JScrollPane scrollPropertyListPane = new JScrollPane();
	      scrollPropertyListPane.getViewport().add(propertyList);
	      JLabel DefProperties = new JLabel("Defined Properties");
	      DefProperties.setAlignmentX(Component.CENTER_ALIGNMENT);
	      rightPanel.add(DefProperties);
	      rightPanel.add(scrollPropertyListPane);
	      
	      //add  formula textbox
	      PreDefineSteps = new JLabel("Pre Define Checking Steps:");
	      PreDefineSteps.setAlignmentX(Component.CENTER_ALIGNMENT);
	      rightPanel.add(PreDefineSteps);
	      rightPanel.add(steptext = new JTextField(CreateGui.getModel().getPropertyFormula()));
	      
	      // 4 Add z3 check button
	      rightPanel.add(runZ3CheckButton = new ButtonBar("Z3 Run Check", checkButtonClick,
	              guiDialog.getRootPane()));
	}
	
	public void addClauseToPropertyFormulaFromGUI(){
		String placeName = propertyPlaceComboBoxSelectionString;
		Place p  = sourceDataLayer.getPlaceByName(placeName);
		DataType dt = p.getDataType();
		Token tok = new Token(dt);
		String tokenText = this.PropertyTokenText.getText();
		String tempTokenStr = tokenText.substring(1, tokenText.length()-1);
		String[] tokenStrs = tempTokenStr.split(",");
		for(int i=0;i<tokenStrs.length;i++){
			if(dt.getTypebyIndex(i)==0){
				tok.Tlist.add(new BasicType(0, Integer.parseInt(tokenStrs[i]),""));
			}else{
				tok.Tlist.add(new BasicType(1, 0, tokenStrs[i]));
			}
		}
		
		Property.RelationType reltype = Property.RelationType.CONJUNCTION;
		if(!relationTypeString.equals("CONJUNCTION"))reltype = Property.RelationType.DISJUNCTION;
		Property.Operator optype = Property.Operator.EQ;
		if(!operatorTypeString.equals("EQ"))optype = Property.Operator.NEQ;
		Property newProp = new Property(placeName, tok, reltype, optype);
		this.propertyBuilderList.add(newProp);
	}
	
	public void updateClauseToPropertyFormulaFromGUI(int index) {
		String placeName = propertyPlaceComboBoxSelectionString;
		Place p  = sourceDataLayer.getPlaceByName(placeName);
		DataType dt = p.getDataType();
		Token tok = new Token(dt);
		String tokenText = this.PropertyTokenText.getText();
		String tempTokenStr = tokenText.substring(1, tokenText.length()-1);
		String[] tokenStrs = tempTokenStr.split(",");
		for(int i=0;i<tokenStrs.length;i++){
			if(dt.getTypebyIndex(i)==0){
				tok.Tlist.add(new BasicType(0, Integer.parseInt(tokenStrs[i]),""));
			}else{
				tok.Tlist.add(new BasicType(1, 0, tokenStrs[i]));
			}
		}
		
		Property.RelationType reltype = Property.RelationType.CONJUNCTION;
		if(!relationTypeString.equals("CONJUNCTION"))reltype = Property.RelationType.DISJUNCTION;
		Property.Operator optype = Property.Operator.EQ;
		if(!operatorTypeString.equals("EQ"))optype = Property.Operator.NEQ;
		Property newProp = new Property(placeName, tok, reltype, optype);
		this.propertyBuilderList.set(index, newProp);
	}
	
	public void deleteClauseToPropertyFormulaFromGUI(int index) {
		this.propertyBuilderList.remove(index);
	}

	public void boundedModelCheckingWindow() {
		 // Build interface
		guiDialog = new EscapableDialog(CreateGui.appGui, MODULE_NAME, true);
		JPanel mainPanel = new JPanel();
		guiDialog.getContentPane().add(mainPanel);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		buildLeftPanel();
		buildRightPanel();
		
		   // 1 Set layout
//	      Container contentPane = guiDialog.getContentPane();
//		JScrollPane resultScrollPane = new JScrollPane();
//		JScrollPane controlScrollPane = new JScrollPane();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftPanel, rightPanel);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(0.5);
		
		//Provide minimum sizes for the two components in the split pane
		Dimension minimumSize = new Dimension(100, 100);
		leftPanel.setMinimumSize(minimumSize);
		rightPanel.setMinimumSize(minimumSize);
	      
//		controlScrollPane.setLayout(new BoxLayout(controlScrollPane,BoxLayout.PAGE_AXIS));
		
		mainPanel.add(splitPane);	      	      
	      
	      // Make window fit contents' preferred size
	      guiDialog.pack();
	      
	      // Move window to the middle of the screen
	      guiDialog.setLocationRelativeTo(null);
	      mainPanel.setVisible(true);
	      guiDialog.setVisible(true);
	}
	
	  public String getName() {
	      return MODULE_NAME;
	   }
	  
	  /**
	    * Translate button click handler
	    */
	   ActionListener checkButtonClick = new ActionListener() {
	      
	      public void actionPerformed(ActionEvent e) {
	    	  BufferedWriter bufferedWriter = null;
			  BufferedReader bufferedReader = null;
			  String r = "";
	    	  
	    	  String steps = new String(steptext.getText());
	  	    if(steps.equals("")){
	  	    	steptext.setText("pre defined step must be specified before checking !");
	  	    	return;
	  	    }
	  	    results.setText("Checking...");
	  	    runZ3CheckButton.setEnabled(false);
	    	HLPNModelToZ3Converter convert = new HLPNModelToZ3Converter(sourceDataLayer, Integer.parseInt(steps), propertyBuilderList);
	    	boolean z3CheckingResult = false; 
	    	try {
	    		String t;
	    		bufferedReader = new BufferedReader(new FileReader("z3Result.txt"));
	    		if((t=bufferedReader.readLine())!=null){
	    			r += t +"\n";
	    			if(t.equals("sat")){
	    				z3CheckingResult = true;
	    			}
	    		}
				while((t=bufferedReader.readLine())!=null){
					   r += t +"\n";
				}
				if(z3CheckingResult){
					r += "\n*******************Error Model**********************\n";
					bufferedReader = new BufferedReader(new FileReader("newOutput.txt"));
					while((t=bufferedReader.readLine())!=null){
					   r += t +"\n";
				   }  
				}
				   results.setText(r);
	    	}catch(IOException ioe){
				   ioe.printStackTrace();
			} 
	    	runZ3CheckButton.setEnabled(true);
	      }
	   };

	   /**
	    * JList Section Listener
	    */
	   ListSelectionListener propertySelectionListener = new ListSelectionListener() {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			int index = propertyList.getSelectedIndex();
			if(index<0)return;
			Property prop = propertyBuilderList.get(index);
			propertyPlaceComboBox.setSelectedItem(prop.getPlaceName());
//			PropertyPlaceText.setText(prop.getPlaceName());
			String tokenStr = "";
			Token t = prop.getToken();
			for(BasicType bt:t.Tlist){
				tokenStr = bt.getValueAsString();
			}
			PropertyTokenText.setText("[" + tokenStr + "]");
			
			if(prop.getRelationType() == Property.RelationType.CONJUNCTION)
				relationTypeComboBox.setSelectedIndex(0);
			else 
				relationTypeComboBox.setSelectedIndex(1);
			
			if(prop.getOperator() == Property.Operator.EQ)
				operatorComboBox.setSelectedIndex(0);
			else 
				operatorComboBox.setSelectedIndex(1);
			
		}
		   
	   };
	
	   private String initPropertyTokenTypeText(boolean flag, Place place)
	   {
		   String result = "";
		   if(place.getDataType() != null)
		   {
			   DataType d = place.getDataType();
			   if(d.getDef())
			   {
		    		  Vector<String> types = d.getTypes();
					  String s = "";
					  for(int j = 0; j < types.size(); j ++)
					  {
						  s += types.get(j);
						  if(j < types.size() - 1)
						  {
							  s += " ,";
						  }
					  }
					  result = "Input Format:\n[" + s + "]";
		    	}
			   
		   }
		   if(flag)
		   {
			   this.PropertyTokenText.setForeground(Color.gray);
			   PropertyTokenText.setText(result);//to be modified;
		   }
		   return result;
	   }
}

