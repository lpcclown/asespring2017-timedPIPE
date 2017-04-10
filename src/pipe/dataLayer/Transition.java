package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JOptionPane;

import formula.absyntree.Sentence;
import formula.parser.ErrorMsg;
import formula.parser.Interpreter;
import formula.parser.Parse;
import formula.parser.Symbol;
import formula.parser.SymbolTable;
import pipe.gui.CreateGui;
import pipe.gui.Grid;
import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.undo.ChangeRateParameterEdit;
import pipe.gui.undo.ClearRateParameterEdit;
import pipe.gui.undo.SetRateParameterEdit;
import pipe.gui.undo.TransitionFormulaEdit;
import pipe.gui.undo.TransitionLowerBoundEdit;
import pipe.gui.undo.TransitionPriorityEdit;
import pipe.gui.undo.TransitionRateEdit;
import pipe.gui.undo.TransitionRotationEdit;
import pipe.gui.undo.TransitionServerSemanticEdit;
import pipe.gui.undo.TransitionTimingEdit;
import pipe.gui.undo.TransitionUpperBoundEdit;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.widgets.EscapableDialog;
import pipe.gui.widgets.TransitionEditor;

/**
 * <b>Transition</b> - Petri-Net Transition Class
 *
 * @author James D Bloom *
 * @author Dave Patterson Add fields and methods to handle delay time for
 *         exponentially distributed timed transitions.
 *         <p>
 *         Note: The exponential distribution is based on a single parameter,
 *         which can either be specified as a delay or a rate. The two values
 *         are inverses of each other, so no matter how the parameter is
 *         specified, the code after the constructor is the same. When a timed
 *         transition is found to be enabled, a projected delay is calculated
 *         based on its exponential distribution. As long as the transition does
 *         not get disabled before it can be fired, that delay governs the
 *         timing of the transition's firing. If other timed transitions are
 *         fired, the delay before they fire (that is, the progress of the
 *         virtual clock) is decremented from this transition's delay time to
 *         simulate the progress of time in the virtual space. Thus, at any
 *         time, the next timed transition to fire is the one with the lowest
 *         delay remaining. If a timed transition gets an expected delay and
 *         then later becomes disabled, its delay is no longer valid. When it
 *         next becomes enabled, it will get a new expected delay (an offset
 *         from the current virtual time). This explaination was included to
 *         clarify any confusion about fields and methods with "delay" in their
 *         names. They have nothing to do with whether the parameter of the
 *         exponential distribution is specified as a rate, or an expected
 *         delay. to the
 * @version 1.0
 * @see
 *      <p>
 *      <a href="..\PNMLSchema\index.html">PNML - Petri-Net XMLSchema
 *      (stNet.xsd)</a>
 * @see
 *      </p>
 *      <p>
 *      <a href="..\..\..\UML\dataLayer.html">UML - PNML Package </a>
 *      </p>
 */
public class Transition extends PlaceTransitionObject {

	/**
	 * Transition is of Rectangle2D.Double
	 */
	private GeneralPath transition;
	private Shape proximityTransition;
	/**
	 * Place Width
	 */
	public static final int TRANSITION_HEIGHT = Pipe.PLACE_TRANSITION_HEIGHT;
	/**
	 * Place Width
	 */
	public static final int TRANSITION_WIDTH = TRANSITION_HEIGHT / 3;

	private int angle;
	private boolean enabled = false;
	private boolean enabledBackwards = false;
	public boolean highlighted = false;

	private boolean infiniteServer = false;

	/**
	 * The delay before this transition fires.
	 */
	private double delay;

	/**
	 * A boolean to track whether the delay is valid or not.
	 */
	private boolean delayValid;

	/**
	 * Is this a timed transition or not?
	 */
	private boolean timed = false;

	private static final double rootThreeOverTwo = 0.5 * Math.sqrt(3);

	private ArrayList arcAngleList = new ArrayList();

	/**
	 * The transition rate
	 */
	private double rate = 1;

	/**
	 * The transition lowerBound
	 */
	private int lowerBound = 1;

	/**
	 * The transition upperBound
	 */
	private int upperBound = 1;

	/**
	 * Rate X-axis Offset
	 */
	private Double rateOffsetX = 0.0;

	/**
	 * Rate Y-axis Offset
	 */
	private Double rateOffsetY = 24.0;

	/**
	 * The transition priority
	 */
	private Integer priority = 1;

	/**
	 * Priority X-axis Offset
	 */
	private Double priorityOffsetX = 0.0;

	/**
	 * Priority Y-axis Offset
	 */
	private Double priorityOffsetY = 13.0;

	/**
	 * The transtion rate parameter
	 */
	private RateParameter rateParameter = null;

	/**
	 * add formula attributes edit by Su Liu
	 */
	protected String formula;
	protected String XMLformula;
	protected ArrayList<String> tranVarList = new ArrayList<String>();
	protected ArrayList<String> arcInVarList = new ArrayList<String>();
	protected ArrayList<String> arcOutVarList = new ArrayList<String>();
	protected ArrayList<Arc> arcList = new ArrayList<Arc>();
	/**
	 * store the arcs go into the transition
	 */
	protected ArrayList<Arc> arcInList = new ArrayList<Arc>();
	/**
	 * store the arcs go out of the transition
	 */
	protected ArrayList<Arc> arcOutList = new ArrayList<Arc>();
	/**
	 * stores places have arc go into transition;
	 */
	protected ArrayList<Place> placeInList = new ArrayList<Place>();
	/**
	 * stores places have arc go out of transition;
	 */
	protected ArrayList<Place> placeOutList = new ArrayList<Place>();

