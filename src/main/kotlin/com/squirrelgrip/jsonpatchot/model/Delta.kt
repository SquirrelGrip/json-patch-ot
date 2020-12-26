package com.squirrelgrip.jsonpatchot.model

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

    private fun getFillerOperations(
        candidateOperations: List<Operation>,
        document: Document
    ): List<Operation> {
        return candidateOperations.map {
            it.path
        }.filter {
            it.parent != null && document.source.at(it.parent!!.path).isMissingNode
        }.distinct().flatMap {
            pathsUpTo(it)
        }
    }

    private fun getTransformedOperations(document: Document): List<Operation> {
        val appliedOperations = getAppliedOperations(document).apply {
            LOGGER.debug("appliedOperations: $this")
        }
        val originalDocument = document.getOriginalSource(this)
        var transformedOperations = operations.prepare(originalDocument, appliedOperations)
        appliedOperations.forEach {
            transformedOperations = it.transform(transformedOperations)
        }
        return transformedOperations
    }

    fun getAppliedOperations(document: Document): List<Operation> {
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

fun List<Operation>.prepare(source: JsonNode, appliedOperations: List<Operation>): List<Operation> {
    val candidatePartition = this.partition {
        it.isArrayOperation()
    }
    val appliedOperationsGroupedByPath = appliedOperations.filter {
        it.isArrayOperation()
    }.groupBy {
        if (it.path.isArrayElement) {
            it.path.parent!!
        } else {
            it.path
        }
    }
    val arrayOperations = candidatePartition.first.groupBy {
        if (it.path.isArrayElement) {
            it.path.parent!!
        } else {
            it.path
        }
    }.map {
        val original = source.at(it.key.path)
        val originalSet = if (original.isEmpty || !original.isArray) {
            mutableSetOf()
        } else {
            (original as ArrayNode).map { it.asInt() }.toSet()
        }
        val appliedOperationsForPath = appliedOperationsGroupedByPath.getOrDefault(it.key, emptyList())
        println("groupPath:${it.key.path}")
        println("groupOriginal:$originalSet")
        println("groupAppliedOperations:${appliedOperationsForPath}")
        val removeValues1 = mutableListOf<Any>()
        val addValues1 = mutableListOf<Any>()
        appliedOperationsForPath.forEach { operation ->
            when (operation) {
                is AddOperation -> {
                    val values = operation.values()
                    addValues1.addAll(values)
                    removeValues1.removeAll(values)
                }
                is RemoveOperation -> {
                    val values = operation.values()
                    addValues1.removeAll(values)
                    removeValues1.addAll(values)
                }
                is ReplaceOperation -> {
                    val values = operation.values()
                    val fromValues = operation.fromValues()
                    addValues1.addAll(values - fromValues)
                    removeValues1.addAll(fromValues - values)
                }
            }
        }
        println("removeValues1:$removeValues1")
        println("addValues1:$addValues1")
        println("groupOperations:${it.value}")
        val removeValues2 = mutableListOf<Any>()
        val addValues2 = mutableListOf<Any>()
        it.value.forEach { operation ->
            when (operation) {
                is AddOperation -> {
                    val values = operation.values()
                    addValues2.addAll(values)
                }
                is RemoveOperation -> {
                    val values = operation.values()
                    removeValues2.addAll(values)
                }
                is ReplaceOperation -> {
                    val values = operation.values()
                    val fromValues = operation.fromValues()
                    addValues2.addAll(values - fromValues)
                    removeValues2.addAll(fromValues - values)
                }
            }
        }
        println("removeValues2:$removeValues2")
        println("addValues2:$addValues2")
        val intersect1 = addValues1.intersect(removeValues1)
        val intersect2 = addValues2.intersect(removeValues2)
        val final = originalSet + (addValues1 - intersect1) + (addValues2 - intersect2) - (removeValues1 - intersect1) - (removeValues2 - intersect2)
        println("final:$final")
        if (original.isMissingNode) {
            AddOperation(it.key.path, final)
        } else {
            ReplaceOperation(it.key.path, final, originalSet)
        }.apply {
            println(this)
        }
    }
    return listOf(candidatePartition.second, arrayOperations).flatten()
}

fun ValueOperation.values(): List<Any> {
    return if (value.isArray) {
        (value as ArrayNode).map { it.asInt() }
    } else {
        listOf(value.asInt())
    }
}

fun ReplaceOperation.fromValues(): List<Any> {
    return if (fromValue.isArray) {
        (fromValue as ArrayNode).map { it.asInt() }
    } else {
        listOf(fromValue.asInt())
    }
}
