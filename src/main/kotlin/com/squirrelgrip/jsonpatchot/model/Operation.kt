package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.squirrelgrip.jsonpatchot.model.operation.*
import org.slf4j.LoggerFactory

abstract class Operation(
    val path: JsonPointer
) {
    constructor(path: String) : this(JsonPointer.compile(path))

    companion object {
        val LOGGER = LoggerFactory.getLogger(Operation::class.java)

        fun create(jsonNode: JsonNode): Operation {
            val path = jsonNode["path"].asText()
            val value = jsonNode["value"] ?: ""
            val fromValue = jsonNode["fromValue"] ?: ""
            val from = jsonNode["from"]?.asText() ?: ""
            return when (jsonNode["op"].asText()) {
                "add" -> AddOperation(path, value)
                "test" -> TestOperation(path, value)
                "remove" -> RemoveOperation(path, value)
                "replace" -> ReplaceOperation(path, value, fromValue)
                "move" -> MoveOperation(path, from)
                "copy" -> CopyOperation(path, from)
                else -> throw InvalidOperationException("Unknown operation code")
            }
        }

    }

    abstract val operation: OperationType

    open fun transform(operations: List<Operation>): List<Operation> {
        return operations
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Operation
        if (path != other.path) return false
        return operation == other.operation
    }

    override fun hashCode(): Int {
        return 31 * path.hashCode() + operation.hashCode()
    }

    open fun isArrayOperation(): Boolean {
        return path.last().mayMatchElement()
    }

    fun isScalarOperation(): Boolean {
        return !isArrayOperation()
    }
}

abstract class FromOperation(
    path: JsonPointer,
    val from: JsonPointer
) : Operation(path) {
    override fun toString(): String {
        return """{"op":"${operation.value}","path":"$path","from":"$from"}"""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as FromOperation
        return from == other.from
    }

    override fun hashCode(): Int {
        return 31 * super.hashCode() + from.hashCode()
    }

}

abstract class ValueOperation(
    path: JsonPointer,
    val value: JsonNode
) : Operation(path) {
    override fun toString(): String {
        return """{"op":"${operation.value}","path":"$path","value":$value}"""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return value == (other as ValueOperation).value
    }

    override fun hashCode(): Int {
        return 31 * super.hashCode() + value.hashCode()
    }

    override fun isArrayOperation(): Boolean {
        return super.isArrayOperation() || value.isArray
    }
}
