open class LoxInnerFunction(declaration: Stmt.Function?, closure: Environment, isInit: Boolean) : LoxFunction(declaration, closure, isInit) {
    override fun bind(instance: LoxInstance): LoxFunction {
        this.closure = Environment()
        this.closure.define("this", instance)
        return this
    }

    override fun toString(): String {
        return "<inner function>"
    }
}