package analysis;

import formula.absyntree.Sentence;
import formula.parser.ErrorMsg;
import formula.parser.Formula2Promela;
import formula.parser.Parse;
import formula.parser.VariableDefinition;
import org.apache.commons.lang.StringUtils;
import pipe.dataLayer.*;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static analysis.PromelaUtil.getMappedType;
import static analysis.QuantifierEvalInfo.QuantifierType;

/**
 * Created by Maks on 5/11/2016.
 */
public class PNPromela implements PNTranslator {
  private static final Pattern sPattern = Pattern.compile("\"([^\"]*)\"");

  private static final String BOUND_TEMPLATE = "#define Bound_%s %d%n";
  private static final String BLOCK_END_TEMPLATE = "};%n%n";

  //  P0.field1
  private static final String FIELD_NAME_TEMPLATE = "field%d";

  //  int P0.field1;
  private static final String FIELD_DEFINITION_TEMPLATE = "  %s %s;%n";

  //  typedef type_P0 {
  private static final String DATA_TYPE_START_TEMPLATE = "typedef type_%s {%n";


  //  chan place_P0 = [Bound_P0] of {type_P0};
  private static final String CHAN_DEFINITION_TEMPLATE = "chan place_%s = [Bound_%s] of {type_%s};%n";

  //  inline is_enabled_T0() {
//    Fields initialization of places
//    Take a peek on message channels - place_P0??<P0.field1>
//    if
//    :: precondition matches -> T1_is_enabled = true;
//    :: else -> skip
//    fi
//  }
  private static final String ENABLED_CHECKER_START_TEMPLATE = "inline is_enabled_%s() {%n";
  private static final String ENABLED_CHECKER_TEMPLATE = "%%%dsif%%n%%%ds:: %%s -> %%s_is_enabled = true; %%s%%n%%%ds:: else -> skip;%n%%%dsfi";

  private static Map<String, FunctionGenerator> sFunctionGenerators = new HashMap<String, FunctionGenerator>() {{
    put("random", new RandomNumberGeneratorInPromela());
  }};

  private Set<String> mSourcesToClear = new HashSet<>(3);

  public PNPromela() {
  }

  public void translate(final DataLayer pPNModel, final String pPropertySpecLTL, final Writer pTranslationWriter) throws IOException {
    defineBounds(pPNModel, pTranslationWriter);
    definePolls(pPNModel, pTranslationWriter);
    defineMTypes(pPNModel, pTranslationWriter);
    definePlaceDataStructures(pPNModel, pTranslationWriter);
    defineMessageChannels(pPNModel.getPlaces(), pTranslationWriter);

    defineMessageCleanerFunction(pTranslationWriter);
    defineTransitionFunctions(pPNModel, pTranslationWriter);
    defineMainFunction(pPNModel, pTranslationWriter);
    defineInitFunction(pPNModel, pTranslationWriter);
    pTranslationWriter.write(String.format("%nltl f { %s }%n%n", pPropertySpecLTL));
  }

  public static final String MESSAGE_CLEANER_TEMPLATE =
      "inline clean(source, var) {%n" +
          "  do%n" +
          "  :: source?var%n" +
          "  od%n" +
          "}%n%n";

  private void defineMessageCleanerFunction(final Writer pWriter) throws IOException {
    pWriter.write(String.format(MESSAGE_CLEANER_TEMPLATE));
  }

  private void definePolls(final DataLayer pPNModel, final Writer pWriter) throws IOException {
    for (Place place : pPNModel.getPlaces()) {
      AtomicInteger counter = new AtomicInteger(1);
      final List<String> fields = place.getDataType().getTypes().stream().map(s -> String.format("p%d", counter.getAndIncrement())).collect(Collectors.toList());
      pWriter.write(String.format("#define %s(%s) place_%s??[%s]%n", place.getName(), fields.stream().collect(Collectors.joining(",")),
          place.getName(), fields.stream().map(s -> String.format("eval(%s)", s)).collect(Collectors.joining(","))));
    }
    pWriter.write(String.format("%n"));
  }

  private void defineMTypes(final DataLayer pPNModel, final Writer pWriter) throws IOException {
    Set<String> stringTokens = new HashSet<>();
    for (Place place : pPNModel.getPlaces()) {
      stringTokens.addAll(mTypesInPlace(place));
    }

    for (Transition transition : pPNModel.getTransitions()) {
      stringTokens.addAll(mTypesInTransition(transition));
    }

    if (stringTokens.size() > 0) {
      pWriter.write(String.format("mtype = {%s};%n%n", stringTokens.stream().collect(Collectors.joining(", "))));
    }
  }

