package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.squirrelgrip.jsonpatchot.model.operation.*
import org.slf4j.LoggerFactory

abstract class Operation(
    val path: JsonPath
) {
    constructor(path: String) : this(JsonPath(path))

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
                else -> throw InvalidOperationException("TestOperation cannot be reverese")
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
        if (operation != other.operation) return false
        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + operation.hashCode()
        return result
    }

    abstract fun updatePath(updatedPath: JsonPath): Operation

    open fun isArrayOperation(): Boolean {
        return path.isArrayElement
    }
    fun isScalarOperation(): Boolean {
        return !isArrayOperation()
    }

    fun shiftIndices(
        proposedOperations: List<Operation>,
        isAdd: Boolean = false
    ): List<Operation> {
        return if (path.isArrayElement) {
            proposedOperations.map {
                if (it.path.hasSameArrayPath(path)) {
                    it.updatePath(it.path.replacePathIndices(path, isAdd))
                } else {
                    it
                }
            }
        } else {
            proposedOperations
        }
    }

    fun removeOperations(
        proposedOps: List<Operation>,
        acceptedWinsOnEqualPath: Boolean = false,
        allowWhitelist: Boolean = false
    ): List<Operation> {
        return proposedOps.filter {
            var matchesFromToPath = false;
            if (it is FromOperation) {
                matchesFromToPath = it.from == path || it.from.intersects(JsonPath("${path.path}/"))
            }
            val matchesPathToPath = (acceptedWinsOnEqualPath && it.path == path) || it.path.intersects(JsonPath("${path.path}/"))
            val shouldKeep = if (allowWhitelist) isWhitelisted(this, it) else false
            shouldKeep || !(matchesFromToPath || matchesPathToPath)
        }
    }

    fun isWhitelisted(acceptedOp: Operation, proposedOp: Operation): Boolean {
        return (proposedOp is AddOperation || proposedOp is TestOperation) && path.intersects(proposedOp.path)
    }

    abstract fun reverse(): Operation

}

abstract class FromOperation(
    path: JsonPath,
    val from: JsonPath
) : Operation(path) {
    override fun toString(): String {
        return """{"op":"${operation.value}","path":"$path","from":"$from"}"""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as FromOperation
        if (from != other.from) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + from.hashCode()
        return result
    }

}

abstract class ValueOperation(
    path: JsonPath,
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
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }

    override fun isArrayOperation(): Boolean {
        return super.isArrayOperation() || value.isArray
    }
}

fun JsonNode.values() = if (this.isEmpty || this.at("/a").isMissingNode) emptySet() else (this["a"] as ArrayNode).map { it.asInt() }.toSortedSet()
