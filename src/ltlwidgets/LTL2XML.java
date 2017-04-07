package ltlwidgets;

import ltlparser.ltlabsyntree.Variable;
import ltlparser.ltlabsyntree.Predicate;
import ltlparser.ltlabsyntree.Identifier;
import ltlparser.ltlabsyntree.QuantLogicOp;
import ltlparser.visitor.Visitor;
import ltlparser.ltlabsyntree.Function;
import ltlparser.ltlabsyntree.UnLogicOp;
import ltlparser.ltlabsyntree.BinArithOp;
import ltlparser.ltlabsyntree.SetOp;
import ltlparser.ltlabsyntree.UnArithOp;
import ltlparser.ltlabsyntree.SetMembOp;
import ltlparser.ltlabsyntree.BinLogicOp;
import ltlparser.sym;
import ltlparser.ltlabsyntree.SetDef;
import ltlparser.ltlabsyntree.TupleSel;
import ltlparser.ltlabsyntree.LogicSentence;
import ltlparser.ltlabsyntree.Constant;
import ltlparser.ltlabsyntree.RelOp;
import ltlparser.ltlabsyntree.SentenceWPar;
import ltlparser.ParseLTL;
import ltlparser.errormsg.ErrorMsg;


public class LTL2XML {

    // Just keep the first error message.
    private ErrorMsg m_errorMsgs = null;
    private String m_xml = "";
    private String m_str = "";

    public LTL2XML(String str) {
        m_str = str;
        m_xml = buildXML();
    }

    private String buildXML(){
        java.io.Reader inp = (java.io.Reader) (new java.io.StringReader(m_str));
        m_errorMsgs = new ErrorMsg(m_str);
        ParseLTL parse = new ParseLTL(m_str, m_errorMsgs);
        if (m_errorMsgs.anyErrors) {
            return "";
        }
        // Get the abstract syntax tree.
        LogicSentence sentence = parse.absyn;
        XMLVisitor gen = new XMLVisitor();
        gen.visit(sentence);
        return gen.getXML();
    }
    public String getXML(){
        return new String(m_xml);
    }
    public String getString(){
        return new String(m_str);
    }
    public boolean hasErrors(){
        return m_errorMsgs.anyErrors;
    }
    public int getFirstErrorPos(){
        if (!m_errorMsgs.anyErrors)
            return -1;
        ErrorMsg.Msg msg = (ErrorMsg.Msg)m_errorMsgs.getMsgs().firstElement();
        return msg.pos;
    }
    public String getFirstErrorMsg(){
        if (!m_errorMsgs.anyErrors)
            return "";
        ErrorMsg.Msg msg = (ErrorMsg.Msg)m_errorMsgs.getMsgs().firstElement();
        return msg.msg;
    }

}

class XMLVisitor implements Visitor {
    StringBuffer xmlStr;

    public XMLVisitor() {
        xmlStr = new StringBuffer();
    }

    public String getXML() {
        return xmlStr.toString();
    }

