class Parser {
    private class ParseError : RuntimeException()

    val tokens : List<Token>
    var current = 0

    constructor(tokens: List<Token>){
        this.tokens = tokens
    }

    fun parse() : List<Stmt> {
        val statements = ArrayList<Stmt>()

        while (!isAtEnd()) {
            val decl = declaration()

            if (decl != null) {
                statements.add(decl)
            }
        }

        return statements
    }

    private fun declaration() : Stmt? {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration()
            }

            if (match(TokenType.FUN)) {
                return function("function")
            }

            if (match(TokenType.CLASS)) {
                return classDeclaration()
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

    private fun function(kind: String) : Stmt.Function {
        val name = consume(TokenType.IDENTIFIER, "Expected ${kind} name")
        consume(TokenType.LEFT_PAREN, "Expected '(' after ${kind} name")

        val parameters = ArrayList<Token>()
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (parameters.size >= 8) {
                    error(peek(), "Cannot have more than 8 parameters")
                }

                parameters.add(consume(TokenType.IDENTIFIER, "Expected parameter name"))
            } while (match(TokenType.COMMA))
        }
        consume(TokenType.RIGHT_PAREN, "Expected ')' after parameters")

        consume(TokenType.LEFT_BRACE, "Expected '{' before ${kind} body")
        val body = block()
        return Stmt.Function(name, parameters, body)
    }

    private fun classDeclaration() : Stmt {
        val name = consume(TokenType.IDENTIFIER, "Expected class name")

        var superclass: Expr.Variable? = null
        if (match(TokenType.LESS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name")
            superclass = Expr.Variable(previous())
        }

        consume(TokenType.LEFT_BRACE, "Expected '{' after class name")

        val methods = ArrayList<Stmt.Function>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"))
        }

        consume(TokenType.RIGHT_BRACE, "Expected '}' after class body")

        return Stmt.Class(name, superclass, methods)
    }

    private fun statement() : Stmt {
        if (match(TokenType.WHILE)) {
            return whileStatement()
        }

        if (match(TokenType.IF)) {
            return ifStatement()
        }

        if (match(TokenType.PRINT)) {
            return printStatement()
        }

        if (match(TokenType.FOR)) {
            return forStatement()
        }

        if (match(TokenType.LEFT_BRACE)) {
            return Stmt.Block(block())
        }

        if (match(TokenType.RETURN)) {
            return returnStatement()
        }

        if (match(TokenType.BREAK)) {
            return breakStatement()
        }

        if (match(TokenType.CONTINUE)) {
            return continueStatement()
        }

        return expressionStatement()
    }

    private fun whileStatement() : Stmt {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'while'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after 'while' condition")

        val body = statement()

        return Stmt.While(condition, body, null, null)
    }

    private fun breakStatement() : Stmt {
        val stmt = Stmt.Break(previous())
        consume(TokenType.SEMICOLON, "Expected ';' after break")
        return stmt
    }

    private fun continueStatement() : Stmt {
        val stmt = Stmt.Continue(previous())
        consume(TokenType.SEMICOLON, "Expected ';' after continue")
        return stmt
    }

    private fun forStatement() : Stmt {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'for'")

        val initializer = if (match(TokenType.SEMICOLON)) null else if (match(TokenType.VAR)) varDeclaration() else expressionStatement()
        var condition = if (check(TokenType.SEMICOLON)) null else expression()
        consume(TokenType.SEMICOLON, "Expected ';' after loop condition")

        val increment = if (check(TokenType.RIGHT_PAREN)) null else expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after 'for' clause")

        var body = statement()
        val incrementStatement = if (increment != null) Stmt.Expression(increment) else null

        if (condition == null) {
            condition = Expr.Literal(true)
        }

        body = Stmt.While(condition, body, initializer, incrementStatement)

        return body
    }

    private fun ifStatement() : Stmt {
        consume(TokenType.LEFT_PAREN, "Expected '(' after 'if'")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expected ')' after 'if' condition")

        val thenBranch = statement()
        val elseBranch = if (match(TokenType.ELSE)) statement() else null

        return Stmt.If(condition, thenBranch, elseBranch)
    }

    private fun printStatement() : Stmt {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expected ; after print")
        return Stmt.Print(value)
    }

    private fun block() : List<Stmt> {
        val statements = ArrayList<Stmt>()

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            val decl = declaration()
            if (decl != null) {
                statements.add(decl)
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expected } after block")
        return statements
    }

    private fun expressionStatement() : Stmt {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expected ; after expression")
        return Stmt.Expression(expr)
    }

    private fun returnStatement() : Stmt {
        val keyword = previous()
        val value = if (check(TokenType.SEMICOLON)) null else expression()

        consume(TokenType.SEMICOLON, "Expected ';' after return value")
        return Stmt.Return(keyword, value)
    }

    private fun expression() : Expr {
        return assignment()
    }

    private fun assignment() : Expr {
        val expr = elvis()

        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()

            if (expr is Expr.Variable) {
                val name = expr.name
                return Expr.Assign(name, value)
            } else if (expr is Expr.Get) {
                return Expr.Set(expr.obj, expr.name, value)
            }

            error(equals, "Invalid assignment target")
        }

        return expr
    }

    private fun elvis() : Expr {
        var expr = or()

        if (match(TokenType.QUESTION)) {
            if (match(TokenType.COLON)) {
                val elseExpr = elvis()
                return Expr.Elvis(expr, null, elseExpr)
            } else {
                val thenExpr = elvis()
                consume(TokenType.COLON, "Expected ':' after then statement in elvis operator")
                val elseExpr = elvis()
                return Expr.Elvis(expr, thenExpr, elseExpr)
            }
        }

        return expr
    }

    private fun or() : Expr {
        var expr = and()

        while (match(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }

        return expr
    }

    private fun and() : Expr {
        var expr = equality()

        while (match(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
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

        return call()
    }

    private fun call() : Expr {
        var expr = primary()

        while (true) {
            if (match(TokenType.LEFT_PAREN)) {
                expr = finishCall(expr)
            } else if (match(TokenType.DOT)) {
                val name = consume(TokenType.IDENTIFIER, "Expected property name after '.'")
                expr = Expr.Get(expr, name)
            } else {
                break
            }
        }

        return expr
    }

    private fun finishCall(callee: Expr) : Expr {
        val arguments =  ArrayList<Expr>()

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size >= 8) {
                    error(peek(), "Cannot have more than 8 arguments")
                }

                arguments.add(expression())
            } while (match(TokenType.COMMA))
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments")

        return Expr.Call(callee, paren, arguments)
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

        if (match(TokenType.THIS)) {
            return Expr.This(previous())
        }

        if (match(TokenType.SUPER)) {
            val keyword = previous()
            consume(TokenType.DOT, "Expected '.' after super")
            val method = consume(TokenType.IDENTIFIER, "Expected superclass method name")
            return Expr.Super(keyword, method)
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