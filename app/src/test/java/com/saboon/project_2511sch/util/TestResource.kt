package com.saboon.project_2511sch.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TestResource {

    @Test
    fun `mapData transforms the data of Success`() {
        // WHEN
        val result = Resource.Success(listOf(1, 2, 3)).mapData { it.size }

        // THEN
        assertThat(result).isInstanceOf(Resource.Success::class.java)
        assertThat(result.data).isEqualTo(3)
    }

    @Test
    fun `mapData keeps the message of Error and does not run the transform`() {
        // GIVEN
        var called = false

        // WHEN
        val result = Resource.Error<List<Int>>("Database Error").mapData { called = true; it.size }

        // THEN
        assertThat(result).isInstanceOf(Resource.Error::class.java)
        assertThat(result.message).isEqualTo("Database Error")
        assertThat(called).isFalse()
    }

    @Test
    fun `mapData keeps Loading and Idle`() {
        assertThat(Resource.Loading<Int>().mapData { it + 1 }).isInstanceOf(Resource.Loading::class.java)
        assertThat(Resource.Idle<Int>().mapData { it + 1 }).isInstanceOf(Resource.Idle::class.java)
    }

    @Test
    fun `mapData passes a null value of a nullable Success through`() {
        // WHEN
        val result = Resource.Success<String?>(null).mapData { it ?: "none" }

        // THEN
        assertThat(result.data).isEqualTo("none")
    }
}
