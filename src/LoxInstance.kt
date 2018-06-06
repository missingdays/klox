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

    fun set(name: Token, value: Any?) {
        fields.put(name.lexeme, value)
    }

    override fun toString(): String {
        return loxClass.name + " instance"
    }
}