	protected SymbolTable symTable = new SymbolTable();

	/**
	 * Create Petri-Net Transition object
	 *
	 * @param positionXInput
	 *            X-axis Position
	 * @param positionYInput
	 *            Y-axis Position
	 * @param idInput
	 *            Transition id
	 * @param nameInput
	 *            Name
	 * @param nameOffsetXInput
	 *            Name X-axis Position
	 * @param nameOffsetYInput
	 *            Name Y-axis Position
	 * @param infServer
	 *            TODO
	 */
	public Transition(double positionXInput, double positionYInput, String idInput, String nameInput,
			double nameOffsetXInput, double nameOffsetYInput, double rateInput, boolean timedTransition,
			boolean infServer, int angleInput, int priority, String formula) {
		super(positionXInput, positionYInput, idInput, nameInput, nameOffsetXInput, nameOffsetYInput);
		componentWidth = TRANSITION_HEIGHT; // sets width
		componentHeight = TRANSITION_HEIGHT;// sets height
		rate = rateInput;
		timed = timedTransition;
		infiniteServer = infServer;
		constructTransition();
		angle = 0;
		setCentre((int) positionX, (int) positionY);
		rotate(angleInput);
		updateBounds();
		// this.updateEndPoints();
		this.priority = priority;
		this.formula = formula;
	}

	/**
	 * Create Petri-Net Transition object
	 *
	 * @param positionXInput
	 *            X-axis Position
	 * @param positionYInput
	 *            Y-axis Position
	 */
	public Transition(double positionXInput, double positionYInput) {
		super(positionXInput, positionYInput);
		componentWidth = TRANSITION_HEIGHT; // sets width
		componentHeight = TRANSITION_HEIGHT;// sets height
		constructTransition();
		setCentre((int) positionX, (int) positionY);
		updateBounds();
		this.updateEndPoints();
	}

	public Transition paste(double x, double y, boolean fromAnotherView) {
		Transition copy = new Transition(Grid.getModifiedX(x + this.getX() + Pipe.PLACE_TRANSITION_HEIGHT / 2),
				Grid.getModifiedY(y + this.getY() + Pipe.PLACE_TRANSITION_HEIGHT / 2));
		copy.pnName.setName(this.pnName.getName() + "(" + this.getCopyNumber() + ")");
		this.newCopy(copy);
		copy.nameOffsetX = this.nameOffsetX;
		copy.nameOffsetY = this.nameOffsetY;

		copy.timed = this.timed;
		copy.rate = this.rate;
		copy.angle = this.angle;

		copy.attributesVisible = this.attributesVisible;
		copy.priority = this.priority;
		copy.transition.transform(AffineTransform.getRotateInstance(Math.toRadians(copy.angle),
				Transition.TRANSITION_HEIGHT / 2, Transition.TRANSITION_HEIGHT / 2));
		copy.rateParameter = null;// this.rateParameter;
		return copy;
	}

	public Transition copy() {
		Transition copy = new Transition(Zoomer.getUnzoomedValue(this.getX(), zoom),
				Zoomer.getUnzoomedValue(this.getY(), zoom));
		copy.pnName.setName(this.getName());
		copy.nameOffsetX = this.nameOffsetX;
		copy.nameOffsetY = this.nameOffsetY;
		copy.timed = this.timed;
		copy.rate = this.rate;
		copy.angle = this.angle;
		copy.attributesVisible = this.attributesVisible;
		copy.priority = this.priority;
		copy.setOriginal(this);
		copy.rateParameter = this.rateParameter;
		return copy;
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (selected && !ignoreSelection) {
			g2.setColor(Pipe.SELECTION_FILL_COLOUR);
		} else {
			g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
		}

		if (timed) {
			if (infiniteServer) {
				for (int i = 2; i >= 1; i--) {
					g2.translate(2 * i, -2 * i);
					g2.fill(transition);
					Paint pen = g2.getPaint();
					if (highlighted) {
						g2.setPaint(Pipe.ENABLED_TRANSITION_COLOUR);
					} else if (selected && !ignoreSelection) {
						g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
					} else {
						g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
					}
					g2.draw(transition);
					g2.setPaint(pen);
					g2.translate(-2 * i, 2 * i);
				}
			}
			g2.fill(transition);
		}

		if (highlighted) {
			g2.setPaint(Pipe.ENABLED_TRANSITION_COLOUR);
		} else if (selected && !ignoreSelection) {
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else {
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}

		g2.draw(transition);
		if (!timed) {
			if (infiniteServer) {
				for (int i = 2; i >= 1; i--) {
					g2.translate(2 * i, -2 * i);
					Paint pen = g2.getPaint();
					g2.setPaint(Pipe.ELEMENT_FILL_COLOUR);
					g2.fill(transition);
					g2.setPaint(pen);
					g2.draw(transition);
					g2.translate(-2 * i, 2 * i);
				}
			}
			g2.draw(transition);
			g2.fill(transition);
		}
	}

	/**
	 * Rotates the Transition through the specified angle around the midpoint
	 */
	public UndoableEdit rotate(int angleInc) {
		angle = (angle + angleInc) % 360;
		transition.transform(
				AffineTransform.getRotateInstance(Math.toRadians(angleInc), componentWidth / 2, componentHeight / 2));
		outlineTransition();

		Iterator arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			((ArcAngleCompare) arcIterator.next()).calcAngle();
		}
		Collections.sort(arcAngleList);

		updateEndPoints();
		repaint();

		return new TransitionRotationEdit(this, angle);
	}

