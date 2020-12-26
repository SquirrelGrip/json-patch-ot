package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.JsonPath
import com.squirrelgrip.jsonpatchot.model.Operation
import com.squirrelgrip.jsonpatchot.model.OperationType
import com.squirrelgrip.jsonpatchot.model.ValueOperation

class AddOperation(
    path: JsonPath,
    value: JsonNode
) : ValueOperation(path, value) {
    constructor(path: String, value: Any) : this(JsonPath(path), value.toJson().toJsonNode())
    constructor(path: String, value: JsonNode) : this(JsonPath(path), value)

    override val operation: OperationType = OperationType.ADD

    override fun transform(operations: List<Operation>): List<Operation> {
        return operations.map {
            if (it is AddOperation && it.isScalarOperation()) {
                ReplaceOperation(it.path, it.value, value)
            } else {
                it
            }
        }
    }

    override fun updatePath(updatedPath: JsonPath): Operation {
        return AddOperation(updatedPath, value)
    }

    override fun reverse(): Operation {
        return RemoveOperation(path, value)
    }

}