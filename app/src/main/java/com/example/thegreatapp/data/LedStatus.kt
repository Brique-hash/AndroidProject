package com.example.thegreatapp.data

class LedStatus {
    /**
     * LedStatus model
     */
    data class LedStatus(var identifier: String = "", var status: Boolean = false) {
        fun setIdentifier(identifier: String): LedStatus {
            this.identifier = identifier
            return this
        }

        fun setStatus(status: Boolean): LedStatus {
            this.status = status
            return this
        }

        fun reverseStatus(): LedStatus {
            return setStatus(!status)
        }

        fun clone(): LedStatus {
            return LedStatus(identifier, status)
        }
    }
}