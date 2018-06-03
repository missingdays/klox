// THIS FILE IS AUTO GENERATED. SEE tool/generate_ast.py
abstract class Stmt {
  interface Visitor<R> {
    fun visitBlockStmt (stmt: Block) : R?
    fun visitExpressionStmt (stmt: Expression) : R?
    fun visitPrintStmt (stmt: Print) : R?
    fun visitVarStmt (stmt: Var) : R?
  }

  class Block : Stmt {
    val statements: List<Stmt?>
    constructor(statements: List<Stmt?>) {
      this.statements = statements
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitBlockStmt(this)
    }
  }

  class Expression : Stmt {
    val expression: Expr
    constructor(expression: Expr) {
      this.expression = expression
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitExpressionStmt(this)
    }
  }

  class Print : Stmt {
    val expression: Expr
    constructor(expression: Expr) {
      this.expression = expression
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitPrintStmt(this)
    }
  }

  class Var : Stmt {
    val name: Token
    val initializer: Expr?
    constructor(name: Token, initializer: Expr?) {
      this.name = name
      this.initializer = initializer
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitVarStmt(this)
    }
  }

  abstract fun<R> accept(visitor: Visitor<R>) : R?
}
