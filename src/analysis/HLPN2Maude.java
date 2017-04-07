package analysis;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import formula.parser.ErrorMsg;
import formula.parser.Formula2Maude;
import formula.parser.Parse;
import formula.absyntree.Sentence;
import org.apache.commons.lang.ObjectUtils;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Place;
import pipe.dataLayer.Token;
import pipe.dataLayer.Transition;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import formulaParser.Printer;

/**
 * Get String output as Maude, from a DataLayer of Petri net
 * @author Reng Zeng, Zhuo Sun, 2015
 */

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author: $
 * @version $Revision: $ $Date: $
 */

public class HLPN2Maude {
  private static final HashMap<String, String> sTypesMap = new HashMap<String, String>(){{
    put("int", "Int");
    put("number", "Int");
    put("string", "String");
  }};

  public DataLayer dataLayer;
  public String propertyFormula = "";
  public String sMaude = "";

  public HLPN2Maude(DataLayer data, String formula){
    dataLayer = data;
    propertyFormula = formula;
    //fmod MARKING
    defineMARKING();

    //define places
    definePlaceDataStructure();

    defineTransFunc();

    sMaude += "endm\n";

    sMaude += String.format("%n%n%s%n", defineInitialMarking());

    sMaude += String.format("%n%n%n%s", propertyFormula);
  }

  public static final String INITIAL_MARKING_TEMPLATE = "mod InitialMarking is\n" +
      "  including MARKING .\n" +
      "  including MainMod .\n" +
      "\n" +
      "  op init : -> Marking .\n" +
      "  eq init = %s .\n" +
      "\n" +
      "endm";
  private String defineInitialMarking() {
    StringBuilder initialMarkingBuilder = new StringBuilder();
    for (Place place : dataLayer.getPlaces()) {
      if (place.getToken().getTokenCount() > 0) {
        String placeName = place.getName();
        Stream stream = place.getToken().listToken.stream()
            .map(token -> String.format("OP-%s(%s)", placeName, token.displayToken(false)));
        if (place.getDataType().getPow()) {
          initialMarkingBuilder.append(String.format(" OP-%sPowerSet(%s) ", placeName, stream.collect(Collectors.joining("; "))));
        }
        else {
          initialMarkingBuilder.append(stream.collect(Collectors.joining(" ")));
        }
      }
    }

    return String.format(INITIAL_MARKING_TEMPLATE, initialMarkingBuilder.toString());
  }

  private void defineMARKING(){

    sMaude += "fmod MARKING is\n";
    sMaude += "  including INT .\n";
    sMaude += "  including STRING .\n";
    sMaude += "  sort Marking .\n";
    sMaude += "  op empty : -> Marking [ctor] .\n";
    sMaude += "  op __ : Marking Marking -> Marking [ctor assoc comm id: empty] .\n";
    sMaude += "endfm\n";

    sMaude += "\n";
  }

  //e.g. Define PURSE-SET based on Purse and it sequence of field types: string int string ...
  private String defineSet(boolean isPowerSet, String setName, String fieldTypes)
  {
    String sDef = "";
    String lc_p = "OP-" + setName;
    lc_p = lc_p.replace('_', '-');
    char uc_P = setName.toUpperCase().charAt(0);
    String PS = uc_P + "S";
    String QS = "";
    String Purse = setName;
    String PurseSet = setName + "Set";
    String PursePowerSet = setName + "PowerSet";
    String op_powerset = "OP-" + setName + "PowerSet";
    String emptyPurse = "empty" + Purse;
    emptyPurse = emptyPurse.replace('_', '-');

    if (uc_P != 'Z')
    {
      QS = (char) (uc_P + 1) + "S";
    }
    else
    {
      QS = "AS";
    }
    sDef += "fmod " + Purse.toUpperCase() + "-SET is\n";
    sDef += "  including MARKING .\n";
    sDef += "  sort " + Purse + " .\n";
    sDef += "  op " + lc_p + " : " + fieldTypes + " -> " + Purse + " [ctor] .\n";
    sDef += "\n";
    sDef += "  var " + uc_P + " : " + Purse + " . "; // No \n
    sDef += "  vars " + PS + " " + QS + " : " + PurseSet + " .\n";
    sDef += "\n";
    sDef += "  sort " + PurseSet + " .\n";
    sDef += "  subsort " + PurseSet + " < Marking .\n";
    sDef += "  subsort " + Purse + " < " + PurseSet + " .\n";
    sDef += "  op " + emptyPurse + " : -> " + PurseSet + " [ctor] .\n";
    sDef += "  op _;_ : " + PurseSet + " " + PurseSet + " -> " + PurseSet + " [ctor assoc comm id: " + emptyPurse + "] .\n";
    sDef += "  eq " + uc_P + " ; " + uc_P + " = " + uc_P + " .\n";
    sDef += "\n";
    sDef += "  op _\\_ : " + PurseSet + " " + PurseSet + " -> " + PurseSet + " .\n";
    sDef += "  eq (" + uc_P + " ; " + PS + ") \\ (" + uc_P + " ; " + QS + ") = " + PS + " \\ " + QS + " .\n";
    sDef += "  eq " + PS + " \\ " + QS + " = " + PS + " [owise] .\n";
    if (isPowerSet)
    {
      sDef += "\n";
      sDef += "  sort " + PursePowerSet + " .\n";
      sDef += "  op " + op_powerset + " : " + PurseSet + " -> " + PursePowerSet + " [ctor] .\n";
      sDef += "  subsort " + PursePowerSet + " < Marking .\n";
    }
    sDef += "endfm\n";

    sDef += "\n";

    return sDef;
  }

