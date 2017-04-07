package analysis;

import formula.absyntree.Sentence;
import formula.parser.ErrorMsg;
import formula.parser.Formula2Promela;
import formula.parser.Parse;
import pipe.dataLayer.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

//import formulaParser.Printer;

/**
 * Get String output as Promela, from a DataLayer of Petri net
 *
 * @author Zhuo Sun, 2010
 * @modified by Su Liu 2012
 * @modified by Xudong He 2015
 */

public class myPromela {

  private static final String BOUND_TEMPLATE = "#define Bound_%s %d%n";

  public DataLayer dataLayer;
  public String propertyFormula = "";
  public String sPromela = "";

  public myPromela(DataLayer data, String formula) {
    dataLayer = data;
    propertyFormula = formula;
    //Promela definition
    defineBound();
    definePlaceDataStructure();
    definePlaceChan();
    //defineNonDetPickFunc();   12/17/15

    //define transition functions
    defineIsEnabledFunc();
    defineFireFunc();
    defineTransFunc();

    //define process
    defineMainProcess();
    defineInitFunc();

    //define property formula
    defineFormula();
  }

  private void defineBound() {
    for (Place place : dataLayer.getPlaces()) {
      sPromela += String.format(BOUND_TEMPLATE, place.getName(), place.getCapacity());
    }
    sPromela += "\n";
  }

  private void definePlaceDataStructure() {
    int placeSize = dataLayer.getPlacesCount();
    Place[] places = dataLayer.getPlaces();

    String placeName;
    for (int placeNo = 0; placeNo < placeSize; placeNo++) {
      placeName = places[placeNo].getName();
      sPromela += "typedef " + "type_" + placeName + " " + "{" + "\n";

      Vector<String> types = places[placeNo].getDataType().getTypes();

      for (int j = 0; j < types.size(); j++) {
        sPromela += "  ";
        sPromela += getMappedType(types.get(j));
        sPromela += " " + placeName + "_field" + Integer.toString(j + 1);

        if ((j + 1) != types.size()) sPromela += ";\n";
      }

      sPromela += "\n};\n\n";
    }
  }

  public String getMappedType(String pType) {
    if (BasicType.TYPES[BasicType.NUMBER].equals(pType)) {
      return "int";
    }
    else if (BasicType.TYPES[BasicType.STRING].equals(pType)) {
      return "short";
    }

    return pType;
  }

  private void definePlaceChan() {
    int placeSize = dataLayer.getPlacesCount();
    Place[] places = dataLayer.getPlaces();

    String placeName;
    for (int placeNo = 0; placeNo < placeSize; placeNo++) {
      placeName = places[placeNo].getName();
      sPromela += "chan place_" + placeName + " = [Bound_" + placeName
          + "] of {" + "type_" + placeName + "};\n";
    }

    sPromela += "\n";
  }

  /*	private void defineNonDetPickFunc(){
      sPromela +="inline pick(var, place_chan, msg){\n";
      sPromela +="	var = 1;\n";
      sPromela +="	select(var:1..len(place_chan));\n";
      sPromela +="	do\n";
      sPromela +="	::(var > 1) -> place_chan?msg; place_chan!msg; var--\n";
      sPromela +="	::(var == 1) -> break\n";
      sPromela +="	od\n";
      sPromela +="}\n";
    }
  */    //not useful 12/17/15
  //Modified by He 12/17/15
  private void defineIsEnabledFunc() {
    int transSize = dataLayer.getTransitionsCount();
    Transition[] trans = dataLayer.getTransitions();
    String transName;

    for (int transNo = 0; transNo < transSize; transNo++) {
      transName = trans[transNo].getName();
      sPromela += "inline is_enabled_" + transName + "() {\n";
      String else_temp = "skip";

      //declare local variables for input token
      ArrayList<Place> inputPlaces = trans[transNo].getPlaceInList();
      LinkedList<Arc> inputArcs = trans[transNo].getArcInList();

      for (Arc arc : inputArcs) {
        String placeName;
      }

      for (int ipNo = 0; ipNo < inputPlaces.size(); ipNo++) {
        String placeName;
        placeName = inputPlaces.get(ipNo).getName();

        Vector<String> types = inputPlaces.get(ipNo).getDataType().getTypes();
        for (int j = 0; j < types.size(); j++) {
          String stype = getMappedType(types.get(j));
          sPromela += "  " + stype + " " + placeName + "_field" + Integer.toString(j + 1);
          sPromela += ";\n";
        }

        // test input token
        if (!inputPlaces.get(ipNo).getToken().getDataType().getPow()) {
          sPromela += "  place_" + placeName + "?<";
        } else sPromela += "  place_" + placeName + "??<";

        for (int j = 0; j < types.size(); j++) {
          sPromela += placeName + "_field" + Integer.toString(j + 1);
          if ((j + 1) != types.size()) sPromela += ",";
        }
        sPromela += ">";
      }

      sPromela += ";\n";
      sPromela += "  if\n";
      sPromela += "  :: ";

      //precondition
      String formula = trans[transNo].getFormula();
      ErrorMsg errorMsg = new ErrorMsg(formula);
      Parse p = new Parse(formula, errorMsg);
      Sentence s = p.absyn;
      System.out.println(trans[transNo].getName());
      s.accept(new Formula2Promela(errorMsg, trans[transNo], 0));
//			s.accept(new Printer());

      if (!("").equals(s.strPre)) {
        sPromela += s.strPre + "\n";
        sPromela += "	->" + transName + "_is_enabled = true\n";
      } else {
        sPromela += "true ->" + transName + "_is_enabled = true\n";
      }

      sPromela += "  :: else -> " + else_temp + "\n";
      sPromela += "  fi\n";
      sPromela += "}\n";
    }
  }