	private void outlineTransition() {
		proximityTransition = (new BasicStroke(Pipe.PLACE_TRANSITION_PROXIMITY_RADIUS)).createStrokedShape(transition);
	}

	/**
	 * Determines whether Transition is enabled
	 *
	 * @param animationStatus
	 *            Anamation status
	 * @return True if enabled
	 */
	public boolean isEnabled(boolean animationStatus) {
		if (animationStatus == true) {
			if (enabled == true) {
				highlighted = true;
				return true;
			} else {
				highlighted = false;
			}
		}
		return false;
	}

	/**
	 * Determines whether Transition is enabled backwards
	 *
	 * @return True if enabled
	 */
	public boolean isEnabledBackwards() {
		return enabledBackwards;
	}

	/**
	 * Determines whether Transition is enabled
	 *
	 * @return True if enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public void setHighlighted(boolean status) {
		highlighted = status;
	}

	public UndoableEdit setInfiniteServer(boolean status) {
		infiniteServer = status;
		repaint();
		return new TransitionServerSemanticEdit(this);
	}

	public boolean isInfiniteServer() {
		return infiniteServer;
	}

	/**
	 * Sets whether Transition is enabled
	 *
	 * @return enabled if True
	 */
	public void setEnabled(boolean status) {
		if (enabled && !status) { // going from enabled to disabled
			delayValid = false; // mark that delay is not valid
		}
		enabled = status;
	}

	/**
	 * Sets whether Transition is enabled
	 *
	 * @return enabled if True
	 */
	public void setEnabledBackwards(boolean status) {
		enabledBackwards = status;
	}

	/* Called at the end of animation to reset Transitions to false */
	public void setEnabledFalse() {
		enabled = false;
		highlighted = false;
	}

	public UndoableEdit setRate(double _rate) {
		double oldRate = rate;

		rate = _rate;
		pnName.setText(getText());
		repaint();
		return new TransitionRateEdit(this, oldRate, rate);
	}

	public UndoableEdit setLowerBound(int _lowerBound) {
		int oldLowerBound = lowerBound;

		lowerBound = _lowerBound;
		pnName.setText(getText());
		repaint();
		return new TransitionLowerBoundEdit(this, oldLowerBound, lowerBound);
	}

	public UndoableEdit setUpperBound(int _upperBound) {
		int oldUpperBound = upperBound;

		upperBound = _upperBound;
		pnName.setText(getText());
		repaint();
		return new TransitionUpperBoundEdit(this, oldUpperBound, upperBound);
	}

	public double getRate() {
		return rate;
	}

