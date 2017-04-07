package pipe.dataLayer.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import pipe.dataLayer.AnnotationNote;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Note;
import pipe.dataLayer.Transition;

public class AnnotationNoteConverter extends DefaultPTNObjectConverter <AnnotationNote> {
	  private final int DISPLAY_SCALE_FACTORX = 7; // Scale factors for loading other Petri-Nets (not yet implemented)
	   /** Y-Axis Scale Value */
	   private final int DISPLAY_SCALE_FACTORY = 7;
	   
	   private final int DISPLAY_SHIFT_FACTORX = 270; // Scale factors for loading other Petri-Nets (not yet implemented)
	   /** Y-Axis Shift Value */
	   private final int DISPLAY_SHIFT_FACTORY = 120; // Scale factors for loading other Petri-Nets (not yet implemented)
	   
	@Override
	public AnnotationNote toPTNObject(final Element pElement, final DataLayer pDataLayer) {
		return createAnnotation (pElement);
	}
	
	public Element toElement(final AnnotationNote pPTNObject, final Document pDocument) {
		return createAnnotationNoteElement (pPTNObject,pDocument) ;
	}
	
	 private AnnotationNote createAnnotation (Element inputLabelElement) {
	      int positionXInput = 0;
	      int positionYInput = 0;
	      int widthInput = 0;
	      int heightInput = 0;
	      String text = null;
	      boolean borderInput = true;

	      String positionXTempStorage = inputLabelElement.getAttribute("xPosition");
	      String positionYTempStorage = inputLabelElement.getAttribute("yPosition");
	      String widthTemp = inputLabelElement.getAttribute("w");
	      String heightTemp = inputLabelElement.getAttribute("h");
	      String textTempStorage = inputLabelElement.getAttribute("txt");
	      String borderTemp = inputLabelElement.getAttribute("border");
	      String nameTemp = inputLabelElement.getAttribute("name");
	      
	      if (positionXTempStorage.length() > 0) {
	         positionXInput = Integer.valueOf(positionXTempStorage).intValue() *
	                  (false ? DISPLAY_SCALE_FACTORX : 1) +
	                  (false ? DISPLAY_SHIFT_FACTORX : 1);
	      }
	      
	      if (positionYTempStorage.length() > 0){
	         positionYInput = Integer.valueOf(positionYTempStorage).intValue() *
	                  (false ? DISPLAY_SCALE_FACTORX : 1) +
	                  (false ? DISPLAY_SHIFT_FACTORX : 1);
	      }
	      
	      if (widthTemp.length() > 0) {
	         widthInput = Integer.valueOf(widthTemp).intValue() *
	                  (false ? DISPLAY_SCALE_FACTORY : 1) +
	                  (false ? DISPLAY_SHIFT_FACTORY : 1);
	      }
	      
	      if (heightTemp.length() > 0) {
	         heightInput = Integer.valueOf(heightTemp).intValue() *
	                  (false ? DISPLAY_SCALE_FACTORY : 1) +
	                  (false ? DISPLAY_SHIFT_FACTORY : 1);
	      }
	      
	      if (borderTemp.length()>0) {
	         borderInput = Boolean.valueOf(borderTemp).booleanValue();
	      } else {
	         borderInput = true;
	      }
	      
	      if (textTempStorage.length() > 0) {
	         text = textTempStorage;
	      } else {
	         text = "";
	      }
	 
	      return new AnnotationNote(text, positionXInput, positionYInput, 
	              widthInput, heightInput, borderInput);
	   } 
	 
	 private Element createAnnotationNoteElement(AnnotationNote inputLabel, Document document) {
	      Element labelElement = null;
	      
	      if (document != null) {
	         labelElement = document.createElement("labels");
	      }

	      if (inputLabel != null ) {
	         int positionXInput = inputLabel.getOriginalX();
	         int positionYInput = inputLabel.getOriginalY();
	         int widthInput = inputLabel.getNoteWidth();
	         int heightInput = inputLabel.getNoteHeight();
	         String nameInput = inputLabel.getNoteText();
	         boolean borderInput = inputLabel.isShowingBorder();
	         
	         labelElement.setAttribute("positionX", 
	                 (positionXInput >= 0.0 ? String.valueOf(positionXInput) : ""));
	         labelElement.setAttribute("positionY", 
	                 (positionYInput >= 0.0 ? String.valueOf(positionYInput) : ""));
	         labelElement.setAttribute("width", 
	                 (widthInput>=0.0? String.valueOf(widthInput):""));
	         labelElement.setAttribute("height", 
	                 (heightInput>=0.0? String.valueOf(heightInput):""));
	         labelElement.setAttribute("border",String.valueOf(borderInput));
	         labelElement.setAttribute("text", (nameInput != null ? nameInput : ""));
	      }
	      return labelElement;
	   }

	   
	   /**
	    * Creates a Transition Element for a PNML Petri-Net DOM
	    * @param inputTransition Input Transition
	    * @param document Any DOM to enable creation of Elements and Attributes
	    * @return Transition Element for a PNML Petri-Net DOM
	    */
	   private Element createTransitionElement(Transition inputTransition,
	           Document document) {
	      Element transitionElement = null;
	      
	      if (document != null) {
	         transitionElement = document.createElement("transition");
	      }
	      
	      if (inputTransition != null ) {
	         Integer attrValue = null;
	         Double positionXInput = inputTransition.getPositionXObject();
	         Double positionYInput = inputTransition.getPositionYObject();
	         Double nameOffsetXInput = inputTransition.getNameOffsetXObject();
	         Double nameOffsetYInput = inputTransition.getNameOffsetYObject();
	         String idInput = inputTransition.getId();
	         String nameInput = inputTransition.getName();
	         double aRate = inputTransition.getRate();
	         boolean timedTrans = inputTransition.isTimed();
	         boolean infiniteServer = inputTransition.isInfiniteServer();
	         int orientation = inputTransition.getAngle();
	         int priority = inputTransition.getPriority();
	         String rateParameter = "";
	         if (inputTransition.getRateParameter() != null) {
	            rateParameter = inputTransition.getRateParameter().getName();
	         }
	         
	         String formula = inputTransition.getFormula();
	         
	         transitionElement.setAttribute("positionX", 
	                 (positionXInput != null ? String.valueOf(positionXInput) : ""));
	         transitionElement.setAttribute("positionY", 
	                 (positionYInput != null ? String.valueOf(positionYInput) : ""));
	         transitionElement.setAttribute("nameOffsetX", 
	                 (nameOffsetXInput != null ? String.valueOf(nameOffsetXInput) : ""));
	         transitionElement.setAttribute("nameOffsetY", 
	                 (nameOffsetYInput != null ? String.valueOf(nameOffsetYInput) : ""));
	         transitionElement.setAttribute("name", 
	                 (nameInput != null ? nameInput : (idInput != null && idInput.length() > 0? idInput : "")));
	         transitionElement.setAttribute("id", 
	                 (idInput != null ? idInput : "error"));
	         transitionElement.setAttribute("rate", 
	                 (aRate != 1 ? String.valueOf(aRate):"1.0"));
	         transitionElement.setAttribute("timed", String.valueOf(timedTrans));
	         transitionElement.setAttribute("infiniteServer", 
	                 String.valueOf(infiniteServer));
	         transitionElement.setAttribute("angle", String.valueOf(orientation));
	         transitionElement.setAttribute("priority", String.valueOf(priority));
	         transitionElement.setAttribute("parameter", 
	                 (rateParameter != null ? rateParameter : ""));
	         transitionElement.setAttribute("formula", (String.valueOf(formula)));
	      }
	      return transitionElement;
	   }

}
