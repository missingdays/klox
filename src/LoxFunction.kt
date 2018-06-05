class LoxFunction : LoxCallable {
    val declaration : Stmt.Function
    val closure : Environment

    constructor(declaration: Stmt.Function, closure: Environment) {
        this.declaration = declaration
        this.closure = closure
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val environment = Environment(this.closure)

        for (i in 0 until declaration.parameters.size) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i))
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue : Return) {
            return returnValue.value
        }

        return null
    }

    override fun arity(): Int {
        return declaration.parameters.size
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}