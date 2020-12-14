package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.flipkart.zjsonpatch.DiffFlags
import com.flipkart.zjsonpatch.JsonDiff
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assumptions.assumeFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream

class OperationTest {

    companion object {
        @JvmStatic
        fun operations(): Stream<Arguments> {
            val values = listOf(
                """{}""",
                """{"a":1}""",
                """{"a":2}""",
                """{"a":3}""",
                """{"a":[]}""",
                """{"a":[1]}""",
                """{"a":[2]}""",
                """{"a":[3]}""",
                """{"a":[1,2]}""",
                """{"a":[1,3]}""",
                """{"a":[2,3]}""",
                """{"a":[1,2,3]}"""
            )
            return values.flatMap { original ->
                values.flatMap { documentA ->
                    values.flatMap { documentB ->
                        if (original != documentA && original != documentB) {
                            listOf(Arguments.of(original.toJsonNode(), documentA.toJsonNode(), documentB.toJsonNode()))
                        } else {
                            emptyList<Arguments>()
                        }
                    }
                }
            }.stream()
        }
    }

    @Test
    fun testToString() {
        assertThat(
            AddOperation(
                "/hello",
                "world"
            ).toString()
        ).isEqualTo("""{"op":"add","path":"/hello","value":"world"}""")
        assertThat(RemoveOperation("/hello", "world").toString()).isEqualTo("""{"op":"remove","path":"/hello"}""")
        assertThat(
            ReplaceOperation(
                "/hello",
                "world"
            ).toString()
        ).isEqualTo("""{"op":"replace","path":"/hello","value":"world"}""")
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

    @Test
    fun `build paths using AddOperation`() {
        assertThat(pathsUpTo(JsonPath("/a"))).isEmpty()
        assertThat(pathsUpTo(JsonPath("/a/0"))).containsExactly(AddOperation("/a", "[]".toJsonNode()))
        assertThat(pathsUpTo(JsonPath("/a/0/a/0"))).containsExactly(
            AddOperation("/a", "[]".toJsonNode()),
            AddOperation("/a/0", "{}".toJsonNode()),
            AddOperation("/a/0/a", "[]".toJsonNode())
        )
    }

    @ParameterizedTest
    @MethodSource("operations")
    fun `Applying Delta A then B and appling Delta B then A`(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode
    ) {
        val originalDocument = Document(original)
        val deltaA: Delta = originalDocument.generatePatch(documentA)
        val deltaB: Delta = originalDocument.generatePatch(documentB)
        val diffA: JsonNode = JsonDiff.asJson(original, documentA, EnumSet.of(DiffFlags.REMOVE_REMAINING_FROM_END))
        val diffB: JsonNode = JsonDiff.asJson(original, documentB, EnumSet.of(DiffFlags.REMOVE_REMAINING_FROM_END))

        println("Inputs")
        println("original = $original")
        println("A = $documentA => $diffA => $deltaA")
        println("B = $documentB => $diffB => $deltaB")
        assertThat(deltaA.toJsonNode()).isEqualTo(diffA.toString().toJsonNode())
        assertThat(deltaB.toJsonNode()).isEqualTo(diffB.toString().toJsonNode())

        println("Outputs")

        val appliedDocumentA = originalDocument.transform(deltaA)
        val appliedDocumentB = originalDocument.transform(deltaB)

        println("appliedDocumentA = $appliedDocumentA => ${appliedDocumentA.appliedDeltas}")
        println("appliedDocumentB = $appliedDocumentB => ${appliedDocumentB.appliedDeltas}")


        assumeFalse(
            documentA.toString() != "{}" && documentB.toString() != "{}" &&
                    (
                            (documentA["a"].isArray && !documentB["a"].isArray) ||
                            (documentB["a"].isArray && !documentA["a"].isArray)
                    )
        )
        assumeFalse(
            documentA.toString() == "{}" || documentB.toString() == "{}"
        )

        val appliedDocumentAB = appliedDocumentA.transform(deltaB)
        val appliedDocumentBA = appliedDocumentB.transform(deltaA)

        println("appliedDocumentAB = $appliedDocumentAB => ${appliedDocumentAB.appliedDeltas}")
        println("appliedDocumentBA = $appliedDocumentBA => ${appliedDocumentBA.appliedDeltas}")

        var orderDoesNotMatter = false
        if (documentA.toString() == documentB.toString()) {
            assertThat(appliedDocumentAB.appliedDeltas[1].operations).isEmpty()
            assertThat(appliedDocumentBA.appliedDeltas[1].operations).isEmpty()
            assertThat(appliedDocumentAB.source.toString()).isEqualTo(appliedDocumentBA.source.toString())
            orderDoesNotMatter = true
        }
        if (original.isEmpty || (!documentA.isEmpty && !documentA["a"].isArray && !documentB.isEmpty && !documentB["a"].isArray)) {
            assertThat(appliedDocumentAB.source["a"].asText()).isEqualTo(documentB["a"].asText())
            assertThat(appliedDocumentBA.source["a"].asText()).isEqualTo(documentA["a"].asText())
        }
        if (orderDoesNotMatter) {
            assertThat(appliedDocumentAB.source.toString()).isEqualTo(appliedDocumentBA.source.toString())
        }

        assertThat(appliedDocumentA.source.toString()).isEqualTo(documentA.toString())
        assertThat(appliedDocumentB.source.toString()).isEqualTo(documentB.toString())
    }

}
