package pipe.dataLayer.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.MarkingParameter;
import pipe.dataLayer.Parameter;
import pipe.dataLayer.RateParameter;
import pipe.dataLayer.calculations.Marking;

/**
 * Created by dalam004 on 10/6/2015.
 */
public class ParameterConverter extends DefaultPTNObjectConverter<Parameter> {

  private static final String DOC_ROOT = "definitions";
  private static final String ATTR_ID = "id";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_TYPE = "type";
  private static final String ATTR_DEF_TYPE = "defType";
  private static final String ATTR_EXPRESSION = "expression";
  private static final String ATTR_POS_X = "positionX";
  private static final String ATTR_POS_Y = "positionY";
  private static final String ATTR_WIDTH = "width";
  private static final String ATTR_HEIGHT = "height";
  private static final String ATTR_BORDER = "border";

  @Override
  public Element toElement(final Parameter pPTNObject, final Document pDocument) {
    return createDefinition(pPTNObject, pDocument);
  }

  private Element createDefinition(Parameter pParameter, Document document) {
    Element labelElement = null;

    if (document != null) {
      labelElement = document.createElement(DOC_ROOT);
    }

    if (pParameter != null ) {

      int positionXInput = pParameter.getOriginalX();//getX()
      int positionYInput = pParameter.getOriginalY();//getY()
      int widthInput = pParameter.getNoteWidth();
      int heightInput = pParameter.getNoteHeight();
      boolean borderInput = pParameter.isShowingBorder();

      String type = pParameter instanceof RateParameter ? "real" : "int";

      labelElement.setAttribute(ATTR_ID, pParameter.getId());
      labelElement.setAttribute(ATTR_NAME, pParameter.getId());
      labelElement.setAttribute(ATTR_TYPE, type);
      labelElement.setAttribute(ATTR_DEF_TYPE, type);
      labelElement.setAttribute(ATTR_EXPRESSION, String.valueOf(pParameter.getValue()));

      labelElement.setAttribute(ATTR_POS_X, (positionXInput >= 0.0 ? String.valueOf(positionXInput) : ""));
      labelElement.setAttribute(ATTR_POS_Y, (positionYInput >= 0.0 ? String.valueOf(positionYInput) : ""));
      labelElement.setAttribute(ATTR_WIDTH, (widthInput>=0.0? String.valueOf(widthInput):""));
      labelElement.setAttribute(ATTR_HEIGHT, (heightInput>=0.0? String.valueOf(heightInput):""));
      labelElement.setAttribute(ATTR_BORDER,String.valueOf(borderInput));
    }

    return labelElement;
  }

  @Override
  public Parameter toPTNObject(final Element pElement, final DataLayer pDataLayer) {
    int positionXInput = 0;
    int positionYInput = 0;

    String positionXTempStorage = pElement.getAttribute(ATTR_POS_X);
    String positionYTempStorage = pElement.getAttribute(ATTR_POS_Y);
    String nameTemp = pElement.getAttribute(ATTR_NAME);
    String expressionTemp = pElement.getAttribute(ATTR_EXPRESSION);

    if (positionXTempStorage.length() > 0) {
      positionXInput = Integer.valueOf(positionXTempStorage);
    }

    if (positionYTempStorage.length() > 0){
      positionYInput = Integer.valueOf(positionYTempStorage);
    }

    Parameter parameter;
    if ("int".equals(pElement.getAttribute(ATTR_TYPE))) {
      parameter = new RateParameter(nameTemp, Double.parseDouble(expressionTemp), positionXInput, positionYInput);
    } else {
      parameter = new MarkingParameter(nameTemp, Integer.parseInt(expressionTemp), positionXInput, positionYInput);
    }

    parameter.showBorder(Boolean.valueOf(pElement.getAttribute(ATTR_BORDER)));

    return parameter;
  }
}
