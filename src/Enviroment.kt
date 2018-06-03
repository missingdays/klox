class Enviroment {
    val enclosing: Enviroment?
    val values = HashMap<String, Any?>()

    constructor() {
        enclosing = null
    }

    constructor(enclosing: Enviroment) {
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

    fun get(name: Token) : Any? {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme)
        }

        if (enclosing != null) {
            return enclosing.get(name)
        }

        throw LoxRuntimeError(name, "Undefined variable ${name.lexeme}")
    }
}