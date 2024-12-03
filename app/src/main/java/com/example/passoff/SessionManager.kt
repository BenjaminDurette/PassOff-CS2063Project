package com.example.passoff

object SessionManager {
    private var masterPassword: String? = null

    fun setMasterPassword(enteredMasterPassword: String) {
        masterPassword = enteredMasterPassword
    }

    fun getMasterPassword(): String? {
        return masterPassword;
    }
}