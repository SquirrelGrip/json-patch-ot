package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.flipkart.zjsonpatch.JsonDiff
import com.github.squirrelgrip.extension.json.toJsonNode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class DocumentTest {

    companion object {

        @JvmStatic
        fun multipleScalars(): Stream<Arguments> {
            return generateArguments(
                """{}""",
                """{"a":1}""",
                """{"a":2}""",
                """{"a":3}""",
                """{"b":1}""",
                """{"b":2}""",
                """{"b":3}""",
                """{"a":1,"b":1}""",
                """{"a":2,"b":2}""",
                """{"a":3,"b":3}""",
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
                """{"a":[5,4,3,2,1]}""",
                """{"a":[1,2,3,4,5]}""",
                """{"a":[5,1,4,2,3]}""",
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

    @ParameterizedTest
    @MethodSource("multipleScalars")
    fun multipleScalars(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode
    ) {
        verifyOperations(
            original,
            documentA,
            documentB
        ) { documentAB, documentBA ->
            val originalValueA = original["a"]
            val originalValueB = original["b"]
            val documentAvalueA = documentA["a"]
            val documentAvalueB = documentA["b"]
            val documentBvalueA = documentB["a"]
            val documentBvalueB = documentB["b"]
            val documentABvalueA = documentAB.source["a"]
            val documentABvalueB = documentAB.source["b"]
            val documentBAvalueA = documentBA.source["a"]
            val documentBAvalueB = documentBA.source["b"]

            verifyScalarValues(originalValueA, documentAvalueA, documentBvalueA, documentABvalueA)
            verifyScalarValues(originalValueA, documentBvalueA, documentAvalueA, documentBAvalueA)
            verifyScalarValues(originalValueB, documentAvalueB, documentBvalueB, documentABvalueB)
            verifyScalarValues(originalValueB, documentBvalueB, documentAvalueB, documentBAvalueB)
        }

    }

    private fun verifyScalarValues(
        originalValue: JsonNode?,
        valueA: JsonNode?,
        valueB: JsonNode?,
        finalValue: JsonNode?
    ) {
        if (originalValue == null) {
            if (valueB != null) {
                assertThat(finalValue).isEqualTo(valueB)
            } else if (valueA != null) {
                assertThat(finalValue).isEqualTo(valueA)
            } else {
                assertThat(finalValue).isNull()
            }
        } else {
            if (valueB != originalValue) {
                assertThat(finalValue).isEqualTo(valueB)
            } else if (valueA != originalValue) {
                assertThat(finalValue).isEqualTo(valueA)
            } else {
                assertThat(finalValue).isEqualTo(originalValue)
            }
        }
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
            documentB
        ) { appliedDocumentAB, appliedDocumentBA ->
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
        }
    }

    fun JsonNode.values() =
        if (this.isEmpty || this.at("/a").isMissingNode) emptySet() else (this["a"] as ArrayNode)
            .toSet()

    private fun verifyOperations(
        original: JsonNode,
        documentA: JsonNode,
        documentB: JsonNode,
        verifier: (appliedDocumentAB: Document, appliedDocumentBA: Document) -> Unit
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
        assertThat(deltaA.toJsonNode()).isEqualTo(diffA)
        assertThat(deltaB.toJsonNode()).isEqualTo(diffB)

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

        if (documentA.toString() == documentB.toString()) {
            assertThat(appliedDocumentAB.appliedDeltas[1].operations).isEqualTo(appliedDocumentBA.appliedDeltas[1].operations)
            assertThat(appliedDocumentAB.source.toString()).isEqualTo(appliedDocumentBA.source.toString())
        }
        verifier(appliedDocumentAB, appliedDocumentBA)
    }

}