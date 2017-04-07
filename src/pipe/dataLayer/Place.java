package pipe.dataLayer;

import java.awt.BasicStroke;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import pipe.gui.CreateGui;
import pipe.gui.Pipe;
import pipe.gui.Grid;
import pipe.gui.undo.ChangeMarkingParameterEdit;
import pipe.gui.undo.ClearMarkingParameterEdit;
import pipe.gui.undo.SetMarkingParameterEdit;
import pipe.gui.undo.UndoableEdit;
import pipe.gui.undo.PlaceCapacityEdit;
import pipe.gui.undo.PlaceMarkingEdit;
import pipe.gui.widgets.PlaceEditorPanel;
import pipe.gui.widgets.PlaceTypePanel;
import pipe.gui.Zoomer;
import pipe.gui.widgets.EscapableDialog;


/**
 * <b>Place</b> - Petri-Net Place Class
 *
 * @see <p><a href="..\PNMLSchema\index.html">PNML  -  Petri-Net XMLSchema (stNet.xsd)</a>
 * @see </p><p><a href="..\..\..\UML\dataLayer.html">UML  -  PNML Package </a></p>
 * @version 1.0
 * @author James D Bloom
 *  
 * @author Edwin Chung corresponding states of matrixes has been set 
 * to change when markings are altered. Users will be prompted to save their
 * work when the markings of places are altered. (6th Feb 2007)
 * 
 * @author Edwin Chung 16 Mar 2007: modified the constructor and several
 * other functions so that DataLayer objects can be created outside the
 * GUI
 */
