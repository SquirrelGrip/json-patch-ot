package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.flipkart.zjsonpatch.DiffFlags
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
import com.github.squirrelgrip.extension.json.toJsonNode
import java.util.*

class Document(
    val source: JsonNode,
    val version: Int,
    val appliedDeltas: List<Delta>
) {
    constructor(source: JsonNode) : this(source, 0, emptyList())

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
        val diff = JsonDiff.asJson(source, target, EnumSet.of(DiffFlags.REMOVE_REMAINING_FROM_END))
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
        val appliedOperations = document.appliedDeltas.flatMap {
            it.operations
        }
        val candidateOperations = if (appliedOperations.isEmpty()) {
            operations
        } else {
            var proposedOperation = operations
            appliedOperations.forEach {
                proposedOperation = it.transform(proposedOperation)
            }
            proposedOperation
        }

        val fillerOperations = candidateOperations.map {
            it.path
        }.filter {
            it.parent != null && document.source.at(it.parent!!.path).isMissingNode
        }.distinct().flatMap {
            pathsUpTo(it)
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

