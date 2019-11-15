/*
 * Copyright © 2019 The Android Password Authors. All Rights Reserved.
 * SPDX-License-Identifier: GPL-3.0-Only
 */
@file:JvmName("OpenPgpDecryptionResult")
package org.openintents.openpgp

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class OpenPgpDecryptionResult() : Parcelable {
    private var result = 0
    private var sessionKey: ByteArray? = null
    private var decryptedSessionKey: ByteArray? = null

    constructor(result: Int) : this() {
        this.result = result
        sessionKey = null
        decryptedSessionKey = null
    }

    constructor(
        result: Int,
        sessionKey: ByteArray?,
        decryptedSessionKey: ByteArray?
    ) : this() {
        this.result = result
        if (sessionKey == null != (decryptedSessionKey == null)) {
            throw AssertionError("sessionkey must be null iff decryptedSessionKey is null")
        }
        this.sessionKey = sessionKey
        this.decryptedSessionKey = decryptedSessionKey
    }

    fun getResult(): Int {
        return result
    }

    fun hasDecryptedSessionKey(): Boolean {
        return sessionKey != null
    }

    fun getSessionKey(): ByteArray? {
        return if (sessionKey == null) {
            null
        } else sessionKey!!.copyOf(sessionKey!!.size)
    }

    fun getDecryptedSessionKey(): ByteArray? {
        return if (sessionKey == null || decryptedSessionKey == null) {
            null
        } else decryptedSessionKey!!.copyOf(decryptedSessionKey!!.size)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        /**
         * NOTE: When adding fields in the process of updating this API, make sure to bump
         * [.PARCELABLE_VERSION].
         */
        dest.writeInt(PARCELABLE_VERSION)
        // Inject a placeholder that will store the parcel size from this point on
        // (not including the size itself).
        val sizePosition = dest.dataPosition()
        dest.writeInt(0)
        val startPosition = dest.dataPosition()
        // version 1
        dest.writeInt(result)
        // version 2
        dest.writeByteArray(sessionKey)
        dest.writeByteArray(decryptedSessionKey)
        // Go back and write the size
        val parcelableSize = dest.dataPosition() - startPosition
        dest.setDataPosition(sizePosition)
        dest.writeInt(parcelableSize)
        dest.setDataPosition(startPosition + parcelableSize)
    }

    override fun toString(): String {
        return "\nresult: $result"
    }

    companion object CREATOR : Creator<OpenPgpDecryptionResult> {
        /**
         * Since there might be a case where new versions of the client using the library getting
         * old versions of the protocol (and thus old versions of this class), we need a versioning
         * system for the parcels sent between the clients and the providers.
         */
        const val PARCELABLE_VERSION = 2

        // content not encrypted
        const val RESULT_NOT_ENCRYPTED = -1
        // insecure!
        const val RESULT_INSECURE = 0
        // encrypted
        const val RESULT_ENCRYPTED = 1
        override fun createFromParcel(source: Parcel): OpenPgpDecryptionResult? {
            val version = source.readInt() // parcelableVersion
            val parcelableSize = source.readInt()
            val startPosition = source.dataPosition()
            val result = source.readInt()
            val sessionKey = if (version > 1) source.createByteArray() else null
            val decryptedSessionKey =
                if (version > 1) source.createByteArray() else null
            val vr =
                OpenPgpDecryptionResult(result, sessionKey, decryptedSessionKey)
            // skip over all fields added in future versions of this parcel
            source.setDataPosition(startPosition + parcelableSize)
            return vr
        }

        override fun newArray(size: Int): Array<OpenPgpDecryptionResult?> {
            return arrayOfNulls(size)
        }
    }
}
