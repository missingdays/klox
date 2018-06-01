import com.sun.org.apache.xpath.internal.WhitespaceStrippingElementMatcher

class Scanner {
    val source: String
    val tokens: MutableList<Token> = ArrayList()

    var start = 0
    var current = 0
    var line = 0

    val keywords = mapOf(
            "and" to TokenType.AND,
            "class" to TokenType.CLASS,
            "else" to TokenType.ELSE,
            "false" to TokenType.FALSE,
            "for" to TokenType.FOR,
            "fun" to TokenType.FUN,
            "if" to TokenType.IF,
            "nil" to TokenType.NIL,
            "or" to TokenType.OR,
            "print" to TokenType.PRINT,
            "return" to TokenType.RETURN,
            "super" to TokenType.SUPER,
            "this" to TokenType.THIS,
            "true" to TokenType.TRUE,
            "var" to TokenType.VAR,
            "while" to TokenType.WHILE
    )

    constructor(source: String) {
        this.source = source
    }

    fun scanTokens() : List<Token> {
        tokens.clear()

        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", null, line))

        return tokens
    }

    private fun isAtEnd() : Boolean {
        return current >= source.length
    }

    private fun scanToken() {
        val c = advance()

        when {
            c == '(' -> addToken(TokenType.LEFT_PAREN)
            c == ')' -> addToken(TokenType.RIGHT_PAREN)
            c == '{' -> addToken(TokenType.LEFT_BRACE)
            c == '}' -> addToken(TokenType.RIGHT_BRACE)
            c == ',' -> addToken(TokenType.COMMA)
            c == '.' -> addToken(TokenType.DOT)
            c == '-' -> addToken(TokenType.MINUS)
            c == '+' -> addToken(TokenType.PLUS)
            c == ';' -> addToken(TokenType.SEMICOLON)
            c == '*' -> addToken(TokenType.STAR)
            c == '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            c == '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else TokenType.EQUAL)
            c == '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            c == '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            c == '/' -> {
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance()
                } else {
                    addToken(TokenType.SLASH)
                }
            }
            c == ' ' || c == '\r' || c == '\t' -> {}
            c == '\n' -> line++
            c == '"' -> addString()
            c in '0'..'9'  -> addNumber()
            c.isLetter() -> addIdentifier()
            else -> error(line, "Unexpected character")
        }
    }

    private fun advance() : Char {
        current++
        return source[current-1]
    }

    private fun addToken(type: TokenType) {
        addToken(type, null)
    }

    private fun addToken(type: TokenType, literal: Any?) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, literal, line))
    }

    private fun match(expected: Char) : Boolean {
        if (isAtEnd() || source[current] != expected) {
            return false
        }

        current++
        return true
    }

    private fun peek() : Char {
        if (isAtEnd()) {
            return '\u0000'
        }

        return source[current]
    }

    private fun peekNext() : Char {
        if (current + 1 >= source.length) {
            return '\u0000'
        }

        return source[current+1]
    }

    private fun addString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                line++
            }
            advance()
        }

        if (isAtEnd()) {
            error(line, "Unexpected EOF while parsing string")
            return
        }

        advance()

        val text = source.substring(start + 1, current - 1)
        addToken(TokenType.STRING, text)
    }

    private fun addNumber() {
        while (peek() in '0'..'9') {
            advance()
        }

        if (peek() == '.' && peekNext() in '0'..'9'){
            advance()

            while (peek() in '0'..'9') {
                advance()
            }
        }

        addToken(TokenType.NUMBER, source.substring(start, current).toDouble())
    }

    private fun addIdentifier() {
        while (peek().isLetterOrDigit()) {
            advance()
        }

        val text = source.substring(start, current)

        val type = if (keywords.containsKey(text)) keywords.getValue(text) else TokenType.IDENTIFIER

        addToken(type)
    }

}