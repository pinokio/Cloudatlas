package pl.edu.mimuw.cloudatlas.interpreter.query.Absyn; // Java Package generated by the BNF Converter.

public class EStrC extends BasicExpr {
  public final String string_;

  public EStrC(String p1) { string_ = p1; }

  public <R,A> R accept(pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.BasicExpr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o instanceof pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EStrC) {
      pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EStrC x = (pl.edu.mimuw.cloudatlas.interpreter.query.Absyn.EStrC)o;
      return this.string_.equals(x.string_);
    }
    return false;
  }

  public int hashCode() {
    return this.string_.hashCode();
  }


}
