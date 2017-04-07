package ltlwidgets;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;


public class XML2LTL extends Throwable{

    private String m_xml;
    private String m_str;
    private int m_errorPos;
    private String m_errorMsg;
    private boolean m_hasErrors;

    private HashMap<String,String> m_symbols;

    private XML2LTL(){}

    public XML2LTL(String xml){
        m_xml = xml;
        m_str = "";
        m_errorPos = -1;
        m_errorMsg = "";
        m_hasErrors = false;
        m_str = buildString();
    }
    private String buildString(){
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = null;
        try {
                db = dbf.newDocumentBuilder();
        } catch (Exception e) {
                e.printStackTrace();
        }
        Document doc = null;
        ByteArrayInputStream byteStream;
        byteStream = new ByteArrayInputStream(m_xml.getBytes());
        try {
                doc = db.parse(byteStream);
        } catch (Exception e) {
                //e.printStackTrace();
                setError(0,"Error in reading the XML data!");
                return "";
        }
        if (doc.getChildNodes().getLength() != 1){
            setError(0,"Error, should have only one child.");
            return "";
        }
        return processSentence(doc.getFirstChild());
    }
    private void setError(int pos, String msg){
        m_errorPos = pos;
        m_errorMsg = msg;
        m_hasErrors = true;
    }
    public String getXML(){
        return new String(m_xml);
    }
    public String getString(){
        return new String(m_str);
    }
    public boolean hasErrors(){
        return m_hasErrors;
    }
    public int getFirstErrorPos(){
        if (!m_hasErrors)
            return -1;
        return m_errorPos;
    }
    public String getFirstErrorMsg(){
        if (!m_hasErrors)
            return "";
        return m_errorMsg;
    }




    // We could avoid this by using a schema!!!
    protected String processSentence(Node node){
        String res = "";
        String name = node.getNodeName();
        if (name.equals(Names_of_LTL_OPS.LOGIC_SENTENCE)){
            res = processLogicExp(node.getFirstChild());
        }else{
            setError(0,"Expecting tag " + Names_of_LTL_OPS.LOGIC_SENTENCE);
            res="";
        }
        return res;
    }

    private String processLogicExp(Node node){
        String res = "";
        String name = node.getNodeName();
        if (name.equals(Names_of_LTL_OPS.BIN_LOGIC_OP))
            res = processBinLogicOp(node);
        else if (name.equals(Names_of_LTL_OPS.PREDICATE))
            res = processPredicate(node);
        else if (name.equals(Names_of_LTL_OPS.QUANT_LOGIC_OP))
            res = processQuantLogicOp(node);
        else if (name.equals(Names_of_LTL_OPS.PAREN)) // OK
            res = "("+processLogicExp(node.getFirstChild())+")";
        else if (name.equals(Names_of_LTL_OPS.UN_LOGIC_OP))
            res = processUnLogicOp(node);
        else
            res = processTerm(node); // See whether it's a term
        return res;
    }
    private String processBinLogicOp(Node node){
        String res = "";
        if (node.getChildNodes().getLength()>1){
            setError(0,"Only one child allowed.");
            return "";
        }
        node = node.getFirstChild(); // TODO check that there is only one child
        String name = node.getNodeName();
        char code = 0;
        if (name.equals(Names_of_LTL_OPS.AND))
            code = Unicode_of_LTL_OPS.LAnd;
        else if (name.equals(Names_of_LTL_OPS.OR))
            code = Unicode_of_LTL_OPS.LOr;
        else if (name.equals(Names_of_LTL_OPS.IMP))
            code = Unicode_of_LTL_OPS.LImplies;
        else if (name.equals(Names_of_LTL_OPS.EQUIV))
            code = Unicode_of_LTL_OPS.EQUIV;
        else {
            setError(0,"Unknown binary operation.");
            return "";
        }
        // There has to be two different nodes.
        Node leftNode;
        Node rightNode;
        if (node.getFirstChild().getNodeName().equals(Names_of_LTL_OPS.LEFT)){
            leftNode = node.getFirstChild().getFirstChild();
            rightNode = node.getLastChild().getFirstChild();
        }else{
            rightNode = node.getFirstChild().getFirstChild();
            leftNode = node.getLastChild().getFirstChild();
        }
        res = processLogicExp(leftNode) + code + processLogicExp(rightNode);
        return res;
    }

