package com.squirrelgrip.jsonpatchot.model;

import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.AddOperation
import com.squirrelgrip.jsonpatchot.model.operation.RemoveOperation

import org.junit.jupiter.api.Test

class GuessTest {

    @Test
    fun something() {
        val acceptedOps: List<Operation> = listOf(
            """{op:"remove",path:"/a/2",value:3}""",
            """{op:"remove",path:"/a/1",value:2}""",
            """{op:"add",path:"/a/0",value:3}""",
            """{op:"add",path:"/a/1",value:2}"""
        ).map{Operation.create(it.toJsonNode())}


        val proposedOps: List<Operation> = listOf(
            """{op:"remove",path:"/a/2",value:3}""",
            """{op:"remove",path:"/a/1",value:2}""",
            """{op:"add",path:"/a/0",value:3}""",
            """{op:"add",path:"/a/1",value:2}"""
        ).map{Operation.create(it.toJsonNode())}

        transform(acceptedOps, proposedOps)
    }

}
