package pipe.dataLayer.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pipe.dataLayer.Arc;
import pipe.dataLayer.BidirectionalArc;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.DataType;
import pipe.dataLayer.InhibitorArc;
import pipe.dataLayer.NormalArc;
import pipe.dataLayer.Place;
import pipe.dataLayer.PlaceTransitionObject;
import pipe.gui.Pipe;

public class ArcConverter extends DefaultPTNObjectConverter<Arc> {

  private static final String BIDIRECTIONAL_TYPE = "bidirectional";

@Override
  public Arc toPTNObject(final Element pElement, final DataLayer pDataLayer) {
    return createArc(pElement, pDataLayer);
  }

  public Element toElement(final Arc pPTNObject, final Document pDocument) {
    return createArcElement(pPTNObject, pDocument);
  }

  private Arc createArc(final Element inputArcElement, final DataLayer pDataLayer) {
    String idInput = null;
    String sourceInput = null;
    String targetInput = null;
    int weightInput = 1;
    double inscriptionOffsetXInput = 0;
    double inscriptionOffsetYInput = 0;
    double startX = 0;
    double startY = 0;
    double endX = 0;
    double endY = 0;
    boolean taggedArc;

    sourceInput = inputArcElement.getAttribute("source");
    targetInput = inputArcElement.getAttribute("target");
    String idTempStorage = inputArcElement.getAttribute("id");
    String sourceTempStorage = inputArcElement.getAttribute("source");
    String targetTempStorage = inputArcElement.getAttribute("target");
    String inscriptionTempStorage = inputArcElement.getAttribute("inscription");
    String taggedTempStorage = inputArcElement.getAttribute("tagged");
    String var = inputArcElement.getAttribute("Var");
//	    String inscriptionOffsetXTempStorage = inputArcElement.getAttribute("inscriptionOffsetX");
//	    String inscriptionOffsetYTempStorage = inputArcElement.getAttribute("inscriptionOffsetY");

    taggedArc = !(taggedTempStorage.length() == 0 ||
        taggedTempStorage.length() == 5);

    if (idTempStorage.length() > 0) {
      idInput = idTempStorage;
    }
    if (sourceTempStorage.length() > 0) {
      sourceInput = sourceTempStorage;
    }
    if (targetTempStorage.length() > 0) {
      targetInput = targetTempStorage;
    }
    if (inscriptionTempStorage.length() > 0) {
      weightInput = Integer.parseInt(
          (inputArcElement.getAttribute("inscription") != null
              ? inputArcElement.getAttribute("inscription")
              : "1"));
    }

    if (sourceInput.length() > 0) {
      if (pDataLayer.getPlaceTransitionObject(sourceInput) != null) {
//	        System.out.println("PNMLDATA: sourceInput is not null");
        startX = pDataLayer.getPlaceTransitionObject(sourceInput).getPositionX();
        startX += pDataLayer.getPlaceTransitionObject(sourceInput).centreOffsetLeft();
        startY = pDataLayer.getPlaceTransitionObject(sourceInput).getPositionY();
        startY += pDataLayer.getPlaceTransitionObject(sourceInput).centreOffsetTop();
      }
    }
    if (targetInput.length() > 0) {
      if (pDataLayer.getPlaceTransitionObject(targetInput) != null) {
//	        System.out.println("PNMLDATA: targetInput is not null");
        endX = pDataLayer.getPlaceTransitionObject(targetInput).getPositionX();
        endY = pDataLayer.getPlaceTransitionObject(targetInput).getPositionY();
      }
    }

    PlaceTransitionObject sourceIn = pDataLayer.getPlaceTransitionObject(sourceInput);
    PlaceTransitionObject targetIn = pDataLayer.getPlaceTransitionObject(targetInput);

    // add the insets and offset
    int aStartx = sourceIn.getX() + sourceIn.centreOffsetLeft();
    int aStarty = sourceIn.getY() + sourceIn.centreOffsetTop();

    int aEndx = targetIn.getX() + targetIn.centreOffsetLeft();
    int aEndy = targetIn.getY() + targetIn.centreOffsetTop();


    double _startx = aStartx;
    double _starty = aStarty;
    double _endx = aEndx;
    double _endy = aEndy;
    //TODO

    Arc tempArc;

    String type = "normal";  // default value
    NodeList nl = inputArcElement.getElementsByTagName("type");
    if (nl.getLength() > 0) {
      type = ((Element) (nl.item(0))).getAttribute("type");
    }

    if (type.equals("inhibitor")) {
      tempArc = new InhibitorArc(_startx, _starty,
          _endx, _endy,
          sourceIn,
          targetIn,
          weightInput,
          idInput);
    } else if(type.equals(BIDIRECTIONAL_TYPE)) {
        tempArc = new BidirectionalArc(_startx, _starty,
                _endx, _endy,
                sourceIn,
                targetIn,
                weightInput,
                idInput,
                taggedArc);
    	
    } else {
      tempArc = new NormalArc(_startx, _starty,
          _endx, _endy,
          sourceIn,
          targetIn,
          weightInput,
          idInput,
          taggedArc);
    }


    tempArc.setVar(var);
    tempArc.setVar();
    pDataLayer.getPlaceTransitionObject(sourceInput).addConnectFrom(tempArc);
    pDataLayer.getPlaceTransitionObject(targetInput).addConnectTo(tempArc);

    //**********************************************************************************
    //The following section attempts to load and display arcpath details****************

    //NodeList nodelist = inputArcElement.getChildNodes();
    NodeList nodelist = inputArcElement.getElementsByTagName("arcpath");
    if (nodelist.getLength() > 0) {
      tempArc.getArcPath().purgePathPoints();
      for (int i = 0; i < nodelist.getLength(); i++) {
        Node node = nodelist.item(i);
        if (node instanceof Element) {
          Element element = (Element) node;
          if ("arcpath".equals(element.getNodeName())) {
            String arcTempX = element.getAttribute("x");
            String arcTempY = element.getAttribute("y");
            String arcTempType = element.getAttribute("arcPointType");
            float arcPointX = Float.valueOf(arcTempX).floatValue();
            float arcPointY = Float.valueOf(arcTempY).floatValue();
            arcPointX += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
            arcPointY += Pipe.ARC_CONTROL_POINT_CONSTANT + 1;
            boolean arcPointType =
                Boolean.valueOf(arcTempType).booleanValue();
            tempArc.getArcPath().addPoint(arcPointX, arcPointY, arcPointType);
          }
        }
      }
    }

    //Arc path creation ends here***************************************************************
    //******************************************************************************************
    return tempArc;
  }


