package pipe.client.ui.status;

import pipe.client.api.model.AnimationHistory;
import pipe.client.api.model.AnimationHistoryItem;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class AnimationHistoryUI {

  private final AnimationHistory mModel;
  private JTree mTree;
  private DefaultMutableTreeNode mRootNode;
  private DefaultTreeModel mTreeModel;

  public AnimationHistoryUI(final AnimationHistory pModel) {
    mModel = pModel;
    initialize();
  }

  private void initialize() {
//    initModels();
//    initCommands();
    initComponents();
    initListeners();
//    initLayout();
//    initState();
  }


  private void initListeners() {
    mModel.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(final PropertyChangeEvent evt) {
        if ("run.newItem".equals(evt.getPropertyName())) {
          AnimationHistoryItem itemModel = (AnimationHistoryItem) evt.getNewValue();
          DefaultMutableTreeNode newNode = createNode(itemModel);
          mRootNode.add(newNode);
          mTree.scrollPathToVisible(new TreePath(newNode.getPath()));
          mTreeModel.reload(mRootNode);
          System.out.println("Chile COunt: " + mTree.getModel().getChildCount(mRootNode));
        }
      }
    });
  }

  private DefaultMutableTreeNode createNode(final AnimationHistoryItem pItemModel) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(pItemModel.getTitle());
    node.add(new DefaultMutableTreeNode(pItemModel.getAnimationType()));
    node.add(new DefaultMutableTreeNode(pItemModel.getTerminationCriteria()));

    final DefaultMutableTreeNode timeTakenStatusNode = new DefaultMutableTreeNode(pItemModel.getTimeTakenInNanos());
    final DefaultMutableTreeNode transitionSequenceNode = new DefaultMutableTreeNode("Transitions");
    node.add(timeTakenStatusNode);
    node.add(transitionSequenceNode);

    pItemModel.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(final PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if ("run.newtransition".equals(propertyName)) {
          AnimationHistoryItem.TransitionItem transitionItem = (AnimationHistoryItem.TransitionItem) evt.getNewValue();
          DefaultMutableTreeNode transitionNode = new DefaultMutableTreeNode(transitionItem.getTransitionName());
          transitionNode.add(new DefaultMutableTreeNode("Input token combinations: Not shown"));
          transitionNode.add(new DefaultMutableTreeNode("Input symbols: Not shown"));
          transitionNode.add(new DefaultMutableTreeNode("Output tokens: Not shown"));
          transitionNode.add(new DefaultMutableTreeNode(transitionItem.getTimeTakenInMillis() + " Milli Seconds"));

          transitionSequenceNode.add(transitionNode);
        }
        else if ("run.totalTime".equals(propertyName)) {
          timeTakenStatusNode.setUserObject(mModel.getCurrentItem().getTimeTakenInMillis() + " Milli Seconds");
        }
      }
    });

    return node;
  }

  private void initComponents() {
    mRootNode = new DefaultMutableTreeNode(mModel.getTitle());
    mTreeModel = new DefaultTreeModel(mRootNode);

    mTree = new JTree(mTreeModel);
    mTree.setShowsRootHandles(true);
  }

  public JComponent rootComponent() {
    return mTree;
  }
}
