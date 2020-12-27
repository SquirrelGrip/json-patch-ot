package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.OperationType
import com.squirrelgrip.jsonpatchot.model.ValueOperation

class ReplaceOperation(
    path: JsonPointer,
    value: JsonNode,
    val fromValue: JsonNode
) : ValueOperation(path, value) {
    constructor(path: String, value: JsonNode, fromValue: JsonNode) : this(JsonPointer.compile(path), value, fromValue)
    constructor(path: String, value: Any, fromValue: Any) : this(path, value.toJson().toJsonNode(), fromValue.toJson().toJsonNode())

    override val operation: OperationType = OperationType.REPLACE

    override fun toString(): String {
        return """{"op":"${operation.value}","fromValue":$fromValue,"path":"$path","value":$value}"""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as ReplaceOperation
        if (fromValue != other.fromValue) return false
        return operation == other.operation
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + fromValue.hashCode()
        return 31 * result + operation.hashCode()
    }

}