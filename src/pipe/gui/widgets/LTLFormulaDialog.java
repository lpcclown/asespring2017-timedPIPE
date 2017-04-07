package pipe.gui.widgets;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import ltlparser.errormsg.ErrorMsg;
import ltlparser.ParseLTL;
import formula.parser.SyntaxTreeCrawler;

public class LTLFormulaDialog extends JDialog {
	
	    public LTLFormulaPanel m_Panel;
	    private String m_xmlString = "";
	    private FormulaDialogInterface m_dlgInterface;
	    private ActionListener  mCallback;
	    private String mInitialFormula;
	    HashSet<String> mDefinedVariables;
	    
	    
	    private SyntaxTreeCrawler syntree;

	    public LTLFormulaDialog(final String pInitialFormula, final HashSet<String> pDefinedVariables, final ActionListener pCallback){
	    	super();
	    	mCallback = pCallback;
	    	mInitialFormula = pInitialFormula;
	    	initialize();
	    }
	    public LTLFormulaDialog(FormulaDialogInterface dlgInterface) throws HeadlessException {
	        super();
	        m_dlgInterface = dlgInterface;
	        initialize();
	    }

	    public LTLFormulaDialog(String title, FormulaDialogInterface dlgInterface) throws HeadlessException {
	        super();
	        m_dlgInterface = dlgInterface;
	        initialize();
	        setTitle(title);
	    }

	    public LTLFormulaDialog(String title, String xml, FormulaDialogInterface dlgInterface) throws HeadlessException {
	        super();
	        m_dlgInterface = dlgInterface;
	        initialize();
//	        setXMLText(xml);
	        setTitle(title);
	    }

//	    public void setXMLText(String xml){
//	        m_xmlString = xml;
//	        XMLToString xmlToStr = new XMLToString(xml);
////	                               String str = XMLToString.. .generateStrFromXML(xml);
////	        m_LTLPanel.setString(str);
//	        if (xmlToStr.hasErrors()){
//	            // Show some error message
//	            m_FOLPanel.setString("");
//	        }else
//	            m_FOLPanel.setString(xmlToStr.getString());
//	    }

	    protected void initialize() {
	        setTitle("Formula Editor");
	        m_Panel = new LTLFormulaPanel(mInitialFormula);
	        JScrollPane scrollPane = new JScrollPane();
	        scrollPane.getViewport().add(m_Panel);
	        getContentPane().setLayout(null);
	        getContentPane().add(scrollPane, java.awt.BorderLayout.NORTH);
	        scrollPane.setSize(m_Panel.getSize().width + 5,
	                           m_Panel.getSize().height + 5);
	        scrollPane.setLocation(5, 5);
	        Border lineborder = BorderFactory.createEtchedBorder(EtchedBorder.
	                RAISED);
	        scrollPane.setBorder(lineborder);

	        JButton closeBut = new JButton("Ok");
	        closeBut.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent e) {
	            	//checkVarConsistencyPressed();
	                okPressed();
	            }
	        });
	        closeBut.setSize(100, 30);
	        closeBut.setLocation((scrollPane.getWidth() + scrollPane.getX()) / 3,
	                             scrollPane.getY() + scrollPane.getHeight() + 5);

	        getContentPane().add(closeBut);
	        this.rootPane.setDefaultButton(closeBut);
	        this.setSize(scrollPane.getWidth() + scrollPane.getX() + 20,
	                     closeBut.getY() + closeBut.getHeight() + 45);
	        //add XML button
//	        JButton XMLBut = new JButton("EditXML");
//	        XMLBut.addActionListener(new java.awt.event.ActionListener() {
//	            public void actionPerformed(java.awt.event.ActionEvent e) {
//	                xmlPressed();
//	            }
//	        });
//	        XMLBut.setSize(100,30);
//	        XMLBut.setLocation(closeBut.getX() + closeBut.getWidth() + 5, scrollPane.getY() + scrollPane.getHeight() + 5);
//	        getContentPane().add(XMLBut);
	        
	        //add Check variable consistency button