  private Set<String> mTypesInTransition(final Transition pTransition) {
    Matcher matcher = sPattern.matcher(pTransition.getFormula());
    Set<String> tokens = new HashSet<>();
    while (matcher.find()) {
      tokens.add(PromelaUtil.stringToMType(matcher.group(1)));
    }

    return tokens;
  }

  private Set<String> mTypesInPlace(final Place pPlace) {
    Set<String> tokens = new HashSet<>();
    for (Token token : pPlace.getToken().getListToken()) {
      for (BasicType bt : token.Tlist) {
        if (bt.kind == BasicType.STRING) {
          tokens.add(PromelaUtil.stringToMType(bt.getValueAsString()));
        }
      }
    }

    return tokens;
  }

  private void defineInitFunction(final DataLayer pPNModel, final Writer pWriter) throws IOException {
    pWriter.write(String.format("init {%n"));
    for (Place place : pPNModel.getPlaces()) {
      for (Token token : place.getToken().getListToken()) {
        String sendExpression = PromelaUtil.tokenToSendExpression(token);//tokenToFieldAssignments(token, place, pWriter);
        pWriter.write(String.format("  place_%s!%s;%n", place.getName(), sendExpression));
      }
      pWriter.write(String.format("%n"));
    }
    pWriter.write(String.format("  run Main()%n}%n%n"));
  }

  private static final String FIELD_ASSIGNMENT_TEMPLATE = "  %s.field%d = %s;%n";

  private void writeTokenToFieldAssignments(final Token pToken, final Place pPlace, final Writer pWriter) throws IOException {
    int counter = 1;
    for (BasicType basicType : pToken.Tlist) {
      pWriter.write(String.format(FIELD_ASSIGNMENT_TEMPLATE, pPlace.getName(), counter++, PromelaUtil.stringToMType(basicType.getValueAsString())));
    }
  }

  protected void defineMainFunction(final DataLayer pDatalayer, final Writer pWriter) throws IOException {
    pWriter.write(String.format("proctype Main() {%n"));
    for (Transition transition : pDatalayer.getTransitions()) {
      pWriter.write(String.format("  bool %s_is_enabled = false;%n", transition.getName()));
    }

    pWriter.write(String.format("%n  do%n"));
    for (Transition transition : pDatalayer.getTransitions()) {
      pWriter.write(String.format("  :: %s()%n", transition.getName()));
    }
    pWriter.write(String.format("  od%n}%n%n"));
  }

  public static final String INLINE_FUNCTION_START = "inline %s(%s) {%n";
  public static final String INLINE_FUNCTION_END = "}%n%n";
  private static final String TRANSITION_EXECUTION_FUNCTION_BODY =
        "  %s%n" +
                "  %s%n" +
                "  atomic {%n" +
                "    is_enabled_%s();%n" +
                "    if%n" +
                "    :: %s_is_enabled -> fire_%s()%n" +
                "    :: else -> skip%n" +
                "    fi%n" +
                "  }%n";
  private static final String TRANSITION_EXECUTION_FUNCTION_TEMPLATE = INLINE_FUNCTION_START + TRANSITION_EXECUTION_FUNCTION_BODY + INLINE_FUNCTION_END;

  protected void defineTransitionExecutionFunctions(final Transition pTransition, final Formula2Promela pTranslator, final Writer pWriter) throws IOException {
    String name = pTransition.getName();
    String declarations = getVariableDeclarations(name, pTranslator);
    String returnDeclarations = getReturnDeclarations(name, pTranslator);
    pWriter.write(String.format(TRANSITION_EXECUTION_FUNCTION_TEMPLATE, name, "", returnDeclarations, declarations, name, name, name));
  }

  protected String getReturnDeclarations(final String pName, final Formula2Promela pTranslator) {
    final StringBuilder returnDeclarations = new StringBuilder();
    for (String rv : pTranslator.getInvokedFunction()) {
      returnDeclarations.append(sFunctionGenerators.get(rv).getReturnVariableDeclarations().stream().collect(Collectors.joining(";\n"))).append(";\n");
    }

    return returnDeclarations.toString();
  }

