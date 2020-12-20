package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.flipkart.zjsonpatch.JsonDiff
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class OperationTest {

    companion object {
        @JvmStatic
        fun intScalars() =
            arguments(
                """{}""",
                """{"a":1}""",
                """{"a":2}""",
                """{"a":3}""",
            )


        @JvmStatic
        fun intArrays() =
            arguments(
                """{}""",
                """{"a":[]}""",
                """{"a":[1]}""",
                """{"a":[2]}""",
                """{"a":[3]}""",
                """{"a":[1,2]}""",
                """{"a":[1,3]}""",
                """{"a":[2,3]}""",
                """{"a":[1,2,3]}""",
//                """{"a":[3,2,1]}""",
            )


        private fun arguments(vararg values: String): Stream<Arguments> {
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

    @ParameterizedTest
    @MethodSource("intArrays")
    fun intArrays(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode
    ) {
        verifyOperations(
            original,
            documentA,
            documentB,
            { if (it.isEmpty || it.at("/a").isEmpty) """{"a":[]}""".toJsonNode() else it },
            { appliedDocumentAB, appliedDocumentBA ->
                if (original.isEmpty || (!documentA.isEmpty && !documentB.isEmpty)) {
                    assertThat(appliedDocumentAB.source["a"].asText()).isEqualTo(documentB["a"].asText())
                    assertThat(appliedDocumentBA.source["a"].asText()).isEqualTo(documentA["a"].asText())
                }
                assertThat(appliedDocumentAB.source["a"].size()).isEqualTo(appliedDocumentBA.source["a"].size())
            })
    }

    @ParameterizedTest
    @MethodSource("intScalars")
    fun intScalars(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode
    ) {
        verifyOperations(
            original,
            documentA,
            documentB,
            { it },
            { appliedDocumentAB, appliedDocumentBA ->
                if (original.isEmpty || (!documentA.isEmpty && !documentB.isEmpty)) {
                    println(appliedDocumentAB.source)
                    println(documentB)
                    assertThat(appliedDocumentAB.source["a"].asText()).isEqualTo(documentB["a"].asText())
                    assertThat(appliedDocumentBA.source["a"].asText()).isEqualTo(documentA["a"].asText())
                }
            })
    }

    fun verifyOperations(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode,
        expectedDocumentAdapter: (JsonNode) -> JsonNode,
        verifier: (appliedDocumentAB: Document, appliedDocumentBA: Document) -> Unit
    ) {
        val originalDocument = Document(original)
        val deltaA: Delta = originalDocument.generatePatch(documentA)
        val deltaB: Delta = originalDocument.generatePatch(documentB)
        val diffA: JsonNode = JsonDiff.asJson(original, documentA, Document.DIFF_FLAGS)
        val diffB: JsonNode = JsonDiff.asJson(original, documentB, Document.DIFF_FLAGS)

        println("Inputs")
        println("original = $original")
        println("A = $documentA")
        println("B = $documentB")
        assertThat(deltaA.toJsonNode()).isEqualTo(diffA.toString().toJsonNode())
        assertThat(deltaB.toJsonNode()).isEqualTo(diffB.toString().toJsonNode())

        println("Outputs")
        val appliedDocumentA = originalDocument.transform(deltaA)
        println("appliedDocumentA = $appliedDocumentA => ${appliedDocumentA.appliedDeltas}")
        assertThat(appliedDocumentA.source).isEqualTo(expectedDocumentAdapter(documentA))

        val appliedDocumentB = originalDocument.transform(deltaB)
        println("appliedDocumentB = $appliedDocumentB => ${appliedDocumentB.appliedDeltas}")
        assertThat(appliedDocumentB.source).isEqualTo(expectedDocumentAdapter(documentB))

        val appliedDocumentAB = appliedDocumentA.transform(deltaB)
        println("appliedDocumentAB = $appliedDocumentAB => ${appliedDocumentAB.appliedDeltas}")
        val appliedDocumentBA = appliedDocumentB.transform(deltaA)
        println("appliedDocumentBA = $appliedDocumentBA => ${appliedDocumentBA.appliedDeltas}")

        if (documentA.toString() == documentB.toString()) {
            assertThat(appliedDocumentAB.appliedDeltas[1].operations).isEmpty()
            assertThat(appliedDocumentBA.appliedDeltas[1].operations).isEmpty()
            assertThat(appliedDocumentAB.source.toString()).isEqualTo(appliedDocumentBA.source.toString())
        }

        verifier(appliedDocumentAB, appliedDocumentBA)
    }

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


}
