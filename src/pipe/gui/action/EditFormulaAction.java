package pipe.gui.action;

import org.apache.commons.lang.StringUtils;
import pipe.dataLayer.Arc;
import pipe.dataLayer.Transition;
import pipe.gui.CreateGui;
import pipe.gui.widgets.FormulaDialog;

import javax.swing.AbstractAction;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

//import pipe.gui.action.TransitionGuardEdit;

/**
 * This class allows the user to change the weight on an arc.
 *
 * @author unknown
 * @author Dave Patterson May 4, 2007: Handle cancel choice without an
 *         exception. Change error messages to ask for a positive integer.
 */
public class EditFormulaAction
    extends AbstractAction {

  private static final long serialVersionUID = 2003;
  private Container contentPane;
  private Transition myTransition;
  //   private FormulaPanel m_panel;
  private FormulaDialog m_dlg;
  private String m_formulaString = "";
//   private TransitionGuardEdit m_guardEdit = null; 


  public EditFormulaAction(Container contentPane, Transition a) {
    this.contentPane = contentPane;
    myTransition = a;
  }

  private HashSet<String> searchDefinedVars() {
    HashSet<String> definedVars = new HashSet();
    for (Arc a : this.myTransition.getArcList()) {
      String[] vars = a.getVars();
      for (int i = 0; i < vars.length; i++) {
        definedVars.add(vars[i]);
      }
    }

    return definedVars;
  }

  public void actionPerformed(ActionEvent e) {

    if (!myTransition.checkTransitionIsReadyToDefine()) {
      return;
    }
    m_dlg = new FormulaDialog(myTransition.getFormula(), searchDefinedVars(), new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        String currentFormula = myTransition.getFormula();
        String newFormula = e.getActionCommand();
        if (StringUtils.isNotBlank(newFormula) && !newFormula.equals(currentFormula)) {
          CreateGui.getView().getUndoManager().addNewEdit(myTransition.setFormula(newFormula));
        }
      }
    });
    m_dlg.setVisible(true);
  }

}