	public int getAngle() {
		return angle;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * Set priority
	 *
	 * @param newPriority
	 *            Integer value for ...
	 */
	public UndoableEdit setPriority(int newPriority) {
		int oldPriority = priority;

		priority = new Integer(newPriority);
		pnName.setText(getText());
		repaint();
		return new TransitionPriorityEdit(this, oldPriority, priority);
	}

	public UndoableEdit setFormula(String formulaInput) {
		String oldFormula = formula;
		if (oldFormula == null) {
			oldFormula = "";
		}
		formula = formulaInput;
		// formulaLabel.setText(oldFormula);
		// formulaLabel.updateSize();
		repaint();
		return new TransitionFormulaEdit(this, oldFormula, formula);
	}

	// public void setXMLFormula(){
	// StringToXML strxml = new StringToXML(getFormula());
	// this.XMLformula = strxml.getXML();
	// }

	public String getXMLFormula() {
		return this.XMLformula;
	}

	public String getFormula() {
		return formula;
	}

	/**
	 * Set the timed transition attribute (for GSPNs)
	 */
	public UndoableEdit setTimed(boolean change) {
		timed = change;
		pnName.setText(getText());
		repaint();
		return new TransitionTimingEdit(this);
	}

	/**
	 * Get the timed transition attribute (for GSPNs)
	 */
	public boolean isTimed() {
		return timed;
	}

	/**
	 * This is a setter for the delay for this transition.
	 *
	 * @param _delay
	 *            the time until this transition will fire
	 * @author Dave Patterson as part of the Exponential Distribution support
	 *         for timed transitions.
	 */
	public void setDelay(double _delay) {
		delay = _delay;
		delayValid = true;
	}

	/**
	 * This is a getter for the delay for this transition.
	 *
	 * @return a double with the amount of delay
	 * @author Dave Patterson as part of the Exponential Distribution support
	 *         for timed transitions.
	 */
	public double getDelay() {
		return delay;
	}

	/**
	 * This method is a getter for the boolean indicating if the delay is valid
	 * or not.
	 *
	 * @return the delayValid a boolean that is true if the delay is valid, and
	 *         false otherwise
	 * @author Dave Patterson as part of the Exponential Distribution support
	 *         for timed transitions.
	 */
	public boolean isDelayValid() {
		return delayValid;
	}

	/**
	 * This method is used to set a flag to indicate that the delay is valid or
	 * invalid. (Mainly it is used to invalidate the delay.)
	 *
	 * @param _delayValid
	 *            a boolean that is true if the delay is valid, false otherwise
	 * @author Dave Patterson as part of the Exponential Distribution support
	 *         for timed transitions.
	 */
	public void setDelayValid(boolean _delayValid) {
		delayValid = _delayValid;
	}

	private void constructTransition() {
		transition = new GeneralPath();
		transition.append(
				new Rectangle2D.Double((componentWidth - TRANSITION_WIDTH) / 2, 0, TRANSITION_WIDTH, TRANSITION_HEIGHT),
				false);

		outlineTransition();
	}

	public boolean contains(int x, int y) {
		int zoomPercentage = zoom;

		double unZoomedX = (x - COMPONENT_DRAW_OFFSET) / (zoomPercentage / 100.0);
		double unZoomedY = (y - COMPONENT_DRAW_OFFSET) / (zoomPercentage / 100.0);

		someArc = CreateGui.getView().createArc;
		if (someArc != null) { // Must be drawing a new Arc if non-NULL.
			if ((proximityTransition.contains((int) unZoomedX, (int) unZoomedY)
					|| transition.contains((int) unZoomedX, (int) unZoomedY)) && areNotSameType(someArc.getSource())) {
				// assume we are only snapping the target...
				if (someArc.getTarget() != this) {
					someArc.setTarget(this);
				}
				someArc.updateArcPosition();
				return true;
			} else {
				if (someArc.getTarget() == this) {
					someArc.setTarget(null);
					removeArcCompareObject(someArc);
					updateConnected();
				}
				return false;
			}
		} else {
			return transition.contains((int) unZoomedX, (int) unZoomedY);
		}
	}

	@Override
	public void removeFromArc(final Arc oldArc) {
		super.removeFromArc(oldArc);
		removeArcCompareObject(oldArc);
		updateConnected();
	}

	public void removeArcCompareObject(Arc a) {
		Iterator arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			if (((ArcAngleCompare) arcIterator.next()).arc == a) {
				arcIterator.remove();
			}
		}
	}

	@Override
	public void removeToArc(final Arc oldArc) {
		super.removeToArc(oldArc);
		removeArcCompareObject(oldArc);
		updateConnected();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * pipe.dataLayer.PlaceTransitionObject#updateEndPoint(pipe.dataLayer.Arc)
	 */
	public void updateEndPoint(Arc arc) {
		boolean match = false;

		Iterator arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = (ArcAngleCompare) arcIterator.next();
			if (thisArc.arc == arc || !arc.inView()) {
				thisArc.calcAngle();
				match = true;
				break;
			}
		}

		if (!match) {
			arcAngleList.add(new ArcAngleCompare(arc, this));
		}

		Collections.sort(arcAngleList);
		updateEndPoints();
	}

	public void updateEndPoints() {
		ArrayList top = new ArrayList();
		ArrayList bottom = new ArrayList();
		ArrayList left = new ArrayList();
		ArrayList right = new ArrayList();

		Iterator arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = (ArcAngleCompare) arcIterator.next();
			double thisAngle = thisArc.angle - Math.toRadians(angle);
			if (Math.cos(thisAngle) > (rootThreeOverTwo)) {
				top.add(thisArc);
				thisArc.arc.setPathToTransitionAngle(angle + 90);
			} else if (Math.cos(thisAngle) < -rootThreeOverTwo) {
				bottom.add(thisArc);
				thisArc.arc.setPathToTransitionAngle(angle + 270);
			} else if (Math.sin(thisAngle) > 0) {
				left.add(thisArc);
				thisArc.arc.setPathToTransitionAngle(angle + 180);
			} else {
				right.add(thisArc);
				thisArc.arc.setPathToTransitionAngle(angle);
			}
		}

		AffineTransform transform = AffineTransform.getRotateInstance(Math.toRadians(angle + Math.PI));
		Point2D.Double transformed = new Point2D.Double();
		transform.concatenate(Zoomer.getTransform(zoom));

