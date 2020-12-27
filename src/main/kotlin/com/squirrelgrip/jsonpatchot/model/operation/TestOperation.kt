package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.OperationType
import com.squirrelgrip.jsonpatchot.model.ValueOperation

class TestOperation(
    path: JsonPointer,
    value: JsonNode
): ValueOperation(path, value) {
    constructor(path: String, value: JsonNode): this(JsonPointer.compile(path), value)
    constructor(path: String, value: Any): this(path, value.toJson().toJsonNode())

    override val operation: OperationType = OperationType.TEST
}