package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.JsonPath
import com.squirrelgrip.jsonpatchot.model.Operation
import com.squirrelgrip.jsonpatchot.model.OperationType
import com.squirrelgrip.jsonpatchot.model.ValueOperation

class ReplaceOperation(
    path: JsonPath,
    value: JsonNode,
    val fromValue: JsonNode
) : ValueOperation(path, value) {
    constructor(path: String, value: Any, fromValue: Any) : this(JsonPath(path), value.toJson().toJsonNode(), fromValue.toJson().toJsonNode())
    constructor(path: String, value: JsonNode, fromValue: JsonNode) : this(JsonPath(path), value, fromValue)

    override val operation: OperationType = OperationType.REPLACE

    override fun toString(): String {
        return """{"op":"${operation.value}","fromValue":$fromValue,"path":"$path","value":$value}"""
    }
    override fun transform(operations: List<Operation>): List<Operation> {
        return operations
    }

    override fun updatePath(updatedPath: JsonPath): Operation {
        return ReplaceOperation(updatedPath, value, fromValue)
    }

    override fun reverse(): Operation {
        return ReplaceOperation(path, fromValue, value)
    }
}