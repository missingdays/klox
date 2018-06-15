class LoxArray : LoxClass{
    constructor(globals: Environment) : super("array", null, HashMap<String, LoxFunction>()) {
        val innerArrayName = "__lox_inner_array"

        methods["init"] = object : LoxInnerFunction(null, globals, true) {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                getThis().set(innerArrayName, ArrayList<Any?>())
                return null
            }
        }

        methods["size"] = object : LoxInnerFunction(null, globals, true) {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                return (getThis().get(innerArrayName) as ArrayList<Any?>).size
            }
        }

        methods["get"] = object : LoxInnerFunction(null, globals, false) {
            override fun arity(): Int {
                return 1
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val index = getIndex(arguments[0])

                val array = getThis().get(innerArrayName) as ArrayList<Any?>

                if (index >= array.size) {
                    throw LoxRuntimeError(Token(TokenType.IDENTIFIER, "array.get", "array.get", 0), "Index of out range")
                }

                return array[index]
            }
        }

        methods["set"] = object : LoxInnerFunction(null, globals, false) {
            override fun arity(): Int {
                return 2
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val index = getIndex(arguments[0])
                val value = arguments[1]

                val array = getThis().get(innerArrayName) as ArrayList<Any?>

                if (index >= array.size) {
                    throw LoxRuntimeError(Token(TokenType.IDENTIFIER, "array.set", "array.set", 0), "Index of out range")
                }

                array[index] = value

                return null
            }
        }

        methods["append"] = object : LoxInnerFunction(null, globals, false) {
            override fun arity(): Int {
                return 1
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val value = arguments[0]

                val array = getThis().get(innerArrayName) as ArrayList<Any?>
                array.add(value)
                return null
            }
        }

        methods["pop"] = object : LoxInnerFunction(null, globals, false) {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
                val array = getThis().get(innerArrayName) as ArrayList<Any?>

                if (array.size == 0) {
                    throw LoxRuntimeError(Token(TokenType.IDENTIFIER, "array.pop", "array.pop", 0),
                            "Cannot pop from empty array")
                }

                val value = array[array.size-1]
                array.removeAt(array.size-1)
                return value
            }
        }

    }

    private fun getIndex(argument: Any?) : Int {
        if (argument is Int) {
            return argument
        }

        if (argument is Double) {
            return argument.toInt()
        }

        throw LoxRuntimeError(Token(TokenType.IDENTIFIER, "array.index", "array.index", 0),
                "Indexes can only be numbers")
    }
}