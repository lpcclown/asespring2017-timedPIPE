package pipe.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.jar.JarEntry;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import analysis.Maude;
import analysis.ModelCompletenessCheck;
import analysis.PNPromela;
import analysis.PNPromelaTransitionAsProc;
import analysis.Promela;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataLayerWriter;
import pipe.dataLayer.PNMLTransformer;
import pipe.dataLayer.PetriNetObject;
import pipe.dataLayer.TNTransformer;
import pipe.experiment.Experiment;
import pipe.experiment.editor.gui.ExperimentEditor;
import pipe.gui.action.GuiAction;
import pipe.gui.handler.AnalysisModuleHandler;
import pipe.gui.handler.BoundedModelCheckingModule;
import pipe.gui.widgets.FileBrowser;
import pipe.io.JarUtilities;

/**
 * @author Edwin Chung changed the code so that the firedTransitions array list
 *         is reset when the animation mode is turned off
 * @author Ben Kirby, 10 Feb 2007: Changed the saveNet() method so that it calls
 *         new DataLayerWriter class and passes in current net to save.
 * @author Ben Kirby, 10 Feb 2007: Changed the createNewTab method so that it
 *         loads an XML file using the new PNMLTransformer class and
 *         createFromPNML DataLayer method.
 * @author Edwin Chung modifed the createNewTab method so that it assigns the
 *         file name of the newly created DataLayer object in the dataLayer
 *         class (Mar 2007)
 * @author Oliver Haggarty modified initaliseActions to fix a bug that meant not
 *         all example nets were loaded if there was a non .xml file in the
 *         folder
 */
public class GuiFrame extends JFrame implements ActionListener, Observer {

	// for zoom combobox and dropdown
	private final String[] zoomExamples = { "40%", "60%", "80%", "100%", "120%", "140%", "160%", "180%", "200%",
			"300%" };
	private String frameTitle; // Frame title
	private DataLayer appModel;
	private GuiFrame appGui;
	private GuiView appView;
	private int mode, prev_mode, old_mode; // *** mode WAS STATIC ***
	private int newNameCounter = 1;
	private JTabbedPane appTab;
	private StatusBar statusBar;
	private JMenuBar menuBar;
	private JToolBar animationToolBar, drawingToolBar;

	// private Map actions = new HashMap();
	private JComboBox zoomComboBox;

	private FileAction createAction, openAction, closeAction, saveAction, saveAsAction, exitAction, printAction,
			exportPNGAction, exportTNAction, exportPSAction, importAction;

	private EditAction copyAction, cutAction, pasteAction, undoAction, redoAction;
	private GridAction toggleGrid;
	private ZoomAction zoomOutAction, zoomInAction, zoomAction;
	private DeleteAction deleteAction;
	private TypeAction annotationAction, arcAction, bidirectionalAction, inhibarcAction, placeAction, transAction,
			timedtransAction, tokenAction, selectAction, rateAction, markingAction, deleteTokenAction, dragAction;
	private AnimateAction startAction, stepforwardAction, stepbackwardAction, randomLowLevelAction,
			randomHighLevelAction, randomAnimateAction, modelCheckingAction, convertZ3Action;
	private AnimateAction modelCheckingActionUsingMaude;

	public boolean dragging = false;

	private HelpBox helpAction;
	private ExperimentAction loadExperimentAction, experimentEditorAction;

	private boolean editionAllowed = true;

	private CopyPasteManager copyPasteManager;

	public static int globalTime;

	public static JTextField globalTimeTextField = new JTextField(" Global Time: " + globalTime);

