package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.Operation
import com.squirrelgrip.jsonpatchot.model.OperationType
import com.squirrelgrip.jsonpatchot.model.ValueOperation

class AddOperation(
    path: JsonPointer,
    value: JsonNode
) : ValueOperation(path, value) {
    constructor(path: String, value: JsonNode) : this(JsonPointer.compile(path), value)
    constructor(path: String, value: Any) : this(path, value.toJson().toJsonNode())

    override val operation: OperationType = OperationType.ADD

    override fun transform(operations: List<Operation>): List<Operation> {
        return operations.map {
            if (it is AddOperation && it.isScalarOperation() && it.path == path) {
                ReplaceOperation(it.path, it.value, value)
            } else {
                it
            }
        }
    }
}