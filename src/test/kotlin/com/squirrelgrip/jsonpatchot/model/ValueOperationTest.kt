package com.squirrelgrip.jsonpatchot.model

import com.squirrelgrip.jsonpatchot.model.operation.AddOperation
import com.squirrelgrip.jsonpatchot.model.operation.RemoveOperation
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ValueOperationTest {

    @BeforeEach
    fun setUp() {
    }

    @Test
    fun `AddOperation given scalar reference with same value`() {
        val appliedOperation = AddOperation("/path", 1)
        val candidateOperation = AddOperation("/path", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation given scalar reference with different value`() {
        val appliedOperation = AddOperation("/path", 1)
        val candidateOperation = AddOperation("/path", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation given scalar reference with different path and same value`() {
        val appliedOperation = AddOperation("/path", 1)
        val candidateOperation = AddOperation("/another", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation given scalar reference with different path and different value`() {
        val appliedOperation = AddOperation("/path", 1)
        val candidateOperation = AddOperation("/another", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation given array reference with same value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = AddOperation("/path/0", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation given array reference with different value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = AddOperation("/path/0", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation given array reference with different path and same value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = AddOperation("/path/1", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isFalse()
    }

    @Test
    fun `AddOperation given array reference with different path and different value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = AddOperation("/path/1", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation given scalar reference with same value`() {
        val appliedOperation = RemoveOperation("/path", 1)
        val candidateOperation = RemoveOperation("/path", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation given scalar reference with different value`() {
        val appliedOperation = RemoveOperation("/path", 1)
        val candidateOperation = RemoveOperation("/path", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation given scalar reference with different path and same value`() {
        val appliedOperation = RemoveOperation("/path", 1)
        val candidateOperation = RemoveOperation("/another", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation given scalar reference with different path and different value`() {
        val appliedOperation = RemoveOperation("/path", 1)
        val candidateOperation = RemoveOperation("/another", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation given array reference with same value`() {
        val appliedOperation = RemoveOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/0", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation given array reference with different value`() {
        val appliedOperation = RemoveOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/0", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation given array reference with different path and same value`() {
        val appliedOperation = RemoveOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/1", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation given array reference with different path and different value`() {
        val appliedOperation = RemoveOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/1", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }
    
    @Test
    fun `RemoveOperation and AddOperation given scalar reference with same value`() {
        val appliedOperation = RemoveOperation("/path", 1)
        val candidateOperation = AddOperation("/path", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation and AddOperation given scalar reference with different value`() {
        val appliedOperation = RemoveOperation("/path", 1)
        val candidateOperation = AddOperation("/path", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation and AddOperation given scalar reference with different path and same value`() {
        val appliedOperation = RemoveOperation("/path", 1)
        val candidateOperation = AddOperation("/another", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation and AddOperation given scalar reference with different path and different value`() {
        val appliedOperation = RemoveOperation("/path", 1)
        val candidateOperation = AddOperation("/another", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation and AddOperation given array reference with same value`() {
        val appliedOperation = RemoveOperation("/path/0", 1)
        val candidateOperation = AddOperation("/path/0", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation and AddOperation given array reference with different value`() {
        val appliedOperation = RemoveOperation("/path/0", 1)
        val candidateOperation = AddOperation("/path/0", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation and AddOperation given array reference with different path and same value`() {
        val appliedOperation = RemoveOperation("/path/0", 1)
        val candidateOperation = AddOperation("/path/1", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `RemoveOperation and AddOperation given array reference with different path and different value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/1", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation and RemoveOperation given scalar reference with same value`() {
        val appliedOperation = AddOperation("/path", 1)
        val candidateOperation = RemoveOperation("/path", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation and RemoveOperation given scalar reference with different value`() {
        val appliedOperation = AddOperation("/path", 1)
        val candidateOperation = RemoveOperation("/path", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation and RemoveOperation given scalar reference with different path and same value`() {
        val appliedOperation = AddOperation("/path", 1)
        val candidateOperation = RemoveOperation("/another", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation and RemoveOperation given scalar reference with different path and different value`() {
        val appliedOperation = AddOperation("/path", 1)
        val candidateOperation = RemoveOperation("/another", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation and RemoveOperation given array reference with same value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/0", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isFalse()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isFalse()
    }

    @Test
    fun `AddOperation and RemoveOperation given array reference with different value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/0", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).isEmpty()
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation and RemoveOperation given array reference with different path and same value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/1", 1)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }

    @Test
    fun `AddOperation and RemoveOperation given array reference with different path and different value`() {
        val appliedOperation = AddOperation("/path/0", 1)
        val candidateOperation = RemoveOperation("/path/1", 2)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, true, false)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, true)).contains(candidateOperation)
        assertThat(removeOperations(appliedOperation, candidateOperation, false, false)).contains(candidateOperation)
        assertThat(appliedOperation.keepOperation(candidateOperation, true, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, true, false)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, true)).isTrue()
        assertThat(appliedOperation.keepOperation(candidateOperation, false, false)).isTrue()
    }
}

fun removeOperations(acceptedOp: Operation,
                     candidateOperation: Operation,
                     acceptedWinsOnEqualPath: Boolean,
                     skipWhitelist: Boolean = false
): List<Operation> {
    val proposedOps: MutableList<Operation> = mutableListOf(candidateOperation)
    removeOperations(acceptedOp, proposedOps, acceptedWinsOnEqualPath, skipWhitelist)
    return proposedOps.toList()
}

