/* Not automatically created - DO NOT DELETE */

//package package main.java.logic.parser;

public class Qbf_parserDumpVisitor implements Qbf_parserVisitor
{
  private int indent = 0;

  private String indentString() {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < indent; ++i) {
      sb.append(' ');
    }
    return sb.toString();
  }

  public Object visit(SimpleNode node, Object data) {
    System.out.println(indentString() + node +
                   ": acceptor not unimplemented in subclass?");
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  public Object visit(ASTStart node, Object data) {
    System.out.println(indentString() + node);
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  public Object visit(ASTOp node, Object data) {
    System.out.println(indentString() + node);
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  public Object visit(ASTQuant node, Object data) {
    System.out.println(indentString() + node);
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  public Object visit(ASTVarList node, Object data) {
    System.out.println(indentString() + node);
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

  public Object visit(ASTExpression node, Object data) {
    System.out.println(indentString() + node);
    ++indent;
    data = node.childrenAccept(this, data);
    --indent;
    return data;
  }

//  public Object visit(ASTMult node, Object data) {
//    System.out.println(indentString() + node);
//    ++indent;
//    data = node.childrenAccept(this, data);
//    --indent;
//    return data;
//  }

//  public Object visit(ASTMyOtherID node, Object data) {
//    System.out.println(indentString() + node);
//    ++indent;
//    data = node.childrenAccept(this, data);
//    --indent;
//    return data;
//  }

//  public Object visit(ASTInteger node, Object data) {
//    System.out.println(indentString() + node);
//    ++indent;
//    data = node.childrenAccept(this, data);
//    --indent;
//    return data;
//  }
}

/*end*/
