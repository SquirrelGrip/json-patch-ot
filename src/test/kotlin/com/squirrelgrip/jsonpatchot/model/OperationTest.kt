package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
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
        assertThat(pathsUpTo("/a")).isEmpty()
        assertThat(pathsUpTo("/a/0")).containsExactly(AddOperation("/a", "[]".toJsonNode()))
        assertThat(pathsUpTo("/a/0/a/0")).containsExactly(
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
        val diffA: JsonNode = JsonDiff.asJson(original, documentA)
        val diffB: JsonNode = JsonDiff.asJson(original, documentB)

        println("Inputs")
        println("original = $original")
        println("A = $documentA => $diffA => $deltaA")
        println("B = $documentB => $diffB => $deltaB")
        assertThat(deltaA.toJsonNode()).isEqualTo(diffA.toString().toJsonNode())
        assertThat(deltaB.toJsonNode()).isEqualTo(diffB.toString().toJsonNode())

        val appliedDocumentA = originalDocument.transform(deltaA)
        val appliedDocumentB = originalDocument.transform(deltaB)
        val appliedDocumentAB = appliedDocumentA.transform(deltaB)
        val appliedDocumentBA = appliedDocumentB.transform(deltaA)

        println("Outputs")
        println("appliedDocumentA = $appliedDocumentA")
        println("appliedDocumentB = $appliedDocumentB")
        println("appliedDocumentAB = $appliedDocumentAB")
        println("appliedDocumentBA = $appliedDocumentBA")

        var orderDoesNotMatter = false
        if (documentA.toString() == documentB.toString()) {
            assertThat(appliedDocumentAB.appliedDeltas[1].operations).isEmpty()
            assertThat(appliedDocumentBA.appliedDeltas[1].operations).isEmpty()
            assertThat(appliedDocumentAB.source.toString()).isEqualTo(appliedDocumentBA.source.toString())
            orderDoesNotMatter = true
        }
        if (!deltaA.operations.filter { it is RemoveOperation }.isEmpty()) {
            assertThat(appliedDocumentAB.source["a"]).isEqualTo(documentB["a"])
            assertThat(appliedDocumentBA.source.isEmpty || appliedDocumentBA.source["a"].isArray).isTrue()
        }
        if (!deltaB.operations.filter { it is RemoveOperation }.isEmpty()) {
            assertThat(appliedDocumentBA.source["a"]).isEqualTo(documentA["a"])
            assertThat(appliedDocumentAB.source.isEmpty || appliedDocumentAB.source["a"].isArray).isTrue()
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

class Document(
    val source: JsonNode,
    val version: Int,
    val appliedDeltas: List<Delta>
) {
    constructor(source: JsonNode) : this(source, 0, emptyList())
    constructor() : this("{}".toJsonNode())

    fun transform(delta: Delta): Document {
        val transformedDelta: Delta = delta.transform(this)
        val transformedSource = JsonPatch.apply(transformedDelta.toJsonNode(), source)
        return Document(
            transformedSource,
            transformedDelta.version,
            listOf(*appliedDeltas.toTypedArray(), transformedDelta)
        )
    }

    fun generatePatch(target: JsonNode): Delta {
        val diff = JsonDiff.asJson(source, target)
        return Delta(version, diff.convert())
    }

    override fun toString(): String {
        return source.toString()
    }

}

class Delta(
    val version: Int,
    val operations: List<Operation>
) {
    fun transform(document: Document): Delta {
        val fillerOperations = operations.map {
            it.path.path
        }.filter {
            document.source.at(it).isMissingNode
        }.distinct().flatMap {
            pathsUpTo(it)
        }
        val appliedOperations = document.appliedDeltas.flatMap {
            it.operations
        }
        val candidateOperations = if (appliedOperations.isEmpty()) {
            operations
        } else {
            appliedOperations.flatMap {
                it.transform(operations)
            }
        }
        return Delta(document.version, listOf(fillerOperations, candidateOperations).flatten())
    }

    fun toJsonNode(): JsonNode {
        val json = operations.map {
            it.toString()
        }.joinToString(",", "[", "]")
        return json.toJsonNode()
    }

    override fun toString(): String {
        return operations.toString()
    }

}

fun JsonNode.convert(): List<Operation> =
    if (this is ArrayNode) {
        this.map {
            Operation.create(it)
        }.toList()
    } else {
        listOf(Operation.create(this))
    }

