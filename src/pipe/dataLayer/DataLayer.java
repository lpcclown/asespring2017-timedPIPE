package pipe.dataLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pipe.dataLayer.converter.PTNConverterManager;
import pipe.dataLayer.converter.PTNObjectConverter;
import pipe.gui.CreateGui;
import pipe.gui.Pipe;

//import formulaParser.Yylex;


/**
 * <b>DataLayer</b> - Encapsulates entire Petri-Net, also contains functions to
 * perform calculations
 *
 * @author James D Bloom
 * @author David Patterson Jan 2, 2006: Changed the fireRandomTransition
 *         method to give precedence to immediate transitions.
 * @author Edwin Chung added a boolean attribute to each matrix generated to
 *         prevent them from being created again when they have not been changed
 *         (6th Feb 2007)
 * @author Ben Kirby Feb 10, 2007: Removed savePNML method and the
 *         createPlaceElement, createAnnotationElement, createArcElement, createArcPoint,
 *         createTransitionElement methods it uses to a separate DataLayerWriter class,
 *         as part of refactoring to remove XML related actions from the DataLayer class.
 * @author Ben Kirby Feb 10, 2007: Split loadPNML into two bits. All XML work
 *         (Files, transformers, documents) is done in new PNMLTransformer class. The
 *         calls to actually populate a DataLayer object with the info contained in the
 *         PNML document have been moved to a createFromPNML method. The DataLayer
 *         constructor which previously used loadPNML has been changed to reflect
 *         these modifications. Also moved getDOM methods to PNMLTranformer class, as
 *         getDom is XML related. Removed getDom() (no arguments) completely as this
 *         is not called anywhere in the application.
 * @author Will Master Feb 13 2007: Added methods getPlacesCount and
 *         getTransitionsCount to avoid needlessly copying place and transition
 *         arrayLists.
 * @author Edwin Chung 15th Mar 2007: modified the createFromPNML function so
 *         that DataLayer objects can be created outside GUI
 * @author Dave Patterson 24 April 2007: Modified the fireRandomTransition
 *         method so it is quicker when there is only one transition to fire (just fire
 *         it, don't get a random variable first). Also, throw a RuntimeException if a
 *         rate less than 1 is detected. The current code uses the rate as a weight,
 *         and a rate such as 0.5 leads to a condition like that of bug 1699546 where no
 *         transition is available to fire.
 * @author Dave Patterson 10 May 2007: Modified the fireRandomTransitino method
 *         so it now properly handles fractional weights. There is no RuntimeException
 *         thrown now. The code for timed transitions uses the same logic, but will soon
 *         be changed to use exponentially distributed times where fractional rates
 *         are valid.
 * @author Barry Kearns August 2007: Added clone functionality and storage of
 *         state groups.
 * @version 1.0
 * @see <p><a href="..\PNMLSchema\index.html">PNML  -  Petri-Net XMLSchema
 * (stNet.xsd)</a>
 * @see </p><p><a href="uml\DataLayer.png">DataLayer UML</a></p>
 **/
