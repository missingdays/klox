import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

var hadError = false
var hadRuntimeError = false

fun main(args: Array<String> ) {
    when {
        args.size > 1 -> println("Usage: jlox [script]")
        args.size == 1 -> runFile(args[0])
        else -> runPrompt()
    }
}

private fun runFile(path: String) {
    val bytes = Files.readAllBytes(Paths.get(path))
    run(String(bytes, Charset.defaultCharset()))

    if (hadError) {
        System.exit(65)
    }

    if (hadRuntimeError) {
        System.exit(70)
    }
}

private fun runPrompt() {
    val input = InputStreamReader(System.`in`)
    val reader = BufferedReader(input)

    while (true) {
        print("> ")
        run(reader.readLine())
        hadError = false
    }
}

private fun run(source: String) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    val parser = Parser(tokens)
    val expression = parser.parse()

    if (hadError) {
        return
    }

    val interpreter = Interpreter()
    interpreter.interpret(expression!!)

    if (hadError) return
}

fun parseError(token: Token, message: String) {
    if (token.type == TokenType.EOF) {
        report(token.line, " at end", message)
    } else {
        report(token.line, " at '${token.lexeme}'", message)
    }
}

fun runtimeError(error: LoxRuntimeError) {
    println("${error.message}")
    println("[line ${error.token.line}]")
    hadRuntimeError = true
}

fun error(line: Int, message: String) {
    report(line, "", message)
}

fun report(line: Int, where: String, message: String) {
    println("[line $line] Error$where: $message")
}