package com.wafflestudio.seminar.core.user.api.request

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank

data class SignUpRequest(
    @field: NotBlank
    @field: Email
    val email: String,
    
    @field: NotBlank
    val username: String,
    
    @field: NotBlank
    val password: String,
)