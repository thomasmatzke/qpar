/* Generated By:JJTree&JavaCC: Do not edit this line. Qbf_parser.java */
package main.java.logic.parser;
import java.util.Vector;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.io.*;
import main.java.logic.parser.SimpleNode.NodeType;

public class Qbf_parser implements/*@bgen(jjtree)*/ Qbf_parserTreeConstants,Serializable, Qbf_parserConstants {/*@bgen(jjtree)*/
  protected JJTQbf_parserState jjtree = new JJTQbf_parserState();
        private Vector<Integer> eVars = new Vector<Integer>();
        private Vector<Integer> aVars = new Vector<Integer>();
        private Vector<Integer> vars  = new Vector<Integer>();
        private HashMap<Integer, Integer> literalCount  = new HashMap<Integer, Integer>();
        private HashMap<String, Integer> varNames = new HashMap<String, Integer>();
        private int nextVarInt = 2;
        private SimpleNode root;

        public Qbf_parser() {
                jjtree.reset();
        }

        public HashMap<Integer, Integer> getLiteralCount() {
                return this.literalCount;
        }

        public Vector<Integer> getEVars() {
                return this.eVars;
        }

        public Vector<Integer> getAVars() {
                return this.aVars;
        }

        public Vector<Integer> getVars() {
                return this.vars;
        }

