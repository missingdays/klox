class Environment {
    val enclosing: Environment?
    private val values = HashMap<String, Any?>()

    constructor() {
        enclosing = null
    }

    constructor(enclosing: Environment) {
        this.enclosing = enclosing
    }

    fun define(name: String, value: Any?) {
        values.put(name, value)
    }

    fun assign(name: Token, value: Any?) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value)
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw LoxRuntimeError(name, "Undefined variable ${name.lexeme}")
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).values[name.lexeme] = value
    }

    fun get(name: Token) : Any? {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme)
        }

        if (enclosing != null) {
            return enclosing.get(name)
        }

        throw LoxRuntimeError(name, "Undefined variable ${name.lexeme}")
    }

    fun getAt(distance: Int, name: String) : Any? {
        return ancestor(distance).values[name]
    }

    private fun ancestor(distance: Int) : Environment {
        var environment: Environment = this

        for (i in 0 until distance) {
            // Distance is calculated the way so we never will reach enclosing that is null
            environment = environment.enclosing!!
        }

        return environment
    }
}