public class Place 
        extends PlaceTransitionObject {

   public final static String type = "Place";
   /** Initial Marking */
   private Integer initialMarking = 0;
   
   /** Current Marking */
    private Integer currentMarking = 0;
   
   /** Initial Marking X-axis Offset */
   private Double markingOffsetX = 0d;
   
   /** Initial Marking Y-axis Offset */
   private Double markingOffsetY = 0d;
   
   /**  Value of the capacity restriction; 0 means no capacity restriction */
   private Integer capacity = 0;
   /*
   private boolean strongCapacity = false;
  */
   
   /** Initial Marking X-axis Offset */
   private Double capacityOffsetX = 0.0;
   
   /** Initial Marking Y-axis Offset */
   private Double capacityOffsetY = 22.0;
   
   public static final int DIAMETER = Pipe.PLACE_TRANSITION_HEIGHT;
   
   /** Token Width */
   public static int tWidth = 4;
   
   /** Token Height */
   public static int tHeight = 4;
   
   /** Ellipse2D.Double place */
   private static Ellipse2D.Double place = 
            new Ellipse2D.Double(0, 0, DIAMETER, DIAMETER);
   private static Shape proximityPlace =
            (new BasicStroke(Pipe.PLACE_TRANSITION_PROXIMITY_RADIUS)).createStrokedShape(place);

   private MarkingParameter markingParameter = null;
   
   protected ArrayList<Arc> arcOutList = new ArrayList<Arc>();
   protected ArrayList<Transition> transOutList = new ArrayList<Transition>();
   
   public abToken token;
   private DataType dataType;
   private Vector<DataType> group = null;
   
   /**
    * Create Petri-Net Place object
    * @param positionXInput X-axis Position
    * @param positionYInput Y-axis Position
    * @param idInput Place id
    * @param nameInput Name
    * @param nameOffsetXInput Name X-axis Position
    * @param nameOffsetYInput Name Y-axis Position
    * @param initialMarkingInput Initial Marking
    * @param markingOffsetXInput Marking X-axis Position
    * @param markingOffsetYInput Marking Y-axis Position
    * @param capacityInput Capacity 
    */
   public Place(double positionXInput,  double positionYInput, 
                String idInput, 
                String nameInput, 
                Double nameOffsetXInput, Double nameOffsetYInput, 
                int initialMarkingInput, 
                double markingOffsetXInput,  double markingOffsetYInput,
                int capacityInput){
      super(positionXInput, positionYInput,
            idInput,
            nameInput,
            nameOffsetXInput, nameOffsetYInput);
      initialMarking = new Integer(initialMarkingInput);
      currentMarking = new Integer(initialMarkingInput);
      markingOffsetX = new Double(markingOffsetXInput);
      markingOffsetY = new Double(markingOffsetYInput);
      componentWidth = DIAMETER;
      componentHeight = DIAMETER;
      setCapacity(capacityInput);
      setCentre((int)positionX, (int)positionY);
      group = new Vector<DataType>();
      dataType = null;
      token = new abToken();
      
      //updateBounds();
   }   
   

   /**
    * Create Petri-Net Place object
    * @param positionXInput X-axis Position
    * @param positionYInput Y-axis Position
    */
   public Place(double positionXInput, double positionYInput){
      super(positionXInput, positionYInput);
      componentWidth = DIAMETER;
      componentHeight = DIAMETER;
      setCentre((int)positionX, (int)positionY);
      group = new Vector<DataType>();
      dataType = null;
      token = new abToken();
      //updateBounds();    
   }

   
   public Place paste(double x, double y, boolean fromAnotherView){
      Place copy = new Place (
              Grid.getModifiedX(x + this.getX() + Pipe.PLACE_TRANSITION_HEIGHT/2),
              Grid.getModifiedY(y + this.getY() + Pipe.PLACE_TRANSITION_HEIGHT/2));
      copy.pnName.setName(this.pnName.getName()  
                          + "(" + this.getCopyNumber() +")");
      this.newCopy(copy);
      copy.nameOffsetX = this.nameOffsetX;
      copy.nameOffsetY = this.nameOffsetY;
      copy.capacity = this.capacity;
      copy.attributesVisible = this.attributesVisible;
      copy.initialMarking = this.initialMarking;
      copy.currentMarking = this.currentMarking;
      copy.markingOffsetX = this.markingOffsetX;
      copy.markingOffsetY = this.markingOffsetY;
      copy.markingParameter = this.markingParameter;
      copy.update();
      return copy;
   }
   
   
   public Place copy(){
      Place copy = new Place (Zoomer.getUnzoomedValue(this.getX(), zoom), 
                              Zoomer.getUnzoomedValue(this.getY(), zoom));
      copy.pnName.setName(this.getName());
      copy.nameOffsetX = this.nameOffsetX;
      copy.nameOffsetY = this.nameOffsetY;
      copy.capacity = this.capacity;
      copy.attributesVisible = this.attributesVisible;
      copy.initialMarking = this.initialMarking;
      copy.currentMarking = this.currentMarking;
      copy.markingOffsetX = this.markingOffsetX;
      copy.markingOffsetY = this.markingOffsetY;
      copy.markingParameter = this.markingParameter;
      copy.setOriginal(this);
      return copy;
   }   
   
   
   /**
    * Paints the Place component taking into account the number of tokens from 
    * the currentMarking
    * @param g The Graphics object onto which the Place is drawn.
    */
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D)g;

      Insets insets = getInsets();
      int x = insets.left;
      int y = insets.top;
      
      if (hasCapacity()){
         g2.setStroke(new BasicStroke(2.0f));
      } else {
         g2.setStroke(new BasicStroke(1.0f));
      }
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                          RenderingHints.VALUE_ANTIALIAS_ON);
      
      if(selected && !ignoreSelection){
         g2.setColor(Pipe.SELECTION_FILL_COLOUR);
      } else{
         g2.setColor(Pipe.ELEMENT_FILL_COLOUR);
      }
      g2.fill(place);
      
      if (selected && !ignoreSelection){
         g2.setPaint(Pipe.SELECTION_LINE_COLOUR);
      } else{
         g2.setPaint(Pipe.ELEMENT_LINE_COLOUR);
      }
      g2.draw(place);
      
      g2.setStroke(new BasicStroke(1.0f));
      int marking = getCurrentMarking();
      
      // structure sees how many markings there are and fills the place in with 
      // the appropriate number.
      switch(marking) {
         case 5: 
            g.drawOval(x + 6, y + 6, tWidth, tHeight);
            g.fillOval(x + 6, y + 6, tWidth, tHeight);
            /* falls through */
         case 4:
            g.drawOval(x + 18, y + 20, tWidth, tHeight);
            g.fillOval(x + 18, y + 20, tWidth, tHeight);
            /* falls through */
         case 3:
            g.drawOval(x + 6, y + 20, tWidth, tHeight);
            g.fillOval(x + 6, y + 20, tWidth, tHeight);
            /* falls through */
         case 2: 
            g.drawOval(x + 18, y + 6, tWidth, tHeight);
            g.fillOval(x + 18, y + 6, tWidth, tHeight);
            /* falls through */
         case 1:
            g.drawOval(x + 12, y + 13, tWidth, tHeight);
            g.fillOval(x + 12, y + 13, tWidth, tHeight);
            break;
         case 0:
            break;
         default:
            if (marking > 999){
               g.drawString(String.valueOf(marking), x, y + 20);
            } else if (marking > 99){
               g.drawString(String.valueOf(marking), x + 3, y + 20);
            } else if (marking > 9){
               g.drawString(String.valueOf(marking), x + 7, y + 20);
            } else {
               g.drawString(String.valueOf(marking), x + 12, y + 20);
            }
            break;
      }
   }
   
   
   /**
    * Set initial marking
    * @param initialMarkingInput Integer value for initial marking
    */
   public void setInitialMarking(int initialMarkingInput) {
      initialMarking = new Integer(initialMarkingInput);
   }

   
   /**
    * Set current marking
    * @param currentMarkingInput Integer value for current marking
    */
   public UndoableEdit setCurrentMarking(int currentMarkingInput) {
      int oldMarking = currentMarking;

      if (capacity == 0){
         currentMarking = currentMarkingInput;
      } else {
         if (currentMarkingInput > capacity) {
            currentMarking = capacity;
         } else{
            currentMarking = currentMarkingInput;
         }
      }
      repaint();
      return new PlaceMarkingEdit(this, oldMarking, currentMarking);      
   }

   
   /**
    * Set capacity 
    * This method doesn't check if marking fulfilles current capacity restriction
    * @param newCapacity Integer value for capacity restriction
    */
   public UndoableEdit setCapacity(int newCapacity) {
      int oldCapacity = capacity;
      
      if (capacity != newCapacity) {
         capacity = newCapacity;
         update();  
      }
      return new PlaceCapacityEdit(this, oldCapacity, newCapacity);
   }   
   
   /**
    * Get initial marking
    * @return Integer value for initial marking
    */
   public int getInitialMarking() {
      return ((initialMarking == null) ? 0 : initialMarking.intValue());
   }

   
   /**
    * Get current marking
    * @return Integer value for current marking
    */
   public int getCurrentMarking() {
      return ((currentMarking == null) ? 0 : currentMarking.intValue());
   }
   
   
   /**
    * Get current capacity
    * @return Integer value for current capacity
    */
   public int getCapacity() {
      return ((capacity == null) ? 0 : capacity.intValue());
   }   

      
   /**
    * Get current marking
    * @return Integer value for current marking
    */
   public Integer getCurrentMarkingObject() {
      return currentMarking;
   }

   
   /**
    * Get X-axis offset for initial marking
    * @return Double value for X-axis offset of initial marking
    */
   public Double getMarkingOffsetXObject() {
      return markingOffsetX;
   }

   
   /**
    * Get Y-axis offset for initial marking
    * @return Double value for X-axis offset of initial marking
    */
   public Double getMarkingOffsetYObject() {
      return markingOffsetY;
   }

      
   /**
    * Returns the diameter of this Place at the current zoom
    */
   private int getDiameter() {
      return (int)(Zoomer.getZoomedValue(DIAMETER, zoom));
   }   
   
   
   public boolean contains(int x, int y) {
      double unZoomedX = 
              Zoomer.getUnzoomedValue(x - COMPONENT_DRAW_OFFSET, zoom);
      double unZoomedY = 
              Zoomer.getUnzoomedValue(y - COMPONENT_DRAW_OFFSET, zoom);
      
      someArc = CreateGui.getView().createArc;
      if (someArc != null){		// Must be drawing a new Arc if non-NULL.
         if ((proximityPlace.contains((int)unZoomedX, (int)unZoomedY)
                 || place.contains((int)unZoomedX, (int)unZoomedY))
                 && areNotSameType(someArc.getSource())){
            // assume we are only snapping the target...
            if (someArc.getTarget() != this){
               someArc.setTarget(this);
            }
            someArc.updateArcPosition();
            return true;
         } else {
            if (someArc.getTarget() == this) {
               someArc.setTarget(null);
               updateConnected();
            }
            return false;
         }
      } else {
         return place.contains((int)unZoomedX, (int)unZoomedY); 
      }
   }
   
   
   /* (non-Javadoc)
    * @see pipe.dataLayer.PlaceTransitionObject#updateEndPoint(pipe.dataLayer.Arc)
    */
   public void updateEndPoint(Arc arc) {
      if (arc.getSource()==this) {
         // Make it calculate the angle from the centre of the place rather than
         // the current start point
         arc.setSourceLocation(positionX + (getDiameter() * 0.5),
                               positionY + (getDiameter() * 0.5));
         double angle = arc.getArcPath().getStartAngle();
         arc.setSourceLocation(positionX + centreOffsetLeft() 
                                    - (0.5 * getDiameter() * (Math.sin(angle))),
                               positionY + centreOffsetTop() 
                                    + (0.5 * getDiameter() * (Math.cos(angle))));         
      } else {
         // Make it calculate the angle from the centre of the place rather than the current target point
         arc.setTargetLocation(positionX + (getDiameter() * 0.5),
                               positionY + (getDiameter() * 0.5));
         double angle = arc.getArcPath().getEndAngle();
         arc.setTargetLocation(positionX + centreOffsetLeft() 
                                    - (0.5 * getDiameter() * (Math.sin(angle))),
                               positionY + centreOffsetTop() 
                                    + (0.5 * getDiameter() * (Math.cos(angle))));
      }
   }


   public void toggleAttributesVisible(){
      attributesVisible = !attributesVisible;
      update();  
   }

   @Override
   public boolean isMergeable(final PlaceTransitionObject pWith) {
      if (pWith instanceof Place) {
         Place place = (Place) pWith;
         return getDataType() != null && place.getDataType() != null &&
                 getDataType().getStringRepresentation().equals(place.getDataType().getStringRepresentation());
      }

      return false;
   }


   public boolean hasCapacity(){
      return capacity > 0;
   }
   
   public List<Transition> getTransOutList(){
	   return getArcOutList().stream().map((final Arc t) -> (Transition) t.getTarget()).collect(Collectors.toList());
   }

   public void addedToGui(){
      super.addedToGui();
      update();
   }   
   
   
   public void showEditor(){
      // Build interface
      EscapableDialog guiDialog = 
              new EscapableDialog(CreateGui.getApp(), "PIPE2", true);
      
      Container contentPane = guiDialog.getContentPane();
      
      // 1 Set layout
      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));      
     
      // 2 Add Place editor
      contentPane.add( new PlaceEditorPanel(guiDialog.getRootPane(), 
              this, CreateGui.getModel(), CreateGui.getView()));

      guiDialog.setResizable(false);     
      
      // Make window fit contents' preferred size
      guiDialog.pack();
      
      // Move window to the middle of the screen
      guiDialog.setLocationRelativeTo(null);
      guiDialog.setVisible(true);
      
   }
   
   
   public void showType()
   {
	   // Build interface
	      EscapableDialog guiDialog = 
	              new EscapableDialog(CreateGui.getApp(), "PIPE2", true);
	      
	      Container contentPane = guiDialog.getContentPane();
	      
	      // 1 Set layout
	      contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));      
	     
	      // 2 Add Place editor
	      contentPane.add( new PlaceTypePanel(guiDialog.getRootPane(), 
	               this,group,CreateGui.getModel(), CreateGui.getView()));

	      guiDialog.setResizable(false);     
	      
	      // Make window fit contents' preferred size
	      guiDialog.pack();
	      
	      // Move window to the middle of the screen
	      guiDialog.setLocationRelativeTo(null);
	      guiDialog.setVisible(true);
   }
   
  //# Added Abhinav  8/19/2015
   public void CutPoints()
   {
	   
	   
	   
	   final JFrame parent = new JFrame();
       JButton button = new JButton();

       button.setText(" Click to Enter Cut Points");
       parent.add(button);
       parent.pack();
       parent.setVisible(true);
       //parent.setSize(300,200);
      
       button.addActionListener(new java.awt.event.ActionListener() {
          @Override
          public void actionPerformed(java.awt.event.ActionEvent evt) {
             String name = JOptionPane.showInputDialog(parent,
                 "Cut Points", null);
          }
       });
       
   }
   
   public void JoinPoints()
   {
	   
	   
	   
	   final JFrame parent = new JFrame();
       JButton button = new JButton();

       button.setText(" Click to Enter Join Points");
       parent.add(button);
       parent.pack();
       parent.setVisible(true);
       //parent.setSize(300,200);
      
       button.addActionListener(new java.awt.event.ActionListener() 
       {
           @Override
           public void actionPerformed(java.awt.event.ActionEvent evt) 
           {
               String name = JOptionPane.showInputDialog(parent,
                       "Join Points", null);
           }
       });
       
   }
   
         
   public void update() {
      if (attributesVisible == true){
         pnName.setText("\nk=" + (capacity > 0 ? capacity :"\u221E") +
            (markingParameter != null ? "\nm=" + markingParameter.toString() : ""));   
      } else {
         pnName.setText("");
      }          
      pnName.zoomUpdate(zoom);
      super.update();
      repaint();
   }
   
   
   public void delete() {
      if (markingParameter != null) {
         markingParameter.remove(this);
         markingParameter = null;
      }
      super.delete();
   }   

   
   public UndoableEdit setMarkingParameter(MarkingParameter _markingParameter) {
      int oldMarking = currentMarking;
      
      markingParameter = _markingParameter;
      markingParameter.add(this);
      currentMarking = markingParameter.getValue();
      update();      
      return new SetMarkingParameterEdit (this, oldMarking, markingParameter);
   }
   
      
   public UndoableEdit clearMarkingParameter() {
      MarkingParameter oldMarkingParameter = markingParameter;
      
      markingParameter.remove(this);
      markingParameter = null;
      update();
      return new ClearMarkingParameterEdit (this, oldMarkingParameter);
   }         
   
   
   public UndoableEdit changeMarkingParameter(MarkingParameter _markingParameter) {
      MarkingParameter oldMarkingParameter = markingParameter;
      
      markingParameter.remove(this);
      markingParameter = _markingParameter;
      markingParameter.add(this);
      currentMarking = markingParameter.getValue();
      update();
      return new ChangeMarkingParameterEdit(
              this, oldMarkingParameter, markingParameter);
   }        

   
   public MarkingParameter getMarkingParameter() {
      return markingParameter;
   }
   
   public void setDataType(DataType dt)
   {
	   dataType = dt;
	   token.definetype(dt);
   }
   
   public DataType getDataType()
   {
	   return dataType;
   }
    
   public void updateArcDataType()
   {
	   Iterator arcsFrom = getArcOutList().iterator();
       while(arcsFrom.hasNext()) {
          ((Arc)arcsFrom.next()).setDataType(dataType);
          //((Arc)arcsFrom.next()).repaint();
       }
       arcsFrom = getArcInList().iterator();
       while(arcsFrom.hasNext()) {
          ((Arc)arcsFrom.next()).setDataType(dataType);
          //((Arc)arcsFrom.next()).repaint();
       }
   }
   
   public void setGroup(Vector<DataType> _group)
   {
	   group = _group;
   }
   
   public Vector<DataType> getGroup()
   {
	   return this.group;
   }
   
   public void setToken(abToken t)
   {
	   token = t;
   }
   
   public abToken getToken()
   {
	   return token;
   }

   public Token addToken(BasicType[] bt)
   {
      if (dataType == null) {
         return null;
      }
      Token newtoken = new Token(dataType);
      newtoken.add(bt);

      if (token.addToken(newtoken)) {
         return newtoken;
      }
      return null;
   }
   
   
   public void deleteToken()
   {
	   int size = token.listToken.size();
	   for(int i=0;i<size;i++){
		   token.listToken.remove(0);    
		   System.out.println("the size now!!!!: "+size);
	   }
   }
   
   public void tailToken(){
	   if(this.getToken().listToken.firstElement().getTokentype().getPow()){
		   //do nothing because it is abToken, do have to tail it.
	   }else{
		   Token ft = this.getToken().listToken.firstElement();
		   if(ft!=null){
			   this.getToken().listToken.remove(ft);
			   this.getToken().listToken.add(ft);
		  }
	   }
   }

   @Override
   public String toString() {
      return getName();
   }
}