		arcIterator = top.iterator();
		transform.transform(new Point2D.Double(1, 0.5 * TRANSITION_HEIGHT), transformed); // +1
																							// due
																							// to
																							// rounding
																							// making
																							// it
																							// off
																							// by
																							// 1
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = (ArcAngleCompare) arcIterator.next();
			if (thisArc.sourceOrTarget()) {
				thisArc.arc.setTargetLocation(positionX + centreOffsetLeft() + transformed.x,
						positionY + centreOffsetTop() + transformed.y);
			} else {
				thisArc.arc.setSourceLocation(positionX + centreOffsetLeft() + transformed.x,
						positionY + centreOffsetTop() + transformed.y);
			}
		}

		arcIterator = bottom.iterator();
		transform.transform(new Point2D.Double(0, -0.5 * TRANSITION_HEIGHT), transformed);
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = (ArcAngleCompare) arcIterator.next();
			if (thisArc.sourceOrTarget()) {
				thisArc.arc.setTargetLocation(positionX + centreOffsetLeft() + transformed.x,
						positionY + centreOffsetTop() + transformed.y);
			} else {
				thisArc.arc.setSourceLocation(positionX + centreOffsetLeft() + transformed.x,
						positionY + centreOffsetTop() + transformed.y);
			}
		}

		arcIterator = left.iterator();
		double inc = TRANSITION_HEIGHT / (left.size() + 1);
		double current = TRANSITION_HEIGHT / 2 - inc;
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = (ArcAngleCompare) arcIterator.next();
			transform.transform(new Point2D.Double(-0.5 * TRANSITION_WIDTH, current + 1), transformed); // +1
																										// due
																										// to
																										// rounding
																										// making
																										// it
																										// off
																										// by
																										// 1
			if (thisArc.sourceOrTarget()) {
				thisArc.arc.setTargetLocation(positionX + centreOffsetLeft() + transformed.x,
						positionY + centreOffsetTop() + transformed.y);
			} else {
				thisArc.arc.setSourceLocation(positionX + centreOffsetLeft() + transformed.x,
						positionY + centreOffsetTop() + transformed.y);
			}
			current -= inc;
		}

		inc = TRANSITION_HEIGHT / (right.size() + 1);
		current = -TRANSITION_HEIGHT / 2 + inc;
		arcIterator = right.iterator();
		while (arcIterator.hasNext()) {
			ArcAngleCompare thisArc = (ArcAngleCompare) arcIterator.next();
			transform.transform(new Point2D.Double(+0.5 * TRANSITION_WIDTH, current), transformed);
			if (thisArc.sourceOrTarget()) {
				thisArc.arc.setTargetLocation(positionX + centreOffsetLeft() + transformed.x,
						positionY + centreOffsetTop() + transformed.y);
			} else {
				thisArc.arc.setSourceLocation(positionX + centreOffsetLeft() + transformed.x,
						positionY + centreOffsetTop() + transformed.y);
			}
			current += inc;
		}
	}

	public void addedToGui() {
		super.addedToGui();
		update();
	}

	private String getText() {
		if (attributesVisible == true) {
			if (isTimed()) {
				if (rateParameter != null) {
					return "\nr=" + rateParameter.getName();
				} else {
					return "\nr=" + rate;
				}
			} else {
				if (rateParameter != null) {
					return "\n" + '\u03C0' + "=" + priority + "\nw=" + rateParameter.getName();
				} else {
					return "\n" + '\u03C0' + "=" + priority + "\nw=" + rate;
				}
			}
		}
		return "";
	}

	public void setCentre(double x, double y) {
		super.setCentre(x, y);
		update();
	}

	public void toggleAttributesVisible() {
		attributesVisible = !attributesVisible;
		pnName.setText(getText());
	}

	@Override
	public boolean isMergeable(final PlaceTransitionObject pObject) {
		return true;
	}

	public void showEditor() {
		// Build interface
		EscapableDialog guiDialog = new EscapableDialog(CreateGui.getApp(), "PIPE2", true);

		TransitionEditor te = new TransitionEditor(guiDialog.getRootPane(), this, CreateGui.getModel(),
				CreateGui.getView());

		guiDialog.add(te);

		guiDialog.getRootPane().setDefaultButton(null);

		guiDialog.setResizable(false);

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);

		guiDialog.setVisible(true);

		guiDialog.dispose();
	}

	public RateParameter getRateParameter() {
		return rateParameter;
	}

	public UndoableEdit setRateParameter(RateParameter _rateParameter) {
		double oldRate = rate;
		rateParameter = _rateParameter;
		rateParameter.add(this);
		rate = rateParameter.getValue();
		update();
		return new SetRateParameterEdit(this, oldRate, rateParameter);
	}

	public UndoableEdit clearRateParameter() {
		RateParameter oldRateParameter = rateParameter;
		rateParameter.remove(this);
		rateParameter = null;
		update();
		return new ClearRateParameterEdit(this, oldRateParameter);
	}

	public UndoableEdit changeRateParameter(RateParameter _rateParameter) {
		RateParameter oldRateParameter = rateParameter;
		rateParameter.remove(this);
		rateParameter = _rateParameter;
		rateParameter.add(this);
		rate = rateParameter.getValue();
		update();
		return new ChangeRateParameterEdit(this, oldRateParameter, rateParameter);
	}

	public void update() {
		pnName.setText(getText());
		pnName.zoomUpdate(zoom);
		super.update();
		this.repaint();
	}

	public void delete() {
		if (rateParameter != null) {
			rateParameter.remove(this);
			rateParameter = null;
		}
		super.delete();
	}

	class ArcAngleCompare implements Comparable {

		private final static boolean SOURCE = false;
		private final static boolean TARGET = true;
		private Arc arc;
		private Transition transition;
		private double angle;

		public ArcAngleCompare(Arc _arc, Transition _transition) {
			arc = _arc;
			transition = _transition;
			calcAngle();
		}

		public int compareTo(Object arg0) {
			double angle2 = ((ArcAngleCompare) arg0).angle;

			return (angle < angle2 ? -1 : (angle == angle2 ? 0 : 1));
		}

		private void calcAngle() {
			int index = sourceOrTarget() ? arc.getArcPath().getEndIndex() - 1 : 1;
			Point2D.Double p1 = new Point2D.Double(positionX + centreOffsetLeft(), positionY + centreOffsetTop());
			Point2D.Double p2 = new Point2D.Double(arc.getArcPath().getPoint(index).x,
					arc.getArcPath().getPoint(index).y);

			if (p1.y <= p2.y) {
				angle = Math.atan((p1.x - p2.x) / (p2.y - p1.y));
			} else {
				angle = Math.atan((p1.x - p2.x) / (p2.y - p1.y)) + Math.PI;
			}

			// This makes sure the angle overlap lies at the intersection
			// between
			// edges of a transition
			// Yes it is a nasty hack (a.k.a. ingeneous solution). But it works!
			if (angle < (Math.toRadians(30 + transition.getAngle()))) {
				angle += (2 * Math.PI);
			}

			// Needed to eliminate an exception on Windows
			if (p1.equals(p2)) {
				angle = 0;
			}

		}

		private boolean sourceOrTarget() {
			return (arc.getSource() == transition ? SOURCE : TARGET);
		}

	}

	/**
	 * used for Check Var Consistency
	 */
	public ArrayList<Arc> getArcList() {
		Iterator<ArcAngleCompare> arcIterator = arcAngleList.iterator();
		while (arcIterator.hasNext()) {
			arcList.add(arcIterator.next().arc);
		}
		return arcList;
	}

	public ArrayList<String> getTranVarList() {
		return tranVarList;
	}

	// public void setTranVarList(){
	// XMLToString xmlToStr = new XMLToString(this);
	// xmlToStr.buildString();
	// NodeList nl = xmlToStr.doc.getElementsByTagName("Variable");
	// int n_var = nl.getLength();
	// tranVarList.clear();
	// for(int i=0;i<n_var;i++){
	// tranVarList.add(nl.item(i).getFirstChild().getNodeValue());
	// }
	// }

	/**
	 * update and get places from connectFrom arcs
	 */
	public ArrayList<Place> getPlaceInList() {
		this.placeInList.clear();
		this.arcInList.clear();

		for (Arc arc : getArcInList()) {
			this.arcInList.add(arc);
		}

		Iterator<Arc> arcIterator = arcInList.iterator();
		while (arcIterator.hasNext()) {
			Arc thisArc = arcIterator.next();
			placeInList.add((Place) thisArc.getSource());
		}

		return placeInList;
	}

	/**
	 * update and get places from connectTo arcs
	 */
	public ArrayList<Place> getPlaceOutList() {
		this.placeOutList.clear();
		this.arcOutList.clear();

		for (Arc arc : getArcOutList()) {
			this.arcOutList.add(arc);
		}

		Iterator<Arc> arcIterator = arcOutList.iterator();
		while (arcIterator.hasNext()) {
			Arc thisArc = arcIterator.next();
			placeOutList.add((Place) thisArc.getTarget());
		}

		return placeOutList;
	}

	/**
	 * get variables from arcs points to the transition
	 */
	public ArrayList<String> getArcInVarList() {
		arcInVarList.clear();
		Iterator<Arc> arcIterator = getArcInList().iterator();
		while (arcIterator.hasNext()) {
			Arc thisArc = arcIterator.next();
			arcInVarList.add(thisArc.getVar());
		}

		return arcInVarList;
	}

	/**
	 * get variables from arcs come out of the transition
	 */
	public ArrayList<String> getArcOutVarList() {
		arcOutVarList.clear();
		Iterator<Arc> arcIterator = getArcOutList().iterator();
		while (arcIterator.hasNext()) {
			Arc thisArc = arcIterator.next();
			arcOutVarList.add(thisArc.getVar());
		}

		return arcOutVarList;
	}

	public void getAndRemoveTokenFromPlaceForCurSymbolTable() {
		for (Arc arc : this.getArcInList()) {
			String[] arcVariables = arc.getVars();
			for (String variable : arcVariables) {
				if (arc.getSource() instanceof Place && symTable.exist(variable)) {
					Place sourcePlace = (Place) arc.getSource();
					if (arc.isSetVar()) {
						abToken token = (abToken) symTable.lookup(variable);
						sourcePlace.getToken().listToken.removeAll(token.listToken);
					} else {
						sourcePlace.getToken().listToken.remove(symTable.lookup(variable));
					}
				}
			}
		}
	}

	public Symbol getSymbolForOutputVariable(final String pVariable) {
		Symbol symbol = null;
		for (Arc ao : getArcOutList()) {
			if (ao.hasVariable(pVariable)) {
				if (ao.isSetVar()) {
					abToken token = new abToken(ao.getDataType());
					symbol = new Symbol(pVariable, token, Symbol.TYPE.MULTI);
				} else {
					Token token = new Token(ao.getDataType());
					token.defineTlist(ao.getDataType());
					symbol = new Symbol(pVariable, token, Symbol.TYPE.SINGLE);
				}

				return symbol;
			}
		}

		System.out.println("Warning: no variable found with name " + pVariable);
		return null;
	}

	/**
	 * update transition out variables to transition output places
	 *
	 * @return
	 */
	public void sendTokenFromCurSymbolTable() {
		for (Arc arc : getArcOutList()) {
			Place place = (Place) arc.getTarget();
			for (String var : arc.getVars()) {
				if (symTable.exist(var)) {
					Object binder = symTable.lookup(var);
					if (arc.isSetVar() && binder instanceof abToken) {
						place.getToken().listToken.addAll(((abToken) binder).listToken);
					} else if (!arc.isSetVar() && binder instanceof Token) {
						place.getToken().listToken.add((Token) binder);
					}
				}
			}
		}
	}

	public void tokCombinationsInSymbolTable(ArrayList<SymbolTable> tokCombs, SymbolTable solution,
			ArrayList<Arc> arcList, int curArcNo) {

		if (curArcNo == arcList.size()) {
			if (!solution.table.isEmpty()) {
				tokCombs.add(new SymbolTable(solution));
				CleanSymbolTable(tokCombs, arcList); // added by He 7/31/15
			}
			return;
		}

		ArrayList<SymbolTable> tokenCombinationsFromPlace = new ArrayList<SymbolTable>();
		tokCombsForFromPlace(tokenCombinationsFromPlace, new SymbolTable(), arcList.get(curArcNo), 0);
		for (SymbolTable symbolTable : tokenCombinationsFromPlace) {
			SymbolTable temporarySolution = new SymbolTable(solution);
			if (checkIfCompatible(temporarySolution, symbolTable)) {
				updateCurrentSolution(temporarySolution, symbolTable);
				tokCombinationsInSymbolTable(tokCombs, temporarySolution, arcList, curArcNo + 1);
			}
		}
	}

	private boolean checkIfCompatible(final SymbolTable pSolution1, final SymbolTable pSolution2) {
		for (String key : pSolution2.table.keySet()) {
			if (pSolution1.exist(key) && !pSolution1.lookup(key).equals(pSolution2.lookup(key))) {
				return false;
			}
		}

		return true;
	}

	private void updateCurrentSolution(final SymbolTable pSolution, final SymbolTable pCombination) {
		Iterator<String> combinationKeyIterator = pCombination.table.keySet().iterator();
		while (combinationKeyIterator.hasNext()) {
			String key = combinationKeyIterator.next();
			if (!pSolution.exist(key)) {
				Symbol uniqueSymbol = pCombination.lookUpForSymbol(key);
				pSolution.insert(key, uniqueSymbol.getBinder(), uniqueSymbol.getType());
			}
		}
	}

	private boolean isTokensEqual(final Object pToken1, final Object pToken2) {
		if (pToken1 instanceof Token && pToken2 instanceof Token) {
			return isTokensEqual((Token) pToken1, (Token) pToken2);
		} else if (pToken1 instanceof abToken && pToken2 instanceof abToken) {
			return isTokensEqual((abToken) pToken1, (abToken) pToken2);
		}

		throw new IllegalStateException("Tokens to test equality must be of same type");
	}

	private boolean isTokensEqual(final Token pToken1, final Token pToken2) {
		return pToken1.displayToken().equals(pToken2.displayToken());
	}

	private boolean isTokensEqual(final abToken pToken1, final abToken pToken2) {
		if (pToken1.listToken.size() != pToken2.listToken.size()) {
			return false;
		}

		for (Token token : pToken1.listToken) {
			boolean equals = false;
			for (Token token1 : pToken2.listToken) {
				if (token.displayToken().equals(token1.displayToken())) {
					equals = true;
					break;
				}
			}
			if (!equals) {
				return false;
			}
		}

		return true;
	}

	public void tokCombsForFromPlace(ArrayList<SymbolTable> results, SymbolTable solution, Arc arc, int k) {
		Place place = (Place) (arc.getSource());
		int varCount = arc.getVarCount();
		if (!(arc.isSetVar())) // changed by He to handle set variable - 8/2/15
		{
			if (k == varCount) {
				results.add(new SymbolTable(solution));
				return;
			}
			for (int i = 0; i < place.getToken().getTokenCount(); i++) {
				if (solution.containsToken(place.getToken().getTokenbyIndex(i))) {
					continue;
				}
				solution.insert((arc.getVars())[k], place.getToken().getTokenbyIndex(i), Symbol.TYPE.SINGLE);
				tokCombsForFromPlace(results, solution, arc, k + 1);
				solution.delete((arc.getVars())[k]);
			}
		} else { // changed by He - 8/5/15
			abToken aTok = new abToken(place.getDataType());
			for (int i = 0; i < place.getToken().getTokenCount(); i++) {
				aTok.addToken(place.getToken().getTokenbyIndex(i));
			}
			solution.insert(arc.getVar(), aTok, Symbol.TYPE.MULTI);
			results.add(solution);
			return;
		}
	}

	// added by He - 7/31/2015 This method checks whether a variable name
	// appears in multiple
	// arc labels, and removes all impossible token combinations
	public void CleanSymbolTable(ArrayList<SymbolTable> tokCombs, ArrayList<Arc> arcList) {
		// int combNo = tokCombs.size();
		// for (int k = 0; k < combNo; k++) {
		// SymbolTable next = (SymbolTable) tokCombs.get(k);
		// int noToken = next.table.size();
		// for (int i = 0; i < noToken - 1; i++) {
		// for (int j = i + 1; j < noToken; j++) {
		// if (next.table.get(i).getKey().equals(next.table.get(j).getKey())) {
		// if (((Token)
		// (next.table.get(i).getBinder())).Tlist.firstElement().kind == 0 &&
		// (((Token) (next.table.get(j).getBinder())).Tlist.firstElement().kind
		// == 0)) {
		// if (((Token)
		// (next.table.get(i).getBinder())).Tlist.firstElement().Tint !=
		// (((Token)
		// (next.table.get(j).getBinder())).Tlist.firstElement().Tint)) {
		// tokCombs.remove(next);
		// }
		//
		// }
		// else if (((Token)
		// (next.table.get(i).getBinder())).Tlist.firstElement().kind == 1 &&
		// (((Token) (next.table.get(j).getBinder())).Tlist.firstElement().kind
		// == 1)) {
		// if (!((Token)
		// (next.table.get(i).getBinder())).Tlist.firstElement().Tstring.equals(
		// (((Token)
		// (next.table.get(j).getBinder())).Tlist.firstElement().Tstring))) {
		// tokCombs.remove(next);
		// }
		// }
		// else {
		// System.out.println("Modeling error - the same variable name used in
		// different arcs connected to the same transition must be of the same
		// type");
		// }
		// }
		// }
		// }
		//
		// }
	}

	public boolean checkStatusAndFireWhenEnabled() {
		boolean status = false;

		// int numOfPlaces = this.getPlaceInList().size();

		// int[] tokens = new int[numOfPlaces];
		ArrayList<SymbolTable> tokCombs = new ArrayList<SymbolTable>();

		// check if any input place is empty
		for (Place p : getPlaceInList()) {
			if (p.getToken().listToken.isEmpty()) {
				System.out.println("Transition.checkStatusAndFire: input place lacks of token.");
				return false;
			}
		}
		// check if all output places have room since simple place can only hold
		// at most one token
		for (Place p : this.getPlaceOutList()) {
			if (!p.getDataType().getPow()) {
				if (p.getToken().getTokenCount() > 0) {
					boolean bidirect = false;
					for (Place q : this.getPlaceInList()) // added by He -
															// 8/3/15
					{
						if (p.getName().equals(q.getName())) {
							bidirect = true;
							break;
						}
					}
					if (!bidirect) {
						return false;
					}
				}
			}
		}

		// calculate combinations
		tokCombinationsInSymbolTable(tokCombs, new SymbolTable(), new ArrayList<Arc>(this.getArcInList()), 0);
		for (SymbolTable table : tokCombs) {
			table.printSymTable();
		}
		// check
		Iterator iTokCombs = tokCombs.iterator();
		while (iTokCombs.hasNext() && !status) {
			this.symTable = (SymbolTable) iTokCombs.next();

			String formula = this.getFormula();
			ErrorMsg errorMsg = new ErrorMsg(formula);
			Parse p = new Parse(formula, errorMsg);
			Sentence s = p.absyn;
			s.accept(new Interpreter(errorMsg, this, 1));
			status = s.bool_val;
		}
		// fire
		if (status) {
			getAndRemoveTokenFromPlaceForCurSymbolTable();
			// addOutputPlaceTokenToSymbolTable(); //added by He - 7/29/15
			// String formula = this.getFormula();
			// ErrorMsg errorMsg = new ErrorMsg(formula);
			// Parse p = new Parse(formula, errorMsg);
			// Sentence s = p.absyn;
			// s.accept(new Interpreter(errorMsg, this, 1));
			symTable.printSymTable(); // test
			sendTokenFromCurSymbolTable();
			getTransSymbolTable().cleanTable();
		}

		return status;
	}

	public SymbolTable getTransSymbolTable() {
		return this.symTable;
	}

	public ArrayList<Transition> getDependentTrans() {
		ArrayList<Transition> dependentTrans = new ArrayList<Transition>();

		for (Place p : this.getPlaceOutList()) {
			for (Transition t : p.getTransOutList()) {
				boolean existed = false;
				for (Transition t2 : dependentTrans) {
					if (t2.getId().equals(t.getId())) {
						existed = true;
					}
				}
				if (!existed) {
					dependentTrans.add(t);
				}

			}
		}

		return dependentTrans;
	}

	public boolean checkTransitionIsReadyToDefine() {
		boolean isReady = true;
		// check connected arcs
		for (Arc a : this.getArcList()) {
			if (a.getVar().equals("")) {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"This transition is not ready to specify, please define Arc:" + a.getName() + "'s variable!",
						"Unable To Specify Violation", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// connected places
		for (Place pi : this.getPlaceInList()) {
			if (pi.getDataType() == null) {
				JOptionPane.showMessageDialog(CreateGui.getApp(),
						"This transition is not ready to specify, please define place:" + pi.getName() + "'s datatype!",
						"Unable To Specify Violation", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		for (Place po : this.getPlaceOutList()) {
			if (po.getDataType() == null) {
				JOptionPane.showMessageDialog(
						CreateGui.getApp(), "This transition is not ready to specify, please define place:"
								+ po.getName() + "'s place type!",
						"Unable To Specify Violation", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		return isReady;
	}

	@Override
	public String toString() {
		return getName();
	}

	public int getLowerBound() {
		return lowerBound;
	}

	// public void setLowerBound(int lowerBound) {
	// this.lowerBound = lowerBound;
	// }

	public int getUpperBound() {
		return upperBound;
	}

	// public void setUpperBound(int upperBound) {
	// this.upperBound = upperBound;
	// }
}
