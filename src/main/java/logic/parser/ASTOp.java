/* Generated By:JJTree: Do not edit this line. ASTOp.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package main.java.logic.parser;

public class ASTOp extends SimpleNode {
  public ASTOp(int id) {
    super(id);
  }

  public ASTOp(Qbf_parser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(Qbf_parserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=76c4aa6430fa258944442e4a898313c9 (do not edit this line) */