// THIS FILE IS AUTO GENERATED. SEE tool/lox_ast_types.txt
abstract class Expr {
  interface Visitor<R> {
    fun visitBinaryExpr (expr: Binary) : R?
    fun visitGroupingExpr (expr: Grouping) : R?
    fun visitLiteralExpr (expr: Literal) : R?
    fun visitUnaryExpr (expr: Unary) : R?
  }

  class Binary : Expr {
    val left: Expr
    val operator: Token
    val right: Expr
    constructor(left: Expr, operator: Token, right: Expr) {
      this.left = left
      this.operator = operator
      this.right = right
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitBinaryExpr(this)
    }
  }

  class Grouping : Expr {
    val expression: Expr
    constructor(expression: Expr) {
      this.expression = expression
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitGroupingExpr(this)
    }
  }

  class Literal : Expr {
    val value: Any?
    constructor(value: Any?) {
      this.value = value
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitLiteralExpr(this)
    }
  }

  class Unary : Expr {
    val operator: Token
    val right: Expr
    constructor(operator: Token, right: Expr) {
      this.operator = operator
      this.right = right
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitUnaryExpr(this)
    }
  }

  abstract fun<R> accept(visitor: Visitor<R>) : R?
}
