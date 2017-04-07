package formula.function;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 * Created by Maks on 8/17/2016.
 */
public class FunctionInterpreter implements FunctionHandler {
  private final Random mRandom = new Random(System.currentTimeMillis());

  private static Method[] sSupportedFunctions;
  static {
    List<Method> methods = new ArrayList<>();
    for (Method method : FunctionInterpreter.class.getDeclaredMethods()) {
      if (method.getAnnotation(Function.class) != null) {
        methods.add(method);
      }
    }
    sSupportedFunctions = methods.toArray(new Method[methods.size()]);
  }

  @Function
  private String concat(final String... pStrings) {
    StringBuilder sb = new StringBuilder();
    if (pStrings != null) {
      for (String str : pStrings) {
        sb.append(str);
      }
    }

    return sb.toString();
  }

  @Function
  private BigDecimal sqrt(final BigDecimal pValue) {
    double sq = Math.sqrt(pValue.doubleValue());
    return new BigDecimal(Double.toString(sq));
  }

  @Function
  private BigDecimal clock() {
    return new BigDecimal(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
  }

  @Function
  private BigDecimal random() {
    return new BigDecimal(mRandom.nextInt());
  }

  @Function
  private BigDecimal random(final BigDecimal pBound) {
    return new BigDecimal(mRandom.nextInt(pBound.intValue()));
  }

  @Function
  private BigDecimal random(final BigDecimal pLowerBound, final BigDecimal pUpperBound) {
    int lower = pLowerBound.intValue();
    int upper = pUpperBound.intValue();
    if (lower >= upper) {
      return random();
    }

    return new BigDecimal(lower + mRandom.nextInt(upper-lower));
  }


  @Override
  public boolean isSupported(String pFunctionName, Object[] pArguments) {
    try {
      this.getClass().getMethod(pFunctionName, getParameterTypes(pFunctionName, pArguments));
    } catch (NoSuchMethodException e) {
      return false;
    }

    return true;
  }

  public Object invoke(String functionName, Object[] arguments) {
    Objects.requireNonNull(functionName, "Function name cannot be empty");
    Objects.requireNonNull(arguments, "Function arguments cannot be null");

    try {
      Method method = getMethod(functionName, arguments);
      Object[] args = getNormalizedArguments(functionName, arguments);
      return method.invoke(this, args);
    }
    catch (InvocationTargetException | IllegalAccessException e) {
      String message = Arrays.asList(arguments).stream().map(c->c.toString()).collect(Collectors.joining(","));
      e.printStackTrace();
      throw new IllegalArgumentException(String.format("Error occurred when executing the function with name %s and arguments (%s)", functionName, message), e);
    }
  }

  private Object[] getNormalizedArguments(String functionName, Object[] arguments) {
    int varargIndex = getVarargIndex(functionName);
    Object[] resolved = arguments;
    if (varargIndex >= 0) {
      resolved = new Object[varargIndex + 1];
      for (int i = 0; i<varargIndex; i++) {
        resolved[i] = arguments[i].getClass().cast(arguments[i]);
      }
      int size = arguments.length-varargIndex;
      Object object = Array.newInstance(arguments[varargIndex].getClass(), size);
      System.arraycopy(arguments, varargIndex, object, 0, size);
      resolved[varargIndex] = object;
    }
    return resolved;
  }

  private Method getMethod(final String pFunctionName, final Object[] pArguments) {
    Class[] paramTypes = getParameterTypes(pFunctionName, pArguments);
    try {
      return this.getClass().getDeclaredMethod(pFunctionName, paramTypes);
    }
    catch (NoSuchMethodException e) {
      String types = Arrays.asList(paramTypes).stream().map(c->c.getName()).collect(Collectors.joining(","));
      throw new IllegalArgumentException(String.format("No appropriate method found with name \"%s\" and param types (%s)", pFunctionName, types), e);
    }
  }

  private Class[] getParameterTypes(final String pFunctionName, final Object[] pArguments) {
    int varargIndex = getVarargIndex(pFunctionName);
    Class[] paramTypes = null;
    if (varargIndex >= 0) {
      paramTypes = new Class[varargIndex+1];
      for (int i = 0; i<varargIndex; i++) {
        paramTypes[i] = pArguments[i].getClass();
      }
      paramTypes[varargIndex] = Array.newInstance(pArguments[varargIndex].getClass(), pArguments.length-varargIndex).getClass();
    }
    else {
      paramTypes = new Class[pArguments.length];
      for (int i = 0; i<pArguments.length; i++) {
        paramTypes[i] = pArguments[i].getClass();
      }
    }

    return paramTypes;
  }

  private int getVarargIndex(final String pFunctionName) {
    if ("concat".equals(pFunctionName)) {
      return 0;
    }

    return -1;
  }

  @Override
  public Method[] supportedFunctions() {
    return this.getClass().getDeclaredMethods();
  }
}
