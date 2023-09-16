package com.thn.videoconstruction.models

class LookupModel(private val mLookup: Lookup) {
    val name
        get() = mLookup.name

    val lookupType
        get() = mLookup.lookupType

    val lock
        get() = mLookup.lock
}