  private void definePlaceDataStructure(){
    int placeSize = dataLayer.getPlacesCount();
    Place[] places = dataLayer.getPlaces();

    String placeName, fieldTypes;
    String including = "";
    for(int placeNo = 0; placeNo < placeSize; placeNo++){
      placeName = places[placeNo].getName();

      fieldTypes = "";
      Vector<String> types = places[placeNo].getDataType().getTypes();

      for (int j = 0; j < types.size(); j++) {
        fieldTypes += "  " + convertType(types.get(j));
      }

      boolean isPowerSet = places[placeNo].getDataType().getPow();
      sMaude += defineSet(isPowerSet, placeName, fieldTypes);
      including += "  including " + placeName.toUpperCase() + "-SET .\n";
      sMaude += "\n\n";
    }

    sMaude += "mod MainMod is\n";
    sMaude += including;

  }

  private String convertType(String typeInPN)
  {
    String type = typeInPN.toLowerCase();
    if (!sTypesMap.containsKey(type)) {
      throw new IllegalArgumentException("Invalid type: "+typeInPN);
    }

    return sTypesMap.get(type);
  }

  private void defineTransFunc(){
    int transSize = dataLayer.getTransitionsCount();
    Transition[] trans = dataLayer.getTransitions();
    String transName;

    Multimap<String, String> vars = HashMultimap.create();
    String crl = "", crl_in = "", crl_out = "", crl_if_pre = "", crl_if_post = "";
    for (int transNo = 0; transNo < transSize; transNo++){

      crl_in = ""; crl_out = "";
      crl_if_pre = ""; crl_if_post = "";

      transName = trans[transNo].getName();
      crl += "crl [" + transName + "]:\n";


      ArrayList<Place> inputPlaces= trans[transNo].getPlaceInList();
      ArrayList<String> inputArcVars = trans[transNo].getArcInVarList();

      assert (inputPlaces.size() == inputArcVars.size());

      for(int ipNo = 0; ipNo < inputPlaces.size(); ipNo++){
        String inPlaceName = inputPlaces.get(ipNo).getName();
        if(inputPlaces.get(ipNo).getToken().getDataType().getPow()){
          assert (inputArcVars.get(ipNo).endsWith("}") && inputArcVars.get(ipNo).startsWith("{"));
          String arcName = inputArcVars.get(ipNo).replace('{', ' ').replace('}', ' ').replaceAll(",", " ; ").trim();
          crl_in += "OP-" + inPlaceName.replace('_', '-') + "PowerSet" + "(" + arcName + " ; " + inPlaceName.replace('_', '-') + "S) ";
          crl_out += "OP-" + inPlaceName.replace('_', '-') + "PowerSet" + "(" + inPlaceName.replace('_', '-') + "S) ";

          //vars += "var " + arcName + " : "  + arcName + " .\n";
          boolean isFirstOne = true;
          for (String arcNameOne: arcName.split(";"))
          {
            arcNameOne = arcNameOne.trim();
            if (isFirstOne)
            {
              isFirstOne = false;
            }
            else
            {
              crl_if_pre += " /\\ \n";
            }
            vars.put(inPlaceName, arcNameOne);
            //vars += "var " + arcName + "S" + " : "  + arcName + "Set .\n";
            vars.put(inPlaceName + "Set", inPlaceName.replace('_', '-') + "S");

            crl_if_pre += "OP-" + inPlaceName.replace('_', '-') + "(";
            Vector<String> types = inputPlaces.get(ipNo).getToken().getDataType().getTypes();
            assert (types.size() > 0);

            crl_if_pre += arcNameOne + "_f1";
            //vars += "var " + inPlaceName + "_f1" + ": " + convertType(types.get(0)) +  " .\n";
            vars.put(convertType(types.get(0)), arcNameOne + "_f1");
            for (int i=2; i<types.size()+1; i++)
            {
              crl_if_pre += ", " + arcNameOne + "_f" + i;
              //vars += "var " + inPlaceName + "_f" + i + ": " + convertType(types.get(i-1)) +  " .\n";
              vars.put(convertType(types.get(i-1)), arcNameOne + "_f" + i);
            }
            crl_if_pre += ") := " + arcNameOne;
          }

        }
        else
        {
          String arcName = inputArcVars.get(ipNo).trim();
          crl_in += "OP-" + inPlaceName.replace('_', '-') + "(";
          Vector<String> types = inputPlaces.get(ipNo).getToken().getDataType().getTypes();
          assert (types.size() > 0);

          crl_in += arcName + "_f1";
          //vars += "var " + inPlaceName + "_f1" + ": " + convertType(types.get(0)) +  " .\n";
          vars.put(convertType(types.get(0)), arcName + "_f1");
          for (int i=2; i<types.size()+1; i++)
          {
            crl_in += ", " + arcName + "_f" + i;
            //vars += "var " + inPlaceName + "_f" + i + ": " + convertType(types.get(i-1)) +  " .\n";
            vars.put(convertType(types.get(i-1)), arcName + "_f" + i);
          }
          crl_in += ")\n";
        }
      }

      //"=> " starts

      ArrayList<Place> outputPlaces= trans[transNo].getPlaceOutList();
      ArrayList<String> outputArcVars = trans[transNo].getArcOutVarList();

      assert (outputPlaces.size() == outputArcVars.size());
      for(int ipNo = 0; ipNo < outputPlaces.size(); ipNo++){
        String outPlaceName = outputPlaces.get(ipNo).getName();
        if(outputPlaces.get(ipNo).getToken().getDataType().getPow()){
          assert (outputArcVars.get(ipNo).endsWith("}") && outputArcVars.get(ipNo).startsWith("{"));
          String arcName = outputArcVars.get(ipNo).replace('{', ' ').replace('}', ' ').replaceAll(",", " ; ").trim();
          crl_out += "OP-" + outPlaceName.replace('_', '-') + "PowerSet" + "(" + arcName + " ; " + outPlaceName.replace('_', '-') + "S) ";
          crl_in +=  "OP-" + outPlaceName.replace('_', '-') + "PowerSet" + "(" + outPlaceName.replace('_', '-') + "S) ";

          //vars += "var " + arcName + " : "  + arcName + " .\n";
          boolean isFirstOne = true;
          for (String arcNameOne: arcName.split(";"))
          {
            arcNameOne = arcNameOne.trim();
            if (isFirstOne)
            {
              isFirstOne = false;
            }
            else
            {
              crl_if_post += " /\\ \n";
            }
            vars.put(outPlaceName,  arcNameOne);
            //vars += "var " + arcName + "S" + " : "  + arcName + "Set .\n";
            vars.put(outPlaceName + "Set", outPlaceName.replace('_', '-') + "S");

            crl_if_post += arcNameOne + " := OP-" + outPlaceName.replace('_', '-') + "(";
            Vector<String> types = outputPlaces.get(ipNo).getToken().getDataType().getTypes();
            crl_if_post += arcNameOne + "_f1";
            //vars += "var " + outPlaceName + "_f1" + ": " + convertType(types.get(0)) +  " .\n";
            vars.put(convertType(types.get(0)), arcNameOne + "_f1" );

            for (int i=2; i<types.size()+1; i++)
            {
              crl_if_post += ", " + arcNameOne + "_f" + i;
              //vars += "var " + outPlaceName + "_f" + i + ": " + convertType(types.get(i-1)) +  " .\n";
              vars.put(convertType(types.get(i-1)), arcNameOne + "_f" + i);
            }
            crl_if_post += ")";
          }

        }
        else
        {
          String arcName = outputArcVars.get(ipNo).trim();
          crl_out += "OP-" + outPlaceName.replace('_', '-') + "(";
          Vector<String> types = outputPlaces.get(ipNo).getToken().getDataType().getTypes();
          crl_out += arcName + "_f1";
          //vars += "var " + outPlaceName + "_f1" + ": " + convertType(types.get(0)) +  " .\n";
          vars.put(convertType(types.get(0)), arcName + "_f1" );

          for (int i=2; i<types.size()+1; i++)
          {
            crl_out += ", " + arcName + "_f" + i;
            //vars += "var " + outPlaceName + "_f" + i + ": " + convertType(types.get(i-1)) +  " .\n";
            vars.put(convertType(types.get(i-1)), arcName + "_f" + i);
          }
          crl_out += ")\n";
        }
      }

      crl += crl_in + " => "+ crl_out + "if ";

      if (!crl_if_pre.isEmpty())
        crl += crl_if_pre + " /\\ \n";

      String formula = trans[transNo].getFormula();
      ErrorMsg errorMsg = new ErrorMsg(formula);
      Parse p = new Parse(formula, errorMsg);
      Sentence s = p.absyn;
      System.out.println(trans[transNo].getName());

      Formula2Maude visitor = new Formula2Maude(errorMsg, trans[transNo], 0);
      s.accept(visitor);
      crl += visitor.getTranslation(ObjectUtils.identityToString(s));
      if (!crl_if_post.isEmpty())
        crl += "\n/\\ " + crl_if_post;
      crl += " . \n\n";
//			s.accept(new Printer());

    }
    Set<String> vars_keys = vars.keySet();

    for (String key: vars_keys)
    {
      String vars_value;
      Collection<String> values = vars.get(key);
      if (values.size() == 1)
        vars_value = "var ";
      else
        vars_value = "vars ";
      for (String value: values)
      {
        vars_value += value + " ";
      }
      vars_value += ": ";
      vars_value += key + " .\n";
      sMaude += vars_value;
    }

    sMaude += "\n";

    sMaude += crl;
  }


  public String getMaude()
  {
    return sMaude;
  }
}
