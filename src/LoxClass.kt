open class LoxClass : LoxCallable {
    val name: String
    private val superclass: LoxClass?
    protected val methods: MutableMap<String, LoxFunction>

    constructor(name: String, superclass: LoxClass?, methods: MutableMap<String, LoxFunction>) {
        this.name = name
        this.superclass = superclass
        this.methods = methods
    }

    fun findMethod(instance: LoxInstance, name: String) : LoxFunction? {
        if (methods.containsKey(name)) {
            return methods[name]?.bind(instance)
        }

        if (superclass != null) {
            return superclass.findMethod(instance, name)
        }

        return null
    }

    override fun arity(): Int {
        val init = methods["init"]
        if (init == null) {
            return 0
        }
        return init.arity()
    }

    override fun call(interpreter: Interpreter, arguments: List<Any?>): Any? {
        val instance = LoxInstance(this)

        val init = methods["init"]
        if (init != null) {
            init.bind(instance).call(interpreter, arguments)
        }

        return instance
    }

    override fun toString() : String {
        return name
    }
}