package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.JsonPath
import com.squirrelgrip.jsonpatchot.model.Operation
import com.squirrelgrip.jsonpatchot.model.OperationType
import com.squirrelgrip.jsonpatchot.model.ValueOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AddOperation(
    path: JsonPath,
    value: JsonNode
) : ValueOperation(path, value) {
    constructor(path: String, value: Any) : this(JsonPath(path), value.toJson().toJsonNode())
    constructor(path: String, value: JsonNode) : this(JsonPath(path), value)

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(AddOperation::class.java)
    }

    override val operation: OperationType = OperationType.ADD

    override fun transform(operations: List<Operation>): List<Operation> {
        LOGGER.debug("appliedOperation = $this")
        LOGGER.debug("operations = $operations")
        val a = operations.partition {
            it !is AddOperation || !(it.path.isArrayElement && path.isArrayElement && it.path.parent == path.parent && it.value == value)
        }
        var b = a.first
        a.second.forEach {
            b = shiftIndices(b, it is RemoveOperation)
        }
        val c = b.map {
            if (it is AddOperation && path == it.path && value != it.value && !it.path.isArrayElement) {
                ReplaceOperation(it.path, it.value)
            } else {
                it
            }
        }
        val transformedOperations = removeOperations(shiftIndices(c, true), true, false)
        LOGGER.debug("d = ${transformedOperations.first}")
        return transformedOperations.first
    }

    override fun updatePath(updatedPath: JsonPath): Operation {
        return AddOperation(updatedPath, value)
    }

    override fun keepOperation(operation: Operation, replaceAccepted: Boolean, allowWhiteList: Boolean): Boolean {
        return if (operation is ReplaceOperation) {
            replaceAccepted && !operation.path.isArrayElement && operation.path == path && operation.value != value
        } else if (operation is ValueOperation) {
            if (operation.path.isArrayElement) {
                operation.path.parent != path.parent || operation.value != value
            } else {
                val keepScalar = operation.path != path || operation.value != value
                val isEmptyArray = operation.value.isArray && operation.value.isEmpty
                keepScalar && !isEmptyArray
            }
        } else {
            super.keepOperation(operation, replaceAccepted, allowWhiteList)
        }
    }

    override fun reverse(): Operation = RemoveOperation(path, value)

}