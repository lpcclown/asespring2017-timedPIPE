package pipe.gui;

import pipe.client.ui.status.AnimationHistoryUI;
import pipe.dataLayer.DataLayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;


public class CreateGui {
  public static final String HISTORY_KEY_TEMPLATE = "history_%d";

  public static GuiFrame appGui;
  private static Animator animator;
  private static JTabbedPane appTab;
  private static int freeSpace;

  private static ArrayList<TabData> tabs = new ArrayList<TabData>();
  private static final HashMap<String, pipe.client.api.model.AnimationHistory> sAnimationHistories = new HashMap<>();

  public static String imgPath, userPath; // useful for stuff

  private static class TabData { // a structure for holding a tab's data

    public DataLayer appModel;
    public GuiView appView;
    public File appFile;

  }

  private static JSplitPane pane;
  /**
   * The Module will go in the top pane, the animation window in the bottom pane
   */
  private static JSplitPane leftPane;
  private static AnimationHistory animBox;
  private static JPanel sHistoryPanel;
  private static JScrollPane scroller;


  public static void init() {
    imgPath = "Images/";
//      imgPath = System.getProperty("File.separator")+"Images" + System.getProperty("file.separator");

    // make the initial dir for browsing be My Documents (win), ~ (*nix), etc
    userPath = null;

    appGui = new GuiFrame("PIPE+Verifier For Bounded Model Checking High Level Petri Nets " +
        "1.0");

    Grid.enableGrid();

    appTab = new JTabbedPane();

    animator = new Animator();
    appGui.setTab();   // sets Tab properties

    // create the tree
//      ModuleManager moduleManager = new ModuleManager();
//      JTree moduleTree = moduleManager.getModuleTree();

//      leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,null,moduleTree);
//      leftPane.setContinuousLayout(true);
//      leftPane.setDividerSize(0);

    sHistoryPanel = new JPanel(new CardLayout());
    scroller = new JScrollPane(sHistoryPanel);
    scroller.setMinimumSize(new Dimension(300, 600));

    sHistoryPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    scroller.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

    pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroller, appTab);

    pane.setContinuousLayout(true);
    pane.setOneTouchExpandable(true);
    pane.setBorder(null); // avoid multiple borders
    pane.setDividerSize(8);

    appGui.getContentPane().add(pane);

    appGui.createNewTab(null, false);

    appGui.setVisible(true);
    appGui.init();
  }


  public static GuiFrame getApp() {  //returns a reference to the application
    return appGui;
  }


  public static DataLayer getModel() {
    return getModel(appTab.getSelectedIndex());
  }

  public static DataLayer getModel(int index) {
    if (index < 0) {
      return null;
    }

    TabData tab = (TabData) (tabs.get(index));
    if (tab.appModel == null) {
      tab.appModel = new DataLayer();
    }
    return tab.appModel;
  }


  public static GuiView getView(int index) {
    if (index < 0) {
      return null;
    }

    TabData tab = (TabData) (tabs.get(index));
    while (tab.appView == null) {
      try {
        tab.appView = new GuiView(tab.appModel);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    return tab.appView;
  }


  public static GuiView getView() {
    return getView(appTab.getSelectedIndex());
  }


  public static File getFile() {
    TabData tab = (TabData) (tabs.get(appTab.getSelectedIndex()));
    return tab.appFile;
  }


  public static void setFile(File modelfile, int fileNo) {
    if (fileNo >= tabs.size()) {
      return;
    }
    TabData tab = (TabData) (tabs.get(fileNo));
    tab.appFile = modelfile;
  }


  public static int getFreeSpace() {
    tabs.add(new TabData());
    return tabs.size() - 1;
  }

  public static int getAgentFreeSpace(DataLayer agentLayer) {
    TabData agentTabData = new TabData();
    agentTabData.appModel = agentLayer;
    tabs.add(agentTabData);
    return tabs.size() - 1;
  }


  public static void removeTab(int index) {
    tabs.remove(index);
  }


  public static JTabbedPane getTab() {
    return appTab;
  }

  public static ArrayList<TabData> getTabsDataList() {
    return tabs;
  }

  public static DataLayer getTabsModel(Object obj) {
    if (obj instanceof TabData) {
      return ((TabData) obj).appModel;
    }
    else {
      return null;
    }
  }

  public static Animator getAnimator() {
    return animator;
  }


  /**
   * returns the current dataLayer object -
   * used to get a reference to pass to the modules
   */
  public static DataLayer currentPNMLData() {
    if (appTab.getSelectedIndex() < 0) {
      return null;
    }
    TabData tab = (TabData) (tabs.get(appTab.getSelectedIndex()));
    return tab.appModel;
  }


  /**
   * Creates a new animationHistory text area, and returns a reference to it
   */
  public static void addAnimationHistory() {
    try {
      animBox = new AnimationHistory("Animation history\n");
      animBox.setEditable(false);

      scroller = new JScrollPane(animBox);
      scroller.setBorder(new EmptyBorder(0, 0, 0, 0)); // make it less bad on XP

      leftPane.setTopComponent(scroller);
//         leftPane.setBottomComponent(scroller);

      leftPane.setDividerLocation(0.5);
      leftPane.setDividerSize(8);
    }
    catch (javax.swing.text.BadLocationException be) {
      be.printStackTrace();
    }
  }

  public static void addAnimationHistory(final String pName, final int pIndex) {
    String historyKey = getHistoryKey(pIndex);
    pipe.client.api.model.AnimationHistory animationHistory = new pipe.client.api.model.AnimationHistory(pName);
    sAnimationHistories.put(historyKey, animationHistory);

    AnimationHistoryUI historyUI = new AnimationHistoryUI(animationHistory);
    sHistoryPanel.add(historyUI.rootComponent(), historyKey, pIndex);
    ((CardLayout) sHistoryPanel.getLayout()).show(sHistoryPanel, historyKey);
  }

  public static void showAnimationHistory(final int pIndex) {
    ((CardLayout) sHistoryPanel.getLayout()).show(sHistoryPanel, getHistoryKey(pIndex));
  }


  public static void removeAnimationHistory(final int pIndex) {
    sHistoryPanel.remove(pIndex);
    sAnimationHistories.remove(getHistoryKey(pIndex));
  }


  public static pipe.client.api.model.AnimationHistory getAnimationHistory(final int pIndex) {
    return sAnimationHistories.get(getHistoryKey(pIndex));
  }

  private static String getHistoryKey(final int pIndex) {
    return String.format(HISTORY_KEY_TEMPLATE, pIndex);
  }

  public static AnimationHistory getAnimationHistory() {
    return animBox;
  }

}
