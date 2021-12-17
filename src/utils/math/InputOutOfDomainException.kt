package utils.math

import java.lang.RuntimeException

class InputOutOfDomainException : RuntimeException {
    constructor(message: String?) : super(message)
    constructor() : super()
}