	public GuiFrame(String title) {
		// HAK-arrange for frameTitle to be initialized and the default file
		// name
		// to be appended to basic window title
		frameTitle = title;
		setTitle(null);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception exc) {
			System.err.println("Error loading L&F: " + exc);
		}
		this.setIconImage(new ImageIcon(
				Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "icon.png")).getImage());

		// this.setIconImage(new
		// ImageIcon(GuiFrame.class.getResource("/Images/icon.png")).getImage());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(screenSize.width * 80 / 100, screenSize.height * 80 / 100);
		this.setLocationRelativeTo(null);

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		buildMenus();

		// Status bar...
		statusBar = new StatusBar();
		getContentPane().add(statusBar, BorderLayout.PAGE_END);

		// Build menus
		buildToolbar();

		addWindowListener(new WindowHandler());

		//
		copyPasteManager = new CopyPasteManager();

		this.setForeground(java.awt.Color.BLACK);
		this.setBackground(java.awt.Color.WHITE);
	}

	/**
	 * This method does build the menus.
	 *
	 * @author unknown
	 * @author Dave Patterson - fixed problem on OSX due to invalid character in
	 *         URI caused by unescaped blank. The code changes one blank
	 *         character if it exists in the string version of the URL. This way
	 *         works safely in both OSX and Windows. I also added a
	 *         printStackTrace if there is an exception caught in the setup for
	 *         the "Example nets" folder.
	 **/
	private void buildMenus() {
		menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');

		addMenuItem(fileMenu, createAction = new FileAction("New", "Create a new Petri net", "ctrl N"));
		addMenuItem(fileMenu, openAction = new FileAction("Open", "Open", "ctrl O"));
		addMenuItem(fileMenu, closeAction = new FileAction("Close", "Close the current tab", "ctrl W"));
		fileMenu.addSeparator();
		addMenuItem(fileMenu, importAction = new FileAction("Import", "Import from Timenet", ""));
		addMenuItem(fileMenu, saveAction = new FileAction("Save", "Save", "ctrl S"));
		addMenuItem(fileMenu, saveAsAction = new FileAction("Save as", "Save as...", "shift ctrl S"));

		// Export menu
		JMenu exportMenu = new JMenu("Export");
		exportMenu.setIcon(new ImageIcon(
				Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "Export.png")));
		// exportMenu.setIcon(new
		// ImageIcon(GuiFrame.class.getResource("/Images/Export.png")));
		addMenuItem(exportMenu, exportPNGAction = new FileAction("PNG", "Export the net to PNG format", "ctrl G"));
		addMenuItem(exportMenu,
				exportPSAction = new FileAction("PostScript", "Export the net to PostScript format", "ctrl T"));
		addMenuItem(exportMenu, exportTNAction = new FileAction("Timenet", "Export the net to Timenet format", ""));
		fileMenu.add(exportMenu);
		fileMenu.addSeparator();
		addMenuItem(fileMenu, printAction = new FileAction("Print", "Print", "ctrl P"));
		fileMenu.addSeparator();

		// Example files menu
		// try {
		// URL examplesDirURL = Thread.currentThread().getContextClassLoader().
		// getResource("Example nets" + System.getProperty("file.separator"));
		//
		// if (JarUtilities.isJarFile(examplesDirURL)){
		//
		// JarFile jarFile = new
		// JarFile(JarUtilities.getJarName(examplesDirURL));
		//
		// ArrayList <JarEntry> nets =
		// JarUtilities.getJarEntries(jarFile, "Example nets");
		//
		// Arrays.sort(nets.toArray(), new Comparator(){
		// public int compare(Object one, Object two) {
		// return
		// ((JarEntry)one).getName().compareTo(((JarEntry)two).getName());
		// }
		// });
		//
		// if (nets.size() > 0) {
		// JMenu exampleMenu=new JMenu("Example nets");
		// exampleMenu.setIcon(
		// new ImageIcon(Thread.currentThread().getContextClassLoader().
		// getResource(CreateGui.imgPath + "Example.png")));
		// int index = 0;
		// for (int i = 0; i < nets.size(); i++){
		// if (nets.get(i).getName().toLowerCase().endsWith(".xml")){
		// addMenuItem(exampleMenu,
		// new ExampleFileAction(nets.get(i),
		// (index < 10) ?("ctrl " + index) :null));
		// index++;
		// }
		// }
		// fileMenu.add(exampleMenu);
		// fileMenu.addSeparator();
		// }
		// } else {
		// File examplesDir = new File(examplesDirURL.toURI());
		// /**
		// * The next block fixes a problem that surfaced on Mac OSX with
		// * PIPE 2.4. In that environment (and not in Windows) any blanks
		// * in the project name in Eclipse are property converted to '%20'
		// * but the blank in "Example nets" is not. The following code
		// * will do nothing on a Windows machine or if the logic on OSX
		// * changess. I also added a stack trace so if the problem
		// * occurs for another environment (perhaps multiple blanks need
		// * to be manually changed) it can be easily fixed. DP
		// */
		// // examplesDir = new File(new URI(examplesDirURL.toString()));
		// String dirURLString = examplesDirURL.toString();
		// int index = dirURLString.indexOf( " " );
		// if ( index > 0 ) {
		// StringBuffer sb = new StringBuffer( dirURLString );
		// sb.replace( index, index + 1, "%20" );
		// dirURLString = sb.toString();
		// }
		//
		// examplesDir = new File( new URI(dirURLString ) );
		//
		// File[] nets = examplesDir.listFiles();
		//
		// Arrays.sort(nets,new Comparator(){
		// public int compare(Object one, Object two) {
		// return ((File)one).getName().compareTo(((File)two).getName());
		// }
		// });
		//
		// // Oliver Haggarty - fixed code here so that if folder contains non
		// // .xml file the Example x counter is not incremented when that file
		// // is ignored
		// if (nets.length > 0) {
		// JMenu exampleMenu=new JMenu("Example nets");
		// exampleMenu.setIcon(
		// new ImageIcon(Thread.currentThread().getContextClassLoader().
		// getResource(CreateGui.imgPath + "Example.png")));
		// int k = 0;
		// for (int i = 0; i < nets.length; i++){
		// if(nets[i].getName().toLowerCase().endsWith(".xml")){
		// addMenuItem(exampleMenu,
		// new ExampleFileAction(nets[i], (k<10)?"ctrl " + (k++) :null));
		// }
		// }
		// fileMenu.add(exampleMenu);
		// fileMenu.addSeparator();
		// }
		// }
		// } catch (Exception e) {
		// System.err.println("Error getting example files:" + e);
		// e.printStackTrace();
		// }
		addMenuItem(fileMenu, exitAction = new FileAction("Exit", "Close the program", "ctrl Q"));

		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		addMenuItem(editMenu, undoAction = new EditAction("Undo", "Undo (Ctrl-Z)", "ctrl Z"));
		addMenuItem(editMenu, redoAction = new EditAction("Redo", "Redo (Ctrl-Y)", "ctrl Y"));
		editMenu.addSeparator();
		addMenuItem(editMenu, cutAction = new EditAction("Cut", "Cut (Ctrl-X)", "ctrl X"));
		addMenuItem(editMenu, copyAction = new EditAction("Copy", "Copy (Ctrl-C)", "ctrl C"));
		addMenuItem(editMenu, pasteAction = new EditAction("Paste", "Paste (Ctrl-V)", "ctrl V"));
		addMenuItem(editMenu, deleteAction = new DeleteAction("Delete", "Delete selection", "DELETE"));

		JMenu drawMenu = new JMenu("Draw");
		drawMenu.setMnemonic('D');
		addMenuItem(drawMenu, selectAction = new TypeAction("Select", Pipe.SELECT, "Select components", "S", true));
		drawMenu.addSeparator();
		addMenuItem(drawMenu, placeAction = new TypeAction("Place", Pipe.PLACE, "Add a place", "P", true));
		addMenuItem(drawMenu, transAction = new TypeAction("Immediate transition", Pipe.IMMTRANS,
				"Add an immediate transition", "I", true));
		addMenuItem(drawMenu, timedtransAction = new TypeAction("Timed transition", Pipe.TIMEDTRANS,
				"Add a timed transition", "T", true));
		addMenuItem(drawMenu, arcAction = new TypeAction("Arc", Pipe.ARC, "Add an arc", "A", true));
		addMenuItem(drawMenu, bidirectionalAction = new TypeAction("Bidirectional Arc", Pipe.BIDIRECTARC,
				"Add bidirectional arc", "B", true));
		addMenuItem(drawMenu,
				inhibarcAction = new TypeAction("Inhibitor Arc", Pipe.INHIBARC, "Add an inhibitor arc", "H", true));
		addMenuItem(drawMenu,
				annotationAction = new TypeAction("Annotation", Pipe.ANNOTATION, "Add an annotation", "N", true));
		drawMenu.addSeparator();
		addMenuItem(drawMenu, tokenAction = new TypeAction("Add token", Pipe.ADDTOKEN, "Add a token", "ADD", true));
		addMenuItem(drawMenu,
				deleteTokenAction = new TypeAction("Delete token", Pipe.DELTOKEN, "Delete a token", "SUBTRACT", true));
		drawMenu.addSeparator();
		addMenuItem(drawMenu, rateAction = new TypeAction("Rate Parameter", Pipe.RATE, "Rate Parameter", "R", true));
		addMenuItem(drawMenu,
				markingAction = new TypeAction("Marking Parameter", Pipe.MARKING, "Marking Parameter", "M", true));

		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic('V');

		JMenu zoomMenu = new JMenu("Zoom");
		zoomMenu.setIcon(new ImageIcon(
				Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "Zoom.png")));
		// zoomMenu.setIcon(new
		// ImageIcon(GuiFrame.class.getResource("/Images/Zoom.png")));
		addZoomMenuItems(zoomMenu);

		addMenuItem(viewMenu, zoomOutAction = new ZoomAction("Zoom out", "Zoom out by 10% ", "ctrl MINUS"));
		addMenuItem(viewMenu, zoomInAction = new ZoomAction("Zoom in", "Zoom in by 10% ", "ctrl PLUS"));
		viewMenu.add(zoomMenu);

		viewMenu.addSeparator();
		addMenuItem(viewMenu, toggleGrid = new GridAction("Cycle grid", "Change the grid size", "G"));
		addMenuItem(viewMenu, dragAction = new TypeAction("Drag", Pipe.DRAG, "Drag the drawing", "D", true));

		JMenu animateMenu = new JMenu("Animate");
		animateMenu.setMnemonic('A');
		addMenuItem(animateMenu,
				startAction = new AnimateAction("Animation mode", Pipe.START, "Toggle Animation Mode", "Ctrl A", true));
		animateMenu.addSeparator();
		addMenuItem(animateMenu,
				stepbackwardAction = new AnimateAction("Back", Pipe.STEPBACKWARD, "Step backward a firing", "4"));
		addMenuItem(animateMenu,
				stepforwardAction = new AnimateAction("Forward", Pipe.STEPFORWARD, "Step forward a firing", "6"));
		addMenuItem(animateMenu, randomLowLevelAction = new AnimateAction("RandomLowLevel", Pipe.RANDOMLOWLEVEL,
				"Randomly fire a low level transition", "5"));
		addMenuItem(animateMenu, randomHighLevelAction = new AnimateAction("RandomHighLevel", Pipe.RANDOMHIGHLEVEL,
				"Randomly fire a high level transition", "6"));
		addMenuItem(animateMenu, randomAnimateAction = new AnimateAction("Animate", Pipe.ANIMATE,
				"Randomly fire a number of transitions", "7", true));
		addMenuItem(animateMenu, modelCheckingAction = new AnimateAction("ModelChecking", Pipe.MODELCHECKING,
				"Model checking Petri nets", "8", true));
		addMenuItem(animateMenu, modelCheckingActionUsingMaude = new AnimateAction("ModelCheckingUsingMaude",
				Pipe.MODELCHECKING_MAUDE, "Model checking of Petri nets using Maude", "9", true));
		addMenuItem(animateMenu, convertZ3Action = new AnimateAction("Z3", Pipe.CONVERTZ3,
				"Convert Petri Nets model to Z3 SMT", "10", true));

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('H');
		helpAction = new HelpBox("Help", "View documentation", "F1", "index.htm");
		addMenuItem(helpMenu, helpAction);
		JMenuItem aboutItem = helpMenu.add("About PIPE");
		aboutItem.addActionListener(this); // Help - About is implemented
											// differently

		URL iconURL = Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "About.png");
		// URL iconURL =
		// GuiFrame.class.getResource("/Images/About.png");
		if (iconURL != null) {
			aboutItem.setIcon(new ImageIcon(iconURL));
		}

		JMenu experimentMenu = new JMenu("Experiment");
		addMenuItem(experimentMenu,
				loadExperimentAction = new ExperimentAction("Load experiment", "Load an experiment file", ""));
		addMenuItem(experimentMenu,
				experimentEditorAction = new ExperimentAction("Experiment Editor", "Start the experiment editor", ""));

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenu);
		menuBar.add(drawMenu);
		menuBar.add(animateMenu);
		// menuBar.add(experimentMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
	}

	public static void addGlobalTimeText() {
		globalTimeTextField.setText(" Global Time: " + globalTime);
	}

	private void buildToolbar() {
		// Create the toolbar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);// Inhibit toolbar floating

		addButton(toolBar, createAction);
		addButton(toolBar, openAction);
		addButton(toolBar, saveAction);
		addButton(toolBar, saveAsAction);
		addButton(toolBar, closeAction);
		toolBar.addSeparator();
		addButton(toolBar, printAction);
		toolBar.addSeparator();
		// addButton(toolBar,cutAction);
		// addButton(toolBar,copyAction);
		// addButton(toolBar,pasteAction);
		addButton(toolBar, deleteAction);
		// addButton(toolBar,undoAction);
		// addButton(toolBar,redoAction);
		toolBar.addSeparator();

		addButton(toolBar, zoomOutAction);
		addZoomComboBox(toolBar, zoomAction = new ZoomAction("Zoom", "Select zoom percentage ", ""));
		addButton(toolBar, zoomInAction);
		toolBar.addSeparator();
		addButton(toolBar, toggleGrid);
		addButton(toolBar, dragAction);
		addButton(toolBar, startAction);

		drawingToolBar = new JToolBar();
		drawingToolBar.setFloatable(false);

		toolBar.addSeparator();
		addButton(drawingToolBar, selectAction);
		drawingToolBar.addSeparator();

		addButton(drawingToolBar, placeAction);// Add Draw Menu Buttons
		addButton(drawingToolBar, transAction);

		// addButton(drawingToolBar,timedtransAction);
		addButton(drawingToolBar, arcAction);
		addButton(drawingToolBar, bidirectionalAction);
		// addButton(drawingToolBar,inhibarcAction);
		// addButton(drawingToolBar,annotationAction);
		drawingToolBar.addSeparator();
		// addButton(drawingToolBar,tokenAction);
		// addButton(drawingToolBar,deleteTokenAction);
		drawingToolBar.addSeparator();
		// addButton(drawingToolBar,rateAction);
		// addButton(drawingToolBar,markingAction);

		toolBar.add(drawingToolBar);

		animationToolBar = new JToolBar();
		animationToolBar.setFloatable(false);
		// addButton(animationToolBar, stepbackwardAction);
		// addButton(animationToolBar, stepforwardAction);
		// addButton(animationToolBar, randomLowLevelAction);
		// addButton(animationToolBar, randomAnimateAction);
		addButton(animationToolBar, randomHighLevelAction);
		addButton(animationToolBar, randomAnimateAction);
		animationToolBar.addSeparator();
		addButton(animationToolBar, modelCheckingAction);
		addButton(animationToolBar, modelCheckingActionUsingMaude);
		addButton(animationToolBar, convertZ3Action);
		toolBar.add(animationToolBar);
		animationToolBar.setVisible(false);

		toolBar.addSeparator();
		addButton(toolBar, helpAction);

		toolBar.addSeparator();
		// toolBar.add(new JTextField(" Global Time: " + globalTime));
		toolBar.add(globalTimeTextField);
		for (int i = 0; i < toolBar.getComponentCount(); i++) {
			toolBar.getComponent(i).setFocusable(false);
		}

		getContentPane().add(toolBar, BorderLayout.PAGE_START);
	}

	private void addButton(JToolBar toolBar, GuiAction action) {

		if (action.getValue("selected") != null) {
			toolBar.add(new ToggleButton(action));
		} else {
			toolBar.add(action);
		}
	}

	/**
	 * @param JMenu
	 *            - the menu to add the submenu to
	 * @author Ben Kirby Takes the method of setting up the Zoom menu out of the
	 *         main buildMenus method.
	 */
	private void addZoomMenuItems(JMenu zoomMenu) {
		for (int i = 0; i <= zoomExamples.length - 1; i++) {
			JMenuItem newItem = new JMenuItem(
					new ZoomAction(zoomExamples[i], "Select zoom percentage", i < 10 ? "ctrl shift " + i : ""));
			zoomMenu.add(newItem);
		}
	}

	/**
	 * @param toolBar
	 *            the JToolBar to add the button to
	 * @param action
	 *            the action that the ZoomComboBox performs
	 * @author Ben Kirby Just takes the long-winded method of setting up the
	 *         ComboBox out of the main buildToolbar method. Could be adapted
	 *         for generic addition of comboboxes
	 */
	private void addZoomComboBox(JToolBar toolBar, Action action) {
		Dimension zoomComboBoxDimension = new Dimension(65, 28);
		zoomComboBox = new JComboBox(zoomExamples);
		zoomComboBox.setEditable(true);
		zoomComboBox.setSelectedItem("100%");
		zoomComboBox.setMaximumRowCount(zoomExamples.length);
		zoomComboBox.setMaximumSize(zoomComboBoxDimension);
		zoomComboBox.setMinimumSize(zoomComboBoxDimension);
		zoomComboBox.setPreferredSize(zoomComboBoxDimension);
		zoomComboBox.setAction(action);
		toolBar.add(zoomComboBox);
	}

	private JMenuItem addMenuItem(JMenu menu, Action action) {
		JMenuItem item = menu.add(action);
		KeyStroke keystroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);

		if (keystroke != null) {
			item.setAccelerator(keystroke);
		}
		return item;
	}

	/* sets all buttons to enabled or disabled according to status. */
	private void enableActions(boolean status) {

		saveAction.setEnabled(status);
		saveAsAction.setEnabled(status);

		placeAction.setEnabled(status);
		arcAction.setEnabled(status);
		bidirectionalAction.setEnabled(status);
		inhibarcAction.setEnabled(status);
		annotationAction.setEnabled(status);
		transAction.setEnabled(status);
		timedtransAction.setEnabled(status);
		tokenAction.setEnabled(status);
		deleteAction.setEnabled(status);
		selectAction.setEnabled(status);
		deleteTokenAction.setEnabled(status);
		rateAction.setEnabled(status);
		markingAction.setEnabled(status);

		// toggleGrid.setEnabled(status);

		if (status) {
			startAction.setSelected(false);
			randomAnimateAction.setSelected(false);
			stepbackwardAction.setEnabled(!status);
			stepforwardAction.setEnabled(!status);
			drawingToolBar.setVisible(true);
			animationToolBar.setVisible(false);
		}
		randomLowLevelAction.setEnabled(!status);
		randomAnimateAction.setEnabled(!status);
		randomHighLevelAction.setEnabled(!status);
		modelCheckingAction.setEnabled(!status);
		modelCheckingActionUsingMaude.setEnabled(!status);
		convertZ3Action.setEnabled(!status);

		if (!status) {
			drawingToolBar.setVisible(false);
			animationToolBar.setVisible(true);
			pasteAction.setEnabled(status);
			undoAction.setEnabled(status);
			redoAction.setEnabled(status);
		} else {
			pasteAction.setEnabled(getCopyPasteManager().pasteEnabled());
		}
		copyAction.setEnabled(status);
		cutAction.setEnabled(status);
		deleteAction.setEnabled(status);
	}

	// set frame objects by array index
	private void setObjects(int index) {
		appModel = CreateGui.getModel(index);
		appView = CreateGui.getView(index);
	}

	// HAK set current objects in Frame
	public void setObjects() {
		appModel = CreateGui.getModel();
		appView = CreateGui.getView();
	}

	private void setObjectsNull(int index) {
		CreateGui.removeTab(index);
	}

	// set tabbed pane properties and add change listener that updates tab with
	// linked model and view
	public void setTab() {

		appTab = CreateGui.getTab();
		appTab.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				if (appGui.getCopyPasteManager().pasteInProgress()) {
					appGui.getCopyPasteManager().cancelPaste();
				}

				int index = appTab.getSelectedIndex();
				setObjects(index);
				CreateGui.showAnimationHistory(index);
				if (appView != null) {
					appView.setVisible(true);
					appView.repaint();
					updateZoomCombo();

					enableActions(!appView.isInAnimationMode());
					// CreateGui.getAnimator().restoreModel();
					// CreateGui.removeAnimationHistory();

					setTitle(appTab.getTitleAt(index));

					// TODO: change this code... it's ugly :)
					if (appGui.getMode() == Pipe.SELECT) {
						appGui.init();
					}

				} else {
					setTitle(null);
				}

				// CreateGui.s

			}

		});
		appGui = CreateGui.getApp();
		appView = CreateGui.getView();
	}

	// Less sucky yet far, far simpler to code About dialogue
	public void actionPerformed(ActionEvent e) {

		JOptionPane.showMessageDialog(this,
				"Imperial College DoC MSc Group And MSc Individual Project\n\n"
						+ "Original version PIPE(c)\n2003 by Jamie Bloom, Clare Clark, Camilla Clifford, Alex Duncan, Haroun Khan and Manos Papantoniou\n\n"
						+ "MLS(tm) Edition PIPE2(c)\n2004 by Tom Barnwell, Michael Camacho, Matthew Cook, Maxim Gready, Peter Kyme and Michail Tsouchlaris\n"
						+ "2005 by Nadeem Akharware\n\n" + "PIPE 2.4 by Tim Kimber, Ben Kirby, Thomas Master, "
						+ "Matthew Worthington\n\n" + "PIPE 2.5 by Pere Bonet (Universitat de les Illes Balears)\n\n"
						+ "http://pipe2.sourceforge.net/",
				"About PIPE2", JOptionPane.INFORMATION_MESSAGE);
	}

	// HAK Method called by netModel object when it changes
	public void update(Observable o, Object obj) {
		if ((mode != Pipe.CREATING) && (!appView.isInAnimationMode())) {
			appView.setNetChanged(true);
		}
	}

	public void saveOperation(boolean forceSaveAs) {

		if (appView == null) {
			return;
		}

		File modelFile = CreateGui.getFile();
		if (!forceSaveAs && modelFile != null) { // ordinary save
			/*
			 * //Disabled as currently ALWAYS prevents the net from being saved
			 * - Nadeem 26/05/2005 if (!appView.netChanged) { return; }
			 */
			saveNet(modelFile);
		} else { // save as
			String path = null;
			if (modelFile != null) {
				path = modelFile.toString();
			} else {
				path = appTab.getTitleAt(appTab.getSelectedIndex());
			}
			String filename = new FileBrowser(path).saveFile();
			if (filename != null) {
				saveNet(new File(filename));
			}
		}
	}

	private void saveNet(File outFile) {
		try {
			// BK 10/02/07:
			// changed way of saving to accomodate new DataLayerWriter class
			DataLayerWriter saveModel = new DataLayerWriter(appModel);
			saveModel.savePNML(outFile);
			// appModel.savePNML(outFile);

			CreateGui.setFile(outFile, appTab.getSelectedIndex());
			appView.setNetChanged(false);
			appTab.setTitleAt(appTab.getSelectedIndex(), outFile.getName());
			setTitle(outFile.getName()); // Change the window title
			appView.getUndoManager().clear();
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
		} catch (Exception e) {
			System.err.println(e);
			JOptionPane.showMessageDialog(GuiFrame.this, e.toString(), "File Output Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
	}

	/**
	 * Creates a new tab with the selected file, or a new file if filename==null
	 *
	 * @param filename
	 *            Filename of net to load, or <b>null</b> to create a new, empty
	 *            tab
	 */
	public void createNewTab(File file, boolean isTN) {
		int freeSpace = CreateGui.getFreeSpace();
		String name = "";

		// if we are in the middle of a paste action, we cancel it because we
		// will
		// create a new tab now
		if (this.getCopyPasteManager().pasteInProgress()) {
			this.getCopyPasteManager().cancelPaste();
		}

		setObjects(freeSpace);

		appModel.addObserver((Observer) appView); // Add the view as Observer
		appModel.addObserver((Observer) appGui); // Add the app window as
													// observer

		if (file == null) {
			name = "Basenet " + (newNameCounter++) + ".xml";
		} else {
			try {
				// BK 10/02/07: Changed loading of PNML to accomodate new
				// PNMLTransformer class
				CreateGui.setFile(file, freeSpace);
				if (isTN) {
					TNTransformer transformer = new TNTransformer();
					appModel.createFromPNML(transformer.transformTN(file.getPath()));
				} else {
					// ProgressBar progressBar = new
					// ProgressBar(file.getName());
					// Thread t = new Thread(progressBar);
					// t.start();
					PNMLTransformer transformer = new PNMLTransformer();
					appModel.createFromPNML(transformer.transformPNML(file.getPath()));
					// progressBar.exit();
					appView.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
				}

				CreateGui.setFile(file, freeSpace);
				name = file.getName();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(GuiFrame.this,
						"Error loading file:\n" + name + "\nGuru meditation:\n" + e.toString(), "File load error",
						JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return;
			}
		}

		appView.setNetChanged(false); // Status is unchanged

		JScrollPane scroller = new JScrollPane(appView);
		// make it less bad on XP
		scroller.setBorder(new BevelBorder(BevelBorder.LOWERED));
		appTab.addTab(name, null, scroller, null);
		appTab.setSelectedIndex(freeSpace);
		CreateGui.addAnimationHistory(name, freeSpace);

		appView.updatePreferredSize();
		// appView.add( new ViewExpansionComponent(appView.getWidth(),
		// appView.getHeight());

		setTitle(name);// Change the program caption
		appTab.setTitleAt(freeSpace, name);
		selectAction.actionPerformed(null);
	}

	/**
	 * Loads an Experiment XML file and shows a suitable message in case of
	 * error.
	 *
	 * @param path
	 *            the absolute path to the experiment file.
	 */
	private void loadExperiment(String path) {
		Experiment exp = new Experiment(path, appModel);
		try {
			exp.Load();
		} catch (org.xml.sax.SAXParseException spe) {
			// if the experiment file does not fit the schema.
			JOptionPane.showMessageDialog(GuiFrame.this,
					"The Experiment file is not valid." + System.getProperty("line.separator") + "Line "
							+ spe.getLineNumber() + ": " + spe.getMessage(),
					"Experiment Input Error", JOptionPane.ERROR_MESSAGE);
		} catch (pipe.experiment.validation.NotMatchingException nme) {
			// if the experiment file does not match with the current net.
			JOptionPane.showMessageDialog(GuiFrame.this,
					"The Experiment file is not valid." + System.getProperty("line.separator") + nme.getMessage(),
					"Experiment Input Error", JOptionPane.ERROR_MESSAGE);
		} catch (pipe.experiment.InvalidExpressionException iee) {
			JOptionPane.showMessageDialog(GuiFrame.this,
					"The Experiment file is not valid." + System.getProperty("line.separator") + iee.getMessage(),
					"Experiment Input Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * If current net has modifications, asks if you want to save and does it if
	 * you want.
	 *
	 * @return true if handled, false if cancelled
	 */
	private boolean checkForSave() {

		if (appView.getNetChanged()) {
			int result = JOptionPane.showConfirmDialog(GuiFrame.this, "Current file has changed. Save current file?",
					"Confirm Save Current File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			switch (result) {
			case JOptionPane.YES_OPTION:
				saveOperation(false);
				break;
			case JOptionPane.CLOSED_OPTION:
			case JOptionPane.CANCEL_OPTION:
				return false;
			}
		}
		return true;
	}

	/**
	 * If current net has modifications, asks if you want to save and does it if
	 * you want.
	 *
	 * @return true if handled, false if cancelled
	 */
	private boolean checkForSaveAll() {
		// Loop through all tabs and check if they have been saved
		for (int counter = 0; counter < appTab.getTabCount(); counter++) {
			appTab.setSelectedIndex(counter);
			if (checkForSave() == false) {
				return false;
			}
		}
		return true;
	}

	public void setRandomAnimationMode(boolean on) {
		if (on == false) {
			// stepforwardAction.setEnabled(CreateGui.getAnimationHistory().isStepForwardAllowed());
			// stepbackwardAction.setEnabled(CreateGui.getAnimationHistory().isStepBackAllowed());
		} else {
			stepbackwardAction.setEnabled(false);
			stepforwardAction.setEnabled(false);
		}
		randomLowLevelAction.setEnabled(!on);
		randomHighLevelAction.setEnabled(!on);
		randomAnimateAction.setSelected(on);
		modelCheckingAction.setSelected(!on);
		modelCheckingActionUsingMaude.setSelected(!on);
	}

	private void setAnimationMode(boolean on) {
		randomAnimateAction.setSelected(false);
		startAction.setSelected(on);
		CreateGui.getView().changeAnimationMode(on);
		if (on) {
			CreateGui.getAnimator().storeModel();
			CreateGui.currentPNMLData().setEnabledTransitions();
			CreateGui.getAnimator().highlightEnabledTransitions();
			// CreateGui.addAnimationHistory();
			enableActions(false);// disables all non-animation buttons
			setEditionAllowed(false);
			statusBar.changeText(statusBar.textforAnimation);
		} else {
			setEditionAllowed(true);
			statusBar.changeText(statusBar.textforDrawing);
			CreateGui.getAnimator().restoreModel();
			// CreateGui.removeAnimationHistory();
			enableActions(true); // renables all non-animation buttons
		}
	}

	public void resetMode() {
		setMode(old_mode);
	}

	public void setFastMode(int _mode) {
		old_mode = mode;
		setMode(_mode);
	}

	public void setMode(int _mode) {
		// Don't bother unless new mode is different.
		if (mode != _mode) {
			prev_mode = mode;
			mode = _mode;
		}
	}

	public int getMode() {
		return mode;
	}

	public void restoreMode() {
		mode = prev_mode;
		placeAction.setSelected(mode == Pipe.PLACE);
		transAction.setSelected(mode == Pipe.IMMTRANS);
		timedtransAction.setSelected(mode == Pipe.TIMEDTRANS);
		arcAction.setSelected(mode == Pipe.ARC);
		bidirectionalAction.setSelected(mode == Pipe.BIDIRECTARC);
		inhibarcAction.setSelected(mode == Pipe.INHIBARC);
		tokenAction.setSelected(mode == Pipe.ADDTOKEN);
		deleteTokenAction.setSelected(mode == Pipe.DELTOKEN);
		rateAction.setSelected(mode == Pipe.RATE);
		markingAction.setSelected(mode == Pipe.MARKING);
		selectAction.setSelected(mode == Pipe.SELECT);
		annotationAction.setSelected(mode == Pipe.ANNOTATION);
	}

	public void setTitle(String title) {
		super.setTitle((title == null) ? frameTitle : frameTitle + ": " + title);
	}

	public boolean isEditionAllowed() {
		return editionAllowed;
	}

	public void setEditionAllowed(boolean flag) {
		editionAllowed = flag;
	}

	public void setUndoActionEnabled(boolean flag) {
		undoAction.setEnabled(flag);
	}

	public void setRedoActionEnabled(boolean flag) {
		redoAction.setEnabled(flag);
	}

	public CopyPasteManager getCopyPasteManager() {
		return copyPasteManager;
	}

	public void init() {
		// Set selection mode at startup
		setMode(Pipe.SELECT);
		selectAction.actionPerformed(null);
	}

	/**
	 * @author Ben Kirby Remove the listener from the zoomComboBox, so that when
	 *         the box's selected item is updated to keep track of ZoomActions
	 *         called from other sources, a duplicate ZoomAction is not called
	 */
	public void updateZoomCombo() {
		ActionListener zoomComboListener = (zoomComboBox.getActionListeners())[0];
		zoomComboBox.removeActionListener(zoomComboListener);
		zoomComboBox.setSelectedItem(String.valueOf(appView.getZoomController().getPercent()) + "%");
		zoomComboBox.addActionListener(zoomComboListener);
	}

	public StatusBar getStatusBar() {
		return statusBar;
	}

	private Component c = null; // arreglantzoom
	private Component p = new BlankLayer(this);

	/* */
	void hideNet(boolean doHide) {
		if (doHide) {
			c = appTab.getComponentAt(appTab.getSelectedIndex());
			appTab.setComponentAt(appTab.getSelectedIndex(), p);
		} else {
			if (c != null) {
				appTab.setComponentAt(appTab.getSelectedIndex(), c);
				c = null;
			}
		}
		appTab.repaint();
	}

	class AnimateAction extends GuiAction {

		private int typeID;
		private AnimationHistory animBox;

		AnimateAction(String name, int typeID, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
			this.typeID = typeID;
		}

		AnimateAction(String name, int typeID, String tooltip, String keystroke, boolean toggleable) {
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}

		public void actionPerformed(ActionEvent ae) {
			if (appView == null) {
				return;
			}

			animBox = CreateGui.getAnimationHistory();

			switch (typeID) {
			case Pipe.START:
				ModelCompletenessCheck ck = new ModelCompletenessCheck(CreateGui.getModel());
				if (!ck.getResult() && !appView.isInAnimationMode()) {
					JOptionPane.showMessageDialog(GuiFrame.this, ck.getMessage(), "Model Completeness Check Error",
							JOptionPane.ERROR_MESSAGE);
				}
				try {
					setAnimationMode(!appView.isInAnimationMode());
					if (!appView.isInAnimationMode()) {
						restoreMode();
						PetriNetObject.ignoreSelection(false);
					} else {
						setMode(typeID);
						PetriNetObject.ignoreSelection(true);
						// Do we keep the selection??
						appView.getSelectionObject().clearSelection();
					}
				} catch (Exception e) {
					System.err.println(e);
					JOptionPane.showMessageDialog(GuiFrame.this, e.toString(), "Animation Mode Error",
							JOptionPane.ERROR_MESSAGE);
					startAction.setSelected(false);
					appView.changeAnimationMode(false);
				}
				stepforwardAction.setEnabled(false);
				stepbackwardAction.setEnabled(false);
				break;

			case Pipe.RANDOMLOWLEVEL:
				// animBox.clearStepsForward();
				CreateGui.getAnimator().doLowLevelRandomFiring();
				// stepforwardAction.setEnabled(animBox.isStepForwardAllowed());
				// stepbackwardAction.setEnabled(animBox.isStepBackAllowed());
				break;

			case Pipe.MODELCHECKING:
				// animBox.clearStepsForward();
				showModelCheckingOptionsWithSpin();
				break;

			case Pipe.MODELCHECKING_MAUDE:
				// animBox.clearStepsForward();
				new AnalysisModuleHandler(new Maude()).modelToPromelaWindow();
				break;

			case Pipe.RANDOMHIGHLEVEL:
				// animBox.clearStepsForward();
				CreateGui.getAnimator().startAnimation(new FixedStepsAnimationStrategy(1),
						CreateGui.getAnimationHistory(appTab.getSelectedIndex()));
				// stepforwardAction.setEnabled(animBox.isStepForwardAllowed());
				// stepbackwardAction.setEnabled(animBox.isStepBackAllowed());
				break;

			case Pipe.STEPFORWARD:
				// animBox.stepForward();
				CreateGui.getAnimator().stepForward();
				// stepforwardAction.setEnabled(animBox.isStepForwardAllowed());
				// stepbackwardAction.setEnabled(animBox.isStepBackAllowed());
				break;

			case Pipe.STEPBACKWARD:
				// animBox.stepBackwards();
				CreateGui.getAnimator().stepBack();
				// stepforwardAction.setEnabled(animBox.isStepForwardAllowed());
				// stepbackwardAction.setEnabled(animBox.isStepBackAllowed());
				break;

			case Pipe.ANIMATE:
				Animator a = CreateGui.getAnimator();
				if (a.isAnimating()) {
					a.stopAnimation();
					setSelected(false);
					return;
				}

				stepbackwardAction.setEnabled(false);
				stepforwardAction.setEnabled(false);
				randomLowLevelAction.setEnabled(false);
				randomHighLevelAction.setEnabled(false);
				modelCheckingAction.setEnabled(false);
				modelCheckingActionUsingMaude.setEnabled(false);
				setSelected(true);
				// animBox.clearStepsForward();

				JDialog configDialogue = new JDialog(CreateGui.getApp(), "Animation Configuration", true);
				PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
				propertyChangeSupport.addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent evt) {
						Object newValue = evt.getNewValue();
						configDialogue.setVisible(false);
						if (newValue != null && newValue instanceof AnimationStrategy) {
							a.startAnimation((AnimationStrategy) newValue,
									CreateGui.getAnimationHistory(appTab.getSelectedIndex()));
						} else {
							a.stopAnimation();
						}
					}
				});
				configDialogue.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

				configDialogue.setLayout(new BoxLayout(configDialogue.getContentPane(), BoxLayout.LINE_AXIS));
				configDialogue.getContentPane().add(new AnimationConfigUI(propertyChangeSupport).getRootComponent());

				configDialogue.pack();
				configDialogue.setLocationRelativeTo(null);
				configDialogue.setVisible(true);

				break;

			case Pipe.CONVERTZ3:
				BoundedModelCheckingModule bmc = new BoundedModelCheckingModule();
				bmc.boundedModelCheckingWindow();
				break;

			default:
				break;
			}
		}

		private void showModelCheckingOptionsWithSpin() {
			final JDialog configDialogue = new JDialog(CreateGui.getApp(), "Model Checking with SPIN", true);
			configDialogue.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			configDialogue.setLayout(new BoxLayout(configDialogue.getContentPane(), BoxLayout.LINE_AXIS));

			JLabel label = new JLabel("Select a strategy to translate");
			final JComboBox<String> optionBox = new JComboBox<>(
					new String[] { "_Select_", "Default", "Transition as Proctype" });
			optionBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					Promela promela = new Promela();
					switch (optionBox.getSelectedIndex()) {
					case 2:
						promela.setTranslator(new PNPromelaTransitionAsProc());
						break;
					case 1:
					default:
						promela.setTranslator(new PNPromela());
					}
					AnalysisModuleHandler mcHandler = new AnalysisModuleHandler(promela);
					mcHandler.modelToPromelaWindow();
					configDialogue.setVisible(false);
				}
			});

			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			panel.add(label, BorderLayout.NORTH);
			panel.add(optionBox, BorderLayout.SOUTH);
			configDialogue.getContentPane().add(panel);
			configDialogue.pack();
			configDialogue.setLocationRelativeTo(null);
			configDialogue.setVisible(true);

		}

	}

	class ExampleFileAction extends GuiAction {

		private File filename;

		ExampleFileAction(File file, String keyStroke) {
			super(file.getName(), "Open example file \"" + file.getName() + "\"", keyStroke);
			filename = file;// .getAbsolutePath();
			putValue(SMALL_ICON, new ImageIcon(
					Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "Net.png")));
			// putValue(SMALL_ICON,
			// new ImageIcon(GuiFrame.class.getResource("/Images/Net.png")));

		}

		ExampleFileAction(JarEntry entry, String keyStroke) {
			super(entry.getName().substring(1 + entry.getName().indexOf(System.getProperty("file.separator"))),
					"Open example file \"" + entry.getName() + "\"", keyStroke);
			putValue(SMALL_ICON, new ImageIcon(
					Thread.currentThread().getContextClassLoader().getResource(CreateGui.imgPath + "Net.png")));
			// putValue(SMALL_ICON,
			// new ImageIcon(GuiFrame.class.
			// getResource("/Images/Net.png")));

			filename = JarUtilities.getFile(entry);// .getPath();
		}

		public void actionPerformed(ActionEvent e) {
			createNewTab(filename, false);
		}

	}

	class DeleteAction extends GuiAction {

		DeleteAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			appView.getUndoManager().newEdit(); // new "transaction""
			appView.getUndoManager().deleteSelection(appView.getSelectionObject().getSelection());
			appView.getSelectionObject().deleteSelection();
		}

	}

	class TypeAction extends GuiAction {

		private int typeID;

		TypeAction(String name, int typeID, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
			this.typeID = typeID;
		}

		TypeAction(String name, int typeID, String tooltip, String keystroke, boolean toggleable) {
			super(name, tooltip, keystroke, toggleable);
			this.typeID = typeID;
		}

		public void actionPerformed(ActionEvent e) {
			// if (!isSelected()){
			this.setSelected(true);

			// deselect other actions
			if (this != placeAction) {
				placeAction.setSelected(false);
			}
			if (this != transAction) {
				transAction.setSelected(false);
			}
			if (this != timedtransAction) {
				timedtransAction.setSelected(false);
			}
			if (this != arcAction) {
				arcAction.setSelected(false);
			}
			if (this != bidirectionalAction) {
				bidirectionalAction.setSelected(false);
			}
			if (this != inhibarcAction) {
				inhibarcAction.setSelected(false);
			}
			if (this != tokenAction) {
				tokenAction.setSelected(false);
			}
			if (this != deleteTokenAction) {
				deleteTokenAction.setSelected(false);
			}
			if (this != rateAction) {
				rateAction.setSelected(false);
			}
			if (this != markingAction) {
				markingAction.setSelected(false);
			}
			if (this != selectAction) {
				selectAction.setSelected(false);
			}
			if (this != annotationAction) {
				annotationAction.setSelected(false);
			}
			if (this != dragAction) {
				dragAction.setSelected(false);
			}

			if (appView == null) {
				return;
			}

			appView.getSelectionObject().disableSelection();
			// appView.getSelectionObject().clearSelection();

			setMode(typeID);
			statusBar.changeText(typeID);

			if ((typeID != Pipe.ARC || typeID != Pipe.BIDIRECTARC) && (appView.createArc != null)) {
				appView.createArc.delete();
				appView.createArc = null;
				appView.repaint();
			}

			if (typeID == Pipe.SELECT) {
				// disable drawing to eliminate possiblity of connecting arc to
				// old coord of moved component
				statusBar.changeText(typeID);
				appView.getSelectionObject().enableSelection();
				appView.setCursorType("arrow");
			} else if (typeID == Pipe.DRAG) {
				appView.setCursorType("move");
			} else {
				appView.setCursorType("crosshair");
			}
		}
		// }

	}

	class GridAction extends GuiAction {

		GridAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			Grid.increment();
			repaint();
		}

	}

	class ZoomAction extends GuiAction {

		ZoomAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			boolean doZoom = false;
			try {
				String actionName = (String) getValue(NAME);
				Zoomer zoomer = appView.getZoomController();
				JViewport thisView = ((JScrollPane) appTab.getSelectedComponent()).getViewport();
				String selection = null, strToTest = null;

				double midpointX = Zoomer.getUnzoomedValue(thisView.getViewPosition().x + (thisView.getWidth() * 0.5),
						zoomer.getPercent());
				double midpointY = Zoomer.getUnzoomedValue(thisView.getViewPosition().y + (thisView.getHeight() * 0.5),
						zoomer.getPercent());

				if (actionName.equals("Zoom in")) {
					doZoom = zoomer.zoomIn();
				} else if (actionName.equals("Zoom out")) {
					doZoom = zoomer.zoomOut();
				} else {
					if (actionName.equals("Zoom")) {
						selection = (String) zoomComboBox.getSelectedItem();
					}
					if (e.getSource() instanceof JMenuItem) {
						selection = ((JMenuItem) e.getSource()).getText();
					}
					strToTest = validatePercent(selection);

					if (strToTest != null) {
						// BK: no need to zoom if already at that level
						if (zoomer.getPercent() == Integer.parseInt(strToTest)) {
							return;
						} else {
							zoomer.setZoom(Integer.parseInt(strToTest));
							doZoom = true;
						}
					} else {
						return;
					}
				}
				if (doZoom == true) {
					updateZoomCombo();
					appView.zoomTo(new java.awt.Point((int) midpointX, (int) midpointY));
				}
			} catch (ClassCastException cce) {
				// zoom
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private String validatePercent(String selection) {

			try {
				String toTest = selection;

				if (selection.endsWith("%")) {
					toTest = selection.substring(0, (selection.length()) - 1);
				}

				if (Integer.parseInt(toTest) < Pipe.ZOOM_MIN || Integer.parseInt(toTest) > Pipe.ZOOM_MAX) {
					throw new Exception();
				} else {
					return toTest;
				}
			} catch (Exception e) {
				zoomComboBox.setSelectedItem("");
				return null;
			}
		}

	}

	class ExperimentAction extends GuiAction {

		ExperimentAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			if (this == loadExperimentAction) {
				File filePath = new FileBrowser(CreateGui.userPath).openFile();
				if ((filePath != null) && filePath.exists() && filePath.isFile() && filePath.canRead()) {
					loadExperiment(filePath.getAbsolutePath());
				}
			}
			if (this == experimentEditorAction) {
				ExperimentEditor ee = new ExperimentEditor(appModel);
				// ee.start();
			}
		}

	}

	class FileAction extends GuiAction {

		// constructor
		FileAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {
			if (this == saveAction) {
				saveOperation(false); // code for Save operation
			} else if (this == saveAsAction) {
				saveOperation(true); // code for Save As operations
			} else if (this == openAction) { // code for Open operation
				File filePath = new FileBrowser(CreateGui.userPath).openFile();
				if ((filePath != null) && filePath.exists() && filePath.isFile() && filePath.canRead()) {
					CreateGui.userPath = filePath.getParent();
					createNewTab(filePath, false);
				}
			} else if (this == importAction) {
				File filePath = new FileBrowser(CreateGui.userPath).openFile();
				if ((filePath != null) && filePath.exists() && filePath.isFile() && filePath.canRead()) {
					CreateGui.userPath = filePath.getParent();
					createNewTab(filePath, true);
					appView.getSelectionObject().enableSelection();
				}
			} else if (this == createAction) {
				createNewTab(null, false); // Create a new tab
			} else if ((this == exitAction) && checkForSaveAll()) {
				dispose();
				System.exit(0);
			} else if ((this == closeAction) && (appTab.getTabCount() > 0) && checkForSave()) {
				setObjectsNull(appTab.getSelectedIndex());
				appTab.remove(appTab.getSelectedIndex());
			} else if (this == exportPNGAction) {
				Export.exportGuiView(appView, Export.PNG, null);
			} else if (this == exportPSAction) {
				Export.exportGuiView(appView, Export.POSTSCRIPT, null);
			} else if (this == exportTNAction) {
				System.out.println("Exportant a TN");
				Export.exportGuiView(appView, Export.TN, appModel);
			} else if (this == printAction) {
				Export.exportGuiView(appView, Export.PRINTER, null);
			}
		}

	}

	class EditAction extends GuiAction {

		EditAction(String name, String tooltip, String keystroke) {
			super(name, tooltip, keystroke);
		}

		public void actionPerformed(ActionEvent e) {

			if (CreateGui.getApp().isEditionAllowed()) {
				if (this == cutAction) {
					ArrayList selection = appView.getSelectionObject().getSelection();
					appGui.getCopyPasteManager().setUpPaste(selection, appView);
					appView.getUndoManager().newEdit(); // new "transaction""
					appView.getUndoManager().deleteSelection(selection);
					appView.getSelectionObject().deleteSelection();
					pasteAction.setEnabled(appGui.getCopyPasteManager().pasteEnabled());
				} else if (this == copyAction) {
					appGui.getCopyPasteManager().setUpPaste(appView.getSelectionObject().getSelection(), appView);
					pasteAction.setEnabled(appGui.getCopyPasteManager().pasteEnabled());
				} else if (this == pasteAction) {
					appView.getSelectionObject().clearSelection();
					appGui.getCopyPasteManager().startPaste(appView);
				} else if (this == undoAction) {
					appView.getUndoManager().undo();
				} else if (this == redoAction) {
					appView.getUndoManager().redo();
				}
			}
		}
	}

	/**
	 * A JToggleButton that watches an Action for selection change
	 *
	 * @author Maxim
	 *         <p>
	 *         Selection must be stored in the action using
	 *         putValue("selected",Boolean);
	 */
	class ToggleButton extends JToggleButton implements PropertyChangeListener {

		public ToggleButton(Action a) {
			super(a);
			if (a.getValue(Action.SMALL_ICON) != null) {
				// toggle buttons like to have images *and* text, nasty
				setText(null);
			}
			a.addPropertyChangeListener(this);
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName() == "selected") {
				Boolean b = (Boolean) evt.getNewValue();
				if (b != null) {
					setSelected(b.booleanValue());
				}
			}
		}

	}

	class WindowHandler extends WindowAdapter {
		// Handler for window closing event
		public void windowClosing(WindowEvent e) {
			exitAction.actionPerformed(null);
		}
	}

}
