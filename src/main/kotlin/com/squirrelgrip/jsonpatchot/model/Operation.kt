package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.squirrelgrip.jsonpatchot.model.operation.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class Operation(
    val path: JsonPath
) {
    constructor(path: String) : this(JsonPath(path))

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Operation::class.java)

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
                else -> throw InvalidOperationException()
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

    fun shiftIndices(
        operations: List<Operation>,
        isAdd: Boolean = false
    ): List<Operation> {
        LOGGER.debug("Before shift: $operations")
        val shiftedOperations = if (path.isArrayElement) {
            operations.map {
                if (it.path.hasSameArrayPath(path)) {
                    it.updatePath(it.path.replacePathIndices(path, isAdd))
                } else {
                    it
                }
            }
        } else {
            operations
        }
        LOGGER.debug("After shift: $shiftedOperations")
        return shiftedOperations
    }

    fun removeOperations(
        operations: List<Operation>,
        replaceAccepted: Boolean = false,
        allowWhitelist: Boolean = false
    ): Pair<List<Operation>, List<Operation>> {
        LOGGER.debug("Before remove: $operations")
        val partition = operations.partition {
            keepOperation(it, replaceAccepted, allowWhitelist)
        }
        LOGGER.debug("After remove (kept): ${partition.first}")
        LOGGER.debug("After remove (removed): ${partition.second}")
        return partition
    }

    open fun keepOperation(
        operation: Operation,
        replaceAccepted: Boolean,
        allowWhiteList: Boolean
    ): Boolean {
        val pathsMatch = replaceAccepted && operation.path == path
        val isWhiteListed = if (allowWhiteList) isWhiteListed(operation) else false
        return isWhiteListed || !pathsMatch
    }

    fun isWhiteListed(operation: Operation): Boolean {
        return (operation is AddOperation || operation is TestOperation || operation is ReplaceOperation) && path.intersects(operation.path)
    }

    abstract fun reverse(): Operation

}

abstract class FromOperation(
    path: JsonPath,
    val from: JsonPath
) : Operation(path) {
    override fun keepOperation(
        operation: Operation,
        replaceAccepted: Boolean,
        allowWhitelist: Boolean
    ): Boolean {
        val pathsMatch =
            (replaceAccepted && operation.path == path) && operation.path.path.indexOf("${path.path}/") == 0
        val shouldKeep = if (allowWhitelist) isWhiteListed(operation) else false
        return shouldKeep || !pathsMatch
    }

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
}

fun allowWhitelist (acceptedOp: Operation, proposedOp: Operation): Boolean {
    return proposedOp.operation in arrayOf(OperationType.ADD, OperationType.TEST) && acceptedOp.path == proposedOp.path
}

fun removeOperations(
    acceptedOp: Operation,
    proposedOps: MutableList<Operation>,
    acceptedWinsOnEqualPath: Boolean,
    skipWhitelist: Boolean = false
) {
    var currentIndex = 0
    var proposedOp: Operation
    while (currentIndex < proposedOps.size) {
        proposedOp = proposedOps[currentIndex]
        val matchesPathToPath = (acceptedWinsOnEqualPath && acceptedOp.path == proposedOp.path) || proposedOp.path.path.indexOf("${acceptedOp.path}/") == 0
        val shouldSkip = if (skipWhitelist) allowWhitelist (acceptedOp, proposedOp) else false
        if (!shouldSkip && matchesPathToPath) {
            proposedOps.removeAt(currentIndex)
            currentIndex--
        }
        currentIndex++
    }
}

fun JsonNode.convert(): List<Operation> =
    if (this is ArrayNode) {
        this.map {
            Operation.create(it)
        }.toList()
    } else {
        listOf(Operation.create(this))
    }

fun getTransformedOperations(
    appliedOperations: List<Operation>,
    candidateOperations: List<Operation>
): List<Operation> {
    return if (appliedOperations.isEmpty()) {
        candidateOperations.filter {
            it !is RemoveOperation || !(it.value.isArray && it.value.isEmpty)
        }
    } else {
        var transformedOperations = candidateOperations.filter {
            it !is AddOperation || !(it.value.isArray && it.value.isEmpty)
        }
        appliedOperations.forEach {
            transformedOperations = it.transform(transformedOperations)
        }
        transformedOperations
    }
}


