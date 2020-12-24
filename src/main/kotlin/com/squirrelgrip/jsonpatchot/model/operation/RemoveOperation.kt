package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.JsonPath
import com.squirrelgrip.jsonpatchot.model.Operation
import com.squirrelgrip.jsonpatchot.model.OperationType
import com.squirrelgrip.jsonpatchot.model.ValueOperation

class RemoveOperation(
    path: JsonPath,
    value: JsonNode
) : ValueOperation(path, value) {
    constructor(path: String, value: Any) : this(JsonPath(path), value.toJson().toJsonNode())
    constructor(path: String, value: JsonNode) : this(JsonPath(path), value)

    override val operation: OperationType = OperationType.REMOVE

    override fun toString(): String {
        return """{"op":"${operation.value}","path":"$path","value":$value}"""
    }

    override fun transform(operations: List<Operation>): List<Operation> {
        LOGGER.debug("appliedOperation = $this")
        LOGGER.debug("operations = $operations")
        val a = removeOperations(operations, true, true)
        var b = a.first
        a.second.forEach {
            b = shiftIndices(b, it is AddOperation)
        }
        val c = b.map {
            if (it is ReplaceOperation && path == it.path && value != it.value) {
                AddOperation(it.path, it.value)
            } else {
                it
            }
        }
        return shiftIndices(removeOperations(c, true, true).first, false)
    }

    override fun updatePath(updatedPath: JsonPath): Operation {
        return RemoveOperation(updatedPath, value)
    }

    override fun keepOperation(operation: Operation, replaceAccepted: Boolean, allowWhiteList: Boolean): Boolean {
        return if (operation is ReplaceOperation) {
            replaceAccepted && operation.path == path && operation.value != value
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

    override fun reverse(): Operation = AddOperation(path, value)

}

