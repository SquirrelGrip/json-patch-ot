package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.AddOperation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Delta(
    val version: Int,
    val operations: List<Operation>
) {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Delta::class.java)

        fun removeDuplicates(operations: List<Operation>): List<Operation> {
            LOGGER.debug("before removeDuplicates: $operations")
            val processedOperations = mutableListOf<Operation>()
            operations.asReversed().forEach {
                if (processedOperations.find { processed ->
                        if (it.path.isArrayElement) {
                            processed.path.isArrayElement && processed.path.parent == it.path.parent && (processed as ValueOperation).value == (it as ValueOperation).value
                        } else {
                            false
                        }
                    } == null) {
                    processedOperations.add(0, it)
                }
            }
            LOGGER.debug("after removeDuplicates: $processedOperations")
            return processedOperations.toList()
        }
    }


    fun transform(document: Document): Delta {
        val appliedOperations = getAppliedOperations(document)
        LOGGER.debug("appliedOperations = $appliedOperations")
        LOGGER.debug("operations = $operations")
        val filteredOperations = getTransformedOperations(appliedOperations).filter {
            it !is AddOperation || !(it.value.isArray && it.value.isEmpty && !document.source.at(it.path.path).isMissingNode)
        }

        val addOperations = getAddFiller(filteredOperations, document)

        return Delta(document.version, listOf(addOperations, filteredOperations).flatten())
    }

    private fun getAddFiller(
        filteredOperations: List<Operation>,
        document: Document
    ): List<Operation> {
        val addOperations = filteredOperations.map {
            it.path
        }.filter {
            it.parent != null && document.source.at(it.parent!!.path).isMissingNode
        }.distinct().flatMap {
            generateFillerOperations(it)
        }
        LOGGER.debug("addOperations = $addOperations")
        return addOperations
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
        return operations.patch()
    }

    override fun toString(): String {
        return operations.toString()
    }

    fun reversed(): Delta {
        return Delta(
            version,
            operations.map { it.reverse() }.reversed()
        )
    }

}

private fun List<Operation>.patch(): JsonNode {
    val json = this.map {
        it.toString()
    }.joinToString(",", "[", "]")
    return json.toJsonNode()
}
