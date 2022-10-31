package com.wafflestudio.seminar.config

import com.wafflestudio.seminar.common.*
import com.wafflestudio.seminar.core.user.service.AuthTokenService
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Configuration
class WebConfig(
    private val authArgumentResolver: AuthArgumentResolver,
    private val authInterceptor: AuthInterceptor,
): WebMvcConfigurer {

    /**
     * TODO 세미나 레포지토리를 참고해서,
     *   헤더를 통한 JWT 인증이 가능하게끔 적절한 컴포넌트들을 구성해주세요.
     */

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authArgumentResolver)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
    }
    
}

@Configuration
class AuthArgumentResolver(
    private val authTokenService: AuthTokenService
): HandlerMethodArgumentResolver {
    
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(UserContext::class.java)
                && parameter.parameterType == Long::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        if (supportsParameter(parameter)) {
            webRequest.getHeader("Authorization")
            return authTokenService.getCurrentUserId(
                webRequest.getHeader("Authorization")
                    ?: throw MismatchingAnnotationException
            )
        } else {
            throw UserContextWrongParameterTypeException
        }
    }
}

@Configuration
class AuthInterceptor(
    private val authTokenService: AuthTokenService
): HandlerInterceptor {
    
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val handlerCasted = handler as? HandlerMethod ?: return true
        
        if (handlerCasted.hasMethodAnnotation(Authenticated::class.java)) {
            authTokenService.verifyToken(
                request.getHeader("Authorization")
                    ?: throw AuthTokenMissingException
            )
        }
        
        return super.preHandle(request, response, handler)
    }
}