package analysis;

import pipe.dataLayer.DataLayer;

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:dalam004@fiu.edu">Dewan Moksedul Alam</a>
 * @author last modified by $Author: $
 * @version $Revision: $ $Date: $
 */
public interface PNTranslator {

    void translate(final DataLayer pPNModel, final String pPropertySpec, final Writer pWriter) throws IOException;
}
