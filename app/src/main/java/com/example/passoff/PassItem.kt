package com.example.passoff

data class PassItem(val title: String, val username: String, val password: String, val domain: String)  {

    val name: String
        get() = title
}
