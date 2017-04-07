package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import pipe.gui.Pipe;
import pipe.gui.Zoomer;
import pipe.gui.undo.TagBidirectArcEdit;
import pipe.gui.undo.UndoableEdit;

/**
 * One more arc type bi-directional arc is used to connect place and transition
 */
public class BidirectionalArc extends Arc {
	private static final long serialVersionUID = 7625753993701479169L;

	private final static Polygon head = 
			new Polygon(new int[]{0, 5, 0, -5}, new int[]{0, -10, -7, -10}, 4);

	private boolean joined = true;
	private final static String type = "bidirectional";

	/** Whether or not the Arc is capable of carrying tagged tokens
	 *  By default it is not
	 */
	private Boolean tagged = false;  

	public BidirectionalArc(BidirectionalArc arc) {
		weightLabel = new NameLabel(zoom);

		for (int i = 0; i <= arc.myPath.getEndIndex(); i++){
			this.myPath.addPoint(arc.myPath.getPoint(i).getX(),
					arc.myPath.getPoint(i).getY(),
					arc.myPath.getPointType(i));         
		}      
		this.myPath.createPath();
		this.updateBounds();  
		this.id = arc.id;
		this.setSource(arc.getSource());
		this.setTarget(arc.getTarget());
		this.setWeight(arc.getWeight());
		this.inView = arc.inView;
		this.joined = arc.joined;
	}

	public BidirectionalArc(double startPositionXInput, double startPositionYInput, 
			double endPositionXInput, double endPositionYInput, 
			PlaceTransitionObject sourceInput, PlaceTransitionObject targetInput, 
			int weightInput, String idInput, boolean taggedInput) {
		super(startPositionXInput, startPositionYInput, endPositionXInput,
				endPositionYInput, sourceInput, targetInput, weightInput, idInput);
		setTagged(taggedInput);
	}

	public BidirectionalArc(PlaceTransitionObject newSource) {
		super(newSource);
	}

	@Override
	public PetriNetObject copy() {
		return new BidirectionalArc(this);
	}

	@Override
	public PetriNetObject paste(double despX, double despY,
			boolean toAnotherView) {
		PlaceTransitionObject source = this.getSource().getLastCopy();
		PlaceTransitionObject target = this.getTarget().getLastCopy();

		if (source == null && target == null) {
			// don't paste an arc with neither source nor target
			return null;
		}

		if (source == null){
			if (toAnotherView) {
				// if the source belongs to another Petri Net, the arc can't be 
				// pasted
				return null;
			} else {
				source = this.getSource();
			}
		}

		if (target == null){
			if (toAnotherView) {
				// if the target belongs to another Petri Net, the arc can't be 
				// pasted            
				return null;
			} else {
				target = this.getTarget();
			}
		}

		BidirectionalArc copy =  new BidirectionalArc(0, 0, //startPoint
				0, 0, //endPoint
				source,
				target,
				this.getWeight(),
				source.getId() + " to " + target.getId(),
				false);      

		copy.myPath.delete();
		for (int i = 0; i <= this.myPath.getEndIndex(); i++){
			copy.myPath.addPoint(this.myPath.getPoint(i).getX() + despX,
					this.myPath.getPoint(i).getY() + despY,
					this.myPath.getPointType(i));         
			copy.myPath.selectPoint(i); 
		}

		source.addConnectFrom(copy);
		target.addConnectTo(copy);

		copy.inView = this.inView;
		copy.joined = this.joined;

		return copy;
	}

	@Override
	public String getType() {
		return type;
	}

	/** Accessor function to set whether or not the Arc is tagged */             
	public UndoableEdit setTagged(boolean flag){
		/**Set the timed transition attribute (for GSPNs)*/

		tagged = flag;

		// If it becomes tagged we must remove any existing weight....
		// ...and thus we can reuse the weightLabel to display that it's tagged!!!
		// Because remember that a tagged arc must have a weight of 1...
		if (tagged) {
			//weight = 1;
			weightLabel.setText("TAG");
			setWeightLabelPosition();
			weightLabel.updateSize();         
		} else {
			weightLabel.setText((weight > 1)?Integer.toString(weight) : "");
		}
		repaint();
		return new TagBidirectArcEdit(this);      
	}

	public boolean isTagged(){
		return tagged;
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.translate(COMPONENT_DRAW_OFFSET + zoomGrow - myPath.getBounds().getX(),
				COMPONENT_DRAW_OFFSET + zoomGrow - myPath.getBounds().getY());

		AffineTransform reset = g2.getTransform();      

		if (selected && !ignoreSelection){
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else{
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}      

		if (joined) {
			g2.translate(myPath.getPoint(0).getX(), myPath.getPoint(0).getY());
			g2.rotate(myPath.getStartAngle() + Math.PI);
			g2.transform(Zoomer.getTransform(zoom)); 
			g2.fillPolygon(head);
			g2.setTransform(reset);         
		}

		g2.setStroke(new BasicStroke(0.01f * zoom));
		g2.draw(myPath);

		g2.translate(myPath.getPoint(myPath.getEndIndex()).getX(),
				myPath.getPoint(myPath.getEndIndex()).getY());

		g2.rotate(myPath.getEndAngle()+Math.PI);
		g2.setColor(java.awt.Color.WHITE);

		g2.transform(Zoomer.getTransform(zoom));   
		g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);    

		if (selected && !ignoreSelection){
			g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
		} else{
			g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
		}

		g2.setStroke(new BasicStroke(0.8f));
		g2.fillPolygon(head);

		g2.transform(reset);   
	}   

}