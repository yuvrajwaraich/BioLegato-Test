/* Generated By:JJTree: Do not edit this line. ASTIdent.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.biopcd.parser;

public
class ASTIdent extends SimpleNode {
  public ASTIdent(int id) {
    super(id);
  }

  public ASTIdent(PCD p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PCDVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=f1ae355ec7e0bc5e6bc39685ee617f08 (do not edit this line) */