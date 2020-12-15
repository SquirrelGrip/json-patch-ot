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
        val filteredOperations = operations.filter {
            it !is RemoveOperation || !it.path.intersects(path) || !it.path.isArrayElement
        }.map {
            if (it is ReplaceOperation && it.path.intersects(path)) {
                AddOperation(it.path, it.value)
            } else {
                it
            }
        }
        return shiftIndices(removeOperations(filteredOperations, true, true), false)
    }

    override fun updatePath(updatedPath: JsonPath): Operation {
        return RemoveOperation(updatedPath, value)
    }

}

