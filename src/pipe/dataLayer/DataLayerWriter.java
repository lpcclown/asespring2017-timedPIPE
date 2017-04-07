package pipe.dataLayer;

//Collections
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pipe.dataLayer.converter.*;

/**
 * Create DataLayerWriter object
 * @param DataLayer object containing net to save
 * @author Ben Kirby
 * @author Pere Bonet (minor changes)
 */
public class DataLayerWriter {

   /** DataLayer object passed in to save */
   private DataLayer netModel;
   
   
   /** Create a writer with the DataLayer object to save*/
   public DataLayerWriter(DataLayer currentNet) {
      netModel = currentNet;
   }
   
   
   /**
    * Save the Petri-Net
    * @param filename URI location to save file
    * @throws ParserConfigurationException
    * @throws DOMException
    * @throws TransformerConfigurationException
    * @throws TransformerException
    */
   public void savePNML(File file) throws NullPointerException, IOException,
           ParserConfigurationException, DOMException, 
           TransformerConfigurationException, TransformerException {
      
      // Error checking
      if (file == null) {
         throw new NullPointerException("Null file in savePNML");
      }
      /*    
       System.out.println("=======================================");
       System.out.println("dataLayer SAVING FILE=\"" + file.getCanonicalPath() + "\"");
       System.out.println("=======================================");
       */
      Document pnDOM = null;

      StreamSource xsltSource = null;
      Transformer transformer = null;
      try {
         // Build a Petri Net XML Document
         DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = builderFactory.newDocumentBuilder();
         pnDOM = builder.newDocument();
         
         Element PNML = pnDOM.createElement("pnml"); // PNML Top Level Element
         pnDOM.appendChild(PNML);
         
         Attr pnmlAttr = pnDOM.createAttribute("xmlns"); // PNML "xmlns" Attribute
         pnmlAttr.setValue("http://www.informatik.hu-berlin.de/top/pnml/ptNetb");
         PNML.setAttributeNode(pnmlAttr);
         
         Element netElement = pnDOM.createElement("net"); // Net Element
         PNML.appendChild(netElement);
         Attr netAttrId = pnDOM.createAttribute("id"); // Net "id" Attribute
         netAttrId.setValue("Net-One");
         netElement.setAttributeNode(netAttrId);
         Attr netAttrType = pnDOM.createAttribute("type"); // Net "type" Attribute
         netAttrType.setValue("P/T net");
         netElement.setAttributeNode(netAttrType);

        convertNetModelToDocumentElement(netModel, netElement);
         //stateGroups = null;         
         
         pnDOM.normalize();
         xsltSource = new StreamSource(Thread.currentThread().
                 getContextClassLoader().getResourceAsStream("xslt/GeneratePNML.xsl"));
         
         transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
         // Write file and do XSLT transformation to generate correct PNML
         File outputObjectArrayList = file;//new File(filename); // Output for XSLT Transformation
         DOMSource source = new DOMSource(pnDOM);
         StreamResult result = new StreamResult(outputObjectArrayList);
         transformer.transform(source, result);
      } catch (ParserConfigurationException e) {
         // System.out.println("=====================================================================================");
         System.out.println("ParserConfigurationException thrown in savePNML() " +
                 ": dataLayerWriter Class : dataLayer Package: filename=\"" + 
                 file.getCanonicalPath() + "\" xslt=\"" + 
                 xsltSource.getSystemId() + "\" transformer=\"" + 
                 transformer.getURIResolver() + "\"");
         // System.out.println("=====================================================================================");
         // e.printStackTrace(System.err);
      } catch (DOMException e) {
         // System.out.println("=====================================================================");
         System.out.println("DOMException thrown in savePNML() " +
                 ": dataLayerWriter Class : dataLayer Package: filename=\"" +
                 file.getCanonicalPath() + "\" xslt=\"" +
                 xsltSource.getSystemId() + "\" transformer=\"" +
                 transformer.getURIResolver() + "\"");
         // System.out.println("=====================================================================");
         // e.printStackTrace(System.err);
      } catch (TransformerConfigurationException e) {
         // System.out.println("==========================================================================================");
         System.out.println("TransformerConfigurationException thrown in savePNML() " +
                 ": dataLayerWriter Class : dataLayer Package: filename=\"" + file.getCanonicalPath() + "\" xslt=\"" + xsltSource.getSystemId() + "\" transformer=\"" + transformer.getURIResolver() + "\"");
         // System.out.println("==========================================================================================");
         // e.printStackTrace(System.err);
      } catch (TransformerException e) {
         // System.out.println("=============================================================================");
         System.out.println("TransformerException thrown in savePNML() : dataLayerWriter Class : dataLayer Package: filename=\"" + file.getCanonicalPath() + "\" xslt=\"" + xsltSource.getSystemId() + "\" transformer=\"" + transformer.getURIResolver() + "\"" + e);
         // System.out.println("=============================================================================");
         // e.printStackTrace(System.err);
      }
   }

