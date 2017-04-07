package pipe.gui.handler;

import analysis.ModelAnalysisSupport;
import ltlparser.ParseLTL;
import ltlparser.PropertyFormulaToPromela;
import ltlparser.errormsg.ErrorMsg;
import ltlparser.parser;
import org.apache.commons.lang.StringUtils;
import pipe.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.LTLFormulaDialog;
import pipe.gui.widgets.ResultsTxtPane;

import javax.swing.*;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

/**
 * Gui for verification in SPIN
 *
 * @author suliu
 */
public class AnalysisModuleHandler extends AbstractAction {

  private ResultsTxtPane results;
  private JTextField formulatext;

  private final ModelAnalysisSupport mModelAnalysisSupport;

  public AnalysisModuleHandler(final ModelAnalysisSupport pModelAnalysisSupport) {
    mModelAnalysisSupport = pModelAnalysisSupport;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    modelToPromelaWindow();
  }

  public void modelToPromelaWindow() {
    EscapableDialog guiDialog = new EscapableDialog(CreateGui.appGui, mModelAnalysisSupport.getModuleTitle(), false);

    Container contentPane = guiDialog.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

//	      // 2 Add file browser
//	      sourceFilePanel = new PetriNetChooserPanel("Source net",pnmlData);
//	      contentPane.add(sourceFilePanel);

    contentPane.add(results = new ResultsTxtPane(null)); //SUTODO: the null parameter in ResultsTxtPane to be reconsider.

    contentPane.add(new JLabel("Property Formula Specification:"));
    contentPane.add(formulatext = new JTextField(CreateGui.getModel().getPropertyFormula()));

    contentPane.add(new ButtonBar("Translate", new TranslationHandler(), guiDialog.getRootPane()));
    contentPane.add(new ButtonBar("Add Forumla", mFormulaAction, guiDialog.getRootPane()));
    contentPane.add(new ButtonBar("Verify", verifyButtonClick, guiDialog.getRootPane()));

    guiDialog.pack();
    guiDialog.setLocationRelativeTo(null);
    guiDialog.setVisible(true);
  }

  public String getName() {
    return mModelAnalysisSupport.getModuleTitle();
  }

  /**
   * Translate button click handler
   */
  private class TranslationHandler implements ActionListener {

    @Override
    public void actionPerformed(final ActionEvent e) {
      DataLayer sourceDataLayer = CreateGui.getModel();
      String propertyFormula = formulatext.getText();

      if (sourceDataLayer == null) {
        JOptionPane.showMessageDialog(null, "Please, choose a source net", "Error", JOptionPane.ERROR_MESSAGE);
      }
      else if (!sourceDataLayer.hasPlaceTransitionObjects()) {
        JOptionPane.showMessageDialog(null, "No Petri net objects defined!", "Error", JOptionPane.ERROR_MESSAGE);
      }
      else if (StringUtils.isBlank(propertyFormula)) {
        JOptionPane.showMessageDialog(null, "Property formula is empty. Please add formula!", "Error", JOptionPane.ERROR_MESSAGE);
      }
      else {
        String result = mModelAnalysisSupport.performTranslation(sourceDataLayer, propertyFormula);
        results.setEnabled(true);
//        System.out.println(String.format("Translated Promela code %s", result));
        results.setText(result);
      }
    }
  }

  ActionListener mFormulaAction = new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      LTLFormulaDialog formulaDialog = new LTLFormulaDialog("", new HashSet<>(), new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          String strtoParse = e.getActionCommand();
          ErrorMsg errorMsg = new ErrorMsg(strtoParse);
          ParseLTL p = new ParseLTL(strtoParse, errorMsg);
          if (!errorMsg.anyErrors) {
            PropertyFormulaToPromela translator = new PropertyFormulaToPromela(errorMsg);
            translator.visit(p.absyn);
            formulatext.setText(p.absyn.formula);
          }
        }
      });
      formulaDialog.setVisible(true);
      formulaDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }
  };


  /**
   * Verify button click handler
   */
  ActionListener verifyButtonClick = new ActionListener() {
    public void actionPerformed(ActionEvent e) {

      DataLayer sourceDataLayer = CreateGui.getModel();
      String propertyFormula = formulatext.getText();
      if (sourceDataLayer == null) {
        JOptionPane.showMessageDialog(null, "Please, choose a source net", "Error", JOptionPane.ERROR_MESSAGE);
      }
      else if (!sourceDataLayer.hasPlaceTransitionObjects()) {
        JOptionPane.showMessageDialog(null, "No Petri net objects defined!", "Error", JOptionPane.ERROR_MESSAGE);
      }
      else if (StringUtils.isBlank(propertyFormula)) {
        JOptionPane.showMessageDialog(null, "Property formula is empty. Please add formula!", "Error", JOptionPane.ERROR_MESSAGE);
      }
      else {
        String result = mModelAnalysisSupport.performVerification(sourceDataLayer, propertyFormula);
        results.setText(result);
      }
    }
  };

}
