import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

var hadError = false
var hadRuntimeError = false

val interpreter = Interpreter()

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
        try {
            run(reader.readLine(), replMode = true)
        } catch (error: LoxRuntimeError) {
            println(error.message)
        }

        hadError = false
    }
}

private fun run(source: String, replMode: Boolean = false) {
    val scanner = Scanner(source)
    val tokens = scanner.scanTokens()

    val parser = Parser(tokens)
    val parseResult = parser.parse()

    if (hadError) {
        return
    }

    val resolver = Resolver(interpreter)
    resolver.resolve(parseResult)

    if (hadError) {
        return
    }

    val res = parseResult[0]

    if (replMode && parseResult.size == 1 && res is Stmt.Expression) {
        val result = interpreter.interpret(res.expression)
        println(result)
    } else {
        interpreter.interpret(parseResult)
    }

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
    hadError = true
}