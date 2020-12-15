package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.flipkart.zjsonpatch.DiffFlags
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.RemoveOperation
import java.util.*

class Document(
    val source: JsonNode,
    val version: Int,
    val appliedDeltas: List<Delta>
) {
    constructor(source: JsonNode) : this(source, 0, emptyList())

    fun transform(delta: Delta): Document {
        val transformedDelta: Delta = delta.transform(this)
//        println()
//        println(transformedDelta)
//        println()
        val transformedSource = JsonPatch.apply(transformedDelta.toJsonNode(), source)
        return Document(
            transformedSource,
            transformedDelta.version + 1,
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
        val appliedOperations = getAppliedOperations(document)
        val candidateOperations = getTransformedOperations(appliedOperations).partition {
            it is RemoveOperation && document.source.at(it.path.path).isArray
        }

        val removeOperations = candidateOperations.first.flatMap {
            val removeOperations = mutableListOf<Operation>()
            var index = 0
            (document.source.at(it.path.path) as ArrayNode).forEach { arrayElement ->
                removeOperations.add(RemoveOperation(JsonPath("${it.path.path}/${index++}"), arrayElement))
            }
            removeOperations.add(0, RemoveOperation(it.path, "[]".toJsonNode()))
            removeOperations.reversed()
        }

        val addOperations = candidateOperations.second.map {
            it.path
        }.filter {
            it.parent != null && document.source.at(it.parent!!.path).isMissingNode
        }.distinct().flatMap {
            pathsUpTo(it)
        }

        return Delta(document.version, listOf(removeOperations, addOperations, candidateOperations.second).flatten())
    }

    private fun getTransformedOperations(appliedOperations: List<Operation>): List<Operation> {
        return if (appliedOperations.isEmpty()) {
            operations
        } else {
            var transformedOperations = operations
            appliedOperations.forEach {
                transformedOperations = it.transform(transformedOperations)
            }
            transformedOperations
        }
    }

    private fun getAppliedOperations(document: Document): List<Operation> {
        val appliedOperations = document.appliedDeltas.filter {
            it.version >= version
        }.flatMap {
            it.operations
        }
        return appliedOperations
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

