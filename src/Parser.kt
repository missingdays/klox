class Parser {
    private class ParseError : RuntimeException()

    val tokens : List<Token>
    var current = 0

    constructor(tokens: List<Token>){
        this.tokens = tokens
    }

    fun parse() : List<Stmt?> {
        val statements = ArrayList<Stmt?>()

        while (!isAtEnd()) {
            statements.add(declaration())
        }

        return statements
    }

    private fun declaration() : Stmt? {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration()
            }

            return statement()
        } catch (error: ParseError) {
            synchronize()
            return null
        }
    }

    private fun varDeclaration() : Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expected variable name")
        val initializer = if (match(TokenType.EQUAL)) expression() else null

        consume(TokenType.SEMICOLON, "Expected ; after variable declaration")
        return Stmt.Var(name, initializer)
    }

    private fun statement() : Stmt {
        if (match(TokenType.PRINT)) {
            return printStatement()
        }

        if (match(TokenType.LEFT_BRACE)) {
            return Stmt.Block(block())
        }

        return expressionStatement()
    }

    private fun printStatement() : Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expected ; after print")
        return Stmt.Print(value)
    }

    private fun block() : List<Stmt?> {
        val statements = ArrayList<Stmt?>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expected } after block")
        return statements
    }

    private fun expressionStatement() : Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expected ; after expression")
        return Stmt.Expression(expr)
    }

    private fun expression() : Expr {
        return assigment()
    }

    private fun assigment() : Expr {
        val expr = equality()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assigment()

            if (expr is Expr.Variable) {
                val name = (expr as Expr.Variable).name
                return Expr.Assign(name, value)
            }

            error(equals, "Invalid assigment target")
        }

        return expr
    }

    private fun equality() : Expr {
        var expr = comparison()

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison() : Expr {
        var expr = addition()

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = addition()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun addition() : Expr {
        var expr = multiplication()

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = multiplication()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun multiplication() : Expr {
        var expr = unary()

        while (match(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary() : Expr {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }

        return primary()
    }

    private fun primary() : Expr {
        if (match(TokenType.FALSE)) {
            return Expr.Literal(false)
        }

        if (match(TokenType.TRUE)) {
            return Expr.Literal(true)
        }

        if (match(TokenType.NIL)) {
            return Expr.Literal(null)
        }

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return Expr.Literal(previous().literal)
        }

        if (match(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
        }

        if (match(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expected ) after expression");
            return Expr.Grouping(expr)
        }

        throw error(peek(), "Expected expression")

    }

    private fun consume(type: TokenType, message: String) : Token {
        if (check(type)) {
            return advance()
        }

        throw error(peek(), message)
    }

    private fun error(token: Token, message: String) : ParseError{
        parseError(token, message)
        return ParseError()
    }

    private fun synchronize() {
        advance()

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) {
                return
            }

            when (peek().type) {
                TokenType.CLASS, TokenType.FUN, TokenType.VAR, TokenType.FOR,
                TokenType.IF, TokenType.WHILE, TokenType.PRINT, TokenType.RETURN -> return
            }

            advance()
        }
    }

    private fun match(vararg types: TokenType) : Boolean{
        for(type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }

        return false
    }

    private fun check(tokenType: TokenType) : Boolean {
        if (isAtEnd()) {
            return false
        }

        return peek().type == tokenType
    }

    private fun advance() : Token {
        if (!isAtEnd()) {
            current++;
        }

        return previous()
    }

    private fun isAtEnd() : Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek() : Token {
        return tokens.get(current)
    }

    private fun previous() : Token {
        return tokens.get(current-1)
    }
}