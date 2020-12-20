package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.flipkart.zjsonpatch.DiffFlags
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.AddOperation
import com.squirrelgrip.jsonpatchot.model.operation.RemoveOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class Document(
    val source: JsonNode,
    val version: Int,
    val appliedDeltas: List<Delta>
) {
    constructor(source: JsonNode) : this(source, 0, emptyList())

    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Document::class.java)

        val DIFF_FLAGS = EnumSet.of(
            DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE,
            DiffFlags.REMOVE_REMAINING_FROM_END,
            DiffFlags.ARRAY_ELEMENT_AS_OBJECT,
            DiffFlags.ADD_ARRAY_ELEMENTS,
            DiffFlags.OMIT_MOVE_OPERATION,
            DiffFlags.OMIT_COPY_OPERATION
        )
    }

    fun transform(delta: Delta): Document {
        LOGGER.debug("original document: $source")
        val transformedDelta: Delta = delta.transform(this)
        val transformedSource = JsonPatch.apply(transformedDelta.toJsonNode(), source)
        return Document(
            transformedSource,
            transformedDelta.version + 1,
            listOf(*appliedDeltas.toTypedArray(), transformedDelta)
        )
    }

    fun generatePatch(target: JsonNode): Delta {
        val diff = JsonDiff.asJson(source, target, DIFF_FLAGS)
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
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Delta::class.java)
    }

    fun transform(document: Document): Delta {
        val appliedOperations = getAppliedOperations(document)
        LOGGER.debug("appliedOperations = $appliedOperations")
        LOGGER.debug("operations = $operations")
        val partitionedOperations = getTransformedOperations(appliedOperations).partition {
            it is RemoveOperation && document.source.at(it.path.path).isArray
        }
        LOGGER.debug("partitionedOperations.first = ${partitionedOperations.first}")
        LOGGER.debug("partitionedOperations.second = ${partitionedOperations.second}")

        val removeOperations = partitionedOperations.first.flatMap {
            val removeOperations = mutableListOf<Operation>()
            var index = 0
            (document.source.at(it.path.path) as ArrayNode).forEach { arrayElement ->
                removeOperations.add(RemoveOperation(JsonPath("${it.path.path}/${index++}"), arrayElement))
            }
            removeOperations.reversed()
        }
        LOGGER.debug("removeOperations = $removeOperations")

        val filteredOperations = partitionedOperations.second.filter {
            !(it is AddOperation && it.value.isArray && it.value.isEmpty && !document.source.at(it.path.path).isMissingNode)
        }

        val addOperations = filteredOperations.map {
            it.path
        }.filter {
            it.parent != null && document.source.at(it.parent!!.path).isMissingNode
        }.distinct().flatMap {
            generateFillerOperations(it)
        }
        LOGGER.debug("addOperations = $addOperations")

        return Delta(document.version, listOf(removeOperations, addOperations, filteredOperations).flatten())
    }

    private fun generateFillerOperations(jsonPath: JsonPath): List<Operation> {
        return jsonPath.names.filter {
            it.parent != null
        }.map {
            if (it.isArrayElement) {
                AddOperation(it.parent!!, "[]".toJsonNode())
            } else {
                AddOperation(it.parent!!, "{}".toJsonNode())
            }
        }
    }

    private fun getTransformedOperations(appliedOperations: List<Operation>): List<Operation> {
        return getTransformedOperations(appliedOperations, operations)
    }

    private fun getAppliedOperations(document: Document): List<Operation> {
        return document.appliedDeltas.filter {
            it.version >= version
        }.flatMap {
            it.operations
        }
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

fun getTransformedOperations(
    appliedOperations: List<Operation>,
    candidateOperations: List<Operation>
): List<Operation> {
    return if (appliedOperations.isEmpty()) {
        candidateOperations
    } else {
        var transformedOperations = candidateOperations
        appliedOperations.forEach {
            transformedOperations = it.transform(transformedOperations)
        }
        transformedOperations
    }
}