public class DataLayer
    extends Observable
    implements Cloneable {

  private static final String ID_TEMPLATE = "%s%s";
  private static Random randomNumber = new Random(); // Random number generator

  /**
   * PNML File Name
   */
  public String pnmlName = null;

  /**
   * List containing all the Place objects in the Petri-Net
   */
  private Map<String, Place> mPlacesMap = new LinkedHashMap<>();

  /**
   * List containing all the SysPlace objects in the Petri-Net
   */
  private ArrayList sysPlacesArray = null;

  /**
   * ArrayList containing all the Transition objects in the Petri-Net
   */
  private Map<String, Transition> mTransitionsMap = new LinkedHashMap<>();

  /**
   * ArrayList containing all the Arc objects in the Petri-Net
   */
  private Map<String, NormalArc> mNormalArcsMap = new LinkedHashMap<>();

  /**
   * ArrayList containing all the Arc objects in the Petri-Net
   */
  private Map<String, InhibitorArc> mInhibitorArcMap = new LinkedHashMap<>();
  
  /**
   * ArrayList containing all the bi-directional arc objects in the Petri-Net
   */
  private Map<String, BidirectionalArc> mBidirectionalArcMap = new LinkedHashMap<>();

  /**
   * ArrayList for net-level label objects (as opposed to element-level labels).
   */
  private Map<String, AnnotationNote> mLabelsMap = new LinkedHashMap<>();

  /**
   * ArrayList for marking Parameters objects.
   */
  private Map<String, MarkingParameter> mMarkingParametersMap = new LinkedHashMap<>();

  /**
   * ArrayList for rate Parameters objects.
   */
  private Map<String, RateParameter> mRateParametersMap = new LinkedHashMap<>();

  /**
   * Initial Marking Vector
   */
  private int[] initialMarkingVector = null;
  /**
   * Current Marking Vector
   */
  private int[] currentMarkingVector = null;
  /**
   * Capacity Matrix
   */
  private int[] capacityVector = null;
  /**
   * Priority Matrix
   */
  private int[] priorityVector = null;
  /**
   * Timed Matrix
   */
  private boolean[] timedVector = null;
  /**
   * Marking Vector Storage used during animation
   */
  private int[] markingVectorAnimationStorage = null;

  /**
   * Forward Incidence Matrix
   */
  private PNMatrix forwardsIncidenceMatrix = null;
  /**
   * Backward Incidence Matrix
   */
  private PNMatrix backwardsIncidenceMatrix = null;
  /**
   * Incidence Matrix
   */
  private PNMatrix incidenceMatrix = null;

  /**
   * Inhibition Matrix
   */
  private PNMatrix inhibitionMatrix = null;

  /**
   * Used to determine whether the matrixes have been modified
   */
  static boolean initialMarkingVectorChanged = true;

  static boolean currentMarkingVectorChanged = true;


  /**
   * X-Axis Scale Value
   */
  private final int DISPLAY_SCALE_FACTORX = 7; // Scale factors for loading other Petri-Nets (not yet implemented)
  /**
   * Y-Axis Scale Value
   */
  private final int DISPLAY_SCALE_FACTORY = 7; // Scale factors for loading other Petri-Nets (not yet implemented)
  /**
   * X-Axis Shift Value
   */
  private final int DISPLAY_SHIFT_FACTORX = 270; // Scale factors for loading other Petri-Nets (not yet implemented)
  /**
   * Y-Axis Shift Value
   */
  private final int DISPLAY_SHIFT_FACTORY = 120; // Scale factors for loading other Petri-Nets (not yet implemented)

  /**
   * Hashtable which maps PlaceTransitionObjects to their list of connected arcs
   */
  private Hashtable arcsMap = null;

  /**
   * Hashtable which maps PlaceTransitionObjects to their list of connected arcs
   */
  private Hashtable inhibitorsMap = null;
  
  /**
   * Hashtable which maps PlaceTransitionObjects to their list of connected arcs
   */
  private Hashtable bidirectionalArcsMap = null;
  
  /**
   * An ArrayList used store the source / destination state groups associated
   * with this Petri-Net
   */
  private LinkedHashMap<String, StateGroup> mStatesGroupMap = new LinkedHashMap<>();
  private ArrayList stateGroups = null;

  public ArrayList<Transition> unknown = null;
  public ArrayList<Transition> disabled = null;
  public ArrayList<Transition> dependents = null;

  private String propertyFormula;
  private HashMap<String, Map<String, ? extends PetriNetObject>> mObjectsHolder;

  private static final Map<String, String> sIdPrefixesMap = new HashMap<String, String>() {{
    put(Place.class.getName(), "P");
    put(Transition.class.getName(), "T");
    put(NormalArc.class.getName(), "A");
    put(InhibitorArc.class.getName(), "I");
    put(BidirectionalArc.class.getName(), "B");
    put(AnnotationNote.class.getName(), "L");
    put(MarkingParameter.class.getName(), "M");
    put(RateParameter.class.getName(), "R");
    put(StateGroup.class.getName(), "SG");
  }};


  /**
   * Create Petri-Net object from PNML file with URI pnmlFileName
   *
   * @param pnmlFileName Name of PNML File
   */
  public DataLayer(String pnmlFileName) {
    this(new File(pnmlFileName));
  }


  /**
   * Create Petri-Net object from pnmlFile
   *
   * @param pnmlFile PNML File
   */
  public DataLayer(File pnmlFile) {
    this();
    PNMLTransformer transform = new PNMLTransformer();
    pnmlName = pnmlFile.getName();
    createFromPNML(transform.transformPNML(pnmlFile.getAbsolutePath()));
  }


  /**
   * Create empty Petri-Net object
   */
  public DataLayer() {
    initializeMatrices();
    initializeObjectsHolder();
  }

  private void initializeObjectsHolder() {
    mObjectsHolder = new HashMap<>();
    mObjectsHolder.put(Place.class.getName(), mPlacesMap);
    mObjectsHolder.put(Transition.class.getName(), mTransitionsMap);
    mObjectsHolder.put(NormalArc.class.getName(), mNormalArcsMap);
    mObjectsHolder.put(InhibitorArc.class.getName(), mInhibitorArcMap);
    mObjectsHolder.put(BidirectionalArc.class.getName(), mBidirectionalArcMap);
    mObjectsHolder.put(AnnotationNote.class.getName(), mLabelsMap);
    mObjectsHolder.put(MarkingParameter.class.getName(), mMarkingParametersMap);
    mObjectsHolder.put(RateParameter.class.getName(), mRateParametersMap);
  }


  /**
   * Method to clone a DataLayer object
   */
  public DataLayer clone() {
    DataLayer newClone = null;
    try {
      newClone = (DataLayer) super.clone();

      newClone.mPlacesMap = deepCopy(this.mPlacesMap);
      newClone.sysPlacesArray = deepCopy(sysPlacesArray);
      newClone.mTransitionsMap = deepCopy(mTransitionsMap);
      newClone.mNormalArcsMap = deepCopy(mNormalArcsMap);
      newClone.mInhibitorArcMap = deepCopy(mInhibitorArcMap);
      //newClone.tokensArray = deepCopy(tokensArray);
      newClone.mLabelsMap = deepCopy(mLabelsMap);
      newClone.mMarkingParametersMap = deepCopy(mMarkingParametersMap);
      newClone.mRateParametersMap = deepCopy(mMarkingParametersMap);
    }
    catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
    return newClone;
  }


  /**
   * @param original arraylist to be deep copied
   * @return a clone of the arraylist
   */
  private static ArrayList deepCopy(ArrayList original) {
    ArrayList result = (ArrayList) original.clone();
    ListIterator listIter = result.listIterator();

    while (listIter.hasNext()) {
      PetriNetObject pnObj = (PetriNetObject) listIter.next();
      listIter.set(pnObj.clone());
    }
    return result;
  }

  private static Map deepCopy(Map<String, ? extends PetriNetObject> pOriginal) {
    Map clonedMap = new LinkedHashMap<>();
    for (Map.Entry<String, ? extends PetriNetObject> entry : pOriginal.entrySet()) {
      clonedMap.put(entry.getKey(), entry.getValue().clone());
    }

    return clonedMap;
  }


  /**
   * Initialize Arrays
   */
  private void initializeMatrices() {
    sysPlacesArray = new ArrayList();
    stateGroups = new ArrayList();
    initialMarkingVector = null;
    forwardsIncidenceMatrix = null;
    backwardsIncidenceMatrix = null;
    incidenceMatrix = null;
    inhibitionMatrix = null;
    unknown = new ArrayList<Transition>();
    disabled = new ArrayList<Transition>();
    dependents = new ArrayList<Transition>();

    // may as well do the hash table here
    arcsMap = new Hashtable();
    inhibitorsMap = new Hashtable();
    bidirectionalArcsMap = new Hashtable();
  }


  /**
   * Update the arcsMap hashtable to reflect the new arc
   *
   * @param arcInput New Arc
   */
  private void addArcToEndpoints(Arc arcInput) {
    // now we want to add the arc to the list of arcs for it's source and target
    PlaceTransitionObject source = arcInput.getSource();
    PlaceTransitionObject target = arcInput.getTarget();

    Map effectiveMap = null;
    if (arcInput instanceof NormalArc) {
      effectiveMap = arcsMap;
    } else if (arcInput instanceof InhibitorArc) {
      effectiveMap = inhibitorsMap;
    } else if (arcInput instanceof BidirectionalArc) {
      effectiveMap = bidirectionalArcsMap;
    }

    if (effectiveMap != null) {
      if (source != null) {
        // Pete: Place/Transitions now always movable
        //      source.setMovable(false);
        List arcsOfSource = (List) effectiveMap.get(source);
        if (arcsOfSource == null) {
          arcsOfSource = new ArrayList<>();
          effectiveMap.put(source, arcsOfSource);
        }
        arcsOfSource.add(arcInput);
      }

      if (target != null) {
        // Pete: Place/Transitions now always movable
        //      target.setMovable(false);
        List arcsOfTarget = (List) effectiveMap.get(target);
        if (arcsOfTarget == null) {
          arcsOfTarget = new ArrayList<>();
          effectiveMap.put(target, arcsOfTarget);
        }
        arcsOfTarget.add(arcInput);
      }
    }
  }


  public void addStateGroup(final StateGroup pStateGroupInput, final boolean pMakeUnique) {
    if (pStateGroupInput.getId() == null || pStateGroupInput.getId().length() == 0 || pMakeUnique) {
      pStateGroupInput.setId(generateUniqueIdentifierForType(pStateGroupInput.getId(), pStateGroupInput.getClass().getName(), mStatesGroupMap.keySet()));
    }

    mStatesGroupMap.put(pStateGroupInput.getId(), pStateGroupInput);
  }

  private String generateUniqueIdentifierForType(final String pPreferredId, final String pObjectType, final Set<String> pExistingIDs) {
    int suffix = pExistingIDs.size();
    String prefix = sIdPrefixesMap.get(pObjectType);
    String generatedId = (pPreferredId == null || pPreferredId.length() == 0) ? String.format(ID_TEMPLATE, prefix, suffix++) : pPreferredId;
    while(pExistingIDs.contains(generatedId)) {
      generatedId = String.format(ID_TEMPLATE, prefix, suffix++);
    }

    return generatedId;
  }

  /**
   * Add any PetriNetObject - the object will be added to the appropriate list.
   * If the object passed in isn't a Transition, Place or Arc nothing will happen.
   * All observers are notified of this change.
   *
   * @param pnObject The PetriNetObject to be added.
   */
  public void addPetriNetObject(PetriNetObject pnObject, final boolean pMakeUnique) {
    Map objectsHolder = getObjectsHolderFor(pnObject);
    if (pnObject.getId() == null || pnObject.getId().length() == 0 || pMakeUnique) {
      pnObject.setId(generateUniqueIdentifierFor(pnObject));
    }

    if (pnObject instanceof Arc) {
      addArcToEndpoints((Arc) pnObject);
    }
    else if (pnObject instanceof Transition) {
      unknown.add((Transition) pnObject);
    }

    objectsHolder.put(pnObject.getId(), pnObject);

    setChanged();
    setMatrixChanged();
    notifyObservers(pnObject);
  }

  private String generateUniqueIdentifierFor(final PetriNetObject pPnObject) {
    Set<String> currentIds = getObjectsHolderFor(pPnObject).keySet();
    return generateUniqueIdentifierForType(pPnObject.getId(), pPnObject.getClass().getName(), currentIds);
  }

  public void addPetriNetObject(PetriNetObject pnObject) {
    addPetriNetObject(pnObject, true);
  }

  private Map<String, ? extends PetriNetObject> getObjectsHolderFor(final PetriNetObject pPnObject) {
    return mObjectsHolder.get(pPnObject.getClass().getName());
  }


  /**
   * Removes the specified object from the appropriate ArrayList of objects.
   * All observers are notified of this change.
   *
   * @param pnObject The PetriNetObject to be removed.
   */
  public void removePetriNetObject(PetriNetObject pnObject) {
    getObjectsHolderFor(pnObject).remove(pnObject.getId());

    try {
      removeAttachedArcs(pnObject);
      removeAttachedPlaceTransition(pnObject);

          setChanged();
          setMatrixChanged();
          // notifyObservers(pnObject.getBounds());
          notifyObservers(pnObject);
    }
    catch (NullPointerException npe) {
      System.out.println("NullPointerException [debug]\n" + npe.getMessage());
      throw npe;
    }
  }

  private void removeAttachedPlaceTransition(final PetriNetObject pnObject) {
    if (! (pnObject instanceof Arc)) {
      return;
    }

    Hashtable arcs = null;
    Arc arc = (Arc) pnObject;
    if (pnObject instanceof NormalArc) {
    	arcs = arcsMap;
    } else if (pnObject instanceof InhibitorArc) {
    	arcs = inhibitorsMap;
    } else if (pnObject instanceof BidirectionalArc) {
    	arcs = bidirectionalArcsMap;
     }

    PlaceTransitionObject attachedSource = arc.getSource();
    if (attachedSource != null) {
      List attachedArcList = (List) arcs.get(attachedSource);
      if (attachedArcList != null) {
        attachedArcList.remove(arc);
      }
      attachedSource.removeFromArc(arc);
    }

    PlaceTransitionObject attachedTarget = arc.getTarget();
    if (attachedTarget != null) {
      List attachedArcList = (List) arcs.get(attachedTarget);
      if (attachedArcList != null) {
        attachedArcList.remove(arc);
      }
      attachedTarget.removeToArc(arc);
    }
  }

  private void removeAttachedArcs(final PetriNetObject pnObject) {
    if (pnObject instanceof PlaceTransitionObject) {
      // get the list of attached arcs for the object we are removing
      List attachedArcs = ((ArrayList) arcsMap.get(pnObject));
      if (attachedArcs != null) {
        // iterate over all the attached arcs, removing them all
        // Pere: in inverse order!
        //for (int i=0; i < attachedArcs.size(); i++){
        for (int i = attachedArcs.size() - 1; i >= 0; i--) {
          ((Arc) attachedArcs.get(i)).delete();
        }
        arcsMap.remove(pnObject);
      }

      // get the list of attached inhibitor arcs for the object we are removing
      List attachedInhibitors = ((ArrayList) inhibitorsMap.get(pnObject));
      if (attachedInhibitors != null) {
        // iterate over all the attached arcs, removing them all
        // Pere: in inverse order!
        //for (int i=0; i < attachedArcs.size(); i++){
        for (int i = attachedInhibitors.size() - 1; i >= 0; i--) {
          ((Arc) attachedInhibitors.get(i)).delete();
        }
        inhibitorsMap.remove(pnObject);
      }
      // get the list of attached bidirectional arcs for the object we are removing
      List attachedBidirectionalArcs = ((ArrayList) bidirectionalArcsMap.get(pnObject));
      if (attachedBidirectionalArcs != null) {
        // iterate over all the attached arcs, removing them all
        // Pere: in inverse order!
        //for (int i=0; i < attachedArcs.size(); i++){
        for (int i = attachedBidirectionalArcs.size() - 1; i >= 0; i--) {
          ((Arc) attachedBidirectionalArcs.get(i)).delete();
        }
        bidirectionalArcsMap.remove(pnObject);
      }

    }
  }


  /**
   * This method removes a state group from the arrayList
   *
   * @param SGObject The State Group objet to be removed
   */
  public void removeStateGroup(StateGroup SGObject) {
    stateGroups.remove(SGObject);
  }

  /**
   * Checks whether a state group with the same name exists already as the
   * argument
   * * @param stateName
   *
   * @return
   */
  public boolean stateGroupExistsAlready(String stateName) {
    Iterator<StateGroup> i = stateGroups.iterator();
    while (i.hasNext()) {
      StateGroup stateGroup = i.next();
      String stateGroupName = stateGroup.getName();
      if (stateName.equals(stateGroupName)) {
        return true;
      }
    }
    return false;
  }


  /**
   * Returns an iterator for the transitions array.
   * Used by Animator.class to set all enabled transitions to highlighted
   */
  public Iterator returnTransitions() {
    return mTransitionsMap.values().iterator();
  }


  /**
   * Returns an iterator of all PetriNetObjects - the order of these cannot be guaranteed.
   *
   * @return An iterator of all PetriNetObjects
   */
  public Iterator<PetriNetObject> getPetriNetObjects() {
    ArrayList all = new ArrayList(mPlacesMap.values());
    all.addAll(mTransitionsMap.values());
    all.addAll(mNormalArcsMap.values());
    all.addAll(mInhibitorArcMap.values());
    all.addAll(mLabelsMap.values());
    //tokensArray removed
    all.addAll(mMarkingParametersMap.values());
    all.addAll(mRateParametersMap.values());

    return all.iterator();
  }


  public boolean hasPlaceTransitionObjects() {
    return (mPlacesMap.size() + mTransitionsMap.size()) > 0;
  }


  private DataLayer createAgentNet(Element agentNetElement) {
    DataLayer agentNet = new DataLayer();
    initializeDataLayerFromDOMElement(agentNet, agentNetElement);

    return agentNet;
  }


  /**
   * Creates all Petri-Net Matrixes from current Petri-Net
   */
  private void createMatrixes() {
    createIncidenceMatrix();
    createInitialMarkingVector();
    createCurrentMarkingVector();
    createInhibitionMatrix();
  }


  /**
   * Creates Forward Incidence Matrix from current Petri-Net
   */
  private void createForwardIncidenceMatrix() {
    int placeSize = mPlacesMap.size();
    int transitionSize = mTransitionsMap.size();

    forwardsIncidenceMatrix = new PNMatrix(placeSize, transitionSize);

    //Fix-Me:: previous arcArray is now represented by as mNormalArcsPam. Test if it is okay.
    for (Arc arc : mNormalArcsMap.values()) {
      if (arc != null) {
        PetriNetObject pnObject = arc.getTarget();
        if (pnObject != null) {
          if (pnObject instanceof Place) {
            Place place = (Place) pnObject;
            pnObject = arc.getSource();
            if (pnObject != null) {
              if (pnObject instanceof Transition) {
                Transition transition = (Transition) pnObject;
                int transitionNo = getListPosition(transition);
                int placeNo = getListPosition(place);
                try {
                  forwardsIncidenceMatrix.set(
                      placeNo, transitionNo, arc.getWeight());
                }
                catch (Exception e) {
                  JOptionPane.showMessageDialog(null,
                      "Problem in forwardsIncidenceMatrix");
                  System.out.println("p:" + placeNo + ";t:" + transitionNo + ";w:" + arc.getWeight());
                }
              }
            }
          }
        }
      }
    }
  }


  /**
   * Creates Backwards Incidence Matrix from current Petri-Net
   */
  private void createBackwardsIncidenceMatrix() {//Matthew
    int placeSize = mPlacesMap.size();
    int transitionSize = mTransitionsMap.size();

    backwardsIncidenceMatrix = new PNMatrix(placeSize, transitionSize);

    for (Arc arc : mNormalArcsMap.values()) {
      if (arc != null) {
        PetriNetObject pnObject = arc.getSource();
        if (pnObject != null) {
          if (pnObject instanceof Place) {
            Place place = (Place) pnObject;
            pnObject = arc.getTarget();
            if (pnObject != null) {
              if (pnObject instanceof Transition) {
                Transition transition = (Transition) pnObject;
                int transitionNo = getListPosition(transition);
                int placeNo = getListPosition(place);
                try {
                  backwardsIncidenceMatrix.set(
                      placeNo, transitionNo, arc.getWeight());
                }
                catch (Exception e) {
                  JOptionPane.showMessageDialog(null,
                      "Problem in backwardsIncidenceMatrix");
                  System.out.println("p:" + placeNo + ";t:" + transitionNo + ";w:" + arc.getWeight());
                }
              }
            }
          }
        }
      }
    }
  }


  /**
   * Creates Incidence Matrix from current Petri-Net
   */
  private void createIncidenceMatrix() {
    createForwardIncidenceMatrix();
    createBackwardsIncidenceMatrix();
    incidenceMatrix = new PNMatrix(forwardsIncidenceMatrix);
    incidenceMatrix = incidenceMatrix.minus(backwardsIncidenceMatrix);
    incidenceMatrix.matrixChanged = false;
  }


  /**
   * Creates Initial Marking Vector from current Petri-Net
   */
  private void createInitialMarkingVector() {
    initialMarkingVector = new int[mPlacesMap.size()];
    int index = 0;
    for (Place place : mPlacesMap.values()) {
      initialMarkingVector[index++] = place.getInitialMarking();
    }
  }


  /**
   * Creates Current Marking Vector from current Petri-Net
   */
  private void createCurrentMarkingVector() {
    currentMarkingVector = new int[mPlacesMap.size()];
    int index = 0;
    for (Place place : mPlacesMap.values()) {
      currentMarkingVector[index++] = place.getCurrentMarking();
    }
  }


  /**
   * Creates Capacity Vector from current Petri-Net
   */
  private void createCapacityVector() {
    capacityVector = new int[mPlacesMap.size()];
    int index = 0;
    for (Place place : mPlacesMap.values()) {
      capacityVector[index++] = place.getCurrentMarking();
    }
  }


  /**
   * Creates Timed Vector from current Petri-Net
   */
  private void createTimedVector() {
    timedVector = new boolean[mTransitionsMap.size()];
    int index = 0;
    for (Transition transition : mTransitionsMap.values()) {
      timedVector[index++] = transition.isTimed();
    }
  }


  /**
   * Creates Priority Vector from current Petri-Net
   */
  private void createPriorityVector() {
    priorityVector = new int[mTransitionsMap.size()];
    int index = 0;
    for (Transition transition : mTransitionsMap.values()) {
      priorityVector[index++] = transition.getPriority();
    }
  }


  /**
   * Creates Inhibition Matrix from current Petri-Net
   */
  private void createInhibitionMatrix() {
    int placeSize = mPlacesMap.size();
    int transitionSize = mTransitionsMap.size();
    inhibitionMatrix = new PNMatrix(placeSize, transitionSize);

    for (InhibitorArc inhibitorArc : mInhibitorArcMap.values()) {
      if (inhibitorArc != null) {
        PetriNetObject pnObject = inhibitorArc.getSource();
        if (pnObject != null) {
          if (pnObject instanceof Place) {
            Place place = (Place) pnObject;
            pnObject = inhibitorArc.getTarget();
            if (pnObject != null) {
              if (pnObject instanceof Transition) {
                Transition transition = (Transition) pnObject;
                int transitionNo = getListPosition(transition);
                int placeNo = getListPosition(place);
                try {
                  inhibitionMatrix.set(
                      placeNo, transitionNo, inhibitorArc.getWeight());
                }
                catch (Exception e) {
                  JOptionPane.showMessageDialog(null,
                      "Problema a inhibitionMatrix");
                  System.out.println("p:" + placeNo + ";t:" + transitionNo + ";w:" + inhibitorArc.getWeight());
                }
              }
            }
          }
        }
      }
    }
  }


  /**
   * Stores Current Marking
   */
  public void storeState() {
    markingVectorAnimationStorage = new int[mPlacesMap.size()];
    int index = 0;
    for (Place place : mPlacesMap.values()) {
      markingVectorAnimationStorage[index++] = place.getCurrentMarking();
    }
  }


  /**
   * Restores To previous Stored Marking
   */
  public void restoreState() {
    if (markingVectorAnimationStorage != null) {
      int index = 0;
      for (Place place : mPlacesMap.values()) {
          place.setCurrentMarking(markingVectorAnimationStorage[index++]);
          setChanged();
          notifyObservers(place);
          setMatrixChanged();
      }
    }
  }


  /**
   * Fire a specified transition, no affect if transtions not enabled
   *
   * @param transition Reference of specifiec Transition
   */
  public void fireTransition(Transition transition) {
    if (transition != null) {
      setEnabledTransitions();
      if (transition.isEnabled()) {
        int transitionNo = getListPosition(transition);
        int placeNo = 0;
        for (Place place : mPlacesMap.values()) {
          place.setCurrentMarking(currentMarkingVector[placeNo] + incidenceMatrix.get(placeNo, transitionNo));
          placeNo++;
        }
      }
    }
    setMatrixChanged();
  }


  /**
   * Fire a random transition, takes rate (probability) of Transitions into account
   */
  public Transition fireRandomTransition() {

    setEnabledTransitions();
    // All the enabled transitions are of the same type:
    // a) all are immediate transitions; or
    // b) all are timed transitions.

    ArrayList enabledTransitions = new ArrayList();
    double rate = 0;
    for (Transition transition : mTransitionsMap.values()) {
      if (transition.isEnabled()) {
        enabledTransitions.add(transition);
        rate += transition.getRate();
      }
    }

    // if there is only one enabled transition, return this transition
    if (enabledTransitions.size() == 1) {
      return (Transition) enabledTransitions.get(0);
    }

    double random = randomNumber.nextDouble();
    double x = 0;
    for (int i = 0; i < enabledTransitions.size(); i++) {
      Transition t = (Transition) enabledTransitions.get(i);

      x += t.getRate() / rate;

      if (random < x) {
        return t;
      }
    }

    // no enabled transition found, so no transition can be fired
    return null;
  }

  /**
   * This method will fire a random transition, and gives precedence
   * to immediate transitions before considering "timed" transitions.
   * The "rate" property of the transition is used as a weighting
   * factor so the probability of selecting a transition is the
   * rate of that transition divided by the sum of the weights of the
   * other enabled transitions of its class. The "rate" property can
   * now be used to give priority among several enabled, immediate
   * transitions, or when there are no enabled, immediate transitions
   * to give priority among several enabled, "timed" transitions.
   * <p>
   * Note: in spite of the name "timed" there is no probabilistic rate
   * calculated -- just a weighting factor among similar transitions.
   * <p>
   * Changed by David Patterson Jan 2, 2006
   * <p>
   * Changed by David Patterson Apr 24, 2007 to clean up problems
   * caused by fractional rates, and to speed up processing when only
   * one transition of a kind is enabled.
   * <p>
   * Changed by David Patterson May 10, 2007 to properly handle fractional
   * weights for immeditate transitions.
   * <p>
   * THe same logic is also used for timed transitions until the exponential
   * distribution is added. When that happens, the code will only be used for
   * immediate transitions.
   * /
   * public Transition fireRandomTransition() {
   * Transition result = null;
   * Transition t;
   * setEnabledTransitions();
   * // int transitionsSize = transitionsArray.size()*transitionsArray.size()*transitionsArray.size();
   * int transitionNo = 0;
   * <p>
   * double rate = 0.0d;
   * double sumOfImmedWeights = 0.0d;
   * double sumOfTimedWeights = 0.0d;
   * ArrayList timedTransitions = new ArrayList();	// ArrayList<Transition>
   * ArrayList immedTransitions = new ArrayList();	// ArrayList<Transition>
   * <p>
   * for(transitionNo = 0 ; transitionNo < transitionsArray.size() ; transitionNo++){
   * t = (Transition) transitionsArray.get(  transitionNo  );
   * rate = t.getRate();
   * if ( t.isEnabled()) {
   * if ( t.isTimed() ) {                     // is it a timed transition
   * timedTransitions.add( t );
   * sumOfTimedWeights += rate;
   * } else {                                  // immediate transition
   * immedTransitions.add( t  );
   * sumOfImmedWeights += rate;
   * }
   * }		// end of if isEnabled
   * }		// end of for transitionNo
   * <p>
   * // Now, if there are immediate transitions, pick one
   * // next block changed by David Patterson to fix bug
   * int count = immedTransitions.size();
   * switch ( count ) {
   * case 0:		// no immediate transitions
   * break;	// skip out
   * case 1: 	// only one immediate transition
   * result = (Transition) immedTransitions.get( 0 );
   * break;	// skip out
   * default:	// several immediate transitions
   * double rval = sumOfImmedWeights * randomNumber.nextDouble();
   * for ( int index = 0; index < count; index++ ) {
   * t = (Transition) immedTransitions.get( index );
   * rval -= t.getRate();
   * if ( rval <= 0.0d ) {
   * result = t;
   * break;
   * }
   * }
   * }
   * if ( result == null ) {             // no immediate transition found
   * count = timedTransitions.size(); // count of timed, enabled transitions
   * switch( count ) {
   * case 0:		// trouble! No enabled transition found
   * break;
   * case 1: 	// only one timed transition
   * result = ( Transition ) timedTransitions.get( 0 );
   * break;
   * default:		// several timed transitions -- for now, pick one
   * double rval = sumOfTimedWeights * randomNumber.nextDouble();
   * for ( int index = 0; index < count; index++ ) {
   * t = (Transition) timedTransitions.get( index );
   * rval -= t.getRate();
   * if ( rval <= 0.0d ) {
   * result = t;
   * break;
   * }
   * }
   * }
   * }
   * <p>
   * if ( result == null ) {
   * System.out.println( "no random transition to fire" );
   * } else {
   * fireTransition(result);
   * createCurrentMarkingVector();
   * }
   * resetEnabledTransitions();
   * return result;
   * }     // end of method fireRandomTransition
   */


  public void fireTransitionBackwards(Transition transition) {
    if (transition != null) {
      setEnabledTransitionsBackwards();
      if (transition.isEnabled()) {
        int transitionNo = getListPosition(transition);
        int placeNo = 0;
        for (Place place : mPlacesMap.values()) {
          place.setCurrentMarking(currentMarkingVector[placeNo] - incidenceMatrix.get(placeNo, transitionNo));
          placeNo++;
        }
      }
    }
    setMatrixChanged();
  }
   

   /* Method not used * /
   public void fireRandomTransitionBackwards() {
      setEnabledTransitionsBackwards();
      int transitionsSize = transitionsArray.size() * transitionsArray.size() *
              transitionsArray.size();
      int randomTransitionNumber = 0;
      Transition randomTransition = null;
      do {
         randomTransitionNumber = randomNumber.nextInt(transitionsArray.size());
         randomTransition = 
                 (Transition)transitionsArray.get(randomTransitionNumber);
         transitionsSize--;
         if(transitionsSize <= 0){
            break;
         }
      } while(! randomTransition.isEnabled());
      fireTransitionBackwards(randomTransition);
//    System.out.println("Random Fired Transition Backwards" + ((Transition)transitionsArray.get(randonTransition)).getId());
   }*/


  public void resetEnabledTransitions() {
    for (Transition transition : mTransitionsMap.values()) {
      transition.setEnabled(false);
      setChanged();
      notifyObservers(transition);
    }
  }


  /**
   * Calculate whether a transition is enabled given a specific marking
   *
   * @param DataLayer - the net
   * @param int[]     - the marking
   * @param int       - the specific transition to test for enabled status
   * @return boolean  - an array of booleans specifying which transitions are
   * enabled in the specified marking
   */
  public boolean getTransitionEnabledStatus(int[] marking, int transition) {
    int transCount = this.getTransitionsCount();
    int placeCount = this.getPlacesCount();
    boolean[] result = new boolean[transCount];
    int[][] CMinus = this.getBackwardsIncidenceMatrix();

    //initialise matrix to true
    for (int k = 0; k < transCount; k++) {
      result[k] = true;
    }
    for (int i = 0; i < transCount; i++) {
      for (int j = 0; j < placeCount; j++) {
        if (marking[j] < CMinus[j][i]) {
          result[i] = false;
        }
      }
    }
    return result[transition];
  }


  /**
   * getTransitionEnabledStatusArray()
   * Calculate which transitions are enabled given a specific marking.
   *
   * @param int[] the marking
   * @return boolean[]  an array of booleans specifying which transitions are
   * enabled in the specified marking
   * @author Matthew Cook (original code), Nadeem Akharware (optimisation)
   * @author Pere Bonet added inhibitor arcs, place capacities and transition
   * priorities
   */
  public boolean[] getTransitionEnabledStatusArray(int[] marking) {
    return getTransitionEnabledStatusArray(
        this.getTransitions(),
        marking,
        this.getBackwardsIncidenceMatrix(),
        this.getForwardsIncidenceMatrix(),
        this.getInhibitionMatrix(),
        this.getCapacityVector(),
        this.getPlacesCount(),
        this.getTransitionsCount());
  }


  /**
   * Determines whether all transitions are enabled and sets
   * the correct value of the enabled boolean
   */
  public void setEnabledTransitionsBackwards() {

    if (currentMarkingVectorChanged) {
      createMatrixes();
    }

    boolean[] enabledTransitions = getTransitionEnabledStatusArray(
        this.getTransitions(),
        this.getCurrentMarkingVector(),
        this.getForwardsIncidenceMatrix(),
        this.getBackwardsIncidenceMatrix(),
        this.getInhibitionMatrix(),
        this.getCapacityVector(),
        this.getPlacesCount(),
        this.getTransitionsCount());

    for (int i = 0; i < enabledTransitions.length; i++) {
      //Fix-Me:: serious performance issue. Check to see if the previous implementation treats the enabled transitions
      // array as similar as original transitions array
      Transition transition = getPetriNetObjectAtIndex(i, Transition.class);
      if (enabledTransitions[i] != transition.isEnabled()) {
        transition.setEnabled(enabledTransitions[i]);
        setChanged();
        notifyObservers(transition);
      }
    }
  }


  /**
   * Determines whether all transitions are enabled and sets
   * the correct value of the enabled boolean
   */
  public void setEnabledTransitions() {

    if (currentMarkingVectorChanged) {
      createMatrixes();
    }

    boolean[] enabledTransitions = getTransitionEnabledStatusArray(
        this.getTransitions(),
        this.getCurrentMarkingVector(),
        this.getBackwardsIncidenceMatrix(),
        this.getForwardsIncidenceMatrix(),
        this.getInhibitionMatrix(),
        this.getCapacityVector(),
        this.getPlacesCount(),
        this.getTransitionsCount());

    for (int i = 0; i < enabledTransitions.length; i++) {
      //Fix-Me: performance issue
      Transition transition = getPetriNetObjectAtIndex(i, Transition.class);
      if (enabledTransitions[i] != transition.isEnabled()) {
        transition.setEnabled(enabledTransitions[i]);
        setChanged();
        notifyObservers(transition);
      }
    }
  }


  /**
   * getTransitionEnabledStatusArray()
   * Calculate which transitions are enabled given a specific marking.
   *
   * @param int[] the marking
   * @return boolean[]  an array of booleans specifying which transitions are
   * enabled in the specified marking
   * @author Matthew Cook (original code), Nadeem Akharware (optimisation)
   * @author Pere Bonet added inhibitor arcs, place capacities and transition
   * priorities
   */
  private boolean[] getTransitionEnabledStatusArray(
      final Transition[] transArray, final int[] marking,
      final int[][] CMinus, final int[][] CPlus, final int[][] inhibition,
      final int capacities[], final int placeCount,
      final int transitionCount) {

    boolean[] result = new boolean[transitionCount];
    boolean hasTimed = false;
    boolean hasImmediate = false;

    int maxPriority = 0;

    for (int i = 0; i < transitionCount; i++) {
      result[i] = true; //inicialitzam a enabled
      for (int j = 0; j < placeCount; j++) {
        if ((marking[j] < CMinus[j][i]) && (marking[j] != -1)) {
          result[i] = false;
          break;
        }

        // capacities
        if ((capacities[j] > 0) &&
            (marking[j] + CPlus[j][i] - CMinus[j][i] > capacities[j])) {
          // firing this transition would break a capacity restriction so
          // the transition is not enabled
          result[i] = false;
          break;
        }

        // inhibitor arcs
        if (inhibition[j][i] > 0 && marking[j] >= inhibition[j][i]) {
          // an inhibitor arc prevents the firing of this transition so
          // the transition is not enabled
          result[i] = false;
          break;
        }
      }


      // we look for the highest priority of the enabled transitions
      if (result[i] == true) {
        if (transArray[i].isTimed() == true) {
          hasTimed = true;
        }
        else {
          hasImmediate = true;
          if (transArray[i].getPriority() > maxPriority) {
            maxPriority = transArray[i].getPriority();
          }
        }
      }

    }

    // Now make sure that if any of the enabled transitions are immediate
    // transitions, only they can fire as this must then be a vanishing state.
    // - disable the immediate transitions with lower priority.
    // - disable all timed transitions if there is an immediate transition enabled.
    for (int i = 0; i < transitionCount; i++) {
      if (!transArray[i].isTimed() &&
          transArray[i].getPriority() < maxPriority) {
        result[i] = false;
      }
      if (hasTimed && hasImmediate) {
        if (transArray[i].isTimed() == true) {
          result[i] = false;
        }
      }
    }

    //print("getTransitionEnabledStatusArray: ",result);//debug
    return result;
  }


  /**
   * Empty all attributes, turn into empty Petri-Net
   */
  private void emptyPNML() {
    pnmlName = null;

    mPlacesMap.clear();
    mTransitionsMap.clear();
    mNormalArcsMap.clear();
    mInhibitorArcMap.clear();
    mBidirectionalArcMap.clear();
    mLabelsMap.clear();
    mMarkingParametersMap.clear();
    mRateParametersMap.clear();

    initialMarkingVector = null;
    forwardsIncidenceMatrix = null;
    backwardsIncidenceMatrix = null;
    incidenceMatrix = null;
    inhibitionMatrix = null;
    arcsMap = null;
    unknown = null;
    disabled = null;
    dependents = null;
    initializeMatrices();
  }


  /**
   * Get position of Petri-Net Object in ArrayList of given Petri-Net Object's type
   *
   * @param pnObject PlaceTransitionObject to get the position of
   * @return Position (-1 if not present) of Petri-Net Object in ArrayList of
   * given Petri-Net Object's type
   */
  public int getListPosition(PetriNetObject pnObject) {
    int index = 0;
    Collection<? extends PetriNetObject> objects = getObjectsHolderFor(pnObject).values();
    for (PetriNetObject object : objects) {
      if (object.getId().equals(pnObject.getId())) {
        break;
      }
      index++;
    }

    return index < objects.size() ? index : -1;
  }


  /**
   * Get a List of all the Place objects in the Petri-Net
   *
   * @return A List of all the Place objects
   */
  public Place[] getPlaces() {
    return mPlacesMap.values().toArray(new Place[mPlacesMap.size()]);
  }


  public int getPlacesCount() {
    return mPlacesMap.size();
  }
   
   
   /* wjk added 03/10/2007 */

  /**
   * Get the current marking of the Petri net
   *
   * @return The current marking of the Petri net
   */
  public int[] getMarking() {
    int[] result = new int[mPlacesMap.size()];

    int index = 0;
    for (Place place : mPlacesMap.values()) {
      result[index++] = place.getCurrentMarking();
    }
    return result;
  }


  /**
   * Get a List of all the net-level NameLabel objects in the Petri-Net
   *
   * @return A List of all the net-level (as opposed to element-specific) label objects
   */
  public AnnotationNote[] getLabels() {
    return mLabelsMap.values().toArray(new AnnotationNote[mLabelsMap.size()]);
  }


  /**
   * Get a List of all the marking Parameter objects in the Petri-Net
   *
   * @return A List of all the marking Parameter objects
   */
  public MarkingParameter[] getMarkingParameters() {
    return mMarkingParametersMap.values().toArray(new MarkingParameter[mMarkingParametersMap.size()]);
  }


  /**
   * Get a List of all the marking Parameter objects in the Petri-Net
   *
   * @return A List of all the marking Parameter objects
   */
  public RateParameter[] getRateParameters() {
    return mRateParametersMap.values().toArray(new RateParameter[mRateParametersMap.size()]);
  }


  /**
   * Get an List of all the Transition objects in the Petri-Net
   *
   * @return An List of all the Transition objects
   */
  public Transition[] getTransitions() {
    return mTransitionsMap.values().toArray(new Transition[mTransitionsMap.size()]);
  }


  public int getTransitionsCount() {
    return mTransitionsMap.size();
  }

  /**
   * Get an List of all the Arcs objects in the Petri-Net
   *
   * @return An List of all the Arc objects
   */
  public Arc[] getArcs() {
    return mNormalArcsMap.values().toArray(new NormalArc[mNormalArcsMap.size()]);
  }


  /**
   * Get an List of all the InhibitorArc objects in the Petri-Net
   *
   * @return An List of all the InhibitorArc objects
   */
  public InhibitorArc[] getInhibitors() {
    return mInhibitorArcMap.values().toArray(new InhibitorArc[mInhibitorArcMap.size()]);
  }
  
  /**
   * Get an List of all the BidirectionalArc objects in the Petri-Net
   *
   * @return An List of all the BidirectionalArc objects
   */
  public BidirectionalArc[] getBidirectionalArcs() {
    return mBidirectionalArcMap.values().toArray(new BidirectionalArc[mBidirectionalArcMap.size()]);
  }


  /**
   * Return the Transition called transitionName from the Petri-Net
   *
   * @param transitionID ID of Transition object to return
   * @return The first Transition object found with a name equal to transitionName
   */
  public Transition getTransitionById(String transitionID) {
    return mTransitionsMap.get(transitionID);
  }


  /**
   * Return the Transition called transitionName from the Petri-Net
   *
   * @param transitionName Name of Transition object to return
   * @return The first Transition object found with a name equal to transitionName
   */
  public Transition getTransitionByName(String transitionName) {
    return getPetriNetObjectByName(transitionName, Transition.class);
  }

  public <T extends PetriNetObject> T getPetriNetObjectByName(final String pObjectName, final Class<T> pObjectType) {
    return pObjectType.cast(mObjectsHolder.get(pObjectType.getName()).get(pObjectName));
  }


  /**
   * Return the Transition called transitionName from the Petri-Net
   *
   * @param transitionNo No of Transition object to return
   * @return The Transition object
   */
  public Transition getTransition(int transitionNo) {
    return getPetriNetObjectAtIndex(transitionNo, Transition.class);
  }

  private <T extends PetriNetObject> T getPetriNetObjectAtIndex(final int pIndex, final Class<T> pObjectType) {
    int index = 0;
    for (Object object : mObjectsHolder.get(pObjectType.getName()).values()) {
      if (index == pIndex) {
        return pObjectType.cast(object);
      }
      index++;
    }

    return null;
  }


  /**
   * Return the Place called placeName from the Petri-Net
   *
   * @param placeId ID of Place object to return
   * @return The first Place object found with id equal to placeId
   */
  public Place getPlaceById(String placeID) {
    return mPlacesMap.get(placeID);
  }


  /**
   * Return the Place called placeName from the Petri-Net
   *
   * @param placeName Name of Place object to return
   * @return The first Place object found with a name equal to placeName
   */
  public Place getPlaceByName(String placeName) {
    return getPetriNetObjectByName(placeName, Place.class);
  }


  /**
   * Return the Place called placeName from the Petri-Net
   *
   * @param placeNo No of Place object to return
   * @return The Place object
   */
  public Place getPlace(int placeNo) {
    return getPetriNetObjectAtIndex(placeNo, Place.class);
  }


  /**
   * Return the PlaceTransitionObject called ptoName from the Petri-Net
   *
   * @param ptoId Id of PlaceTransitionObject object to return
   * @return The first Arc PlaceTransitionObject found with a name equal to ptoName
   */
  public PlaceTransitionObject getPlaceTransitionObject(String ptoId) {
    PlaceTransitionObject object = getPlaceById(ptoId);
    if (object == null) {
      object = getTransitionById(ptoId);
    }
    return object;
  }


  /**
   * Return the Forward Incidence Matrix for the Petri-Net
   *
   * @return The Forward Incidence Matrix for the Petri-Net
   */
  public int[][] getForwardsIncidenceMatrix() {
    if (forwardsIncidenceMatrix == null
        || forwardsIncidenceMatrix.matrixChanged) {
      createForwardIncidenceMatrix();
    }
    return (forwardsIncidenceMatrix != null
        ? forwardsIncidenceMatrix.getArrayCopy()
        : null);
  }


  /**
   * Return the Backward Incidence Matrix for the Petri-Net
   *
   * @return The Backward Incidence Matrix for the Petri-Net
   */
  public int[][] getBackwardsIncidenceMatrix() {
    if (backwardsIncidenceMatrix == null
        || backwardsIncidenceMatrix.matrixChanged) {
      createBackwardsIncidenceMatrix();
    }
    return (backwardsIncidenceMatrix != null
        ? backwardsIncidenceMatrix.getArrayCopy()
        : null);
  }


  /**
   * Return the Incidence Matrix for the Petri-Net
   *
   * @return The Incidence Matrix for the Petri-Net
   */
  public int[][] getIncidenceMatrix() {
    if (incidenceMatrix == null || incidenceMatrix.matrixChanged) {
      createIncidenceMatrix();
    }
    return (incidenceMatrix != null ? incidenceMatrix.getArrayCopy() : null);
  }


  /**
   * Return the Incidence Matrix for the Petri-Net
   *
   * @return The Incidence Matrix for the Petri-Net
   */
  public int[][] getInhibitionMatrix() {
    if (inhibitionMatrix == null || inhibitionMatrix.matrixChanged) {
      createInhibitionMatrix();
    }
    return (inhibitionMatrix != null ? inhibitionMatrix.getArrayCopy() : null);
  }

  /**
   * Return the Initial Marking Vector for the Petri-Net
   *
   * @return The Initial Marking Vector for the Petri-Net
   */
  public int[] getInitialMarkingVector() {
    if (initialMarkingVectorChanged) {
      createInitialMarkingVector();
    }
    return initialMarkingVector;
  }


  /**
   * Return the Initial Marking Vector for the Petri-Net
   *
   * @return The Initial Marking Vector for the Petri-Net
   */
  public int[] getCurrentMarkingVector() {
    if (currentMarkingVectorChanged) {
      createCurrentMarkingVector();
    }
    return currentMarkingVector;
  }


  /**
   * Return the capacity Matrix for the Petri-Net
   *
   * @return The capacity Matrix for the Petri-Net
   */
  public int[] getCapacityVector() {
    createCapacityVector();
    return capacityVector;
  }


  /**
   * Return the capacity Matrix for the Petri-Net
   *
   * @return The capacity Matrix for the Petri-Net
   */
  public int[] getPriorityVector() {
    createPriorityVector();
    return priorityVector;
  }


  /**
   * Return the capacity Matrix for the Petri-Net
   *
   * @return The capacity Matrix for the Petri-Net
   */
  public boolean[] getTimedVector() {
    createTimedVector();
    return timedVector;
  }


  private void setMatrixChanged() {
    if (forwardsIncidenceMatrix != null) {
      forwardsIncidenceMatrix.matrixChanged = true;
    }
    if (backwardsIncidenceMatrix != null) {
      backwardsIncidenceMatrix.matrixChanged = true;
    }
    if (incidenceMatrix != null) {
      incidenceMatrix.matrixChanged = true;
    }
    if (inhibitionMatrix != null) {
      inhibitionMatrix.matrixChanged = true;
    }
    initialMarkingVectorChanged = true;
    currentMarkingVectorChanged = true;
  }


  /**
   * Create model from transformed PNML file
   *
   * @param filename URI location of PNML
   * @author Ben Kirby, 10 Feb 2007
   * @author Edwin Chung
   * This code is modified so that dataLayer objects can be created
   * outside the GUI
   */
  public void createFromPNML(Document PNMLDoc) {
    emptyPNML();
    try {
      if (CreateGui.getApp() != null) {
        // Notifies used to indicate new instances.
        CreateGui.getApp().setMode(Pipe.CREATING);
      }

      initializeDataLayerFromDOMElement(this, PNMLDoc.getDocumentElement());

      if (CreateGui.getApp() != null) {
        CreateGui.getApp().restoreMode();
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void initializeDataLayerFromDOMElement(final DataLayer pDataLayer, final Element pRootElement) {
    PTNConverterManager converterManager = PTNConverterManager.getInstance();
    NodeList nodeList = pRootElement.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node instanceof Element) {
        Element element = (Element) node;
        String name = element.getNodeName();
        PTNObjectConverter<? extends PetriNetObject> converter = converterManager.getConverterFor(name);
        if (converter == null) {
          if ("stategroup".equals(element.getNodeName())) {
            addStateGroup(createStateGroup(element), true);
          }
        }
        else {
          PetriNetObject ptnObject = converter.toPTNObject(element, pDataLayer);
          addPetriNetObject(ptnObject);
        }
      }
    }
  }

  public void createFromDataLayer(DataLayer dataLayer) {

    if (CreateGui.getApp() != null) {
      // Notifies used to indicate new instances.
      CreateGui.getApp().setMode(Pipe.CREATING);
    }
    AnnotationNote[] anArray = dataLayer.getLabels();
    for (int an = 0; an < anArray.length; an++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(anArray[an]);
    }

    MarkingParameter[] mpArray = dataLayer.getMarkingParameters();
    for (int mp = 0; mp < mpArray.length; mp++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(mpArray[mp]);
    }

    RateParameter[] rpArray = dataLayer.getRateParameters();
    for (int rp = 0; rp < rpArray.length; rp++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(rpArray[rp]);
    }

    Place[] pArray = dataLayer.getPlaces();
    for (int p = 0; p < pArray.length; p++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(pArray[p]);
    }

    Transition[] tArray = dataLayer.getTransitions();
    for (int t = 0; t < tArray.length; t++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(tArray[t]);
    }

    InhibitorArc[] iaArray = dataLayer.getInhibitors();
    for (int ia = 0; ia < iaArray.length; ia++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(iaArray[ia]);
    }

    Arc[] aArray = dataLayer.getArcs();
    for (int a = 0; a < aArray.length; a++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(aArray[a]);
    }
    
    BidirectionalArc[] baArray = dataLayer.getBidirectionalArcs();
    for (int ia = 0; ia < baArray.length; ia++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(baArray[ia]);
    }

    StateGroup[] sgArray = dataLayer.getStateGroups();
    for (int sg = 0; sg < sgArray.length; sg++) {
      setChanged();
      setMatrixChanged();
      notifyObservers(sgArray[sg]);
    }

    if (CreateGui.getApp() != null) {
      CreateGui.getApp().restoreMode();
    }
  }


  /**
   * Creates a StateGroup object from a DOM element
   *
   * @param inputStateGroupElement input state group DOM Element
   * @return StateGroup Object
   */
  private StateGroup createStateGroup(Element inputStateGroupElement) {
    // Create the state group with name and id
    String id = inputStateGroupElement.getAttribute("id");
    String name = inputStateGroupElement.getAttribute("name");
    StateGroup newGroup = new StateGroup(id, name);

    Node node = null;
    NodeList nodelist = null;
    StringTokenizer tokeniser;
    nodelist = inputStateGroupElement.getChildNodes();

    // If this state group contains states then add them
    if (nodelist.getLength() > 0) {
      for (int i = 1; i < nodelist.getLength() - 1; i++) {
        node = nodelist.item(i);
        if (node instanceof Element) {
          Element element = (Element) node;
          if ("statecondition".equals(element.getNodeName())) {
            // Loads the condition in the form "P0 > 4"
            String condition = element.getAttribute("value");
            // Now we tokenise the elements of the condition
            // (i.e. "P0" ">" "4") to create a state
            tokeniser = new StringTokenizer(condition);
            newGroup.addState(tokeniser.nextToken(), tokeniser.nextToken(), tokeniser.nextToken());
          }
        }
      }
    }
    return newGroup;
  }


  public StateGroup[] getStateGroups() {
    StateGroup[] returnArray = new StateGroup[stateGroups.size()];
    for (int i = 0; i < stateGroups.size(); i++) {
      returnArray[i] = (StateGroup) stateGroups.get(i);
    }
    return returnArray;
  }


  /**
   * Return a URI for the PNML file for the Petri-Net
   *
   * @return A DOM for the Petri-Net
   */
  public String getURI() {
    return pnmlName;
  }


  /**
   * prints out a brief representation of the dataLayer object
   */
  public void print() {
    System.out.println("No of Places = " + mPlacesMap.size() + "\"");
    System.out.println("No of Transitions = " + mTransitionsMap.size() + "\"");
    System.out.println("No of Arcs = " + mNormalArcsMap.size() + "\"");
    System.out.println("No of Labels = " + mLabelsMap.size() +
        "\" (Model View Controller Design Pattern)");
  }


  public boolean existsMarkingParameter(String name) {
    return mMarkingParametersMap.containsKey(name);
  }


  public boolean existsRateParameter(String name) {
    return mRateParametersMap.containsKey(name);
  }


  public boolean changeRateParameter(String oldName, String newName) {
    if (mRateParametersMap.containsKey(newName) || !mRateParametersMap.containsKey(oldName)) {
      return false;
    }
    RateParameter parameter = mRateParametersMap.remove(oldName);
    parameter.setName(newName);
    mRateParametersMap.put(newName, parameter);
    return true;
  }


  public boolean changeMarkingParameter(String oldName, String newName) {
    if (mMarkingParametersMap.containsKey(newName) || !mMarkingParametersMap.containsKey(oldName)) {
      return false;
    }
    MarkingParameter oldParameter = mMarkingParametersMap.remove(oldName);
    oldParameter.setId(newName);
    mMarkingParametersMap.put(newName, oldParameter);
    return true;
  }


  /**
   * See if the supplied net has any timed transitions.
   *
   * @param DataLayer
   * @return boolean
   * @author Matthew
   */
  public boolean hasTimedTransitions() {
    Transition[] transitions = this.getTransitions();
    int transCount = transitions.length;

    for (int i = 0; i < transCount; i++) {
      if (transitions[i].isTimed() == true) {
        return true;
      }
    }
    return false;
  }


  /**
   * See if the net has any timed transitions.
   *
   * @return boolean
   * @author Matthew
   */
  public boolean hasImmediateTransitions() {
    Transition[] transitions = this.getTransitions();
    int transCount = transitions.length;

    for (int i = 0; i < transCount; i++) {
      if (transitions[i].isTimed() == false) {
        return true;
      }
    }
    return false;
  }


  /**
   * Work out if a specified marking describes a tangible state.
   * A state is either tangible (all enabled transitions are timed)
   * or vanishing (there exists at least one enabled state that is transient,
   * i.e. untimed).
   * If an immediate transition exists, it will automatically fire before a
   * timed transition.
   *
   * @param DataLayer - the net to be tested
   * @param int[]     - the marking of the net to be tested
   * @return boolean  - is it tangible or not
   */
  public boolean isTangibleState(int[] marking) {
    Transition[] trans = this.getTransitions();
    int numTrans = trans.length;
    boolean hasTimed = false;
    boolean hasImmediate = false;

    for (int i = 0; i < numTrans; i++) {
      if (this.getTransitionEnabledStatus(marking, i) == true) {
        if (trans[i].isTimed() == true) {
          //If any immediate transtions exist, the state is vanishing
          //as they will fire immediately
          hasTimed = true;
        }
        else if (trans[i].isTimed() != true) {
          hasImmediate = true;
        }
      }
    }
    return (hasTimed == true && hasImmediate == false);
  }


  private void checkForInverseArc(NormalArc newArc) {
    Iterator iterator = newArc.getSource().getConnectToIterator();

    Arc anArc;
    while (iterator.hasNext()) {
      anArc = (Arc) iterator.next();
      if (anArc.getTarget() == newArc.getSource() &&
          anArc.getSource() == newArc.getTarget()) {
        if (anArc.getClass() == NormalArc.class) {
          if (!newArc.hasInverse()) {
            ((NormalArc) anArc).setInverse(newArc, Pipe.JOIN_ARCS);
          }
        }
      }
    }
  }


  public String getTransitionName(int i) {
    return getPetriNetObjectAtIndex(i, Transition.class).getName();
  }

  public Transition randomPickTransition(ArrayList tArray) {
    int numOfTrans = tArray.size();
    Random random = new Random(System.currentTimeMillis());//set seeds as timeMillis
    int pTransIdx = random.nextInt(numOfTrans);

    return (Transition) tArray.get(pTransIdx);
  }

  public Place randomPickPlace(ArrayList pArray) {
    int numOfPlacess = pArray.size();
    Random random = new Random(System.currentTimeMillis());//set seeds as timeMillis
    int pPlacesIdx = random.nextInt(numOfPlacess);

    return (Place) pArray.get(pPlacesIdx);
  }

  public void setPropertyFormula(String formula) {
    this.propertyFormula = formula;
  }

  public String getPropertyFormula() {
    return this.propertyFormula;
  }
//   public void fireSelectedTransition(Transition transition){
//	   
//		   transition.getToken(true);
//		   String formula = transition.getFormula();
//		   ErrorMsg errorMsg = new ErrorMsg(formula);	   
//		   Parse p = new Parse(formula, errorMsg);
//		   Sentence s = p.absyn;
//		   s.accept(new Interpreter(errorMsg, transition, 1));
//		   
//		   transition.sendToken();
//		   transition.getTransSymbolTable().cleanTable();
//
//   }

  /**
   * This method will merge the given data layer with this by merging the given placeTransitionObjectss.
   * @param pObject The place contained in this datalayer
   * @param pWith  The place contained in the given
   * @param pDataLayer The given datalayer to merge
   */
  public void merge(final PlaceTransitionObject pObject, final PlaceTransitionObject pWith, final DataLayer pDataLayer) {
    if (pObject.isMergeable(pWith)) {
      pWith.getArcOutList().forEach((final Arc arc) -> {
        arc.setSource(pObject);
      });
      pWith.getArcOutList().clear();

      pWith.getArcInList().forEach((final Arc arc) -> {
        arc.setTarget(pObject);
      });
      pWith.getArcInList().clear();

      if (pObject instanceof Place) {
        mergePlaces((Place) pObject, (Place)pWith);
      }
      else {
        mergeTransitions((Transition)pObject, (Transition)pWith);
      }

      pDataLayer.removePetriNetObject(pWith);
      mergeDataLayer(pDataLayer);
    }
  }

  private void mergeTransitions(final Transition pObject, final Transition pWith) {
    pObject.arcInList.clear();
    pObject.arcInVarList.clear();
    pObject.arcList.clear();
    pObject.arcOutList.clear();
    pObject.arcOutVarList.clear();

    pObject.formula = "";
  }

  private void mergeDataLayer(final DataLayer pDataLayer) {
    Iterator<PetriNetObject> objectIterator = pDataLayer.getPetriNetObjects();
    while (objectIterator.hasNext()) {
      addPetriNetObject(objectIterator.next());
    }
  }

  private void mergePlaces(final Place pPlace, final Place pWith) {
    List<DataType> filteredList = pWith.getGroup().stream().filter(dataType -> !pWith.getDataType().getName().equals(dataType.getName())).collect(Collectors.toList());
    pPlace.getGroup().addAll(filteredList);

    pPlace.getToken().listToken.clear();
  }

  /**
   * This method will merge this data layer with the given data layer by adding the given connector.
   * @param pConnector
   * @param pDataLayer
   */
  public void connect(final Arc pConnector, final DataLayer pDataLayer) {
    //form a disjoint set
    mergeDataLayer(pDataLayer);

    //add a  connection between them
    addPetriNetObject(pConnector);
  }

}
