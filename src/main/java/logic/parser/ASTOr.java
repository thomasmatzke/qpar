/* Generated By:JJTree: Do not edit this line. ASTOr.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package main.java.logic.parser;

public class ASTOr extends SimpleNode {
  public ASTOr(int id) {
    super(id);
  }

  public ASTOr(Qbf_parser p, int id) {
    super(p, id);
  }
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}

  /** Accept the visitor. **/
  public Object jjtAccept(Qbf_parserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=090d21c7ccfa3d57034b6c7b08ddbcb8 (do not edit this line) */