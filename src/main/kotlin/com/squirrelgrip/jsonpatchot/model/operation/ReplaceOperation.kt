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
    value: JsonNode
): ValueOperation(path, value) {
    constructor(path: String, value: Any): this(JsonPath(path), value.toJson().toJsonNode())
    constructor(path: String, value: JsonNode): this(JsonPath(path), value)

    override val operation: OperationType = OperationType.REPLACE

    override fun transform(operations: List<Operation>): List<Operation> {
        val filteredOperations = operations.filter {
            it !is ReplaceOperation || it.path.intersects(path) && it.value != value
        }.filter {
            it !is RemoveOperation || it.path.intersects(path) && it.value == value
        }
        return removeOperations(filteredOperations, false, false)
    }

    override fun updatePath(updatedPath: JsonPath): Operation {
        return ReplaceOperation(updatedPath, value)
    }
}