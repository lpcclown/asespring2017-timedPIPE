package analysis;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author: $
 * @version $Revision: $ $Date: $
 */
public interface FunctionGenerator {
  String generateFunctionDefinition();
  void generateFunctionDefinition(final Writer pWriter) throws IOException;
  String getCallableStatement(final String pFunctionName, final List<String> pArguments);
  List<String> getReturnVariableDeclarations();
}