  private void convertNetModelToDocumentElement(final DataLayer pNetModel, final Element pNetElement) {
    Document pnDOM = pNetElement.getOwnerDocument();
    PTNConverterManager converterManager = PTNConverterManager.getInstance();
    AnnotationNoteConverter annotationConverter = converterManager.getAnnotationConverter();
    for (AnnotationNote label : pNetModel.getLabels()) {
      pNetElement.appendChild(annotationConverter.toElement(label, pnDOM));
    }

    ParameterConverter parameterConverter = converterManager.getParameterConverter();
    for (MarkingParameter parameter : pNetModel.getMarkingParameters()) {
      pNetElement.appendChild(parameterConverter.toElement(parameter, pnDOM));
    }

    for (RateParameter parameter : pNetModel.getRateParameters()) {
      pNetElement.appendChild(parameterConverter.toElement(parameter, pnDOM));
    }

    PlaceConverter placeConverter = converterManager.getPlaceConverter();
    for (Place place : pNetModel.getPlaces()) {
      pNetElement.appendChild(placeConverter.toElement(place, pnDOM));
    }

    TransitionConverter transitionConverter = converterManager.getTransitionConverter();
    for (Transition transition : pNetModel.getTransitions()) {
      pNetElement.appendChild(transitionConverter.toElement(transition, pnDOM));
    }

    ArcConverter arcConverter = converterManager.getArcConverter();
    for (Arc arc : pNetModel.getArcs()) {
      pNetElement.appendChild(arcConverter.toElement(arc, pnDOM));
    }

    for (InhibitorArc inhibitorArc : pNetModel.getInhibitors()) {
      pNetElement.appendChild(arcConverter.toElement(inhibitorArc, pnDOM));
    }
    
    for (BidirectionalArc bidirectionalArc : pNetModel.getBidirectionalArcs()) {
        pNetElement.appendChild(arcConverter.toElement(bidirectionalArc, pnDOM));
    }

    StateGroup[] stateGroups = pNetModel.getStateGroups();
    for(int i = 0; i< stateGroups.length; i++) {
      Element newStateGroup = createStateGroupElement(stateGroups[i], pnDOM);

      int numConditions = stateGroups[i].numElements();
      String[] conditions = stateGroups[i].getConditions();
      for(int j = 0; j<numConditions; j++) {
        newStateGroup.appendChild(createCondition(conditions[j], pnDOM));
      }
      pNetElement.appendChild(newStateGroup);
    }
  }


   private Element createAgentNetElement(DataLayer agentLayer, Document document){
	   Element agentNetElement = null;
	   
	   if(document != null){
		   agentNetElement = document.createElement("agentNet");
	   }
	   if(agentLayer != null){
		   convertNetModelToDocumentElement(agentLayer, agentNetElement);
	   }
	   return agentNetElement;
   }
   
   /**
    * Creates a State Group Element for a PNML Petri-Net DOM
    *
    * @param inputStateGroup Input State Group
    * @param document Any DOM to enable creation of Elements and Attributes
    * @return State Group Element for a PNML Petri-Net DOM
    * @author Barry Kearns, August 2007
    */
   private Element createStateGroupElement(StateGroup inputStateGroup, Document document){
      Element stateGroupElement = null;
      
      if(document != null) {
         stateGroupElement = document.createElement("stategroup");
      }
      
      if(inputStateGroup != null ) {
         String idInput = inputStateGroup.getId();
         String nameInput = inputStateGroup.getName();
         
         stateGroupElement.setAttribute("name", 
                 (nameInput != null ? nameInput 
                                    : (idInput != null && idInput.length() > 0? idInput : "")));
         stateGroupElement.setAttribute("id", (idInput != null ? idInput : "error"));
      }
      return stateGroupElement;
   }

   
   private Element createCondition(String condition, Document document) {
      Element stateCondition = null;
      
      if (document != null) { 
         stateCondition = document.createElement("statecondition");
      }
      
      stateCondition.setAttribute("condition", condition);
      return stateCondition;
   }
   
}