  protected String getVariableDeclarations(final String pName, final Formula2Promela pTranslator) {
    final StringBuilder declarations = new StringBuilder();
    pTranslator.getVariableDefinitions().forEach(vd -> {
      String placeName = vd.isInputVariable() ? vd.getInputPlaceNames().get(0) : vd.getOutputPlaceNames().get(0);
      declarations.append(String.format("type_%s %s;%n  ", placeName, vd.getVariableName()));
    });

    return declarations.toString();
  }

  private void defineFireTransitionFunctions(Transition pTransition, Formula2Promela pTranslator, Writer pWriter) throws IOException {
    pWriter.write(String.format("inline fire_%s() {%n", pTransition.getName()));
//    writeSetCreationStatements(pTranslator, pWriter);
    writeSetAssignments(pTranslator, pWriter);
    writeNonSetAssignments(pTranslator, pWriter);
    for (String place : mSourcesToClear) {
      String variable = String.format("to_clean_%s", place);
      pWriter.write(String.format("  type_%s %s; ", place, variable));
      pWriter.write(String.format("  clean(place_%s, %s);%n", place, variable));
    }
    pWriter.write(String.format("%n  %s_is_enabled = false;%n}%n%n", pTransition.getName()));
  }

  private void writeSetCreationStatements(final Formula2Promela pTranslator, final Writer pWriter) throws IOException {
    for (SetDefinitionInfo sdInfo : pTranslator.getSetDefinitionInfo()) {
      pWriter.write(String.format("  %s(place_%s);%n", sdInfo.getFunctionName(), sdInfo.getTarget()));
    }
  }

