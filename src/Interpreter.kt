import sun.util.resources.cldr.xog.LocaleNames_xog

class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Void> {

    val globals = Enviroment()
    var environment = globals

    constructor() {
        globals.define("clock", object : LoxCallable {
            override fun arity() : Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return System.currentTimeMillis() / 1000.0
            }
        })

        globals.define("input", object : LoxCallable {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return readLine()
            }
        })

        globals.define("parseNumber", object : LoxCallable {
            override fun arity(): Int {
                return 1
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val number = arguments[0] as String
                return number.toDouble()
            }
        })
    }

    fun interpret(statements: List<Stmt>) {
        try {
            for (statement in statements) {
                execute(statement)
            }
        } catch (error: LoxRuntimeError) {
            runtimeError(error)
        }
    }

    override fun visitVarStmt(stmt: Stmt.Var): Void? {
        val value = if (stmt.initializer != null) evaluate(stmt.initializer) else null

        environment.define(stmt.name.lexeme, value)
        return null
    }

    override fun visitReturnStmt(stmt: Stmt.Return): Void? {
        val value = if (stmt.value != null) evaluate(stmt.value) else null
        throw Return(value)
    }

    override fun visitAssignExpr(expr: Expr.Assign): Any? {
        val value = if (expr.value == null) null else evaluate(expr.value)

        environment.assign(expr.name, value)
        return value
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return environment.get(expr.name)
    }

    override fun visitGroupingExpr(expr: Expr.Grouping): Any? {
        return evaluate(expr.expression)
    }

    override fun visitLiteralExpr(expr: Expr.Literal): Any? {
        return expr.value
    }

    override fun visitUnaryExpr(expr: Expr.Unary): Any? {
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.BANG -> return !isTruthy(right)
            TokenType.MINUS -> {
                checkNumberOperand(expr.operator, right)
                return -(right as Double)
            }
        }

        return null
    }

    override fun visitBinaryExpr(expr: Expr.Binary): Any? {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)

        when (expr.operator.type) {
            TokenType.SLASH -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) / (right as Double)
            }
            TokenType.STAR  -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) * (right as Double)
            }
            TokenType.MINUS -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) - (right as Double)
            }
            TokenType.PLUS -> {
                if (left is Double && right is Double) {
                    return (left as Double) + (right as Double)
                }

                if (left is String && right is String) {
                    return (left as String) + (right as String)
                }

                throw LoxRuntimeError(expr.operator, "Operands must be two numbers or two strings")
            }

            TokenType.GREATER       -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) > (right as Double)
            }
            TokenType.GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) >= (right as Double)
            }
            TokenType.LESS          -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) < (right as Double)
            }
            TokenType.LESS_EQUAL    -> {
                checkNumberOperands(expr.operator, left, right)
                return (left as Double) <= (right as Double)
            }

            TokenType.EQUAL_EQUAL -> return isEqual(left, right)
            TokenType.BANG_EQUAL  -> return !isEqual(left, right)
        }

        return null
    }

    override fun visitExpressionStmt(stmt: Stmt.Expression): Void? {
        evaluate(stmt.expression)
        return null
    }

    override fun visitPrintStmt(stmt: Stmt.Print): Void? {
        val value = evaluate(stmt.expression)
        println(stringify(value))
        return null
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Void? {
        executeBlock(stmt.statements, Enviroment(environment))
        return null
    }

    override fun visitIfStmt(stmt: Stmt.If): Void? {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }

        return null
    }

    override fun visitWhileStmt(stmt: Stmt.While): Void? {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }

        return null
    }

    override fun visitLogicalExpr(expr: Expr.Logical): Any? {
        val left = evaluate(expr.left)

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left
            }
        } else if (expr.operator.type == TokenType.AND) {
            if (!isTruthy(left)) {
                return left
            }
        }

        return evaluate(expr.right)
    }

    override fun visitCallExpr(expr: Expr.Call): Any? {
        val callee = evaluate(expr.callee)

        val arguments = ArrayList<Any?>()

        for (argument in expr.arguments) {
            arguments.add(evaluate(argument))
        }

        if (callee !is LoxCallable) {
            throw LoxRuntimeError(expr.paren, "Can only call functions and classes")
        }

        val function = callee as LoxCallable

        if (arguments.size != function.arity()) {
            throw LoxRuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}")
        }

        return function.call(this, arguments)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Void? {
        val function = LoxFunction(stmt, this.environment)
        environment.define(stmt.name.lexeme, function)
        return null
    }

    fun executeBlock(statements: List<Stmt?>, environment: Enviroment) {
        val previous = this.environment

        try {
            this.environment = environment

            for(statement in statements) {
                execute(statement!!)
            }
        } finally {
            this.environment = previous
        }
    }

    private fun evaluate(expr: Expr) : Any? {
        return expr.accept(this)
    }

    private fun execute(stmt: Stmt) {
        stmt.accept(this)
    }

    private fun isTruthy(value: Any?) : Boolean {
        if (value == null) {
            return false
        }

        if (value is Boolean) {
            return value as Boolean
        }

        return true
    }

    private fun isEqual(a: Any?, b: Any?) : Boolean {
        if (a == null && b == null) {
            return true
        }

        if (a == null) {
            return false
        }

        return a == b
    }

    private fun checkNumberOperand(operator: Token, operand: Any?) {
        if (operand is Double) {
            return
        }

        throw LoxRuntimeError(operator, "Operand must be number")
    }

    private fun checkNumberOperands(operator: Token, left: Any?, right: Any?) {
        if (left is Double && right is Double) {
            return
        }

        throw LoxRuntimeError(operator, "Operands must be numbers")
    }

    private fun stringify(obj: Any?) : String {
        if (obj == null) {
            return "nil"
        }

        if (obj is Double) {
            var text = obj.toString()

            if (text.endsWith(".0")) {
                text = text.substring(0, text.length - 2)
            }

            return text
        }

        return obj.toString()
    }
}