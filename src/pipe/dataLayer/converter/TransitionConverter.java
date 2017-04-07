package pipe.dataLayer.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.RateParameter;
import pipe.dataLayer.Transition;
import pipe.gui.Grid;
import pipe.gui.Pipe;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class TransitionConverter extends DefaultPTNObjectConverter<Transition> {
    @Override
    public Element toElement(Transition pTransition, Document pDocument) {
        return createTransitionElement(pTransition, pDocument);
    }

    @Override
    public Transition toPTNObject(final Element pElement, final DataLayer pDataLayer) {
        return createTransition(pElement, pDataLayer);
    }

    private Transition createTransition(final Element element, final DataLayer pDataLayer){
        double positionXInput = 0;
        double positionYInput = 0;
        String idInput = null;
        String nameInput = null;
        double nameOffsetYInput = 0;
        double nameOffsetXInput = 0;
        double rate = 1.0;
        boolean timedTransition;
        boolean infiniteServer;
        int angle = 0;
        int priority = 1;
        double weight = 1.0;

        String positionXTempStorage = element.getAttribute("positionX");
        String positionYTempStorage = element.getAttribute("positionY");
        String idTempStorage = element.getAttribute("id");
        String nameTempStorage = element.getAttribute("name");
        String nameOffsetXTempStorage = element.getAttribute("nameOffsetX");
        String nameOffsetYTempStorage = element.getAttribute("nameOffsetY");
        String nameRate = element.getAttribute("rate");
        String nameTimed = element.getAttribute("timed");
        String nameInfiniteServer = element.getAttribute("infiniteServer");
        String nameAngle = element.getAttribute("angle");
        String namePriority = element.getAttribute("priority");
        //String nameWeight = element.getAttribute("weight");
        String parameterTempStorage = element.getAttribute("parameter");
        String formula = element.getAttribute("formula");

      /* wjk - a useful little routine to display all attributes of a transition
       for (int i=0; ; i++) {
          Object obj = inputTransitionElement.getAttributes().item(i);
          if (obj == null) {
             break;
          }
          System.out.println("Attribute " + i + " = " + obj.toString());
       }
       */

      timedTransition = nameTimed.length() != 0 && nameTimed.length() != 5;

        infiniteServer = !(nameInfiniteServer.length() == 0 ||
                nameInfiniteServer.length() == 5);

        if (positionXTempStorage.length() > 0) {
            positionXInput = Double.valueOf(positionXTempStorage).doubleValue() *
                    (false ? Pipe.DISPLAY_SCALE_FACTORX : 1) +
                    (false ? Pipe.DISPLAY_SHIFT_FACTORX : 1);
        }
        if (positionYTempStorage.length() > 0) {
            positionYInput = Double.valueOf(positionYTempStorage).doubleValue() *
                    (false ? Pipe.DISPLAY_SCALE_FACTORY : 1) +
                    (false ? Pipe.DISPLAY_SHIFT_FACTORY : 1);
        }

        positionXInput = Grid.getModifiedX(positionXInput);
        positionYInput = Grid.getModifiedY(positionYInput);

        if (idTempStorage.length() > 0) {
            idInput = idTempStorage;
        } else if (nameTempStorage.length() > 0) {
            idInput = nameTempStorage;
        }

        if (nameTempStorage.length() > 0) {
            nameInput = nameTempStorage;
        } else if (idTempStorage.length() > 0) {
            nameInput = idTempStorage;
        }

        if (nameOffsetXTempStorage.length() > 0) {
            nameOffsetXInput = Double.valueOf(nameOffsetXTempStorage).doubleValue();
        }

        if (nameOffsetYTempStorage.length() > 0) {
            nameOffsetYInput = Double.valueOf(nameOffsetYTempStorage).doubleValue();
        }

        if (nameRate.length()== 0) {
            nameRate = "1.0";
        }
        if (nameRate != "1.0") {
            rate = Double.valueOf(nameRate).doubleValue();
        } else {
            rate = 1.0;
        }

        if (nameAngle.length() > 0) {
            angle = Integer.valueOf(nameAngle).intValue();
        }

        if (namePriority.length() > 0) {
            priority = Integer.valueOf(namePriority).intValue();
        }

        Transition transition =
                new Transition(positionXInput, positionYInput,
                        idInput,
                        nameInput,
                        nameOffsetXInput, nameOffsetYInput,
                        rate,
                        timedTransition,
                        infiniteServer,
                        angle,
                        priority,
                        formula);

        if (parameterTempStorage.length() > 0) {
          //TODO:: find a way to resolve the inclusion of parameters

            if (pDataLayer.existsRateParameter(parameterTempStorage)) {
              transition.setRateParameter(pDataLayer.getPetriNetObjectByName(parameterTempStorage, RateParameter.class));
            }
        }

        return transition;
    }

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

      String formula = (inputTransition.getFormula()==null) ? "" : inputTransition.getFormula();

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
      transitionElement.setAttribute("formula", formula);
    }
    return transitionElement;
  }
}