  private void writeNonSetAssignments(final Formula2Promela pTranslator, final Writer pWriter) throws IOException {
    List<VariableDefinition> nonSetVariables = pTranslator.getVariableDefinitions().stream()
        .filter(vd -> !isIncludedInSetAssignment(vd.getVariableName(), pTranslator.getSetAssignments()) && !vd.isSetVariable())
        .collect(Collectors.toList());

    nonSetVariables.forEach(vd -> {
      if (vd.isInputVariable() || vd.isOutputVariable()) {
        vd.getInputPlaceNames().forEach(s -> {
          try {
            pWriter.write(String.format("  place_%s??%s;%n", s, vd.getEvalSequence()));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      }
    });

    if (StringUtils.isNotBlank(pTranslator.getPostCondition())) {
      pWriter.write(String.format("%n%s;%n%n", pTranslator.getPostCondition()));
    }

    nonSetVariables.forEach(vd -> {
      try {
        for (String place : vd.getOutputPlaceNames()) {
          pWriter.write(String.format("  place_%s!%s;%n", place, vd.getReceiveSequence()));
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private boolean isIncludedInSetAssignment(final String pVariableName, final Map<String, String> pSetAssignments) {
    return pSetAssignments.containsKey(pVariableName) || pSetAssignments.containsValue(pVariableName);
  }

  //  do
//      ::  place_P0?P0;
//  P1.field1 = P0.field1;
//  P1.field2 = P0.field2;
//  place_P1!P1;
//  od
  private void writeSetAssignments(final Formula2Promela pTranslator, final Writer pWriter) throws IOException {
    for (Map.Entry<String, String> entry : pTranslator.getSetAssignments().entrySet()) {
      VariableDefinition assignee = pTranslator.getVariableDefinition(entry.getKey());
      VariableDefinition assignment = pTranslator.getVariableDefinition(entry.getValue());
//      pWriter.write(String.format("  %s;%n  %s;%n", assignee.getDeclaration(), assignment.getDeclaration()));
      pWriter.write(String.format("  do%n  ::"));
      pWriter.write(PromelaUtil.getMultiReceiveStatements(assignment, 4));
//      pWriter.write(PromelaUtil.receiveSequencesToAssignment(assignee.getReceiveSequence(), assignment.getReceiveSequence(), 4));
      for (String output : assignee.getOutputPlaceNames()) {
        pWriter.write(String.format("    place_%s!%s;%n", output, assignment.getVariableName()));
      }
      pWriter.write(String.format("  od%n"));
    }
  }

  public static final String TOKEN_COMBINATION_DETERMINER = "%%%dsfor (%s in place_%s) {%n%%s%n%%%ds}%n";
  public static final String TOKEN_COMBINATION_DETERMINER_BREAK =
      "%%%dsif%%n" +
      "%%%ds:: %%s_is_enabled == true -> break%%n" +
      "%%%ds:: else -> skip%%n" +
      "%%%dsfi";
  private void defineTransitionEnabledCheckers(final Transition pTransition, final Formula2Promela pTranslator, final Writer pWriter) throws IOException {
    pWriter.write(String.format(ENABLED_CHECKER_START_TEMPLATE, pTransition.getName()));
    StringBuilder matchers = new StringBuilder();
    int indent = 2;
    String preTemplate = "%s%s%s";
    for (VariableDefinition vd : pTranslator.getVariableDefinitions()) {
      if (vd.getInputPlaceNames().size() > 0 && !vd.isSetVariable()) {
        String placeName = vd.getInputPlaceNames().get(0);
        String forLoop = String.format(TOKEN_COMBINATION_DETERMINER, indent, vd.getVariableName(), placeName, indent);
        String loopBreak = "";
        if (indent > 2) {
          loopBreak = String.format(String.format(TOKEN_COMBINATION_DETERMINER_BREAK, indent, indent, indent, indent), "", "", pTransition.getName(), "", "");
        }
        preTemplate = String.format(preTemplate, "", forLoop+loopBreak, "");
        indent += 2;

        Iterator<String> places = vd.getInputPlaceNames().iterator();
        places.next(); //skipping the first one. Since the value retrieved from this one will be tested against others
        while (places.hasNext()) {
          matchers.append(String.format("%s(%s) && ", places.next(), vd.getReceiveSequence()));
        }
      }
    }

    StringBuilder quantifierEvalStmt = new StringBuilder();
    for (QuantifierEvalInfo evalInfo : pTranslator.getQuantifiersEvalInfo()) {
      quantifierEvalStmt.append(String.format("  %s = false;%n", evalInfo.getDeciderVariable()))
          .append(String.format("  %s();%n%n", evalInfo.getFunctionName()));
    }

    String precondition = String.format("%s(%s)", matchers.toString(), pTranslator.getPreCondition());
    String preconditionTemplate = String.format(ENABLED_CHECKER_TEMPLATE, indent, indent, indent, indent);
    String preconditionStmts = String.format("%s%s", quantifierEvalStmt.toString(), String.format(preconditionTemplate, "", "", precondition, pTransition.getName(), indent>2?"break;":"", "", ""));
    pWriter.write(String.format(preTemplate, "", preconditionStmts, ""));
    pWriter.write(String.format("}%n%n"));
  }

  private void defineTransitionFunctions(final DataLayer pPNModel, final Writer pWriter) throws IOException {
    Set<String> mFunctionsInvoked = new HashSet<>(3);
    for (Transition transition : pPNModel.getTransitions()) {
      mSourcesToClear.clear();
      Formula2Promela translator = evaluateFormula(transition);
      mFunctionsInvoked.addAll(translator.getInvokedFunction());
      defineQuantifierEvaluationFunctions(transition, translator, pWriter);
      defineSetCreationFunctions(translator, pWriter);
      defineTransitionEnabledCheckers(transition, translator, pWriter);
      defineFireTransitionFunctions(transition, translator, pWriter);
      defineTransitionExecutionFunctions(transition, translator, pWriter);
    }

    for (String function : mFunctionsInvoked) {
      sFunctionGenerators.get(function).generateFunctionDefinition(pWriter);
    }
  }

  private static final String SET_DEF_EVAL_FUNC_TEMPLATE =
      "inline %s(msg_chan) {%n" +
//      "  type_%s %s;%n" +
//      "  type_%s %s;%n" +
      "  %s;" +
      "  for (%s in place_%s) {%n" +
      "    if%n" +
      "    :: %s ->%s" +
      "       msg_chan!%s;%n" +
      "    :: else -> skip;%n" +
      "    fi%n" +
      "  }%n" +
      "}%n%n";
  private void defineSetCreationFunctions(final Formula2Promela pTranslator, final Writer pWriter) throws IOException {
    for (SetDefinitionInfo sdInfo : pTranslator.getSetDefinitionInfo()) {
      String source = sdInfo.getLoopVariable();
      StringBuilder declarations = new StringBuilder();
      declarations.append(String.format("type_%s %s;%n", sdInfo.getSource(), source));
      String receiveSeq = "";
      if (sdInfo.getReceiveSequence() != null) {
        declarations.append(String.format("type_%s %s;%n", sdInfo.getTarget(), sdInfo.getDeclaration()));
        receiveSeq = sdInfo.getReceiveSequence();
      }
      else {
        receiveSeq = source;
      }

      pWriter.write(String.format(SET_DEF_EVAL_FUNC_TEMPLATE,
          sdInfo.getFunctionName(),
//          source, source,
//          sdInfo.getTarget(),sdInfo.getDeclaration(),
          declarations.toString(),
          source, sdInfo.getSource(),
          sdInfo.getPreCondition(), PromelaUtil.reIndent(sdInfo.getPostCondition(), 4),
          receiveSeq));
      mSourcesToClear.add(sdInfo.getSource());
    }
  }

  private static final String QUANT_EVAL_FUNC_TEMPLATE =
      "inline %s() {%n" +
      "  %s = %s;%n" +
      "  type_%s %s;%n" +
      "  //If this quantifier has nested quantifiers those function calls should go here%n" +
      "  for (%s in place_%s) {%n" +
      "    if%n" +
      "    :: %s -> %s = %s; break;%n" + // !(precondition) for universal and precondition for existential
      "    :: else -> skip%n" +
      "    fi%n" +
      "  }%n" +
      "}%n%n";
  private void defineQuantifierEvaluationFunctions(final Transition pTransition, final Formula2Promela pTranslator, final Writer pWriter) throws IOException {
    for (QuantifierEvalInfo evalInfo : pTranslator.getQuantifiersEvalInfo()) {
      boolean isForAll = evalInfo.getType() == QuantifierType.FORALL;
      pWriter.write(String.format("bool %s;%n", evalInfo.getDeciderVariable()));
      pWriter.write(String.format(QUANT_EVAL_FUNC_TEMPLATE,
          evalInfo.getFunctionName(),
          evalInfo.getDeciderVariable(),
          isForAll ? "true" : "false",
          evalInfo.getChannelName(),
          evalInfo.getLoopVariableName(),
          evalInfo.getLoopVariableName(),
          evalInfo.getChannelName(),
          isForAll ? "!("+evalInfo.getCondition()+")" : evalInfo.getCondition(),
          evalInfo.getDeciderVariable(),
          isForAll ? "false" : "true"));
    }
  }

  private void defineMessageChannels(final Place[] pPlaces, final Writer pWriter) throws IOException {
    for (Place place : pPlaces) {
      String name = place.getName();
      pWriter.write(String.format(CHAN_DEFINITION_TEMPLATE, name, name, name));
    }
    pWriter.write(String.format("%n"));
  }

  private void definePlaceDataStructures(DataLayer pPNModel, Writer pWriter) throws IOException {
    for (Place place : pPNModel.getPlaces()) {
      pWriter.write(String.format(DATA_TYPE_START_TEMPLATE, place.getName()));
      defineFieldVariables(place, pWriter);
      pWriter.write(String.format(BLOCK_END_TEMPLATE));
    }
  }

  private void defineFieldVariables(final Place pPlace, final Writer pTranslationWriter) throws IOException {
    int counter = 1;
    for (String type : pPlace.getDataType().getTypes()) {
      pTranslationWriter.write(String.format(FIELD_DEFINITION_TEMPLATE, getMappedType(type), String.format(FIELD_NAME_TEMPLATE, counter++)));
    }
  }

  private void defineBounds(final DataLayer pPNModel, final Writer pWriter) throws IOException {
    int maxCapacity = 0;
    for (Place place : pPNModel.getPlaces()) {
      if (maxCapacity < place.getCapacity()) {
        maxCapacity = place.getCapacity();
      }
    }
    maxCapacity = 2*maxCapacity;
    for (Place place : pPNModel.getPlaces()) {
      pWriter.write(String.format(BOUND_TEMPLATE, place.getName(), maxCapacity));
    }
    pWriter.write(String.format("%n%n"));
  }

  private Formula2Promela evaluateFormula(Transition pTransition) {
    String formula = pTransition.getFormula();
    ErrorMsg errorMsg = new ErrorMsg(formula);
    Formula2Promela translator = new Formula2Promela(errorMsg, pTransition, 0);
    translator.setFunctionGeneratorFactory(sFunctionGenerators);
    Parse p = new Parse(formula, errorMsg);
    Sentence s = p.absyn;
    s.accept(translator);
    return translator;
  }

}
