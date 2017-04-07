package pipe.dataLayer.converter;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pipe.dataLayer.*;
import pipe.gui.Grid;

import java.util.Vector;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class PlaceConverter extends DefaultPTNObjectConverter<Place> {

  @Override
  public Element toElement(final Place pPlace, final Document pDocument) {
    Element placeElement = null;

    if (pDocument != null) {
      placeElement = pDocument.createElement("place");
    }

    if (pPlace != null && placeElement != null) {
      Double positionXInput = pPlace.getPositionXObject();
      Double positionYInput = pPlace.getPositionYObject();
      String idInput = pPlace.getId();
      String nameInput = pPlace.getName();
      Double nameOffsetXInput = pPlace.getNameOffsetXObject();
      Double nameOffsetYInput = pPlace.getNameOffsetYObject();
      Integer initialMarkingInput = pPlace.getCurrentMarkingObject();
      Double markingOffsetXInput = pPlace.getMarkingOffsetXObject();
      Double markingOffsetYInput = pPlace.getMarkingOffsetYObject();
      Integer capacityInput = pPlace.getCapacity();
      DataType datatype = pPlace.getDataType();
      abToken abtoken = pPlace.getToken();
      Vector<DataType> group = pPlace.getGroup();
      String markingParameter = "";
      if (pPlace.getMarkingParameter() != null) {
        markingParameter = pPlace.getMarkingParameter().getName();
      }

      placeElement.setAttribute("positionX", (positionXInput != null ? String.valueOf(positionXInput) : ""));
      placeElement.setAttribute("positionY", (positionYInput != null ? String.valueOf(positionYInput) : ""));
      placeElement.setAttribute("name", (nameInput != null ? nameInput : StringUtils.isNotBlank(idInput) ? idInput : ""));
      placeElement.setAttribute("id", (idInput != null ? idInput : "error"));
      placeElement.setAttribute("nameOffsetX", (nameOffsetXInput != null ? String.valueOf(nameOffsetXInput) : ""));
      placeElement.setAttribute("nameOffsetY", (nameOffsetYInput != null ? String.valueOf(nameOffsetYInput) : ""));
      placeElement.setAttribute("initialMarking", (initialMarkingInput != null ? String.valueOf(initialMarkingInput) : "0"));
      placeElement.setAttribute("markingOffsetX", (markingOffsetXInput != null ? String.valueOf(markingOffsetXInput) : ""));
      placeElement.setAttribute("markingOffsetY", (markingOffsetYInput != null ? String.valueOf(markingOffsetYInput) : ""));
      placeElement.setAttribute("capacity", (capacityInput != null ? String.valueOf(capacityInput) : ""));
      placeElement.setAttribute("parameter",
          (markingParameter != null ? markingParameter : ""));
      if (datatype != null) {
        placeElement.setAttribute("dt", datatype.getName());
        placeElement.setAttribute("ntype", Integer.toString(datatype.getNtype()));

        placeElement.setAttribute("types", datatype.getJoinedTypes());
        placeElement.setAttribute("ifpow", datatype.getPow() ? "T" : "F");
        placeElement.setAttribute("numofelement", Integer.toString(datatype.getNumofElement()));
        placeElement.setAttribute("ifdef", datatype.getDef() ? "T" : "F");

      }
      if (abtoken != null) {
        placeElement.setAttribute("isDef", abtoken.getDef() ? "T" : "F");
        if (abtoken.getDef()) {
          for (int i = 0; i < abtoken.getTokenCount(); i++) {
            Element e = pDocument.createElement("listToken");
            for (int j = 0; j < datatype.getNumofElement(); j++) {
              Element g = pDocument.createElement("Tlist");
              BasicType tempbt = abtoken.getTokenbyIndex(i).getBTbyindex(j);
              g.setAttribute("data", tempbt.getValueAsString());
              e.appendChild(g);
            }
            placeElement.appendChild(e);
          }
        }
      }

      if (group != null) {
        if (group.size() > 0) {
          for (int i = 0; i < group.size(); i++) {
            if (group.get(i) != null) {
              Element g = pDocument.createElement("group");
              g.setAttribute("group-dt", group.get(i).getName());
              g.setAttribute("group-ntype", Integer.toString(group.get(i).getNtype()));

              String types = "";
              for (int j = 0; j < group.get(i).getTypes().size(); j++) {

                types += group.get(i).getTypes().get(j);
                if (j < group.get(i).getTypes().size() - 1) {
                  types += ",";
                }
              }
              g.setAttribute("group-types", types);
              g.setAttribute("group-ifpow", group.get(i).getPow() ? "T" : "F");
              g.setAttribute("group-numofelement", Integer.toString(group.get(i).getNumofElement()));
              g.setAttribute("group-ifdef", group.get(i).getDef() ? "T" : "F");
              placeElement.appendChild(g);
            }
          }
        }
      }

    }
    return placeElement;
  }

  @Override
  public Place toPTNObject(final Element pElement, final DataLayer pDataLayer) {
    double positionXInput = Grid.getModifiedX(toDouble(pElement.getAttribute("positionX")));
    double positionYInput = Grid.getModifiedY(toDouble(pElement.getAttribute("positionY")));

    String idInput = pElement.getAttribute("id");
    String nameInput = pElement.getAttribute("name");

    double nameOffsetXInput = toDouble(pElement.getAttribute("nameOffsetX"));
    double nameOffsetYInput = toDouble(pElement.getAttribute("nameOffsetY"));
    int initialMarkingInput = toInt(pElement.getAttribute("initialMarking"));
    double markingOffsetXInput = toDouble(pElement.getAttribute("markingOffsetX"));
    double markingOffsetYInput = toDouble(pElement.getAttribute("markingOffsetY"));
    int capacityInput = toInt(pElement.getAttribute("capacity"));

    Place place = new Place(positionXInput, positionYInput,
        idInput,
        nameInput,
        nameOffsetXInput, nameOffsetYInput,
        initialMarkingInput,
        markingOffsetXInput, markingOffsetYInput,
        capacityInput);

    place.setGroup(new Vector<DataType>());
    DataType dataType = toDataType(pElement);
    String activeDataTypeName = "";
    if (dataType != null) {
      dataType.setGroup(place.getGroup());
      place.getGroup().add(dataType);
      place.setDataType(dataType);
      activeDataTypeName = dataType.getName();
    }

    NodeList nodelist = pElement.getChildNodes();
    for (int i = 0; i < nodelist.getLength(); i++) {
      if (nodelist.item(i) instanceof Element) {
        Element item = (Element) nodelist.item(i);
        if (item.getNodeName().equals("group-DataType")) {
          DataType groupDataType = toDataType(item);
          if (groupDataType != null && !activeDataTypeName.equals(groupDataType.getName())) {
            groupDataType.setGroup(place.getGroup());
            place.getGroup().add(groupDataType);
          }
        }
        else if (item.getNodeName().equals("token") && place.getDataType() != null) {
          BasicType[] bt = new BasicType[place.getDataType().getNumofElement()];
          int index = 0;
          NodeList tokenDataList = item.getChildNodes();
          for (int j = 0; j < tokenDataList.getLength(); j++) {
            Node tokenDataNode = tokenDataList.item(j);
            if (tokenDataNode instanceof Element && tokenDataNode.getNodeName().equals("data")) {
              Element token = (Element) tokenDataList.item(j);
              String tokenData = token.getAttribute("token-data");
              BasicType basicType = new BasicType(place.getDataType().getTypebyIndex(index), tokenData.trim());

              bt[index++] = basicType;
            }
          }
          place.addToken(bt);
        }
      }
    }

    if (place.getToken().getTokenCount() > capacityInput) {
      place.setCapacity(place.getToken().getTokenCount());
    }

    String parameterTempStorage = pElement.getAttribute("parameter");
    if (parameterTempStorage.length() > 0) {
      if (pDataLayer.existsMarkingParameter(parameterTempStorage)) {
        place.setMarkingParameter(pDataLayer.getPetriNetObjectByName(parameterTempStorage, MarkingParameter.class));
      }
    }

    return place;
  }

  private DataType toDataType(final Element pElement) {
    String dataTypeName = pElement.getAttribute("datatype-name");
    String dataTypeNType = pElement.getAttribute("datatype-Ntype");
    String dataTypeTypes = pElement.getAttribute("datatype-types");
    String dataTypeIsPow = pElement.getAttribute("datatype-ifPow");
    String dataTypeNumOfElement = pElement.getAttribute("datatype-NumofElement");
    String dataTypeIsDef = pElement.getAttribute("datatype-isDef");

    if (StringUtils.isNotBlank(dataTypeName) &&
        StringUtils.isNotBlank(dataTypeNType) &&
        StringUtils.isNotBlank(dataTypeTypes) &&
        StringUtils.isNotBlank(dataTypeIsPow) &&
        StringUtils.isNotBlank(dataTypeNumOfElement) &&
        StringUtils.isNotBlank(dataTypeIsDef)) {

      String[] t = dataTypeTypes.split(",");
      Vector<String> types = new Vector<>();
      for (int i = 0; i < t.length; i++) {
        if ("int".equals(t[i].trim())) {
          types.add(BasicType.TYPES[BasicType.NUMBER]);
        }
        else {
          types.add(t[i].trim());
        }
      }

      DataType dt = new DataType();
      dt.setName(dataTypeName);
      dt.setNtype(Integer.valueOf(dataTypeNType));
      dt.setTypes(types);
      dt.setPow("T".equals(dataTypeIsPow));
      dt.setDef("T".equals(dataTypeIsDef));
      dt.setNumofElement(Integer.valueOf(dataTypeNumOfElement));

      return dt;
    }

    return null;
  }


}
