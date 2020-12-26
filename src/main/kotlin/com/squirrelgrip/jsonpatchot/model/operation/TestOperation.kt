package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.*

class TestOperation(
    path: JsonPath,
    value: JsonNode
): ValueOperation(path, value) {
    constructor(path: String, value: Any): this(JsonPath(path), value.toJson().toJsonNode())
    constructor(path: String, value: JsonNode): this(JsonPath(path), value)

    override val operation: OperationType = OperationType.TEST

    override fun updatePath(updatedPath: JsonPath): Operation {
        return TestOperation(updatedPath, value)
    }

    override fun reverse(): Operation {
        throw InvalidOperationException("TestOperation cannot be reversed")
    }


}