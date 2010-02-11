/* Generated By:JJTree&JavaCC: Do not edit this line. Qbf_parser.java */
package main.java.logic.parser;
import java.util.Vector;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.io.*;

public class Qbf_parser/*@bgen(jjtree)*/implements Qbf_parserTreeConstants, Qbf_parserConstants {/*@bgen(jjtree)*/
  protected static JJTQbf_parserState jjtree = new JJTQbf_parserState();
        private static Vector<Integer> eVars = new Vector<Integer>();
        private static Vector<Integer> aVars = new Vector<Integer>();
        private static Vector<Integer> vars  = new Vector<Integer>();
        private static HashMap<Integer, Integer> literalCount  = new HashMap<Integer, Integer>();

        public HashMap<Integer, Integer> getLiteralCount() {
                return literalCount;
        }

        // thomas' wunschkonzert
        // anz variable negiert und nicht negiert
        // 


        public static void main(String[] args) {
                Qbf_parser parser;
                Node root = null;
                try {
                        parser = new Qbf_parser(new FileInputStream(args[0]));
                }
                catch (FileNotFoundException e) {
                        System.out.println("File not found: " + args[0]);
                        return;
                }

                try {
                        parser.Input();
                        System.out.println("Succesful parse");
                        root = parser.jjtree.rootNode();
//			root.dump("");

                        // convert internal tree to .qpro format
                        String traversedTree = root.traverse();

//			System.out.println("\nQBF\n"+vars.size()+"\n"+traversedTree+"\nQBF\n");
                        System.out.println("vars: " + vars);
                        System.out.println("all-quantified vars: " + aVars);
                        System.out.println("exists-quantified vars: " + eVars);
                        System.out.println("hashmap: " + literalCount);
                }
                catch (ParseException e) {
                        System.out.println("Parse error");
                        System.out.println(e);
                        return;
                }
                catch (TokenMgrError e) {
                        System.out.println(e);
                        return;
                }

        }

// non-terminals
// *	<input>		::= <exp> EOF
  static final public void Input() throws ParseException {
 /*@bgen(jjtree) Input */
  ASTInput jjtn000 = new ASTInput(JJTINPUT);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      Exp();
      jj_consume_token(0);
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

// *	<exp>		::= <NOT> <exp> | <q_set> <exp> | <LP> <exp> <op> <exp> <RP>
// *				| <LP> <exp> <RP> | <VAR>
  static final public void Exp() throws ParseException {
 /*@bgen(jjtree) Exp */
  ASTExp jjtn000 = new ASTExp(JJTEXP);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case NOT:
        Not();
        Exp();
        break;
      case EXISTS:
      case FORALL:
        Q_set();
        Exp();
        break;
      case LP:
        jj_consume_token(LP);
               jjtn000.debugInfo = "(";
        Exp();
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case AND:
        case OR:
          Op();
          Exp();
          break;
        default:
          jj_la1[0] = jj_gen;
          ;
        }
        jj_consume_token(RP);
               jjtree.closeNodeScope(jjtn000, true);
               jjtc000 = false;
               jjtn000.debugInfo = ")";
        break;
      case VAR:
        Var("");
        break;
      case 0:
        jj_consume_token(0);
        break;
      default:
        jj_la1[1] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

// *	<q_set> 	::= <quant> <LSP> <var_list> <RSP>
  static final public void Q_set() throws ParseException {
 /*@bgen(jjtree) Q_set */
        ASTQ_set jjtn000 = new ASTQ_set(JJTQ_SET);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);String s;
    try {
      s = Quant();
      jj_consume_token(LSP);
                jjtn000.debugInfo="[";
      Var(s);
      jj_consume_token(RSP);
                jjtree.closeNodeScope(jjtn000, true);
                jjtc000 = false;
                jjtn000.debugInfo="]";
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

// *	<quant> 	::= <EXISTS> | <FORALL>
  static final public String Quant() throws ParseException {
 /*@bgen(jjtree) Quant */
  ASTQuant jjtn000 = new ASTQuant(JJTQUANT);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case EXISTS:
        Exists();
                   jjtree.closeNodeScope(jjtn000, true);
                   jjtc000 = false;
                   jjtn000.op = "e"; {if (true) return "e";}
        break;
      case FORALL:
        Forall();
                   jjtree.closeNodeScope(jjtn000, true);
                   jjtc000 = false;
                   jjtn000.op = "f"; {if (true) return "f";}
        break;
      default:
        jj_la1[2] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
    throw new Error("Missing return statement in function");
  }

// *	<var_list> 	::= <VAR> <var_list> | <VAR>
//void Var_list():
//{}
//{
////	Var()
////	|
////	Var()
//}

// *	<op>		::= <OR> | <AND>
  static final public void Op() throws ParseException {
 /*@bgen(jjtree) Op */
  ASTOp jjtn000 = new ASTOp(JJTOP);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case OR:
        Or();
               jjtree.closeNodeScope(jjtn000, true);
               jjtc000 = false;
               jjtn000.op = "|";
        break;
      case AND:
        And();
                jjtree.closeNodeScope(jjtn000, true);
                jjtc000 = false;
                jjtn000.op = "&";
        break;
      default:
        jj_la1[3] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
    } catch (Throwable jjte000) {
          if (jjtc000) {
            jjtree.clearNodeScope(jjtn000);
            jjtc000 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte000 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte000;}
          }
          if (jjte000 instanceof ParseException) {
            {if (true) throw (ParseException)jjte000;}
          }
          {if (true) throw (Error)jjte000;}
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

// content nodes
  static final public void And() throws ParseException {
 /*@bgen(jjtree) And */
  ASTAnd jjtn000 = new ASTAnd(JJTAND);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(AND);
                jjtree.closeNodeScope(jjtn000, true);
                jjtc000 = false;
                jjtn000.op = "&";
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void Or() throws ParseException {
 /*@bgen(jjtree) Or */
  ASTOr jjtn000 = new ASTOr(JJTOR);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(OR);
               jjtree.closeNodeScope(jjtn000, true);
               jjtc000 = false;
               jjtn000.op = "|";
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void Not() throws ParseException {
 /*@bgen(jjtree) Not */
  ASTNot jjtn000 = new ASTNot(JJTNOT);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(NOT);
                jjtree.closeNodeScope(jjtn000, true);
                jjtc000 = false;
                jjtn000.op = "!";
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void Exists() throws ParseException {
 /*@bgen(jjtree) Exists */
  ASTExists jjtn000 = new ASTExists(JJTEXISTS);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(EXISTS);
                   jjtree.closeNodeScope(jjtn000, true);
                   jjtc000 = false;
                   jjtn000.op = "e"; System.out.println("EXISTSEXISTSEXISTSEXISTSEXISTS" + jjtn000.op + jjtn000.getClass().getName());
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void Forall() throws ParseException {
 /*@bgen(jjtree) Forall */
  ASTForall jjtn000 = new ASTForall(JJTFORALL);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(FORALL);
                   jjtree.closeNodeScope(jjtn000, true);
                   jjtc000 = false;
                   jjtn000.op = "f";
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static final public void Var(String s) throws ParseException {
 /*@bgen(jjtree) Var */
        ASTVar jjtn000 = new ASTVar(JJTVAR);
        boolean jjtc000 = true;
        jjtree.openNodeScope(jjtn000);Token t;
    try {
      t = jj_consume_token(VAR);
          jjtree.closeNodeScope(jjtn000, true);
          jjtc000 = false;
                // Stripping down the variable name to a number (e.g. "v123" -> 123 and
                // adding it to a vector containing all variable numbers as well as to a
                // vector with all exist- or allquantified variables (that's the reason
                // for the funny String s fallthrough)
                String varName = t.image.replaceAll("[a-z]*","");
                int varNumber = Integer.valueOf(varName).intValue();
                jjtn000.var = varNumber;
                if (s == "e")
                        eVars.add(varNumber);
                if (s == "f")
                        aVars.add(varNumber);
                if (s == "") {

                        if (literalCount.get(varNumber) != null) {
                                literalCount.put(varNumber, literalCount.get(varNumber) + 1);
                        }
                        else {
                                literalCount.put(varNumber, 1);
                        }
                        if (!vars.contains(varNumber)) {
                                vars.add(varNumber);
                        }
                }
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  static private boolean jj_initialized_once = false;
  /** Generated Token Manager. */
  static public Qbf_parserTokenManager token_source;
  static SimpleCharStream jj_input_stream;
  /** Current token. */
  static public Token token;
  /** Next token. */
  static public Token jj_nt;
  static private int jj_ntk;
  static private int jj_gen;
  static final private int[] jj_la1 = new int[4];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x60,0x7181,0x3000,0x60,};
   }

  /** Constructor with InputStream. */
  public Qbf_parser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public Qbf_parser(java.io.InputStream stream, String encoding) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser.  ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new Qbf_parserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  static public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public Qbf_parser(java.io.Reader stream) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new Qbf_parserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  static public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public Qbf_parser(Qbf_parserTokenManager tm) {
    if (jj_initialized_once) {
      System.out.println("ERROR: Second call to constructor of static parser. ");
      System.out.println("       You must either use ReInit() or set the JavaCC option STATIC to false");
      System.out.println("       during parser generation.");
      throw new Error();
    }
    jj_initialized_once = true;
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(Qbf_parserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  static private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  static final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  static final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  static private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  static private java.util.List jj_expentries = new java.util.ArrayList();
  static private int[] jj_expentry;
  static private int jj_kind = -1;

  /** Generate ParseException. */
  static public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[15];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 4; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 15; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  static final public void enable_tracing() {
  }

  /** Disable tracing. */
  static final public void disable_tracing() {
  }

}
