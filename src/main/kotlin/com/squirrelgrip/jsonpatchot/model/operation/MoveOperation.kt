package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.FromOperation
import com.squirrelgrip.jsonpatchot.model.JsonPath
import com.squirrelgrip.jsonpatchot.model.Operation
import com.squirrelgrip.jsonpatchot.model.OperationType

class MoveOperation(
    path: JsonPath,
    from: JsonPath
): FromOperation(path, from) {
    constructor(path: String, from: String): this(JsonPath(path), JsonPath(from))
    override val operation: OperationType = OperationType.MOVE
    override fun updatePath(updatedPath: JsonPath): Operation {
        return MoveOperation(updatedPath, from)
    }
}