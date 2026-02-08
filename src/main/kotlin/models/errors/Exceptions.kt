package models.errors

import models.Value

class BreakException : RuntimeException()
class ContinueException : RuntimeException()
class RetornoException(val value: Value) : RuntimeException()

// find a use case for this use case...
class MainExecutionException(msg: String) : RuntimeException(msg)
class InputException(msg: String) : RuntimeException(msg)
class MagRuntimeException(msg: String) : RuntimeException(msg)
class ArquivoException(msg: String) : RuntimeException(msg)
class SemanticError(msg: String?) : RuntimeException(msg)
class MemoryError(msg: String?) : RuntimeException(msg)