//	        JButton CkVarBut = new JButton("CheckVar");
//	        CkVarBut.addActionListener(new java.awt.event.ActionListener(){
//	        	public void actionPerformed(java.awt.event.ActionEvent e){
//	        		checkVarConsistencyPressed();
//	        	}
//	        });
//	        CkVarBut.setSize(100, 30);
//	        CkVarBut.setLocation(XMLBut.getX() + XMLBut.getWidth() + 5, scrollPane.getY() + scrollPane.getHeight() + 5);
//	        getContentPane().add(CkVarBut);
	   //     setModal(true);
	    }

	    private void okPressed(){
	    	try{
		    	String strtoParse = new String(m_Panel.m_textField.getText());
		    	if(strtoParse.equals("")){
		    		JOptionPane.showMessageDialog(this,"Formula is empty!.","Null Error",JOptionPane.ERROR_MESSAGE);
		    		return;
		    	}
		    	/**
		    	 * Check formula grammar;
		    	 */
		//    	java.io.Reader inp = (java.io.Reader) (new java.io.StringReader(strtoParse));    

		    	
		//		   status = s.bool_val;
		    	mCallback.actionPerformed(new ActionEvent(m_Panel, -1, strtoParse));
//		    	myTransition.getTransSymbolTable().cleanTable();
//		        //store new formula to transition object
		        

	    	}catch(Exception e){
	    		
	    		System.err.println("Exception:Syntax Error Found!");
					e.printStackTrace();
	    		JOptionPane.showMessageDialog(this,"Syntax Errors found in formula.","Formula Syntax Error",JOptionPane.ERROR_MESSAGE);
	    		return;
	    	}
	    	
	    	//change the transition rectangle color
	    	
	    	
	        setVisible(false);
	        if (m_dlgInterface!=null)
	            m_dlgInterface.parseExecuted();
	        
	    	//Test transition enabled HERE
	    	/***********************************/
//	    	myTransition.TestCheckTransitionIsEnabled();
//	      CheckTransitionIsEnabled(myTransition);
	    	/*********************************/
	    }
//	    private boolean getXML() {
//	        String str = new String(m_FOLPanel.getString());
//	        /*java.io.Reader inp = (java.io.Reader) (new java.io.StringReader(str));
//	        ErrorMsg errorMsg = new ErrorMsg(str);
//	        ParseFOL parse = new ParseFOL(str, errorMsg);
//	        if (errorMsg.anyErrors) {
//	            java.util.Vector vec = errorMsg.getMsgs();
//	            return false;
//	        }
//	        // Get the abstract syntax tree.
//	        LogicSentence sentence = parse.absyn;
//	        XMLGenerator gen = new XMLGenerator();
//	        gen.visit(sentence);
//	        //System.out.println("<xml>"+gen.getXML()+"</xml>");
//	        //m_xmlString = "<xml>" + gen.getXML() + "</xml>";
//	        m_xmlString = gen.getXML();
//	        return true;*/
//	        StringToXML strToXml = new StringToXML(str);
//	        if (strToXml.hasErrors()){
//	            return false;
//	        }
//	        m_xmlString = strToXml.getXML();
//	        return true;
//	    }
//	    private void xmlPressed(){
	//
//	    	XMLDialog m_dlg = new XMLDialog(myTransition);
//	    	m_dlg.setVisible(true);
//	    	m_dlg.setSize(600, 600);
//	    	if(m_dlg.m_panel.m_textArea.getText() == null){
//	    		return;
//	    	}
//	    }
	    
//	    private void checkVarConsistencyPressed(){
//	    	String strtoCheck = new String(m_Panel.m_textField.getText());
//	        //store new formula to transition object
//	        String currentFormula = myTransition.getFormula();
//	        if(strtoCheck == null)return;
//	        String newFormula = strtoCheck;
//	        if(currentFormula != newFormula){
//	        	CreateGui.getView().getUndoManager().addNewEdit(myTransition.setFormula(newFormula));
//	            //store new formula as XML
//	            myTransition.setXMLFormula();
//	            //update transition variable list
//	            myTransition.setTranVarList();
//	        }
//	    	
//	    	CheckVar ckvar = new CheckVar(myTransition);
//	    	boolean b = ckvar.check();
//	    	if(b){
//	    		JOptionPane.showMessageDialog(this,"Variable Consistency Satisfied!", "Variable Pass", JOptionPane.OK_CANCEL_OPTION);
//	    	}else{
//	    		JOptionPane.showMessageDialog(this,"Variable inconsistency found!","Variable Error",JOptionPane.ERROR_MESSAGE);
//	    	}
//	    }
	    public String getXMLString(){
	        return  m_xmlString;
	    }
	}
	