  private Element createArcElement(Arc inputArc, Document document) {
    Element arcElement = null;

    if (document != null) {
      arcElement = document.createElement("arc");
    }

    if (inputArc != null) {
      Integer attrValue = null;
      double positionXInputD = (int) inputArc.getStartPositionX();
      Double positionXInput = new Double(positionXInputD);
      double positionYInputD = (int) inputArc.getStartPositionY();
      Double positionYInput = new Double(positionXInputD);
      String idInput = inputArc.getId();
      String sourceInput = inputArc.getSource().getId();
      String targetInput = inputArc.getTarget().getId();
      String var = inputArc.getVar();
      DataType datatype = inputArc.getDataType();

      int inscriptionInput = (inputArc != null ? inputArc.getWeight() : 1);
      // Double inscriptionPositionXInput = inputArc.getInscriptionOffsetXObject();
      // Double inscriptionPositionYInput = inputArc.getInscriptionOffsetYObject();
      arcElement.setAttribute("id", (idInput != null ? idInput : "error"));
      arcElement.setAttribute("source", (sourceInput != null ? sourceInput : ""));
      arcElement.setAttribute("target", (targetInput != null ? targetInput : ""));
      arcElement.setAttribute("type", inputArc.getType());
      arcElement.setAttribute("inscription", Integer.toString(inscriptionInput));
      arcElement.setAttribute("var", var);

      if (datatype != null) {
        arcElement.setAttribute("dt", datatype.getName());
        arcElement.setAttribute("ntype", Integer.toString(datatype.getNtype()));

        String types = "";
        for (int i = 0; i < datatype.getTypes().size(); i++) {
          types += datatype.getTypes().get(i);
          if (i < datatype.getTypes().size() - 1) {
            types += ",";
          }
        }
        arcElement.setAttribute("types", types);
        arcElement.setAttribute("ifpow", datatype.getPow() ? "T" : "F");
        arcElement.setAttribute("numofelement", Integer.toString(datatype.getNumofElement()));
        arcElement.setAttribute("ifdef", datatype.getDef() ? "T" : "F");

      }
      // arcElement.setAttribute("inscriptionOffsetX", (inscriptionPositionXInput != null ? String.valueOf(inscriptionPositionXInput) : ""));
      // arcElement.setAttribute("inscriptionOffsetY", (inscriptionPositionYInput != null ? String.valueOf(inscriptionPositionYInput) : ""));

      if (inputArc instanceof NormalArc) {
        boolean tagged = ((NormalArc) inputArc).isTagged();
        arcElement.setAttribute("tagged", tagged ? "true" : "false");
      }

      int arcPoints = inputArc.getArcPath().getArcPathDetails().length;
      String[][] point = inputArc.getArcPath().getArcPathDetails();
      for (int j = 0; j < arcPoints; j++) {
        arcElement.appendChild(createArcPoint(point[j][0], point[j][1], point[j][2], document, j));
      }
    }
    return arcElement;
  }

  private Element createArcPoint(String x, String y, String type, Document document, int id) {
    Element arcPoint = null;

    if (document != null) {
      arcPoint = document.createElement("arcpath");
    }
    String pointId = String.valueOf(id);
    if (pointId.length() < 3) {
      pointId = "0" + pointId;
    }
    if (pointId.length() < 3) {
      pointId = "0" + pointId;
    }
    arcPoint.setAttribute("id", pointId);
    arcPoint.setAttribute("xCoord", x);
    arcPoint.setAttribute("yCoord", y);
    arcPoint.setAttribute("arcPointType", type);

    return arcPoint;
  }
}
