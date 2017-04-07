package ltlwidgets;

import java.util.LinkedList;


public final class Unicode_of_LTL_OPS {
    private static LinkedList<String> m_codes = null;
    private Unicode_of_LTL_OPS() {
        m_codes = new LinkedList<String>();
        m_codes.add(""+LAnd); m_codes.add(""+LOr);
        m_codes.add(""+LNot); m_codes.add(""+LImplies);
        m_codes.add(""+EQUIV); m_codes.add(""+Eq);
        m_codes.add(""+RelNEq); m_codes.add(""+RelGT);
        m_codes.add(""+RelLT); m_codes.add(""+RelGTEq);
        m_codes.add(""+RelLTEq); m_codes.add(""+ArithPlus);
        m_codes.add(""+ArithMinus); m_codes.add(""+ArithMult);
        m_codes.add(""+ArithMod); m_codes.add(""+ArithDiv);
        m_codes.add(""+FOLForAll); m_codes.add(""+FOLExists);
        m_codes.add(""+FOLNotExists); m_codes.add(""+FOLScope);
        m_codes.add(""+SetBelongs); m_codes.add(""+SetNotBelongs);
        m_codes.add(""+SetEmpty); m_codes.add(""+SetSubset);
        m_codes.add(""+SetNotSubset); m_codes.add(""+SetSubsetSet);
        m_codes.add(""+SetUnion); m_codes.add(""+SetDifference);
        m_codes.add(""+SetTupleSelL); m_codes.add(""+SetTupleSelR);
        // LTL
        m_codes.add(""+FLTLAlways); m_codes.add(""+FLTLSometimes);
        m_codes.add(""+FLTLNext); m_codes.add(""+FLTLUntil);
        m_codes.add(""+FLTLWkUntil); 
    }
    private static final Unicode_of_LTL_OPS theCode = new Unicode_of_LTL_OPS();
    public static boolean isACode(char c){
        return m_codes.indexOf(""+c)!=-1;
    }
    public static final char LAnd = '\uFA21';
    public static final char LOr = '\uFA22';
    public static final char LNot = '\uFA23';
    public static final char LImplies = '\uFA24';
    public static final char EQUIV = '\uFA25';
    public static final char Eq = '=';
    public static final char RelNEq = '\uFA32';
    public static final char RelGT = '>';
    public static final char RelLT = '<';
    public static final char RelGTEq = '\uFA31';
    public static final char RelLTEq = '\uFA30';
    public static final char ArithPlus = '+';
    public static final char ArithMinus = '-';
    public static final char ArithMult = '*';
    public static final char ArithMod = '%';
    public static final char ArithDiv = '/';
    public static final char FOLForAll = '\uFA40';
    public static final char FOLExists = '\uFA41';
    public static final char FOLNotExists = '\uFA42';
    public static final char FOLScope = '\uFA43';
    public static final char SetBelongs = '\uFA50';
    public static final char SetNotBelongs = '\uFA51';
    public static final char SetEmpty = '\uFA52';
    public static final char SetSubset = '\uFA53';
    public static final char SetNotSubset = '\uFA54';
    public static final char SetSubsetSet ='\uFA55';
    public static final char SetUnion = '\uFA56';
    public static final char SetDifference = '\uFA57';
    public static final char SetTupleSelL = '\uFA58';
    public static final char SetTupleSelR = '\uFA59';

    public static final char FLTLAlways = '\uFA60'; // sym.FLTL_ALWAYS
    public static final char FLTLSometimes = '\uFA61'; // sym.FLTL_SOMETIMES // Eventually
    public static final char FLTLNext = '\uFA62';  // sym.FLTL_NEXT // Next
    public static final char FLTLUntil = '\uFA63'; // sym.FLTL_UNTIL // Until
    public static final char FLTLWkUntil = '\uFA64'; // sym.FLTL_WKUNTIL // Weak Until

}
