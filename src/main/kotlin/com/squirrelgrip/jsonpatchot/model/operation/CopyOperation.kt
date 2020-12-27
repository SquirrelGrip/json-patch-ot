package com.squirrelgrip.jsonpatchot.model.operation

import com.fasterxml.jackson.core.JsonPointer
import com.squirrelgrip.jsonpatchot.model.FromOperation
import com.squirrelgrip.jsonpatchot.model.OperationType

class CopyOperation(
    path: JsonPointer,
    from: JsonPointer
): FromOperation(path, from) {
    constructor(path: String, from: String): this(JsonPointer.compile(path), JsonPointer.compile(from))
    override val operation: OperationType = OperationType.COPY
}