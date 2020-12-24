package com.squirrelgrip.jsonpatchot.model

import com.squirrelgrip.jsonpatchot.model.operation.AddOperation
import com.squirrelgrip.jsonpatchot.model.operation.RemoveOperation
import com.squirrelgrip.jsonpatchot.model.operation.ReplaceOperation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class DeltaTest {

    @Test
    fun removeDuplicatesGivenDifferentOperations() {
        val operations = listOf(AddOperation("/a/0", "1"), RemoveOperation("/a/0", "1"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(RemoveOperation("/a/0", "1"))
    }

    @Test
    fun removeDuplicatesGivenOpposingScalarOperations() {
        val operations = listOf(AddOperation("/a", "1"), RemoveOperation("/a", "1"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(AddOperation("/a", "1"), RemoveOperation("/a", "1"))
    }

    @Test
    fun removeDuplicatesGivenOposingScalarOperationsDifferentOrder() {
        val operations = listOf(RemoveOperation("/a", "1"), AddOperation("/a", "1"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(RemoveOperation("/a", "1"), AddOperation("/a", "1"))
    }

    @Test
    fun removeDuplicatesGivenRelatedScalarOperations() {
        val operations = listOf(AddOperation("/a", "1"), ReplaceOperation("/a", "2", "1"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(AddOperation("/a", "1"), ReplaceOperation("/a", "2", "1"))
    }

    @Test
    fun removeDuplicatesGivenSameScalarOperations() {
        val operations = listOf(AddOperation("/a", "1"), AddOperation("/a", "2"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(AddOperation("/a", "1"), AddOperation("/a", "2"))
    }

    @Test
    fun removeDuplicatesGivenDifferentOperationsInDifferentOrder() {
        val operations = listOf(RemoveOperation("/a/0", "1"), AddOperation("/a/0", "1"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(AddOperation("/a/0", "1"))
    }

    @Test
    fun removeDuplicatesGivenDifferentOperationsDifferentValue() {
        val operations = listOf(AddOperation("/a/0", "1"), RemoveOperation("/a/0", "2"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(AddOperation("/a/0", "1"), RemoveOperation("/a/0", "2"))
    }

    @Test
    fun removeDuplicatesGivenSameAddOperation() {
        val operations = listOf(AddOperation("/a/0", "1"), AddOperation("/a/0", "1"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(AddOperation("/a/0", "1"))
    }

    @Test
    fun removeDuplicatesGivenSameAddOperationDifferentValue() {
        val operations = listOf(AddOperation("/a/0", "1"), AddOperation("/a/0", "2"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(AddOperation("/a/0", "1"), AddOperation("/a/0", "2"))
    }
    @Test
    fun removeDuplicatesGivenSameRemvoeOperation() {
        val operations = listOf(RemoveOperation("/a/0", "1"), RemoveOperation("/a/0", "1"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(RemoveOperation("/a/0", "1"))
    }

    @Test
    fun removeDuplicatesGivenSameRemvoeOperationDifferentValue() {
        val operations = listOf(RemoveOperation("/a/0", "1"), RemoveOperation("/a/0", "2"))
        assertThat(Delta.removeDuplicates(operations)).containsExactly(RemoveOperation("/a/0", "1"), RemoveOperation("/a/0", "2"))
    }
}