  private void defineFireFunc() {
    int transSize = dataLayer.getTransitionsCount();
    Transition[] trans = dataLayer.getTransitions();
    String transName;
    for (int transNo = 0; transNo < transSize; transNo++) {
      transName = trans[transNo].getName();
      sPromela += "inline fire_" + transName + "() {\n";

      ArrayList<Place> inputPlaces = trans[transNo].getPlaceInList();
      for (int ipNo = 0; ipNo < inputPlaces.size(); ipNo++) {
        String placeName;
        placeName = inputPlaces.get(ipNo).getName();
        Vector<String> types = inputPlaces.get(ipNo).getDataType().getTypes();
        // remove input token
        if (!inputPlaces.get(ipNo).getToken().getDataType().getPow()) {
          sPromela += "  place_" + placeName + "?";
        } else sPromela += "  place_" + placeName + "??";

        for (int j = 0; j < types.size(); j++) {
          sPromela += placeName + "_field" + Integer.toString(j + 1);
          if ((j + 1) != types.size()) sPromela += ",";
        }
      }
      sPromela += "; \n";

      //post condition
      String formula = trans[transNo].getFormula();
      ErrorMsg errorMsg = new ErrorMsg(formula);
      Parse p = new Parse(formula, errorMsg);
      Sentence s = p.absyn;
//			System.out.println(trans[transNo].getName());
      s.accept(new Formula2Promela(errorMsg, trans[transNo], 0));
      sPromela += s.strPost;

      ArrayList<Place> otPlaces = trans[transNo].getPlaceOutList();
      for (int opNo = 0; opNo < otPlaces.size(); opNo++) {
        String otPlaceName = otPlaces.get(opNo).getName();
        if (!otPlaces.get(opNo).getToken().getDataType().getPow()) {
          sPromela += "  place_" + otPlaceName + "!" + otPlaceName + ";\n";
        }
      }
      sPromela += "  " + transName + "_is_enabled = false\n";
      sPromela += "}\n";
    }

  }

  private void defineTransFunc() {
    int transSize = dataLayer.getTransitionsCount();
    Transition[] trans = dataLayer.getTransitions();
    String transName;
    for (int transNo = 0; transNo < transSize; transNo++) {
      transName = trans[transNo].getName();
      sPromela += "inline " + transName + "() {\n";
      sPromela += "  is_enabled_" + transName + "();\n";
      sPromela += "  if\n";
      sPromela += "  ::  " + transName + "_is_enabled -> atomic{fire_" + transName + "()}\n";
      sPromela += "  ::  else -> skip\n";
      sPromela += "  fi\n";

      sPromela += "}\n";
    }
  }

  private void defineMainProcess() {

    int transSize = dataLayer.getTransitionsCount();
    Transition[] trans = dataLayer.getTransitions();
    String transName;

    sPromela += "proctype " + "Main() {\n";
    for (int transNo = 0; transNo < transSize; transNo++) {
      transName = trans[transNo].getName();
      sPromela += "  bool " + transName + "_is_enabled = false;\n";
    }


    sPromela += "\n  do\n";
    for (int transNo = 0; transNo < transSize; transNo++) {
      transName = trans[transNo].getName();
      sPromela += "  :: " + transName + "() \n";
    }
    sPromela += "  od\n";
    sPromela += "}\n";
  }

  private void defineInitFunc() {
    sPromela += "init {\n";

    int placeSize = dataLayer.getPlacesCount();
    Place[] places = dataLayer.getPlaces();

    String placeName;
    for (int placeNo = 0; placeNo < placeSize; placeNo++) {
      placeName = places[placeNo].getName();
      sPromela += "  type_" + placeName + " " + placeName + ";\n";
      Vector<Token> tokenList = places[placeNo].getToken().listToken;
      for (int i = 0; i < tokenList.size(); i++) {
        Token tempTok = tokenList.get(i);
        Vector<BasicType> btList = tempTok.Tlist;
        for (int j = 0; j < btList.size(); j++) {
          BasicType bt = btList.get(j);
          String value = bt.getValueAsString();
          sPromela += "  " + placeName + "." + placeName + "_field" + Integer.toString(j + 1) +
              "=" + value + ";\n";
        }
        sPromela += "  place_" + placeName + "!" + placeName + ";\n";
      }
    }

    sPromela += "   run Main()\n";
    sPromela += "}\n";
  }

  private void defineFormula() {
//		if(!"".equals(propertyFormula)){
//		ltlparser.errormsg.ErrorMsg errorMsg = new ltlparser.errormsg.ErrorMsg(propertyFormula);
//		ltlparser.ParseLTL p = new ltlparser.ParseLTL(propertyFormula, errorMsg);
//		ltlparser.ltlabsyntree.LogicSentence s = p.absyn;
//		s.accept(new PropertyFormulaToPromela(errorMsg));
//		sPromela += s.formula;
//		}
    sPromela += "ltl f{" + this.propertyFormula + "}";
  }

  public String getPromela() {
    return sPromela;
  }
}
