class Interpreter : Expr.Visitor<Any>, Stmt.Visitor<Void> {

    private val globals = Environment()
    private var environment = globals

    private val locals = HashMap<Expr, Int>()

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

        addArray()
    }

    fun addArray() {
        val methods = HashMap<String, LoxFunction>()
        val innerArrayName = "__lox_inner_array"

        methods["init"] = object : LoxFunction(null, globals, true) {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                (this.closure.getAt(0, "this") as LoxInstance).set(innerArrayName, ArrayList<Any?>())
                return null
            }

            override fun bind(instance: LoxInstance): LoxFunction {
                this.closure = Environment(globals)
                this.closure.define("this", instance)
                return this
            }
        }

        methods["get"] = object : LoxFunction(null, globals, false) {
            override fun arity(): Int {
                return 1
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val index = (arguments[0] as Double).toInt()

                val array = (this.closure.getAt(0, "this") as LoxInstance).get(innerArrayName) as ArrayList<Any?>

                if (index >= array.size){
                    throw LoxRuntimeError(Token(TokenType.IDENTIFIER, "array.get", null, 0), "Index of out range")
                }

                return array[index]
            }

            override fun bind(instance: LoxInstance): LoxFunction {
                this.closure = Environment(globals)
                this.closure.define("this", instance)
                return this
            }
        }

        methods["set"] = object : LoxFunction(null, globals, false) {
            override fun arity(): Int {
                return 2
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val index = arguments[0] as Int
                val value = arguments[1]

                val array = this.closure.getAt(0, innerArrayName) as ArrayList<Any?>

                array[index] = value

                return null
            }
        }

        methods["append"] = object : LoxFunction(null, globals, false) {
            override fun arity(): Int {
                return 1
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val value = arguments[0]

                val array = (this.closure.getAt(0, "this") as LoxInstance).get(innerArrayName) as ArrayList<Any?>
                array.add(value)
                return null
            }

            override fun bind(instance: LoxInstance): LoxFunction {
                this.closure = Environment(globals)
                this.closure.define("this", instance)
                return this
            }

            override fun toString(): String {
                return "<array.append>"
            }
        }

        globals.define("array", LoxClass("array", null, methods))
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

    fun interpret(expr: Expr) : Any? {
        return evaluate(expr)
    }

    fun resolve(expr: Expr, depth: Int) {
        locals.put(expr, depth)
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

        val distance = locals[expr]
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }

        return value
    }

    override fun visitVariableExpr(expr: Expr.Variable): Any? {
        return lookUpVariable(expr.name, expr)
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
                    return left + right
                }

                if ((left is Double || left is String) && (right is Double || right is String)) {
                    return "${stringify(left)}${stringify(right)}"
                }

                throw LoxRuntimeError(expr.operator, "Operands must be numbers or strings")
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

        if (value is LoxInstance){
            println(value.convertToString(this))
        } else {
            println(stringify(value))
        }
        return null
    }

    override fun visitBlockStmt(stmt: Stmt.Block): Void? {
        executeBlock(stmt.statements, Environment(environment))
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

        val arguments = expr.arguments.map { evaluate(it)}

        if (callee !is LoxCallable) {
            throw LoxRuntimeError(expr.paren, "Can only call functions and classes")
        }

        val function = callee

        if (arguments.size != function.arity()) {
            throw LoxRuntimeError(expr.paren, "Expected ${function.arity()} arguments but got ${arguments.size}")
        }

        return function.call(this, arguments)
    }

    override fun visitFunctionStmt(stmt: Stmt.Function): Void? {
        val function = LoxFunction(stmt, this.environment, false)
        environment.define(stmt.name.lexeme, function)
        return null
    }

    override fun visitClassStmt(stmt: Stmt.Class): Void? {
        environment.define(stmt.name.lexeme, null)

        var superclass: Any? = null
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass)

            if (superclass !is LoxClass) {
                throw LoxRuntimeError(stmt.superclass.name, "Superclass must be a class")
            }

            environment = Environment(environment)
            environment.define("super", superclass)
        }

        val methods = HashMap<String, LoxFunction>()
        for (method in stmt.methods) {
            val function = LoxFunction(method, environment, method.name.lexeme == "init")
            methods.put(method.name.lexeme, function)
        }


        val loxClass = LoxClass(stmt.name.lexeme, superclass as LoxClass?, methods)

        if (superclass != null) {
            environment = environment.enclosing!!
        }

        environment.assign(stmt.name, loxClass)
        return null
    }

    override fun visitThisExpr(expr: Expr.This): Any? {
        return lookUpVariable(expr.keyword, expr)
    }

    override fun visitSuperExpr(expr: Expr.Super): Any? {
        val distance = locals[expr]!!
        val superclass = environment.getAt(distance, "super") as LoxClass
        val instance = environment.getAt(distance - 1, "this") as LoxInstance

        val method = superclass.findMethod(instance, expr.method.lexeme)

        if (method == null) {
            throw LoxRuntimeError(expr.method, "Undefined property ${expr.method.lexeme}")
        }

        return method
    }

    override fun visitGetExpr(expr: Expr.Get): Any? {
        val obj = evaluate(expr.obj)

        if (obj is LoxInstance) {
            return obj.get(expr.name)
        }

        throw LoxRuntimeError(expr.name, "Only instances have properties")
    }

    override fun visitSetExpr(expr: Expr.Set): Any? {
        val obj = evaluate(expr.obj)

        if (obj !is LoxInstance) {
            throw LoxRuntimeError(expr.name, "Only instances have fields")
        }

        val value = evaluate(expr.value)
        obj.set(expr.name, value)
        return value
    }

    override fun visitElvisExpr(expr: Expr.Elvis): Any? {
        var result: Any? = null

        if (expr.thenExpr == null) {
            result = evaluate(expr.condition)

            if (!isTruthy(result)) {
                return evaluate(expr.elseExpr)
            } else {
                return result
            }
        } else {
            val condition = evaluate(expr.condition)

            if (isTruthy(condition)) {
                return evaluate(expr.thenExpr)
            } else {
                return evaluate(expr.elseExpr)
            }
        }
    }

    fun executeBlock(statements: List<Stmt?>, environment: Environment) {
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

    private fun lookUpVariable(name: Token, expr: Expr) : Any? {
        val distance = locals[expr]

        if (distance != null) {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals.get(name)
        }
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

    fun stringify(obj: Any?) : String {
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