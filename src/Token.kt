import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class Token {
    val type: TokenType
    val lexeme: String
    val literal: Any?
    val line: Int

    constructor(type: TokenType, lexeme: String, literal: Any?, line: Int) {
        this.type = type
        this.lexeme = lexeme
        this.literal = literal
        this.line = line
    }


    override fun toString() : String {
        return "Token($type, $lexeme, $literal, $line)"
    }
}