package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.Operation
import com.squirrelgrip.jsonpatchot.model.OperationType
import com.squirrelgrip.jsonpatchot.model.ValueOperation

class RemoveOperation(
    path: JsonPointer,
    value: JsonNode
) : ValueOperation(path, value) {
    constructor(path: String, value: JsonNode) : this(JsonPointer.compile(path), value)
    constructor(path: String, value: Any) : this(path, value.toJson().toJsonNode())

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
}

