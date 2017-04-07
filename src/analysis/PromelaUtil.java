package analysis;

import formula.parser.VariableDefinition;
import org.apache.commons.lang.StringUtils;
import pipe.dataLayer.BasicType;
import pipe.dataLayer.DataType;
import pipe.dataLayer.Token;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 *
 * Created by Maks on 5/20/2016.
 */
public final class PromelaUtil {

  public static final String JOINER_SEQUENCE = ",";
  public static final String EVAL_TEMPLATE = "eval(%s)";
  public static final String FIELD_NAME_TEMPLATE = "%s.field%d";
  public static final String FIELD_DECLARATION_TEMPLATE = "%s "+FIELD_NAME_TEMPLATE;

  public static String stringToMType(final String pValue) {
    String mtype = pValue;
    mtype = pValue.replaceAll("//s|//.", "_");
    return mtype;
  }

  public static String getMappedType(final String pType) {
    if (BasicType.TYPES[BasicType.NUMBER].equals(pType)) {
      return "int";
    }
    else if (BasicType.TYPES[BasicType.STRING].equals(pType)) {
      return "mtype";
    }

    return pType;
  }

  public static String dataTypeToVariableDeclaration(final DataType pType, final String pVariable) {
    int counter = 1;
    StringBuilder sb = new StringBuilder();
    for (String type : pType.getTypes()) {
      sb.append(getFieldDeclaration(type, pVariable, counter)).append("; ");
      counter++;
    }
    return sb.toString();
  }

  public static String getFieldDeclaration(final String pType, final String pVariable, final int pFieldPosition) {
    return String.format(FIELD_DECLARATION_TEMPLATE, getMappedType(pType), pVariable, pFieldPosition);
  }

  public static String getFieldName(final String pVariable, final int pFieldPosition) {
    return String.format(FIELD_NAME_TEMPLATE, pVariable, pFieldPosition);
  }

  public static String dataTypeToReceiveSequence(final DataType pDataType, final String pVariable) {
    AtomicInteger counter = new AtomicInteger(1);
    return pDataType.getTypes().stream()
        .map(s -> String.format(FIELD_NAME_TEMPLATE, pVariable, counter.getAndIncrement()))
        .collect(Collectors.joining(JOINER_SEQUENCE));
  }

  public static String dataTypeToEvalSequence(final DataType pDataType, final String pVariable) {
    AtomicInteger counter = new AtomicInteger(1);
    return pDataType.getTypes().stream()
        .map(s -> String.format(EVAL_TEMPLATE, getFieldName(pVariable, counter.getAndIncrement())))
        .collect(Collectors.joining(JOINER_SEQUENCE));
  }

  public static String tokenToSendExpression(final Token pToken) {
    return pToken.Tlist.stream().map(bt -> basicTypeToValueAsString(bt)).collect(Collectors.joining(JOINER_SEQUENCE));
  }

  private static String basicTypeToValueAsString(BasicType pBasicType) {
    if (pBasicType.kind == BasicType.NUMBER) {
      return String.format("%d", pBasicType.getValueAsInt());
    }

    return stringToMType(pBasicType.getValueAsString());
  }

  public static String receiveSequenceToEvalSequence(final String pReceiveSequence) {
    return Arrays.asList(pReceiveSequence.split(JOINER_SEQUENCE)).stream()
        .map(s -> String.format(EVAL_TEMPLATE, s))
        .collect(Collectors.joining(JOINER_SEQUENCE));
  }

  public static String receiveSequencesToAssignment(final String pAssignee, final String pAssignment, final int pSpace) {
    String template = "%s";
    if (pSpace > 0) {
      template = String.format("%%%ds", pSpace);
    }
    template = template+"%s = %s;%n";

    StringBuilder sb = new StringBuilder();
    String[] assigneeFields = pAssignee.split(JOINER_SEQUENCE);
    String[] assignmentFields = pAssignment.split(JOINER_SEQUENCE);
    for (int i = 0; i<assigneeFields.length; i++) {
      sb.append(String.format(template, " ", assigneeFields[i], assignmentFields[i]));
    }
    return sb.toString();
  }

  public static String getMultiReceiveStatements(final VariableDefinition pVariableDefinition, final int pSpace) {
    String template = "%s";
    if (pSpace > 0) {
      template = String.format("%%%ds", pSpace);
    }
    template = template + "place_%s??%s;%n";

    StringBuilder sb = new StringBuilder();
    Iterator<String> inputsIterator = pVariableDefinition.getInputPlaceNames().iterator();
    if (inputsIterator.hasNext()) {
      sb.append(String.format(template, "", inputsIterator.next(), pVariableDefinition.getReceiveSequence()));
    }
    while (inputsIterator.hasNext()) {
      sb.append(String.format(template, "", inputsIterator.next(), pVariableDefinition.getEvalSequence()));
    }

    return sb.toString();
  }

  public static String reIndent(final String pString, final int pIndent) {
    String[] lines = pString.split(String.format("%n"));
    String template = String.format("%%%ds%%s%%n",pIndent);
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      if (StringUtils.isNotBlank(line)) {
        sb.append(String.format(template, "", line));
      }
    }
    return sb.toString();
  }
}
