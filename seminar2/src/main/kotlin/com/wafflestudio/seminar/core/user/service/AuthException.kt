package com.wafflestudio.seminar.core.user.service

import com.wafflestudio.seminar.common.*

typealias AuthException = Seminar401

// 400
object NullNumberException :
    Seminar400("Null value was given for numeric data")

// 401
object InvalidTokenException :
    AuthException("Authorization token is in invalid form (unsupported, malformed, etc.)")
object FailedToLogInException :
    AuthException("Failed to log in")
object AuthTokenExpiredException :
    AuthException("Authorization token is expired")

// 404
object UserNotFoundException :
    Seminar404("Cannot find user info")

// 409
object DuplicateEmailException :
    Seminar409("Given email is already signed up")
object DuplicateParticipantEnrollmentException :
    Seminar409("Already enrolled as participant")