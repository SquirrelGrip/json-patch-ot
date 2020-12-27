package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.flipkart.zjsonpatch.JsonDiff
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

class OperationTest {

    @Test
    fun testToString() {
        assertThat(
            AddOperation(
                "/hello",
                "world"
            ).toString()
        ).isEqualTo("""{"op":"add","path":"/hello","value":"world"}""")
        assertThat(
            RemoveOperation(
                "/hello",
                "world"
            ).toString()
        ).isEqualTo("""{"op":"remove","path":"/hello","value":"world"}""")
        assertThat(
            ReplaceOperation(
                "/hello",
                "world",
                "saturn"
            ).toString()
        ).isEqualTo("""{"op":"replace","fromValue":"saturn","path":"/hello","value":"world"}""")
        assertThat(
            MoveOperation(
                "/hello",
                "/world"
            ).toString()
        ).isEqualTo("""{"op":"move","path":"/hello","from":"/world"}""")
        assertThat(
            CopyOperation(
                "/hello",
                "/world"
            ).toString()
        ).isEqualTo("""{"op":"copy","path":"/hello","from":"/world"}""")
        assertThat(
            TestOperation(
                "/hello",
                "world"
            ).toString()
        ).isEqualTo("""{"op":"test","path":"/hello","value":"world"}""")
    }

}
