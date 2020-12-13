package com.squirrelgrip.jsonpatchot.model

import com.fasterxml.jackson.databind.JsonNode
import com.github.squirrelgrip.extension.json.toJsonNode
import com.squirrelgrip.jsonpatchot.model.operation.*

abstract class Operation(
    val path: JsonPath
) {
    constructor(path: String) : this(JsonPath(path))

    companion object {
        fun create(jsonNode: JsonNode): Operation {
            return when (jsonNode["op"].asText()) {
                "add" -> AddOperation(jsonNode["path"].asText(), jsonNode["value"])
                "test" -> TestOperation(jsonNode["path"].asText(), jsonNode["value"])
                "remove" -> RemoveOperation(jsonNode["path"].asText())
                "replace" -> ReplaceOperation(jsonNode["path"].asText(), jsonNode["value"])
                "move" -> MoveOperation(jsonNode["path"].asText(), jsonNode["from"].asText())
                "copy" -> CopyOperation(jsonNode["path"].asText(), jsonNode["from"].asText())
                else -> throw InvalidOperationException()
            }
        }
    }

    abstract val operation: OperationType

    open fun transform(operations: List<Operation>): List<Operation> {
        return operations
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Operation
        if (path != other.path) return false
        if (operation != other.operation) return false
        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + operation.hashCode()
        return result
    }

    abstract fun updatePath(updatedPath: JsonPath): Operation

    fun shiftIndices(
        proposedOperations: List<Operation>,
        isAdd: Boolean = false
    ): List<Operation> {
        return if (path.isArrayElement) {
            proposedOperations.map {
                // TODO Deal with `from` in FromOperation
                if (it.path.hasSameArrayPath(path)) {
                    it.updatePath(it.path.replacePathIndices(path, isAdd))
                } else {
                    it
                }
            }
        } else {
            proposedOperations
        }
    }

}

abstract class FromOperation(
    path: JsonPath,
    val from: JsonPath
) : Operation(path) {
    override fun toString(): String {
        return """{"op":"${operation.value}","path":"$path","from":"$from"}"""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        other as FromOperation
        if (from != other.from) return false
        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + from.hashCode()
        return result
    }
}

abstract class ValueOperation(
    path: JsonPath,
    val value: JsonNode
) : Operation(path) {
    override fun toString(): String {
        return """{"op":"${operation.value}","path":"$path","value":$value}"""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false
        return value == (other as ValueOperation).value
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + value.hashCode()
        return result
    }
}

class JsonPath(
    val path: String
) {

    val names: List<JsonPath> by lazy {
        var s = ""
        path.substring(1).split('/').map {
            s = "$s/$it"
            JsonPath(s)
        }
    }

    val lastSlashIndex: Int by lazy {
        path.lastIndexOf('/')
    }

    val name: String = path.substring(lastSlashIndex + 1)
    val arrayIndex: Int
        get() = if (isArrayElement) path.substring(lastSlashIndex + 1).toInt() else -1
    val arrayPath: String
        get() = if (isArrayElement) path.substring(0, lastSlashIndex + 1) else ""
    val isArrayElement: Boolean
        get() = name.isNumber()
    val parent: JsonPath?
        get() = if (lastSlashIndex >= 0 -1) JsonPath(path.substring(0, lastSlashIndex)) else null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return path == (other as JsonPath).path
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }

    fun hasSameArrayPath(path: JsonPath): Boolean {
        return isArrayElement && path.isArrayElement && arrayPath.indexOf(path.arrayPath) != 0
    }

    fun replacePathIndices(acceptedPath: JsonPath, increment: Boolean = false): JsonPath {
        val oldIndex = arrayIndex
        val index = acceptedPath.arrayIndex
        // For increment we need to match equal to as well since that element will be bumped forward.
        val isOldBigger = if (increment) oldIndex >= index else oldIndex > index
        return if (isOldBigger) {
            JsonPath("$arrayPath/${oldIndex + (if (increment) 1 else -1)}")
        } else {
            this
        }
    }

    fun intersects(other: JsonPath): Boolean {
        return this.path.indexOf(other.path) == 0
    }

    override fun toString(): String {
        return path
    }
}

fun pathsUpTo(path: JsonPath): List<Operation> {
    val operations = mutableListOf<Operation>()
    var s: JsonPath = path.names.first()
    path.names.forEach {
        if (s != it) {
            if (it.isArrayElement) {
                operations.add(AddOperation(s, "[]".toJsonNode()))
            } else {
                operations.add(AddOperation(s, "{}".toJsonNode()))
            }
        }
        s = it
    }
    return operations.toList()
}

fun String.isNumber(): Boolean {
    return this.matches(Regex("\\d+"))
}
