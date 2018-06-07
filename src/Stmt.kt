// THIS FILE IS AUTO GENERATED. SEE tool/generate_ast.py
abstract class Stmt {
  interface Visitor<R> {
    fun visitBlockStmt (stmt: Block) : R?
    fun visitClassStmt (stmt: Class) : R?
    fun visitExpressionStmt (stmt: Expression) : R?
    fun visitFunctionStmt (stmt: Function) : R?
    fun visitPrintStmt (stmt: Print) : R?
    fun visitReturnStmt (stmt: Return) : R?
    fun visitIfStmt (stmt: If) : R?
    fun visitVarStmt (stmt: Var) : R?
    fun visitWhileStmt (stmt: While) : R?
  }

  class Block : Stmt {
    val statements: List<Stmt>
    constructor(statements: List<Stmt>) {
      this.statements = statements
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitBlockStmt(this)
    }
  }

  class Class : Stmt {
    val name: Token
    val superclass: Expr.Variable?
    val methods: List<Stmt.Function>
    constructor(name: Token, superclass: Expr.Variable?, methods: List<Stmt.Function>) {
      this.name = name
      this.superclass = superclass
      this.methods = methods
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitClassStmt(this)
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

  class Function : Stmt {
    val name: Token
    val parameters: List<Token>
    val body: List<Stmt>
    constructor(name: Token, parameters: List<Token>, body: List<Stmt>) {
      this.name = name
      this.parameters = parameters
      this.body = body
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitFunctionStmt(this)
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

  class Return : Stmt {
    val keyword: Token
    val value: Expr?
    constructor(keyword: Token, value: Expr?) {
      this.keyword = keyword
      this.value = value
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitReturnStmt(this)
    }
  }

  class If : Stmt {
    val condition: Expr
    val thenBranch: Stmt
    val elseBranch: Stmt?
    constructor(condition: Expr, thenBranch: Stmt, elseBranch: Stmt?) {
      this.condition = condition
      this.thenBranch = thenBranch
      this.elseBranch = elseBranch
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitIfStmt(this)
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

  class While : Stmt {
    val condition: Expr
    val body: Stmt
    constructor(condition: Expr, body: Stmt) {
      this.condition = condition
      this.body = body
    }

    override fun<R> accept(visitor: Visitor<R>) : R? { 
      return visitor.visitWhileStmt(this)
    }
  }

  abstract fun<R> accept(visitor: Visitor<R>) : R?
}
