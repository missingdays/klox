open class LoxFunction : LoxCallable {
    val declaration : Stmt.Function?
    var closure : Environment
    val isInit: Boolean

    constructor(declaration: Stmt.Function?, closure: Environment, isInit: Boolean) {
        this.declaration = declaration
        this.closure = closure
        this.isInit = isInit
    }

    open fun bind(instance: LoxInstance) : LoxFunction {
        val environment = Environment(closure)
        environment.define("this", instance)
        return LoxFunction(declaration, environment, isInit)
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        if (declaration == null) {
            return null
        }

        val environment = Environment(this.closure)

        for (i in 0 until declaration.parameters.size) {
            environment.define(declaration.parameters.get(i).lexeme, arguments.get(i))
        }

        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch (returnValue : Return) {
            if (isInit) {
                return closure.getAt(0, "this")
            }

            return returnValue.value
        }

        if (isInit) {
            return closure.getAt(0, "this")
        }

        return null
    }

    override fun arity(): Int {
        if (declaration == null) {
            return 0
        }

        return declaration.parameters.size
    }

    protected fun getThis() : LoxInstance {
        return closure.getAt(0, "this") as LoxInstance
    }

    override fun toString(): String {
        if (declaration == null) {
            return "<fn>"
        }

        return "<fn ${declaration.name.lexeme}>"
    }
}