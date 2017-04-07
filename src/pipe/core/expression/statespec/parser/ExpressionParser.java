package pipe.core.expression.statespec.parser;

import formula.parser.ErrorMsg;
import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import pipe.core.expression.statespec.grammer.Sentence;

import java.io.StringReader;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author$
 * @version $Revision$ $Date$
 */
public class ExpressionParser {

  public Sentence parseExpression(final String pExpression, final ErrorMsg pErrorCollector) throws Exception {
    Scanner scanner = new Yylex(new StringReader(pExpression), pErrorCollector);
    parser parser = new parser(scanner, pErrorCollector);
    Symbol symbol = parser.parse();
    if (pErrorCollector.anyErrors) {
      throw new Exception("Could not parse properly");
    }

    return (Sentence) symbol.value;
  }
}
