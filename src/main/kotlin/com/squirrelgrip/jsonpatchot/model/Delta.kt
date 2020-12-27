package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.AddOperation
import com.squirrelgrip.jsonpatchot.model.operation.RemoveOperation
import com.squirrelgrip.jsonpatchot.model.operation.ReplaceOperation
import org.slf4j.LoggerFactory

class Delta(
    val version: Int,
    val operations: List<Operation>
) {
    companion object {
        val LOGGER = LoggerFactory.getLogger(Delta::class.java)

        fun getFillerOperations(
            candidateOperations: List<Operation>,
            document: Document
        ): List<Operation> {
            return candidateOperations.map {
                it.path
            }.filter {
                it.head() != JsonPointer.empty() && document.source.at(it.head()).isMissingNode
            }.distinct().flatMap {
                getFillerOperations(it, document.source)
            }
        }

        fun getFillerOperations(
            path: JsonPointer,
            source: JsonNode
        ): List<Operation> {
            val head = path.head()
            if (head != JsonPointer.empty() && source.at(head).isMissingNode) {
                val fillerOperation = if (path.last().mayMatchElement()) {
                    AddOperation(head, "[]".toJsonNode())
                } else {
                    AddOperation(head, "{}".toJsonNode())
                }
                return listOf(*getFillerOperations(head, source).toTypedArray(), fillerOperation)
            }
            return emptyList()
        }
    }

    fun transform(document: Document): Delta {
        LOGGER.debug("original: ${document.source}")
        LOGGER.debug("operations: $operations")
        val transformedOperations = getTransformedOperations(document).apply {
            LOGGER.debug("transformedOperations: $this")
        }
        val fillerOperations = getFillerOperations(transformedOperations, document)
        return Delta(document.version, listOf(fillerOperations, transformedOperations).flatten())
    }

    private fun getTransformedOperations(document: Document): List<Operation> {
        val appliedOperations = getAppliedOperations(document).apply {
            LOGGER.debug("appliedOperations: $this")
        }
        var transformedOperations = operations.prepare(document.source)
        appliedOperations.forEach {
            transformedOperations = it.transform(transformedOperations)
        }
        return transformedOperations
    }

    fun getAppliedOperations(document: Document): List<Operation> {
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

fun List<Operation>.prepare(source: JsonNode): List<Operation> {
    val candidatePartition = this.partition {
        it.isArrayOperation()
    }
    val arrayOperations = candidatePartition.first.groupBy {
        if (it.path.last().mayMatchElement()) {
            it.path.head()
        } else {
            it.path
        }
    }.map {
        val original = source.at(it.key)
        val originalSet = if (original.isEmpty || !original.isArray) {
            mutableSetOf()
        } else {
            (original as ArrayNode).toSet()
        }
        println("groupPath:${it.key}")
        println("groupOriginal:$originalSet")
        println("groupOperations:${it.value}")
        val removeValues = mutableListOf<Any>()
        val addValues = mutableListOf<Any>()
        it.value.forEach { operation ->
            when (operation) {
                is AddOperation -> {
                    val values = operation.values()
                    addValues.addAll(values)
                }
                is RemoveOperation -> {
                    val values = operation.values()
                    removeValues.addAll(values)
                }
                is ReplaceOperation -> {
                    val values = operation.values()
                    val fromValues = operation.fromValues()
                    addValues.addAll(values - fromValues)
                    removeValues.addAll(fromValues - values)
                }
            }
        }
        println("groupRemoveValues:$removeValues")
        println("groupAddValues:$addValues")
        val intersectValues = addValues.intersect(removeValues)
        println("groupIntersectValues:$intersectValues")
        val final = originalSet + (addValues - intersectValues) - (removeValues - intersectValues)
        println("final:$final")
        if (original.isMissingNode) {
            AddOperation(it.key.toString(), final)
        } else {
            ReplaceOperation(it.key.toString(), final, originalSet)
        }.apply {
            println(this)
        }
    }
    return listOf(candidatePartition.second, arrayOperations).flatten()
}

fun ValueOperation.values(): List<Any> {
    return if (value.isArray) {
        (value as ArrayNode).toList()
    } else {
        listOf(value)
    }
}

fun ReplaceOperation.fromValues(): List<Any> {
    return if (fromValue.isArray) {
        (fromValue as ArrayNode).toList()
    } else {
        listOf(fromValue)
    }
}
