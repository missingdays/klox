class LoxRuntimeError(token: Token, message: String) : RuntimeException(message) {
    val token = token
}