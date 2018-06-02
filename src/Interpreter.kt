class Interpreter : Expr.Visitor<Any> {

    fun interpret(expression: Expr) {
        try {
            val value = evaluate(expression)
            println(stringify(value))
        } catch (error: LoxRuntimeError) {
            runtimeError(error)
        }
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
                    return (left as Double) + (right as Double)
                }

                if (left is String && right is String) {
                    return (left as String) + (right as String)
                }

                throw LoxRuntimeError(expr.operator, "Operands must be two numbers or two strings")
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

    private fun evaluate(expr: Expr) : Any? {
        return expr.accept(this)
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

    private fun stringify(obj: Any?) : String {
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