    private String processPredicate(Node node){
        String res = "";
        if (node.getChildNodes().getLength()>1){
            setError(0,"Only one child allowed.");
            return "";
        }
        String nameAttr = node.getAttributes().getNamedItem("name").getNodeValue();
        String params = "";
        node = node.getFirstChild(); 
        if (!node.getNodeName().equals(Names_of_LTL_OPS.TERM_LIST)){
            setError(0,"Expecting "+Names_of_LTL_OPS.TERM_LIST);
            return "";
        }
        NodeList nodeList = node.getChildNodes();
        if (nodeList.getLength()>0){
            node = nodeList.item(0);
            if (!node.getNodeName().equals(Names_of_LTL_OPS.TERM_LIST_ELEM)){
                setError(0,"Expecting "+Names_of_LTL_OPS.TERM_LIST_ELEM);
                return "";
            }
            params = processTerm(node.getFirstChild());
        }
        for (int i=1; i<nodeList.getLength();i++){
            node = nodeList.item(i);
            if (!node.getNodeName().equals(Names_of_LTL_OPS.TERM_LIST_ELEM)){
                setError(0,"Expecting "+Names_of_LTL_OPS.TERM_LIST_ELEM);
                return "";
            }
            params += ","+processTerm(node.getFirstChild());
        }
        res = nameAttr+"("+params+")";
        return res;
    }
    private String processQuantLogicOp(Node node){
        String res = "";
        node = node.getFirstChild(); // TODO check that there are two children
        String name = node.getNodeName();
        char code = 0;
        if (name.equals(Names_of_LTL_OPS.FORALL))
            code = Unicode_of_LTL_OPS.FOLForAll;
        else if (name.equals(Names_of_LTL_OPS.EXISTS))
            code = Unicode_of_LTL_OPS.FOLExists;
        else if (name.equals(Names_of_LTL_OPS.NEXISTS))
            code = Unicode_of_LTL_OPS.FOLNotExists;
        else {
            setError(0,"Unknown Quantified Logic operation.");
            return "";
        }
        String vars = "";
        String scopeStr = processLogicExp(node.getLastChild());
        node = node.getFirstChild();
        if (!node.getNodeName().equals(Names_of_LTL_OPS.VAR_LIST)){
            setError(0,"Expecting "+Names_of_LTL_OPS.VAR_LIST);
            return "";
        }
        NodeList nodeList = node.getChildNodes();
        if (nodeList.getLength()>0){
            node = nodeList.item(0);
            if (!node.getNodeName().equals(Names_of_LTL_OPS.VAR_LIST_ELEM)){
                setError(0,"Expecting "+Names_of_LTL_OPS.VAR_LIST_ELEM);
                return "";
            }
            vars = processTerm(node.getFirstChild()); // Should be either processVariable or processSetMembOp
        }
        for (int i=1; i<nodeList.getLength();i++){
            node = nodeList.item(i);
            if (!node.getNodeName().equals(Names_of_LTL_OPS.VAR_LIST_ELEM)){
                setError(0,"Expecting "+Names_of_LTL_OPS.VAR_LIST_ELEM);
                return "";
            }
            vars += ","+processTerm(node.getFirstChild());
        }
        res = ""+code+vars+Unicode_of_LTL_OPS.FOLScope+"("+scopeStr+")";
        return res;
    }
    private String processUnLogicOp(Node node){
        String res = "";
        if (node.getChildNodes().getLength()!=1){
            setError(0,"Only one child expected.");
            return "";
        }
        node = node.getFirstChild();
        String name = node.getNodeName();
        if (name.equals(Names_of_LTL_OPS.NOT))
            res = ""+Unicode_of_LTL_OPS.LNot;
        // LTL
        else if (name.equals(Names_of_LTL_OPS.FLTLAlways))
            res = ""+Unicode_of_LTL_OPS.FLTLAlways;
        else if (name.equals(Names_of_LTL_OPS.FLTLNext))
            res = ""+Unicode_of_LTL_OPS.FLTLNext;
        else if (name.equals(Names_of_LTL_OPS.FLTLSometimes))
            res = ""+Unicode_of_LTL_OPS.FLTLSometimes;
        else if (name.equals(Names_of_LTL_OPS.FLTLUntil))
            res = ""+Unicode_of_LTL_OPS.FLTLUntil;
        else if (name.equals(Names_of_LTL_OPS.FLTLWkUntil))
            res = ""+Unicode_of_LTL_OPS.FLTLWkUntil;
        else{
            setError(0,"Unknown unary logic operation.");
            return "";
        }
        res += processLogicExp(node.getFirstChild());
        return res;
    }
    private String processTerm(Node node){
        String res = "";
        String name = node.getNodeName();
        if (name.equals(Names_of_LTL_OPS.CONSTANT))
            res = processConstant(node);
        else if (name.equals(Names_of_LTL_OPS.FUNCTION))
            res = processFunction(node);
        else if (name.equals(Names_of_LTL_OPS.VARIABLE))
            res = processVariable(node);
        else
            res = processExp(node); // see whether it's an arithmetic expression etc.
        return res;
    }
    private String processConstant(Node node){
        // TODO enhance error checking.
        String res = "";
        node = node.getFirstChild(); // TODO check that there is only one child
        String name = node.getNodeName();
        char code = 0;
        if (name.equals(Names_of_LTL_OPS.BOOL))
            res = node.getLastChild().getNodeValue(); // TODO this value has to be either true or false
        else if (name.equals(Names_of_LTL_OPS.NUM))
            res = node.getFirstChild().getLastChild().getNodeValue(); // The first child node name has to be either
        else if (name.equals(Names_of_LTL_OPS.STR))
            res = "\""+node.getLastChild().getNodeValue()+"\""; 
        else if (name.equals(Names_of_LTL_OPS.EMPTY))
            res = ""+Unicode_of_LTL_OPS.SetEmpty;
        else {
            setError(0,"Unknown Constant value.");
            return "";
        }
        return res;
    }
    private String processFunction(Node node){
        String res = "";
        if (node.getChildNodes().getLength()>1){
            setError(0,"Only one child allowed.");
            return "";
        }
        String nameAttr = node.getAttributes().getNamedItem("name").getNodeValue();
        String params = "";
        node = node.getFirstChild(); 
        if (!node.getNodeName().equals(Names_of_LTL_OPS.PARAM_LIST)){
            setError(0,"Expecting "+Names_of_LTL_OPS.PARAM_LIST);
            return "";
        }
        NodeList nodeList = node.getChildNodes();
        if (nodeList.getLength()>0){
            node = nodeList.item(0);
            if (!node.getNodeName().equals(Names_of_LTL_OPS.PARAM_LIST_ELEM)){
                setError(0,"Expecting "+Names_of_LTL_OPS.PARAM_LIST_ELEM);
                return "";
            }
            params = processTerm(node.getFirstChild());
        }
        for (int i=1; i<nodeList.getLength();i++){
            node = nodeList.item(i);
            if (!node.getNodeName().equals(Names_of_LTL_OPS.PARAM_LIST_ELEM)){
                setError(0,"Expecting "+Names_of_LTL_OPS.PARAM_LIST_ELEM);
                return "";
            }
            params += ","+processTerm(node.getFirstChild());
        }
        res = nameAttr+"("+params+")";
        return res;
    }
    private String processVariable(Node node){
        String res = "";
        if (node.getChildNodes().getLength()!=1){
            setError(0,"Only one variable name expected.");
            return "";
        }
        res = node.getLastChild().getNodeValue(); // The name of this!
        return res;
    }
    private String processExp(Node node){
        String res = "";
        String name = node.getNodeName();
        if (name.equals(Names_of_LTL_OPS.BIN_ARITH_OP))
            res = processBinArithOp(node);
        else if (name.equals(Names_of_LTL_OPS.REL_OP))
            res = processRelOp(node);
        else if (name.equals(Names_of_LTL_OPS.PAREN)) // OK CHECK this!!!
            res = "("+processExp(node.getFirstChild())+")";
        else if (name.equals(Names_of_LTL_OPS.SET_DEF))
            res = processSetDef(node);
        else if (name.equals(Names_of_LTL_OPS.SET_MEMB_OP))
            res = processSetMembOp(node);
        else if (name.equals(Names_of_LTL_OPS.SET_OP))
            res = processSetOp(node);
        else if (name.equals(Names_of_LTL_OPS.TUPLE_SEL))
            res = processTupleSel(node);
        else if (name.equals(Names_of_LTL_OPS.UN_ARITH_OP))
            res = processUnArithOp(node);
        else{
            setError(0,"Unknown exp:"+name);
            fillInStackTrace();
            printStackTrace();
            return "";
        }
        return res;
    }
    private String processBinArithOp(Node node){
        String res = "";
        if (node.getChildNodes().getLength()>1){
            setError(0,"Only one child allowed.");
            return "";
        }
        node = node.getFirstChild(); // TODO check that there is only one child
        String name = node.getNodeName();
        char code = 0;
        if (name.equals(Names_of_LTL_OPS.MINUS))
            code = Unicode_of_LTL_OPS.ArithMinus;
        else if (name.equals(Names_of_LTL_OPS.PLUS))
            code = Unicode_of_LTL_OPS.ArithPlus;
        else if (name.equals(Names_of_LTL_OPS.MULT))
            code = Unicode_of_LTL_OPS.ArithMult;
        else if (name.equals(Names_of_LTL_OPS.DIV))
            code = Unicode_of_LTL_OPS.ArithDiv;
        else if (name.equals(Names_of_LTL_OPS.MOD))
            code = Unicode_of_LTL_OPS.ArithMod;
        else {
            setError(0,"Unknown arithmetic binary operation.");
            return "";
        }
        // There has to be two different nodes.
        if (node.getChildNodes().getLength()!=2){
            setError(0,"Two child nodes needed.");
            return "";
        }
        Node leftNode;
        Node rightNode;
        if (node.getFirstChild().getNodeName().equals(Names_of_LTL_OPS.LEFT)){
            leftNode = node.getFirstChild().getFirstChild();
            rightNode = node.getLastChild().getFirstChild();
        }else{
            rightNode = node.getFirstChild().getFirstChild();
            leftNode = node.getLastChild().getFirstChild();
        }
        res = processLogicExp(leftNode) + code + processLogicExp(rightNode);
        return res;
    }
    private String processRelOp(Node node){
        String res = "";
        if (node.getChildNodes().getLength()>1){
            setError(0,"Only one child allowed.");
            return "";
        }
        node = node.getFirstChild(); // TODO check that there is only one child
        String name = node.getNodeName();
        char code = 0;
        if (name.equals(Names_of_LTL_OPS.EQ))
            code = Unicode_of_LTL_OPS.Eq;
        else if (name.equals(Names_of_LTL_OPS.NEQ))
            code = Unicode_of_LTL_OPS.RelNEq;
        else if (name.equals(Names_of_LTL_OPS.GT))
            code = Unicode_of_LTL_OPS.RelGT;
        else if (name.equals(Names_of_LTL_OPS.LT))
            code = Unicode_of_LTL_OPS.RelLT;
        else if (name.equals(Names_of_LTL_OPS.GEQ))
            code = Unicode_of_LTL_OPS.RelGTEq;
        else if (name.equals(Names_of_LTL_OPS.LEQ))
            code = Unicode_of_LTL_OPS.RelLTEq;
        else {
            setError(0,"Unknown relational operation.");
            return "";
        }
        // There has to be two different nodes.
        if (node.getChildNodes().getLength()!=2){
            setError(0,"Two child nodes needed.");
            return "";
        }
        Node leftNode;
        Node rightNode;
        if (node.getFirstChild().getNodeName().equals(Names_of_LTL_OPS.LEFT)){
            leftNode = node.getFirstChild().getFirstChild();
            rightNode = node.getLastChild().getFirstChild();
        }else{
            rightNode = node.getFirstChild().getFirstChild();
            leftNode = node.getLastChild().getFirstChild();
        }
        res = processLogicExp(leftNode) + code + processLogicExp(rightNode);
        return res;
    }
    private String processSetDef(Node node){
        String res = "";
        node = node.getFirstChild(); 
        if (!node.getNodeName().equals(Names_of_LTL_OPS.TERM_LIST)){
            setError(0,"Expecting "+Names_of_LTL_OPS.TERM_LIST);
            return "";
        }
        String terms = "";
        NodeList nodeList = node.getChildNodes();
        if (nodeList.getLength()>0){
            node = nodeList.item(0);
            if (!node.getNodeName().equals(Names_of_LTL_OPS.TERM_LIST_ELEM)){
                setError(0,"Expecting "+Names_of_LTL_OPS.TERM_LIST_ELEM);
                return "";
            }
            terms = processTerm(node.getFirstChild());
        }
        for (int i=1; i<nodeList.getLength();i++){
            node = nodeList.item(i);
            if (!node.getNodeName().equals(Names_of_LTL_OPS.TERM_LIST_ELEM)){
                setError(0,"Expecting "+Names_of_LTL_OPS.TERM_LIST_ELEM);
                return "";
            }
            terms += ","+processTerm(node.getFirstChild());
        }
        res = "{"+terms+"}";
        return res;
    }
    private String processSetMembOp(Node node){
        String res = "";
        // There has to be two different nodes.
        if (node.getChildNodes().getLength()!=2){
            setError(0,"Two child nodes needed.");
            return "";
        }
        Node leftNode;
        Node rightNode;
        if (node.getFirstChild().getNodeName().equals(Names_of_LTL_OPS.LEFT)){
            leftNode = node.getFirstChild().getFirstChild();
            rightNode = node.getLastChild().getFirstChild();
        }else{
            rightNode = node.getFirstChild().getFirstChild();
            leftNode = node.getLastChild().getFirstChild();
        }
        res = processLogicExp(leftNode) + Unicode_of_LTL_OPS.SetBelongs + processLogicExp(rightNode);
        return res;
    }
    private String processSetOp(Node node){
        String res = "";
        if (node.getChildNodes().getLength()>1){
            setError(0,"Only one child allowed.");
            return "";
        }
        node = node.getFirstChild(); // TODO check that there is only one child
        String name = node.getNodeName();
        char code = 0;
        if (name.equals(Names_of_LTL_OPS.SUBSET))
            code = Unicode_of_LTL_OPS.SetSubset;
        else if (name.equals(Names_of_LTL_OPS.NSUBSET))
            code = Unicode_of_LTL_OPS.SetNotSubset;
        else if (name.equals(Names_of_LTL_OPS.SUBEQSET))
            code = Unicode_of_LTL_OPS.SetSubsetSet;
        else if (name.equals(Names_of_LTL_OPS.UNION))
            code = Unicode_of_LTL_OPS.SetUnion;
        else if (name.equals(Names_of_LTL_OPS.DIFF))
            code = Unicode_of_LTL_OPS.SetDifference;
        else if (name.equals(Names_of_LTL_OPS.NIN))
            code = Unicode_of_LTL_OPS.SetNotBelongs;
        else if (name.equals(Names_of_LTL_OPS.IN))
            code = Unicode_of_LTL_OPS.SetBelongs;
        else {
            setError(0,"Unknown set operation.");
            return "";
        }
        // There has to be two different nodes.
        if (node.getChildNodes().getLength()!=2){
            setError(0,"Two child nodes needed.");
            return "";
        }
        Node leftNode;
        Node rightNode;
        if (node.getFirstChild().getNodeName().equals(Names_of_LTL_OPS.LEFT)){
            leftNode = node.getFirstChild().getFirstChild();
            rightNode = node.getLastChild().getFirstChild();
        }else{
            rightNode = node.getFirstChild().getFirstChild();
            leftNode = node.getLastChild().getFirstChild();
        }
        res = processLogicExp(leftNode) + code + processLogicExp(rightNode);
        return res;
    }
    private String processTupleSel(Node node){
        String res = "";
        Node leftNode;
        Node rightNode;
        if (node.getFirstChild().getNodeName().equals(Names_of_LTL_OPS.LEFT)){
            leftNode = node.getFirstChild().getFirstChild();
            rightNode = node.getLastChild().getFirstChild();
        }else{
            rightNode = node.getFirstChild().getFirstChild();
            leftNode = node.getLastChild().getFirstChild();
        }
        res = processLogicExp(leftNode) + "[" + processLogicExp(rightNode)+"]";
        return res;
    }
    private String processUnArithOp(Node node){
        String res = "";
    if (node.getChildNodes().getLength()!=1){
        setError(0,"Only one child expected.");
        return "";
    }
    String name = node.getNodeName();
    if (name.equals(Names_of_LTL_OPS.UN_ARITH_OP))
        res = ""+Unicode_of_LTL_OPS.ArithMinus+processLogicExp(node.getFirstChild());
    else{
        setError(0,"Unknown unary arithmetic operation.");
        return "";
    }
    return res;

    }


}
