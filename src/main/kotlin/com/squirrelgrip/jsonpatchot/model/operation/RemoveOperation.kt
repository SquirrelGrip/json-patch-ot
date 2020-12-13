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
    value: JsonNode = "".toJsonNode()
) : ValueOperation(path, value) {
    constructor(path: String, value: Any) : this(JsonPath(path), value.toJson().toJsonNode())
    constructor(path: String) : this(JsonPath(path))

    override val operation: OperationType = OperationType.REMOVE

    override fun toString(): String {
        return """{"op":"${operation.value}","path":"$path"}"""
    }

    override fun transform(operations: List<Operation>): List<Operation> {

        val candidateOperations = operations.flatMap {
            val pathsIntersect = it.path.intersects(path)
            if (pathsIntersect) {
                if (it is RemoveOperation) {
                    emptyList()
                } else if (it is ReplaceOperation) {
                    listOf(AddOperation(it.path, it.value))
                } else if (it is AddOperation) {
                    listOf(AddOperation(it.path, it.value))
                } else {
                    listOf(it)
                }
            } else {
                listOf(it)
            }
        }
        return shiftIndices(candidateOperations)
    }

    override fun updatePath(updatedPath: JsonPath): Operation {
        return RemoveOperation(updatedPath)
    }

}

