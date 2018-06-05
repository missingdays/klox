import java.util.*


class Resolver : Expr.Visitor<Void?>, Stmt.Visitor<Void?> {
    private enum class FunctionType {
        NONE, FUNCTION
    }

    private val interpreter : Interpreter
    private val scopes = Stack<MutableMap<String, Boolean>>()
    private var currentFunction = FunctionType.NONE

    constructor(interpreter: Interpreter) {
        this.interpreter = interpreter
    }

    fun resolve(statements: List<Stmt>) {
        for (statement in statements) {
            resolve(statement)
        }
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Void? {
        beginScope()
        resolve(stmt.statements)
        endScope()
        return null
    }

    override fun visitVarStmt(stmt: Stmt.Var): Void? {
        declare(stmt.name)

        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }

        define(stmt.name)

        return null
    }

    override fun visitVariableExpr(expr: Expr.Variable): Void? {
        if(!scopes.isEmpty() && scopes.peek()[expr.name.lexeme] == false) {
            parseError(expr.name, "Cannot read local variable in its own initializer")
        }

        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitAssignExpr(expr: Expr.Assign): Void? {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        return null
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Void? {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.FUNCTION)
        return null
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Void? {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if(stmt.elseBranch != null) {
            resolve(stmt.elseBranch)
        }
        return null
    }

    override fun visitPrintStmt(stmt: Stmt.Print): Void? {
        resolve(stmt.expression)
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Void? {
        if (currentFunction == FunctionType.NONE) {
            parseError(stmt.keyword, "Cannot return from top-level code")
        }

        if (stmt.value != null) {
            resolve(stmt.value)
        }

        return null
    }

    override fun visitWhileStmt(stmt: Stmt.While): Void? {
        resolve(stmt.condition)
        resolve(stmt.body)
        return null
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitCallExpr(expr: Expr.Call): Void? {
        resolve(expr.callee)

        for (argument in expr.arguments) {
            resolve(argument)
        }

        return null
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Void? {
        resolve(expr.expression)

        return null
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Void? {
        resolve(expr.left)
        resolve(expr.right)
        return null
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Void? {
        resolve(expr.right)
        return null
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Void? {
        return null
    }

    private fun declare(name: Token) {
        if (scopes.isEmpty()) {
            return
        }

        val scope = scopes.peek()

        if (scope.containsKey(name.lexeme)) {
            parseError(name, "Variable with the name ${name.lexeme} is already declared in this scope")
        }

        scope[name.lexeme] = false
    }

    private fun define(name: Token) {
        if (scopes.isEmpty()) {
            return
        }

        val scope = scopes.peek()
        scope[name.lexeme] = true
    }

    private fun beginScope() {
        scopes.push(HashMap<String, Boolean>())
    }

    private fun endScope() {
        scopes.pop()
    }


    private fun resolve(statement: Stmt) {
        statement.accept(this)
    }

    private fun resolve(expr: Expr) {
        expr.accept(this)
    }

    private fun resolveLocal(expr: Expr, name: Token) {
        for(i in scopes.size-1 downTo 0) {
            if (scopes[i].containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - i - 1)
                return
            }
        }
    }

    private fun resolveFunction(function: Stmt.Function, type: FunctionType) {
        val enclosingFunction = currentFunction
        currentFunction = type

        beginScope()

        for(param in function.parameters) {
            declare(param)
            define(param)
        }
        resolve(function.body)

        endScope()

        currentFunction = enclosingFunction
    }
}