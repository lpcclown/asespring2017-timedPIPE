package pipe.dataLayer;

import org.apache.commons.lang.StringUtils;
import pipe.gui.CreateGui;
import pipe.gui.GuiView;
import pipe.gui.Pipe;
import pipe.gui.undo.AddArcPathPointEdit;
import pipe.gui.undo.ArcWeightEdit;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.widgets.ArcEditorPanel;
import pipe.gui.widgets.EscapableDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;


/**
 * <b>Arc</b> - Petri-Net Arc Class
 *
 * @see <p><a href="..\PNMLSchema\index.html">PNML  -  Petri-Net XMLSchema (stNet.xsd)</a>
 * @see </p><p><a href="..\..\..\UML\dataLayer.html">UML  -  PNML Package </a></p>
 * @version 1.0
 * @author James D Bloom
 * 
 * @author Pere Bonet modifed the delete method so that the weight label of an 
 * arc is deleted when the associated arc is deleted
 * 
 * @author Edwin Chung 16 Mar 2007: modified the constructor and several
 * other functions so that DataLayer objects can be created outside the GUI
 *
 * @author Nick Dingle 18 Oct 2007: added the ability for an arc to be "tagged"
 * (permit the passage of tagged tokens).
 */
public abstract class  Arc 
        extends PetriNetObject 
        implements Cloneable {
   
   //public final static String TYPE = "arc";
   /** Current Marking */
   protected int weight = 1;
   /** Initial Marking X-axis Offset */
   private Double weightOffsetX = null;
   /** Initial Marking Y-axis Offset */
   private Double weightOffsetY = null;
    
   protected NameLabel weightLabel;
      
   private static Point2D.Double point;
   
   /** references to the objects this arc connects */
   private PlaceTransitionObject source = null;
   private PlaceTransitionObject target = null;
   //private boolean deleted = false; // Used for cleanup purposes
   
   protected ArcPath myPath = new ArcPath(this);
   
   // true if arc is not hidden when a bidirectional arc is used
   protected boolean inView = true;
   
   // bounds of arc need to be grown in order to avoid clipping problems
   protected int zoomGrow = 10;
   
   private String Var = "";
  private String[] variableNames;
   
   private DataType dataType = null;
   
   
   /**
    * Create Petri-Net Arc object
    * @param startPositionXInput Start X-axis Position
    * @param startPositionYInput Start Y-axis Position
    * @param endPositionXInput End X-axis Position
    * @param endPositionYInput End Y-axis Position
    * @param sourceInput Arc source
    * @param targetInput Arc target
    * @param idInput Arc id
    * @param inputTagged TODO
    */
   public Arc(double startPositionXInput, double startPositionYInput, 
              double endPositionXInput, double endPositionYInput, 
              PlaceTransitionObject sourceInput, 
              PlaceTransitionObject targetInput,
              int weightInput,
              String idInput) {   
      weightLabel = new NameLabel(zoom);
      myPath.addPoint((float)startPositionXInput, (float)startPositionYInput, 
               ArcPathPoint.STRAIGHT);
      myPath.addPoint((float)endPositionXInput, (float)endPositionYInput, 
               ArcPathPoint.STRAIGHT);
      myPath.createPath();
      updateBounds();  
      id = idInput;
      setSource(sourceInput);
      setTarget(targetInput);
      setWeight(weightInput);
   }

   
   /**
    * Create Petri-Net Arc object
    */
   public Arc(PlaceTransitionObject newSource) {
      weightLabel = new NameLabel(zoom);
      source = newSource;
      myPath.addPoint();
      myPath.addPoint();
      myPath.createPath();
      if(newSource instanceof Place)
      {
    	  dataType = ((Place)newSource).getDataType();
    	  
      }
   }
   
   
   public Arc() {
      super();
   }
   
   /**
    * Set source
    * @param sourceInput PlaceTransitionObject value for Arc source;
    */
   public void setSource(PlaceTransitionObject sourceInput) {
      source = sourceInput;
      if(sourceInput instanceof Place)
      {
    	  dataType = ((Place)sourceInput).getDataType();
    	  
      }
   }
   
   
   /**
    * Set target
    * @param targetInput PlaceTransitionObject value for Arc target;
    */
   public void setTarget(PlaceTransitionObject targetInput) {
      target = targetInput;
      if(targetInput instanceof Place)
      {
    	  dataType = ((Place)targetInput).getDataType();
      }
//      if (CreateGui.getApp() != null) {
//         updateArcPosition();
//      }
   }
   
   
   /**
    * Set weight
    * @param weightInput String value for Arc weight;
    */
   public UndoableEdit setWeight(int weightInput) {
      int oldWeight = weight;
      
      weight = weightInput;
      if (weight == 1) {
         weightLabel.setText("");
      } else {
         weightLabel.setText(Integer.toString(weight) );
      }
      weightLabel.updateSize();      
      setWeightLabelPosition();
      repaint();
      return new ArcWeightEdit(this, oldWeight, weight);
   }
   
   public void setVar()
   {
	   weightLabel.setText(Var);
	   weightLabel.updateSize();      
	   setWeightLabelPosition();
	   repaint();  
   }
      
   
   public void setWeightLabelPosition() {
      weightLabel.setPosition(
          (int) (myPath.midPoint.x) + weightLabel.getWidth() / 2 - 4,
          (int) (myPath.midPoint.y));
   }
   
   
   public void showEditor(){
	      // Build interface
	      EscapableDialog guiDialog = 
	              new EscapableDialog(CreateGui.getApp(), "PIPE2", true);
	      
	      Container contentPane = guiDialog.getContentPane();
	      
	      // 1 Set layout
	      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));      
	     
	      // 2 Add arc editor
	      contentPane.add( new ArcEditorPanel(guiDialog.getRootPane(), 
	              this, CreateGui.getModel(), CreateGui.getView()));

	      guiDialog.setResizable(false); 
	      
	      
	      // Make window fit contents' preferred size
	      guiDialog.pack();
	      
	      // Move window to the middle of the screen
	      guiDialog.setLocationRelativeTo(null);
	      guiDialog.setVisible(true);
	   }
   
   /**
    * Get id
    * @return String value for Arc id;
    */
   public String getId() {
      if(id != null) {
         return id;
      } else {
         if(source != null && target != null) {
            return source.getId() + " to " + target.getId();
         }
      }
      return "";
   }
   
   public void setName(String _id)
   {
	   id = _id;
   }
   
   
   public String getName() {
      return getId();
   }
   
   
   /**
    * Get source returns null if value not yet entered
    * @return String value for Arc source;
    */
   public PlaceTransitionObject getSource() {
      return source;
   }
   
   
   /**
    * Get target returns null if value not yet entered
    * @return String value for Arc target;
    */
   public PlaceTransitionObject getTarget() {
      return target;
   }
   
   
   /**
    * Get X-axis value of start position
    * @return Double value for X-axis of start position
    */
   public double getStartPositionX() {
      return myPath.getPoint(0).getX();
   }
   
   
   /**
    * Get Y-axis value of start position
    * @return Double value for Y-axis of start position
    */
   public double getStartPositionY() {
      return myPath.getPoint(0).getY();
   }

   
   /**
    * Get weight
    * @return Integer value for Arc weight;
    */
   public int getWeight() {
      return weight;
   }
  
   
   /** 
    * Updates the start position of the arc, resets the arrowhead and updates 
    * the bounds
    */
   public void updateArcPosition() {
      if (source != null) {
         source.updateEndPoint(this);
      }      
      if (target != null) {
         target.updateEndPoint(this);
      }      
      myPath.createPath();
   }
   
   
   public void setEndPoint(double x, double y, boolean type) {
      myPath.setPointLocation(myPath.getEndIndex(),x,y);
      myPath.setPointType(myPath.getEndIndex(),type);
      updateArcPosition();
   }
   
   
   public void setTargetLocation(double x, double y) {
      myPath.setPointLocation(myPath.getEndIndex(),x,y);
      myPath.createPath();
      updateBounds();
      repaint();
   }
   
   
   public void setSourceLocation(double x, double y) {
      myPath.setPointLocation(0,x,y);
      myPath.createPath();
      updateBounds();
      repaint();
   }

   
   /** Updates the bounding box of the arc component based on the arcs bounds*/
   public void updateBounds() {
      bounds = myPath.getBounds();
      bounds.grow(COMPONENT_DRAW_OFFSET + zoomGrow,
          COMPONENT_DRAW_OFFSET + zoomGrow);
      setBounds(bounds);
   }
   
   
   public ArcPath getArcPath() {
      return myPath;
   }
   
   
   public boolean contains(int x, int y) {
      point = new Point2D.Double(
               x + myPath.getBounds().getX() - COMPONENT_DRAW_OFFSET - zoomGrow,
               y + myPath.getBounds().getY() - COMPONENT_DRAW_OFFSET - zoomGrow);
      if (!CreateGui.getView().isInAnimationMode()) {
         if (myPath.proximityContains(point) || selected) {	
            // show also if Arc itself selected
            myPath.showPoints();
         } else {
            myPath.hidePoints();
         }
      }
      return myPath.contains(point);
   }

   
   public void addedToGui() {
      // called by GuiView / State viewer when adding component.
      deleted = false;    
      markedAsDeleted = false;
      
      if (getParent() instanceof GuiView) { 
         myPath.addPointsToGui((GuiView)getParent());
      } else {
         myPath.addPointsToGui((JLayeredPane)getParent());
      }
      updateArcPosition();
      if (getParent() != null && weightLabel.getParent() == null) {
         getParent().add(weightLabel);
      }
   }

   
   public void delete() {
      if (!deleted) {
         if (getParent() != null) {
            getParent().remove(weightLabel);
         }
         myPath.forceHidePoints();
         super.delete();
         deleted = true;
      }
   }

   
   public void setPathToTransitionAngle(int angle) {
      myPath.setTransitionAngle(angle);
   }

   
   public UndoableEdit split(Point2D.Float mouseposition) {
      ArcPathPoint newPoint = myPath.splitSegment(mouseposition);
      return new AddArcPathPointEdit(this, newPoint);
   }
   
   
   public abstract String getType();

   
   public boolean inView() {
      return inView;
   }
   
   
   public Transition getTransition() {
      if (getTarget() instanceof Transition) {
         return (Transition)getTarget();
      } else {
         return (Transition)getSource();
      }
   }   
   
   
   public void removeFromView() {
      if (getParent() != null) {
         getParent().remove(weightLabel);
      }      
      myPath.forceHidePoints();
      removeFromContainer();
   }   
   
   
   public boolean getsSelected(Rectangle selectionRectangle) {
      if (selectable) {
         ArcPath arcPath = getArcPath();
         if (arcPath.proximityIntersects(selectionRectangle)) {
            arcPath.showPoints();
         } else {
            arcPath.hidePoints();
         }
         if (arcPath.intersects(selectionRectangle)) {
            select();
            return true;
         }
      }
      return false;
   }
   

   public int getLayerOffset() {
      return Pipe.ARC_LAYER_OFFSET;
   }   
   
   
   public void translate(int x, int y) {
      // We don't translate an arc, we translate each selected arc point
   }
   
   
   public void zoomUpdate(int percent) {
      zoom = percent;
      this.updateArcPosition();
      weightLabel.zoomUpdate(percent);
      weightLabel.updateSize();
   }
   
   
   public void setZoom(int percent) {
      zoom = percent;
   }
   
   
   public void undelete(DataLayer model, GuiView view) {
      if (this.isDeleted()) {
         model.addPetriNetObject(this);
         view.add(this);
         getSource().addConnectFrom(this);
         getTarget().addConnectTo(this);
      }
   }
   
   
   /**
    * Method to clone an Arc object
    */
   public PetriNetObject clone() { 
      return (Arc) super.clone();
   }   
   
   //added by He - 7/26/2015
   public boolean Abtoken() {
	   if(this.getSource() instanceof Place)
	   {
		   if(((Place) this.getSource()).getDataType() == null)
		   {
			   JOptionPane.showMessageDialog(CreateGui.getApp(), 
					   "Connected Place type undefined!", "Type Undefined Violation", JOptionPane.ERROR_MESSAGE);
			   return false;
		   }
		   if(!((Place)this.getSource()).getDataType().getPow())
		   {
			   return false;
		   }
	   }
	   if(this.getTarget() instanceof Place)
	   {
		   if(!((Place)this.getTarget()).getDataType().getPow())
		   {
			   return false;
		   }
	   }
	   return true;
   }
	   
   
   public boolean setVar(String _Var)
   {
     Place place = (Place) (getSource() instanceof Place ? getSource() : getTarget());
     if (place.getDataType() == null) {
       JOptionPane.showMessageDialog(CreateGui.getApp(),
           "Connected Place type undefined!", "Type Undefined Violation", JOptionPane.ERROR_MESSAGE);
       return false;
     }

     //simple place can only have one var per arc
     String variable = _Var.trim();
       if (StringUtils.isBlank(variable)) {
           return false;
       }
     if (variable.charAt(0) == '{' && variable.charAt(variable.length()-1) == '}') {
       variable = variable.substring(1, variable.length()-1);
     }

     String[] variables = variable.replaceAll("\\s", "").split(",");

     if (!place.getDataType().getPow() && variables.length > 1) {
       JOptionPane.showMessageDialog(CreateGui.getApp(),
           "Arc label cannot have more than one variable for simple place!", "Specification Violation", JOptionPane.ERROR_MESSAGE);
       return false;
     }

     Var = _Var;
     variableNames = variables;

	   return true;
   }
   
   public String getVar()
   {
	   return Var;
   }
   
   public String[] getVars()
   {
	   return variableNames;
   }
   
   public int getVarCount()
   {
	   return variableNames == null? 0 : variableNames.length;
   }
   
   public void setDataType(DataType d)
   {
	   dataType = d;
   }
   
   public DataType getDataType()
   {
	   return dataType;
   }
   
   
   public boolean isSetVar()
   {
     if (Var.startsWith("{") && Var.endsWith("}")) {
       return false;
     }

     Place place = (Place) (getSource() instanceof Place ? getSource() : getTarget());
     return place.getDataType().getPow();
   }


   /**
    *
    * @param pVariableName
    * @param pIsOutward       true if the arc is outward in respect to some transition
    * @return
    */
   public boolean hasVariable(final String pVariableName, final boolean pIsOutward) {
     return hasVariable(pVariableName);
   }

  public boolean hasVariable(final String pVariableName) {
    if (getVarCount() <= 0) {
      return false;
    }
    else if (isSetVar()) {
      return pVariableName.equals(getVar());
    }
    else {
      for (String var : getVars()) {
        if (var.equals(pVariableName)) {
          return true;
        }
      }
    }

    return false;
  }
}
