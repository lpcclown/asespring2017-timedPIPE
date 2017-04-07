package analysis;

import formula.parser.Formula2Promela;
import pipe.dataLayer.DataLayer;
import pipe.dataLayer.Transition;

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author: $
 * @version $Revision: $ $Date: $
 */
public class PNPromelaTransitionAsProc extends PNPromela {
    private static final String TRANSITION_EXECUTION_FUNCTION_BODY =
            "  %s%n" +
            "  %s%n" +
                    "  start_%s:%n" +
            "  atomic {%n" +
            "    is_enabled_%s();%n" +
            "    if%n" +
            "    :: %s_is_enabled -> fire_%s()%n" +
            "    :: else -> skip%n" +
            "    fi%n" +
            "  }%n" +
            "  goto start_%s;%n";

    private static final String TRANSITION_EXECUTION_FUNCTION_TEMPLATE =
            "active proctype %s() {%n" +
                    TRANSITION_EXECUTION_FUNCTION_BODY +
                    INLINE_FUNCTION_END;

    @Override
    protected void defineTransitionExecutionFunctions(final Transition pTransition, final Formula2Promela pTranslator, final Writer pWriter) throws IOException {
        String name = pTransition.getName();
        String declarations = getVariableDeclarations(name, pTranslator);
        String returnDeclarations = getReturnDeclarations(name, pTranslator);
        pWriter.write(String.format("bool %s_is_enabled = false;%n", name));
        pWriter.write(String.format(TRANSITION_EXECUTION_FUNCTION_TEMPLATE, name, returnDeclarations, declarations, name, name, name, name, name));
    }

    @Override
    protected void defineMainFunction(final DataLayer pDatalayer, final Writer pWriter) throws IOException {
        pWriter.write(String.format("proctype Main() {%n  printf(\"main\");%n}%n%n"));
    }
}
