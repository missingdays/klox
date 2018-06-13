class LoxInstance {
    val loxClass : LoxClass
    val fields = HashMap<String, Any?>()

    constructor(loxClass: LoxClass) {
        this.loxClass = loxClass
    }

    fun get(name: Token) : Any? {
        if (fields.containsKey(name.lexeme)) {
            return fields[name.lexeme]
        }

        val method = loxClass.findMethod(this, name.lexeme)
        if (method != null) {
            return method
        }

        throw LoxRuntimeError(name, "Undefined property ${name.lexeme}")
    }

    fun get(name: String) : Any? {
        return get(Token(TokenType.IDENTIFIER, name, name, -1))
    }

    fun set(name: String, value: Any?){
        fields.put(name, value)
    }

    fun set(name: Token, value: Any?) {
        fields.put(name.lexeme, value)
    }

    override fun toString() : String {
        return loxClass.name + " instance"
    }

    fun convertToString(interpreter: Interpreter) : String {
        val userMethod = loxClass.findMethod(this, "toString")

        if (userMethod == null || userMethod.arity() != 0) {
            return this.toString()
        }

        val str = userMethod.call(interpreter, emptyList())

        return interpreter.stringify(str)
    }
}