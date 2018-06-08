// THIS FILE IS AUTO GENERATED. SEE tool/generate_ast.py
abstract class Expr {
  interface Visitor<R> {
    fun visitAssignExpr (expr: Assign) : R?
    fun visitBinaryExpr (expr: Binary) : R?
    fun visitCallExpr (expr: Call) : R?
    fun visitElvisExpr (expr: Elvis) : R?
    fun visitGetExpr (expr: Get) : R?
    fun visitGroupingExpr (expr: Grouping) : R?
    fun visitLiteralExpr (expr: Literal) : R?
    fun visitLogicalExpr (expr: Logical) : R?
    fun visitSetExpr (expr: Set) : R?
    fun visitSuperExpr (expr: Super) : R?
    fun visitThisExpr (expr: This) : R?
    fun visitUnaryExpr (expr: Unary) : R?
    fun visitVariableExpr (expr: Variable) : R?
  }

  class Assign : Expr {
    val name: Token
    val value: Expr
    constructor(name: Token, value: Expr) {
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

  class Elvis : Expr {
    val condition: Expr
    val thenExpr: Expr?
    val elseExpr: Expr
    constructor(condition: Expr, thenExpr: Expr?, elseExpr: Expr) {
      this.condition = condition
      this.thenExpr = thenExpr
      this.elseExpr = elseExpr
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitElvisExpr(this)
    }
  }

  class Get : Expr {
    val obj: Expr
    val name: Token
    constructor(obj: Expr, name: Token) {
      this.obj = obj
      this.name = name
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitGetExpr(this)
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

  class Set : Expr {
    val obj: Expr
    val name: Token
    val value: Expr
    constructor(obj: Expr, name: Token, value: Expr) {
      this.obj = obj
      this.name = name
      this.value = value
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitSetExpr(this)
    }
  }

  class Super : Expr {
    val keyword: Token
    val method: Token
    constructor(keyword: Token, method: Token) {
      this.keyword = keyword
      this.method = method
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitSuperExpr(this)
    }
  }

  class This : Expr {
    val keyword: Token
    constructor(keyword: Token) {
      this.keyword = keyword
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitThisExpr(this)
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
