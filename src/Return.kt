class Return : RuntimeException {
    val value : Any?

    constructor(value: Any?) : super(null, null, false, false) {
        this.value = value
    }
}