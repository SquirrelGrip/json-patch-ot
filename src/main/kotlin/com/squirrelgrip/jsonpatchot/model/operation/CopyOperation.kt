package com.squirrelgrip.jsonpatchot.model.operation

import com.squirrelgrip.jsonpatchot.model.FromOperation
import com.squirrelgrip.jsonpatchot.model.JsonPath
import com.squirrelgrip.jsonpatchot.model.Operation
import com.squirrelgrip.jsonpatchot.model.OperationType

class CopyOperation(
    path: JsonPath,
    from: JsonPath
): FromOperation(path, from) {
    constructor(path: String, from: String): this(JsonPath(path), JsonPath(from))

    override val operation: OperationType = OperationType.COPY

    override fun updatePath(updatedPath: JsonPath): Operation {
        return CopyOperation(updatedPath, from)
    }
}