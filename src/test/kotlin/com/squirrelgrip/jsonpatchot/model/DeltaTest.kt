package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.core.JsonPointer
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.AddOperation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DeltaTest {
    @Test
    fun `build paths using AddOperation`() {
        val source = "{}".toJsonNode()
        assertThat(Delta.getFillerOperations(JsonPointer.compile("/a"), source)).isEmpty()
        assertThat(Delta.getFillerOperations(JsonPointer.compile("/a/0"), source)).containsExactly(AddOperation("/a", "[]".toJsonNode()))
        assertThat(Delta.getFillerOperations(JsonPointer.compile("/a/0/a/0"), source)).containsExactly(
            AddOperation("/a", "[]".toJsonNode()),
            AddOperation("/a/0", "{}".toJsonNode()),
            AddOperation("/a/0/a", "[]".toJsonNode())
        )
    }

}