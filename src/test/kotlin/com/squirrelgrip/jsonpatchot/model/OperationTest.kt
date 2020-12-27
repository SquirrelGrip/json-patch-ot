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

    companion object {
        val file = File("tests.json")
//            .apply {
//            this.delete()
//        }

        @JvmStatic
        fun scalars(): Stream<Arguments> {
            return generateArguments(
                """{}""",
                """{"a":1}""",
                """{"a":2}""",
                """{"a":3}""",
            )
        }

        @JvmStatic
        fun arrays(): Stream<Arguments> {
            return generateArguments(
                """{}""",
                """{"a":[]}""",
                """{"a":[1]}""",
                """{"a":[2]}""",
                """{"a":[3]}""",
                """{"a":[1,2]}""",
                """{"a":[1,3]}""",
                """{"a":[2,3]}""",
                """{"a":[1,2,3]}""",
                """{"a":[3,2,1]}""",
            )
        }

        private fun generateArguments(vararg value: String) = value.flatMap { original ->
            value.flatMap { documentA ->
                value.flatMap { documentB ->
                    if (original != documentA && original != documentB) {
                        listOf(Arguments.of(original.toJsonNode(), documentA.toJsonNode(), documentB.toJsonNode()))
                    } else {
                        emptyList<Arguments>()
                    }
                }
            }
        }.stream()
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

    @ParameterizedTest
    @MethodSource("scalars")
    fun scalars(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode
    ) {
        verifyOperations(
            original,
            documentA,
            documentB,
            { appliedDocumentAB, appliedDocumentBA ->
                if (!documentB.isEmpty)
                    assertThat(appliedDocumentAB.source).isEqualTo(documentB)
                if (!documentA.isEmpty)
                    assertThat(appliedDocumentBA.source).isEqualTo(documentA)
            },
            { it }
        )
    }

    @ParameterizedTest
    @MethodSource("arrays")
    fun arrays(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode
    ) {
        verifyOperations(
            original,
            documentA,
            documentB,
            { appliedDocumentAB, appliedDocumentBA ->
                val originalSet = original.values()
                val documentASet = documentA.values()
                val documentBSet = documentB.values()

                println("originalSet:$originalSet")
                println("documentASet:$documentASet")
                println("documentBSet:$documentBSet")
                val removedASet = originalSet - documentASet
                val addedASet = documentASet - originalSet
                val removedBSet = originalSet - documentBSet
                val addedBSet = documentBSet - originalSet
                println("removedASet:$removedASet")
                println("addedASet:$addedASet")
                println("removedBSet:$removedBSet")
                println("addedBSet:$addedBSet")
                val final = (originalSet + addedASet + addedBSet - removedASet - removedBSet).toSet()
                println("final:$final")

                assertThat(appliedDocumentAB.source.values()).containsExactlyInAnyOrderElementsOf(final)
                assertThat(appliedDocumentBA.source.values()).containsExactlyInAnyOrderElementsOf(final)
            },
            { if (it.isEmpty) """{"a":[]}""".toJsonNode() else it }
        )
    }

    fun JsonNode.values() =
        if (this.isEmpty || this.at("/a").isMissingNode) emptySet() else (this["a"] as ArrayNode)
            .toSet()

    private fun verifyOperations(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode,
        verifier: (appliedDocumentAB: Document, appliedDocumentBA: Document) -> Unit,
        converter: (jsonNode: JsonNode) -> JsonNode
    ) {
        val originalDocument = Document(original)
        val deltaA: Delta = originalDocument.generatePatch(documentA)
        val deltaB: Delta = originalDocument.generatePatch(documentB)
        val diffA: JsonNode = JsonDiff.asJson(original, documentA, Document.DIFF_FLAGS)
        val diffB: JsonNode = JsonDiff.asJson(original, documentB, Document.DIFF_FLAGS)

        println("Inputs")
        println("original = $original")
        println("A = $documentA => $deltaA")
        println("B = $documentB => $deltaB")
        assertThat(deltaA.toJsonNode()).isEqualTo(diffA.toString().toJsonNode())
        assertThat(deltaB.toJsonNode()).isEqualTo(diffB.toString().toJsonNode())

        println("Outputs\n")

        val appliedDocumentA = originalDocument.transform(deltaA)
        println("appliedDocumentA = $appliedDocumentA => ${appliedDocumentA.appliedDeltas}\n")
        val appliedDocumentB = originalDocument.transform(deltaB)
        println("appliedDocumentB = $appliedDocumentB => ${appliedDocumentB.appliedDeltas}\n")

        val appliedDocumentAB = appliedDocumentA.transform(deltaB)
        println("appliedDocumentAB = $appliedDocumentAB => ${appliedDocumentAB.appliedDeltas}\n")
        val appliedDocumentBA = appliedDocumentB.transform(deltaA)
        println("appliedDocumentBA = $appliedDocumentBA => ${appliedDocumentBA.appliedDeltas}\n")

        val objectNode = JsonNodeFactory.instance.objectNode()
        objectNode.set<JsonNode>("original", original)
        objectNode.set<JsonNode>("documentA", documentA)
        objectNode.set<JsonNode>("documentB", documentB)
        objectNode.set<JsonNode>("documentAB", appliedDocumentAB.source)
        objectNode.set<JsonNode>("documentBA", appliedDocumentBA.source)
//        file.appendText("$objectNode\n")

        if (documentA.toString() == documentB.toString()) {
            assertThat(appliedDocumentAB.appliedDeltas[1].operations).isEqualTo(appliedDocumentBA.appliedDeltas[1].operations)
            assertThat(appliedDocumentAB.source.toString()).isEqualTo(appliedDocumentBA.source.toString())
        }
        verifier(appliedDocumentAB, appliedDocumentBA)
    }

}
