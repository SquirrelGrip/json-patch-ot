package com.squirrelgrip.jsonpatchot.model

enum class OperationType(val value: String) {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test")
}