    public void visit(LogicSentence elem) {
        xmlStr.append("<"+Names_of_LTL_OPS.LOGIC_SENTENCE+">");
        elem.exp.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.LOGIC_SENTENCE+">");
    }

    public void visit(Variable elem) {
        xmlStr.append("<"+Names_of_LTL_OPS.VARIABLE+">");
        elem.id.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.VARIABLE+">");
    }

    public void visit(UnLogicOp elem) {
        String elemName;
        switch(elem.type)
        {
        case sym.NOT:
            elemName = Names_of_LTL_OPS.NOT;
            break;
        case sym.FLTL_ALWAYS:
            elemName = Names_of_LTL_OPS.FLTLAlways;
            break;
        case sym.FLTL_SOMETIMES:
            elemName = Names_of_LTL_OPS.FLTLSometimes;
            break;
        case sym.FLTL_NEXT:
            elemName = Names_of_LTL_OPS.FLTLNext;
            break;
        case sym.FLTL_UNTIL:
            elemName = Names_of_LTL_OPS.FLTLUntil;
            break;
        case sym.FLTL_WKUNTIL:
            elemName = Names_of_LTL_OPS.FLTLWkUntil;
            break;
        default:
            xmlStr.append("<ERROR>UnLogicOp</ERROR>");
            return;
        }

        xmlStr.append("<"+Names_of_LTL_OPS.UN_LOGIC_OP+">");
        xmlStr.append("<"+elemName+">");
        elem.l.accept(this);
        xmlStr.append("</"+elemName+">");
        xmlStr.append("</"+Names_of_LTL_OPS.UN_LOGIC_OP+">");
    }

    public void visit(UnArithOp elem) {
        if (elem.type == sym.MINUS) {
            xmlStr.append("<"+Names_of_LTL_OPS.UN_ARITH_OP+">");
            xmlStr.append("<"+Names_of_LTL_OPS.MINUS+">"); // Check MINUS
            elem.t.accept(this);
            xmlStr.append("</"+Names_of_LTL_OPS.MINUS+">");
            xmlStr.append("</"+Names_of_LTL_OPS.UN_ARITH_OP+">");
        } else {
            xmlStr.append("<ERROR>UnArithOp</ERROR>");
        }
    }

    public void visit(TupleSel elem) {
        xmlStr.append("<"+Names_of_LTL_OPS.TUPLE_SEL+">");
        xmlStr.append("<"+Names_of_LTL_OPS.LEFT+">");
        elem.tleft.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.LEFT+">");
        xmlStr.append("<"+Names_of_LTL_OPS.RIGHT+">");
        elem.tright.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.RIGHT+">");
        xmlStr.append("</"+Names_of_LTL_OPS.TUPLE_SEL+">");
    }

    public void visit(SetOp elem) {
        String elemName;
        switch (elem.type) {
        case sym.SUBSET:
            elemName = Names_of_LTL_OPS.SUBSET;
            break;
        case sym.NSUBSET:
            elemName = Names_of_LTL_OPS.NSUBSET;
            break;
        case sym.SUBEQSET:
            elemName = Names_of_LTL_OPS.SUBEQSET;
            break;
        case sym.UNION:
            elemName = Names_of_LTL_OPS.UNION;
            break;
        case sym.DIFF:
            elemName = Names_of_LTL_OPS.DIFF;
            break;
        case sym.NIN:
            elemName = Names_of_LTL_OPS.NIN;
            break;
        case sym.IN:
            elemName = Names_of_LTL_OPS.IN;
            break;
        default:
            xmlStr.append("<ERROR>SetOp</ERROR>");
            return;
        }
        xmlStr.append("<" + Names_of_LTL_OPS.SET_OP + ">");
        xmlStr.append("<" + elemName + ">");
        xmlStr.append("<"+Names_of_LTL_OPS.LEFT+">");
        elem.tleft.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.LEFT+">");
        xmlStr.append("<"+Names_of_LTL_OPS.RIGHT+">");
        elem.tright.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.RIGHT+">");
        xmlStr.append("</" + elemName + ">");
        xmlStr.append("</" + Names_of_LTL_OPS.SET_OP + ">");
    }

    public void visit(SetMembOp elem) {
        xmlStr.append("<"+Names_of_LTL_OPS.SET_MEMB_OP+">");
        xmlStr.append("<"+Names_of_LTL_OPS.LEFT+">");
        elem.v.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.LEFT+">");
        xmlStr.append("<"+Names_of_LTL_OPS.RIGHT+">");
        elem.vset.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.RIGHT+">");
        xmlStr.append("</"+Names_of_LTL_OPS.SET_MEMB_OP+">");
    }

    public void visit(RelOp elem) {
        String elemName;
        switch (elem.type) {
        case sym.EQ:
            elemName = Names_of_LTL_OPS.EQ;
            break;
        case sym.NEQ:
            elemName = Names_of_LTL_OPS.NEQ;
            break;
        case sym.GT:
            elemName = Names_of_LTL_OPS.GT;
            break;
        case sym.LT:
            elemName = Names_of_LTL_OPS.LT;
            break;
        case sym.GEQ:
            elemName = Names_of_LTL_OPS.GEQ;
            break;
        case sym.LEQ:
            elemName = Names_of_LTL_OPS.LEQ;
            break;
        default:
            xmlStr.append("<ERROR>RelOp</ERROR>");
            return;
        }
        xmlStr.append("<" + Names_of_LTL_OPS.REL_OP + ">");
        xmlStr.append("<" + elemName + ">");
        xmlStr.append("<"+Names_of_LTL_OPS.LEFT+">");
        elem.tleft.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.LEFT+">");
        xmlStr.append("<"+Names_of_LTL_OPS.RIGHT+">");
        elem.tright.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.RIGHT+">");
        xmlStr.append("</" + elemName + ">");
        xmlStr.append("</" + Names_of_LTL_OPS.REL_OP + ">");
    }

    public void visit(QuantLogicOp elem) {
        String elemName;
        switch (elem.type) {
        case sym.FORALL:
            elemName = Names_of_LTL_OPS.FORALL;
            break;
        case sym.EXISTS:
            elemName = Names_of_LTL_OPS.EXISTS;
            break;
        case sym.NEXISTS:
            elemName = Names_of_LTL_OPS.NEXISTS;
            break;
        default:
            xmlStr.append("<ERROR>QuantLogicOp</ERROR>");
            return;
        }
        xmlStr.append("<" + Names_of_LTL_OPS.QUANT_LOGIC_OP + ">");
        xmlStr.append("<" + elemName + ">");
        xmlStr.append("<"+Names_of_LTL_OPS.VAR_LIST+">");
        for (int i = 0; i < elem.qlist.size(); i++) {
            xmlStr.append("<"+Names_of_LTL_OPS.VAR_LIST_ELEM+">");
            elem.qlist.elementAt(i).accept(this);
            xmlStr.append("</"+Names_of_LTL_OPS.VAR_LIST_ELEM+">");
        }
        xmlStr.append("</"+Names_of_LTL_OPS.VAR_LIST+">");
        elem.exp.accept(this);
        xmlStr.append("</" + elemName + ">");
        xmlStr.append("</" + Names_of_LTL_OPS.QUANT_LOGIC_OP + ">");
    }

    public void visit(Identifier elem) {
        xmlStr.append(elem.s);
    }

    public void visit(Function elem) {
        // Function names start with lowercase
        xmlStr.append("<"+Names_of_LTL_OPS.FUNCTION+" name=\"");
        elem.name.accept(this);
        xmlStr.append("\">");
        xmlStr.append("<"+Names_of_LTL_OPS.PARAM_LIST+">");
        for (int i = 0; i < elem.params.size(); i++) {
            xmlStr.append("<"+Names_of_LTL_OPS.PARAM_LIST_ELEM+">");
            elem.params.elementAt(i).accept(this);
            xmlStr.append("</"+Names_of_LTL_OPS.PARAM_LIST_ELEM+">");
        }
        xmlStr.append("</"+Names_of_LTL_OPS.PARAM_LIST+">");
        xmlStr.append("</"+Names_of_LTL_OPS.FUNCTION+">");
        /*
             public Identifier name;
           public TermList params;
         */
    }

    public void visit(Constant elem) {
        String elemName;
        String elemInner;
        switch (elem.type) {
        case sym.BOOL:
            elemName = Names_of_LTL_OPS.BOOL;
            if (((Boolean) elem.obj).booleanValue() == true) {
                elemInner = Names_of_LTL_OPS.TRUE;
            } else {
                elemInner = Names_of_LTL_OPS.FALSE;
            }
            break;
        case sym.NUM:
            String str = (String) elem.obj;
            elemName = Names_of_LTL_OPS.NUM;
            if (str.indexOf('.') != -1) {
                elemInner = "<"+Names_of_LTL_OPS.NUM_REAL+">"+str+"</"+Names_of_LTL_OPS.NUM_REAL+">";
            } else {
                elemInner = "<"+Names_of_LTL_OPS.NUM_INTEGER+">"+str+"</"+Names_of_LTL_OPS.NUM_INTEGER+">";
            }
            break;
        case sym.STR:
            elemName = Names_of_LTL_OPS.STR;
            elemInner = (String)elem.obj;
            break;
        case sym.EMPTY:
            elemName = Names_of_LTL_OPS.EMPTY;
            elemInner = "";// Nothing to output
            break;
        default:
            xmlStr.append("<ERROR>Constant</ERROR>");
            return;
        }
        xmlStr.append("<" + Names_of_LTL_OPS.CONSTANT + ">");
        xmlStr.append("<" + elemName + ">");
        xmlStr.append(elemInner);
        xmlStr.append("</" + elemName + ">");
        xmlStr.append("</" + Names_of_LTL_OPS.CONSTANT + ">");

        /*
             public Object obj; // the reference to the "constant" object
           public int type; // the only admissible types are sym.BOOL, sym.NUM, sym.STR and sym.EMPTY
         */
    }

    public void visit(BinLogicOp elem) {
        String elemName;
        switch (elem.type) {
        case sym.AND:
            elemName = Names_of_LTL_OPS.AND;
            break;
        case sym.OR:
            elemName = Names_of_LTL_OPS.OR;
            break;
        case sym.IMP:
            elemName = Names_of_LTL_OPS.IMP;
            break;
        case sym.EQUIV:
            elemName = Names_of_LTL_OPS.EQUIV;
            break;
        default:
            xmlStr.append("<ERROR>BinLogicOp</ERROR>");
            return;
        }
        xmlStr.append("<" + Names_of_LTL_OPS.BIN_LOGIC_OP + ">");
        xmlStr.append("<" + elemName + ">");
        xmlStr.append("<"+Names_of_LTL_OPS.LEFT+">");
        elem.l1.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.LEFT+">");
        xmlStr.append("<"+Names_of_LTL_OPS.RIGHT+">");
        elem.l2.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.RIGHT+">");
        xmlStr.append("</" + elemName + ">");
        xmlStr.append("</" + Names_of_LTL_OPS.BIN_LOGIC_OP + ">");
    }

    public void visit(BinArithOp elem) {
        String elemName;
        switch (elem.type) {
        case sym.MINUS:
            elemName = Names_of_LTL_OPS.MINUS;
            break;
        case sym.PLUS:
            elemName = Names_of_LTL_OPS.PLUS;
            break;
        case sym.MULT:
            elemName = Names_of_LTL_OPS.MULT;
            break;
        case sym.DIV:
            elemName = Names_of_LTL_OPS.DIV;
            break;
        case sym.MOD:
            elemName = Names_of_LTL_OPS.MOD;
            break;
        default:
            xmlStr.append("<ERROR>BinLogicOp</ERROR>");
            return;
        }
        xmlStr.append("<" + Names_of_LTL_OPS.BIN_ARITH_OP + ">");
        xmlStr.append("<" + elemName + ">");
        xmlStr.append("<"+Names_of_LTL_OPS.LEFT+">");
        elem.tleft.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.LEFT+">");
        xmlStr.append("<"+Names_of_LTL_OPS.RIGHT+">");
        elem.tright.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.RIGHT+">");
        xmlStr.append("</" + elemName + ">");
        xmlStr.append("</" + Names_of_LTL_OPS.BIN_ARITH_OP + ">");
        /*
             public int type; // the type of the arithmetic operation
           public Term tleft;
           public Term tright;
         */
    }

    public void visit(Predicate elem) {
        // Predicate names are all uppercase.
        xmlStr.append("<"+Names_of_LTL_OPS.PREDICATE+" name=\"");
        elem.id.accept(this);
        xmlStr.append("\">");
        // CHECK this: predicates have at least one term in its list.
        // Can predicates actually have none?
        xmlStr.append("<"+Names_of_LTL_OPS.TERM_LIST+">");
        for (int i = 0; i < elem.termLst.size(); i++) {
            xmlStr.append("<"+Names_of_LTL_OPS.TERM_LIST_ELEM+">");
            elem.termLst.elementAt(i).accept(this);
            xmlStr.append("</"+Names_of_LTL_OPS.TERM_LIST_ELEM+">");
        }
        xmlStr.append("</"+Names_of_LTL_OPS.TERM_LIST+">");
        xmlStr.append("</"+Names_of_LTL_OPS.PREDICATE+">");
        /*public Identifier id;
           public TermList termLst;*/
    }

    public void visit(SetDef elem) {
        xmlStr.append("<"+Names_of_LTL_OPS.SET_DEF+">");
        xmlStr.append("<"+Names_of_LTL_OPS.TERM_LIST+">");
        for (int i = 0; i < elem.list.size(); i++) {
            xmlStr.append("<"+Names_of_LTL_OPS.TERM_LIST_ELEM+">");
            elem.list.elementAt(i).accept(this);
            xmlStr.append("</"+Names_of_LTL_OPS.TERM_LIST_ELEM+">");
        }
        xmlStr.append("</"+Names_of_LTL_OPS.TERM_LIST+">");
        xmlStr.append("</"+Names_of_LTL_OPS.SET_DEF+">");
        /*
          public TermList list;
         */
    }

    public void visit(SentenceWPar elem){
        xmlStr.append("<"+Names_of_LTL_OPS.PAREN+">");
        elem.exp.accept(this);
        xmlStr.append("</"+Names_of_LTL_OPS.PAREN+">");
    }

}
