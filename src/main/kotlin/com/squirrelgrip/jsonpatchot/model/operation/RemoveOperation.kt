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
        return operations.map {
            if (it is ReplaceOperation && it.path == path) {
                AddOperation(it.path, it.value)
            } else {
                it
            }
        }
    }

    override fun updatePath(updatedPath: JsonPath): Operation {
        return RemoveOperation(updatedPath, value)
    }

    override fun reverse(): Operation {
        return AddOperation(path, value)
    }

}