        public SimpleNode getRootNode() {
                return (SimpleNode)this.jjtree.rootNode();
        }

// non-terminals
// *	<input>		::= <exp> EOF
  final public void Input() throws ParseException {
 /*@bgen(jjtree) Input */
  ASTInput jjtn000 = new ASTInput(JJTINPUT);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
          jjtn000.nodeType = NodeType.START;
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

  final public void And() throws ParseException {
 /*@bgen(jjtree) And */
  ASTAnd jjtn000 = new ASTAnd(JJTAND);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(AND);
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  final public void Or() throws ParseException {
 /*@bgen(jjtree) Or */
  ASTOr jjtn000 = new ASTOr(JJTOR);
  boolean jjtc000 = true;
  jjtree.openNodeScope(jjtn000);
    try {
      jj_consume_token(OR);
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

  final public void Not() throws ParseException {
    jj_consume_token(NOT);
  }

  final public void Exists() throws ParseException {
    jj_consume_token(EXISTS);
  }

  final public void Forall() throws ParseException {
    jj_consume_token(FORALL);
  }

  final public void Var() throws ParseException {
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
                /// vector with all exist- or allquantified variables (that's the reason
                // for the funny String s fallthrough) 
                //String varName = t.image.replaceAll("[a-z]*","");
                //int varNumber = Integer.valueOf(varName).intValue();
                int varNumber = 0;

                if (varNames.containsKey(t.image)) {
                        jjtn000.var = varNames.get(t.image);
                }
                else {
                        varNames.put(t.image,nextVarInt);
                        jjtn000.var = nextVarInt;
                        nextVarInt++;
                }

                jjtn000.nodeType = NodeType.VAR;
                varNumber = jjtn000.var;

                if (literalCount.get(varNumber) != null) {
                        literalCount.put(varNumber, literalCount.get(varNumber) + 1);
                }
                else {
                        literalCount.put(varNumber, 1);
                }
                if (!vars.contains(varNumber)) {
                        vars.add(varNumber);
                }
    } finally {
          if (jjtc000) {
            jjtree.closeNodeScope(jjtn000, true);
          }
    }
  }

// *	<exp>		::= <NOT> <exp> | <q_set> <exp> | <LP> <exp> <op> <exp> <RP>
// *				| <LP> <exp> <RP> | <VAR>
  final public void Exp() throws ParseException {
        String s = "";
        String op = "";
        Token t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NOT:
          ASTLogical jjtn001 = new ASTLogical(JJTLOGICAL);
          boolean jjtc001 = true;
          jjtree.openNodeScope(jjtn001);
      try {
        Not();
                        jjtn001.nodeType = NodeType.NOT;
        Exp();
      } catch (Throwable jjte001) {
          if (jjtc001) {
            jjtree.clearNodeScope(jjtn001);
            jjtc001 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte001 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte001;}
          }
          if (jjte001 instanceof ParseException) {
            {if (true) throw (ParseException)jjte001;}
          }
          {if (true) throw (Error)jjte001;}
      } finally {
          if (jjtc001) {
            jjtree.closeNodeScope(jjtn001,  1);
          }
      }
      break;
    case EXISTS:
    case FORALL:
          ASTQuantifier jjtn002 = new ASTQuantifier(JJTQUANTIFIER);
          boolean jjtc002 = true;
          jjtree.openNodeScope(jjtn002);
      try {
        s = Quant();
        jj_consume_token(LSP);
        label_1:
        while (true) {
          t = jj_consume_token(VAR);
                                        int varNumber = 0;

                                        if (varNames.containsKey(t.image)) {
                                                jjtn002.var = varNames.get(t.image);
                                        }
                                        else {
                                                varNames.put(t.image,nextVarInt);
                                                jjtn002.var = nextVarInt;
                                                nextVarInt++;
                                        }

                                        varNumber = jjtn002.var;

                                        if (s == "e") {
                                                jjtn002.nodeType = NodeType.EXISTS;
                                                if (!vars.contains(varNumber)) {
                                                        eVars.add(varNumber);
                                                }
                                        }

                                        if (s == "f") {
                                                jjtn002.nodeType = NodeType.FORALL;
                                                if (!vars.contains(varNumber)) {
                                                        aVars.add(varNumber);
                                                }
                                        }
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case VAR:
            ;
            break;
          default:
            jj_la1[0] = jj_gen;
            break label_1;
          }
        }
        jj_consume_token(RSP);
        Exp();
      } catch (Throwable jjte002) {
          if (jjtc002) {
            jjtree.clearNodeScope(jjtn002);
            jjtc002 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte002 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte002;}
          }
          if (jjte002 instanceof ParseException) {
            {if (true) throw (ParseException)jjte002;}
          }
          {if (true) throw (Error)jjte002;}
      } finally {
          if (jjtc002) {
            jjtree.closeNodeScope(jjtn002,  1);
          }
      }
      break;
    case LP:
      jj_consume_token(LP);
          ASTLogical jjtn003 = new ASTLogical(JJTLOGICAL);
          boolean jjtc003 = true;
          jjtree.openNodeScope(jjtn003);
      try {
        Exp();
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case AND:
        case OR:
          switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
          case OR:
            jj_consume_token(OR);
                                      jjtn003.nodeType = NodeType.OR;
            break;
          case AND:
            jj_consume_token(AND);
                                      jjtn003.nodeType = NodeType.AND;
            break;
          default:
            jj_la1[1] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
          Exp();
          break;
        default:
          jj_la1[2] = jj_gen;
          ;
        }
      } catch (Throwable jjte003) {
          if (jjtc003) {
            jjtree.clearNodeScope(jjtn003);
            jjtc003 = false;
          } else {
            jjtree.popNode();
          }
          if (jjte003 instanceof RuntimeException) {
            {if (true) throw (RuntimeException)jjte003;}
          }
          if (jjte003 instanceof ParseException) {
            {if (true) throw (ParseException)jjte003;}
          }
          {if (true) throw (Error)jjte003;}
      } finally {
          if (jjtc003) {
            jjtree.closeNodeScope(jjtn003, jjtree.nodeArity() > 1);
          }
      }
      jj_consume_token(RP);
      break;
    case VAR:
      Var();
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

// *	<quant> 	::= <EXISTS> | <FORALL>
  final public String Quant() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case EXISTS:
      Exists();
                   {if (true) return "e";}
      break;
    case FORALL:
      Forall();
                   {if (true) return "f";}
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

// *	<op>		::= <OR> | <AND>
  final public void Op() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OR:
      Or();
      break;
    case AND:
      And();
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  /** Generated Token Manager. */
  public Qbf_parserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[6];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x4000,0x60,0x60,0x7180,0x3000,0x60,};
   }

  /** Constructor with InputStream. */
  public Qbf_parser(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public Qbf_parser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new Qbf_parserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public Qbf_parser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new Qbf_parserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public Qbf_parser(Qbf_parserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(Qbf_parserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jjtree.reset();
    jj_gen = 0;
    for (int i = 0; i < 6; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
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
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[15];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 6; i++) {
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
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}
