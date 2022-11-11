package com.wafflestudio.seminar.core.user

import com.wafflestudio.seminar.common.SeminarException
import com.wafflestudio.seminar.core.user.api.request.EditProfileRequest
import com.wafflestudio.seminar.core.user.api.request.LoginRequest
import com.wafflestudio.seminar.core.user.api.request.SignUpRequest
import com.wafflestudio.seminar.core.user.database.UserRepository
import com.wafflestudio.seminar.core.user.service.AuthService
import com.wafflestudio.seminar.core.user.service.AuthToken
import com.wafflestudio.seminar.core.user.service.AuthTokenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.BDDMockito.anyString
import org.mockito.Mockito.mock
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import javax.servlet.http.HttpServletRequest

@SpringBootTest
internal class UserServiceTest @Autowired constructor(
    private val authService: AuthService,
    private val userTestHelper: UserTestHelper,
    private val userRepository: UserRepository,
) {
    @MockBean
    private lateinit var authTokenService: AuthTokenService
    
    @BeforeEach
    fun cleanRepository() {
        userRepository.deleteAll()
    }
    
    fun givenMockToken() {
        given(authTokenService.generateTokenByUsername(anyString())).willReturn(AuthToken("AUTH_TOKEN"))
    }
    
    @Test
    fun `회원가입 성공`() {
        // given
        givenMockToken()
        val email = "example@email.com"
        val request = SignUpRequest(email, "", "", "PARTICIPANT", null, null, null, null)

        // when
        val result = authService.signUp(request)

        // then
        assertThat(result).isEqualTo("AUTH_TOKEN")
        assertThat(userRepository.findAll()).hasSize(1)
        assertThat(userRepository.findByEmail(email)).isNotNull
    }

    @Test
    fun `회원가입 실패 - 중복 이메일`() {
        // given
        val email = "example@email.com"
        userTestHelper.createParticipant(email, "", "")
        val request = SignUpRequest(email, "", "", "PARTICIPANT", null, null, null, null)

        // when
        val exception = assertThrows<SeminarException> {
            authService.signUp(request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.CONFLICT)
    }
    
    @Test
    fun `로그인 성공`() {
        // given
        givenMockToken()
        val email = "example@email.com"
        val password = "secret"
        userTestHelper.createParticipant(email, "", password)
        val request = LoginRequest(email, password)
        
        // when
        val result = authService.login(request)
        
        // then
        assertThat(result).isEqualTo("AUTH_TOKEN")
    }

    @Test
    fun `로그인 실패 - 잘못된 비밀번호`() {
        // given
        givenMockToken()
        val email = "example@email.com"
        val password = "secret"
        userTestHelper.createParticipant(email, "", password)
        val request = LoginRequest(email, "")

        // when
        val exception = assertThrows<SeminarException> {
            authService.login(request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
    
    // TODO: 내 정보 조회 구현?
    
    @Test
    @Transactional
    fun `유저 정보 조회 성공`() {
        // given
        val email = "example@email.com"
        userTestHelper.createParticipant(email, "", "")
        // FIXME: 사용하지 않는 HttpServletRequest?
        val httpServletRequest: HttpServletRequest = mock(HttpServletRequest::class.java)
        
        // when
        val result = authService.getProfile(1, httpServletRequest)
        
        // then
        assertThat(result.email).isEqualTo(email)
    }
    
    @Test
    @Transactional
    fun `유저 정보 조회 실패 - 존재하지 않는 유저`() {
        // given
        val email = "example@email.com"
        userTestHelper.createParticipant(email, "", "")
        val httpServletRequest = mock(HttpServletRequest::class.java)
        
        // when
        val exception = assertThrows<SeminarException> {
            authService.getProfile(2, httpServletRequest)
        }
        
        // then
        assertThat(exception.status).isEqualTo(HttpStatus.NOT_FOUND)
    }
    
    @Test
    @Transactional
    fun `유저 정보 수정 성공`() {
        // given
        val email = "example@email.com"
        userTestHelper.createParticipant(email, "", "")
        val university = "university"
        val name = "name"
        val request = EditProfileRequest(university, "company", 1, name)
        val httpServletRequest = mock(HttpServletRequest::class.java)
        given(httpServletRequest.getAttribute("email")).willReturn(email)
        
        // when
        val result = authService.editProfile(httpServletRequest, request)
        
        // then
        assertThat(result.username).isEqualTo(name)
        assertThat(result.participant).isNotNull
        assertThat(result.participant!!.university).isEqualTo(university)
        assertThat(result.instructor).isNull()
    }
    
    @Test
    @Transactional
    fun `유저 정보 수정 실패 - year 값이 음수`() {
        // given
        val email = "example@email.com"
        userTestHelper.createInstructor(email, "", "")
        val request = EditProfileRequest("university", "company", -1, "name")
        val httpServletRequest = mock(HttpServletRequest::class.java)
        given(httpServletRequest.getAttribute("email")).willReturn(email)

        // when
        val exception = assertThrows<SeminarException> {
            authService.editProfile(httpServletRequest, request)
        }

        // then
        assertThat(exception.status).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}