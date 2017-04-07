package formula.function;

import java.lang.reflect.Method;

/**
 * Created by Maks on 8/17/2016.
 */
public interface FunctionHandler {
  boolean isSupported(final String pFunctionName, final Object[] pArguments);
  Object invoke(final String pFunctionName, final Object[] pArguments);
  Method[] supportedFunctions();
}
