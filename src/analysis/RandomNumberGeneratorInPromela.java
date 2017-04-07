package analysis;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author: $
 * @version $Revision: $ $Date: $
 */
public class RandomNumberGeneratorInPromela implements FunctionGenerator {
  private static final String RANDOM_DEFAULT_TEMPLATE = "random()";
  private static final String RANDOM_WITHIN_BOUND_TEMPLATE = "randomB(%s)";
  private static final String RANDOM_WITHIN_RANGE_TEMPLATE = "randomR(%s, %s)";
  private static final String STATE_INITIALIZER_TEMPLATE = "int seed = %d;\n" +
      "int multiplier = (1<<17) - 1;\n" +
      "int addend = 101;\n" +
      "int mask = (1<<28) - 1;\n";

  private static final String DEFAULT_FUNCTION_TEMPLATE = "inline random() {\n" +
      "  int next = (seed*multiplier+addend) & mask;\n" +
      "  r = next >> 12;\n" +
      "  seed = next;\n" +
      "}\n";

  private static final String RANDOM_WITHIN_RANGE_FUNCTION_TEMPLATE = "inline randomR(a, b) {\n" +
      "  int n = b-a;\n" +
      "  if\n" +
      "  :: n>0 -> randomB(n); r = r + a;\n" +
      "  :: n<0 -> randomB(a-b); r = r + b;\n" +
      "  :: else -> skip;\n" +
      "  fi\n" +
      "}\n";

  private static final String RANDOM_WITHIN_BOUND_FUNCTION_TEMPLATE = "inline randomB(b) {\n" +
      "  random();\n" +
      "  r = r % b;\n" +
      "}\n";

  public static final List<String> returnVariables = Arrays.asList("int r");


  @Override
  public String generateFunctionDefinition() {
    StringWriter stringWriter = new StringWriter();
    try {
      generateFunctionDefinition(stringWriter);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return stringWriter.toString();
  }

  @Override
  public void generateFunctionDefinition(final Writer pWriter) throws IOException {
    pWriter.write(generateInitializers());
    pWriter.write(generateDefault());
    pWriter.write(generateWithUpperBound());
    pWriter.write(generateWithRange());
  }

  @Override
  public String getCallableStatement(String pFunctionName, List<String> pArguments) {
    if (pArguments == null || pArguments.isEmpty()) {
      return RANDOM_DEFAULT_TEMPLATE;
    }
    else if (pArguments.size() == 1) {
      return String.format(RANDOM_WITHIN_BOUND_TEMPLATE, pArguments.get(0));
    }
    else {
      return String.format(RANDOM_WITHIN_RANGE_TEMPLATE, pArguments.get(0), pArguments.get(1));
    }
  }

  @Override
  public List<String> getReturnVariableDeclarations() {
    return returnVariables;
  }

  private String generateInitializers() {
    int seed = (int) (System.currentTimeMillis() & 0xFFFFFFF);
    return String.format(STATE_INITIALIZER_TEMPLATE, seed);
  }

  private String generateWithRange() {
    return RANDOM_WITHIN_RANGE_FUNCTION_TEMPLATE;
  }

  private String generateWithUpperBound() {
    return RANDOM_WITHIN_BOUND_FUNCTION_TEMPLATE;
  }

  private String generateDefault() {
    return DEFAULT_FUNCTION_TEMPLATE;
  }
}
