package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.flipkart.zjsonpatch.DiffFlags
import com.flipkart.zjsonpatch.JsonDiff
import com.flipkart.zjsonpatch.JsonPatch
import com.github.squirrelgrip.extension.json.toJsonNode
import org.slf4j.LoggerFactory
import java.util.*

class Document(
    val source: JsonNode,
    val version: Int,
    val appliedDeltas: List<Delta>
) {

    companion object {
        val LOGGER = LoggerFactory.getLogger(Document::class.java)

        val DIFF_FLAGS: EnumSet<DiffFlags> = EnumSet.of(
            DiffFlags.REMOVE_REMAINING_FROM_END,
            DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE,
        )
    }

    constructor(source: JsonNode) : this(source, 0, emptyList())

    fun transform(delta: Delta): Document {
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

fun JsonNode.convert(): List<Operation> =
    if (this is ArrayNode) {
        this.map {
            Operation.create(it)
        }.toList()
    } else {
        listOf(Operation.create(this))
    }

