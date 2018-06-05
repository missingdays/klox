// THIS FILE IS AUTO GENERATED. SEE tool/generate_ast.py
abstract class Expr {
  interface Visitor<R> {
    fun visitAssignExpr (expr: Assign) : R?
    fun visitBinaryExpr (expr: Binary) : R?
    fun visitCallExpr (expr: Call) : R?
    fun visitGroupingExpr (expr: Grouping) : R?
    fun visitLiteralExpr (expr: Literal) : R?
    fun visitLogicalExpr (expr: Logical) : R?
    fun visitUnaryExpr (expr: Unary) : R?
    fun visitVariableExpr (expr: Variable) : R?
  }

  class Assign : Expr {
    val name: Token
    val value: Expr?
    constructor(name: Token, value: Expr?) {
      this.name = name
      this.value = value
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitAssignExpr(this)
    }
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

  class Call : Expr {
    val callee: Expr
    val paren: Token
    val arguments: List<Expr>
    constructor(callee: Expr, paren: Token, arguments: List<Expr>) {
      this.callee = callee
      this.paren = paren
      this.arguments = arguments
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitCallExpr(this)
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

  class Logical : Expr {
    val left: Expr
    val operator: Token
    val right: Expr
    constructor(left: Expr, operator: Token, right: Expr) {
      this.left = left
      this.operator = operator
      this.right = right
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitLogicalExpr(this)
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

  class Variable : Expr {
    val name: Token
    constructor(name: Token) {
      this.name = name
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitVariableExpr(this)
    }
  }

  abstract fun<R> accept(visitor: Visitor<R>) : R?
}
