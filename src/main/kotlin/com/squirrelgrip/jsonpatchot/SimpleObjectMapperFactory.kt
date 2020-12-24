package com.squirrelgrip.jsonpatchot

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.squirrelgrip.extension.json.Json
import com.github.squirrelgrip.extension.json.ObjectMapperFactory

class SimpleObjectMapperFactory(): ObjectMapperFactory {
    override fun getObjectMapper(): ObjectMapper {
        return super.getObjectMapper()
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
    }

}