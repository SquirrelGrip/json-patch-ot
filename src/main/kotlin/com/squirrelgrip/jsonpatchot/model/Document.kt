package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.flipkart.zjsonpatch.DiffFlags
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
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
            DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE_ON_ARRAY_ELEMENTS_ONLY,
            DiffFlags.REMOVE_ARRAY_ELEMENTS_FROM_END,
            DiffFlags.ARRAY_ELEMENT_AS_OBJECT,
            DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE,
            DiffFlags.ADD_ARRAY_ELEMENTS,
//            DiffFlags.OMIT_MOVE_OPERATION,
//            DiffFlags.OMIT_COPY_OPERATION
        )
    }

    fun transform(delta: Delta): Document {
        LOGGER.debug("original document: $source")
        val transformedDelta: Delta = delta.transform(this)
        return Document(
            JsonPatch.apply(transformedDelta.toJsonNode(), source),
            transformedDelta.version + 1,
            listOf(*appliedDeltas.toTypedArray(), transformedDelta)
        )
    }

    fun reverse(): Document {
        val lastDelta = appliedDeltas.last().reversed()
        return Document(
            JsonPatch.apply(lastDelta.toJsonNode(), source),
            lastDelta.version,
            appliedDeltas.subList(0, appliedDeltas.size - 1)
        )
    }

//    fun transform(delta: Delta): Document {
//        LOGGER.debug("original document: $source")
//        val transformedDelta: Delta = delta.transform(this)
//        val transformedSource = JsonPatch.apply(transformedDelta.toJsonNode(), source)
//        return Document(
//            transformedSource,
//            transformedDelta.version + 1,
//            listOf(*appliedDeltas.toTypedArray(), transformedDelta)
//        )
//    }

    fun generatePatch(target: JsonNode): Delta {
        val diff = JsonDiff.asJson(source, target, DIFF_FLAGS)
        return Delta(version, diff.convert())
    }

    override fun toString(): String {
        return source.toString()
    }

}

