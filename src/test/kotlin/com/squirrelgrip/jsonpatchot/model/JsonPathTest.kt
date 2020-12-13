package com.squirrelgrip.jsonpatchot.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JsonPathTest {
    @Test
    fun `split`() {
        assertThat(JsonPath("/a").names).containsExactly(JsonPath("/a"))
        assertThat(JsonPath("/a/0").names).containsExactly(JsonPath("/a"), JsonPath("/a/0"))
        assertThat(JsonPath("/a/0/0").names).containsExactly(JsonPath("/a"), JsonPath("/a/0"), JsonPath("/a/0/0"))
    }
}