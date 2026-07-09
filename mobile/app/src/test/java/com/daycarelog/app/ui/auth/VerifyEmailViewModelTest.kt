package com.daycarelog.app.ui.auth

import com.daycarelog.app.data.api.TokenProvider
import com.daycarelog.app.data.model.AuthResponse
import com.daycarelog.app.data.model.UserDto
import com.daycarelog.app.data.model.VerifyEmailResponse
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class VerifyEmailViewModelTest {

    private val user = UserDto(
        id = 1L, email = "parent@example.com", fullName = "Test Parent",
        firstName = "Test", lastName = "Parent", middleName = null, suffix = null,
        role = "parent", profilePhoto = null, isActive = true, emailVerified = true,
    )

    @Before
    @After
    fun resetGlobalTokenState() {
        TokenProvider.token = null
    }

    // ── verifyByCode ─────────────────────────────────────────────────────

    @Test
    fun doVerifyByCode_success_returnsVerifiedAndPersistsSession() = runBlocking {
        var persistedToken: String? = null
        var persistedUser: UserDto? = null
        val vm = VerifyEmailViewModel(
            verifyEmail = { req ->
                assertEquals("parent@example.com", req.email)
                assertEquals("123456", req.code)
                VerifyEmailResponse(message = "ok", token = "fresh.jwt", user = user)
            },
            persistSession = { _, token, u -> persistedToken = token; persistedUser = u },
        )

        val result = vm.doVerifyByCode(null, "parent@example.com", "123456")

        assertTrue(result is VerifyEmailState.Verified)
        assertEquals(user, (result as VerifyEmailState.Verified).user)
        assertEquals("fresh.jwt", persistedToken)
        assertEquals(user, persistedUser)
    }

    @Test
    fun doVerifyByCode_wrongCode_returnsErrorWithCode() = runBlocking {
        val vm = VerifyEmailViewModel(
            verifyEmail = { throw apiError("Incorrect code.", "TOKEN_INVALID") },
        )

        val result = vm.doVerifyByCode(null, "parent@example.com", "000000")

        assertTrue(result is VerifyEmailState.Error)
        assertEquals("Incorrect code.", (result as VerifyEmailState.Error).message)
        assertEquals("TOKEN_INVALID", result.code)
    }

    @Test
    fun doVerifyByCode_tooManyAttempts_returnsErrorWithThatCode() = runBlocking {
        val vm = VerifyEmailViewModel(
            verifyEmail = { throw apiError("Too many incorrect attempts. Request a new code.", "TOO_MANY_ATTEMPTS") },
        )

        val result = vm.doVerifyByCode(null, "parent@example.com", "000000") as VerifyEmailState.Error

        assertEquals("TOO_MANY_ATTEMPTS", result.code)
    }

    @Test
    fun doVerifyByCode_plainNetworkFailure_returnsErrorWithNullCode() = runBlocking {
        val vm = VerifyEmailViewModel(verifyEmail = { throw RuntimeException("Network error") })

        val result = vm.doVerifyByCode(null, "parent@example.com", "123456") as VerifyEmailState.Error

        assertEquals("Network error", result.message)
        assertNull(result.code)
    }

    // ── verifyByToken ────────────────────────────────────────────────────

    @Test
    fun doVerifyByToken_success_returnsVerified() = runBlocking {
        val vm = VerifyEmailViewModel(
            verifyEmail = { req ->
                assertEquals("raw-link-token", req.token)
                VerifyEmailResponse(message = "ok", token = "fresh.jwt", user = user)
            },
            persistSession = { _, _, _ -> },
        )

        val result = vm.doVerifyByToken(null, "raw-link-token")

        assertTrue(result is VerifyEmailState.Verified)
    }

    @Test
    fun doVerifyByToken_expired_returnsErrorWithTokenExpired() = runBlocking {
        val vm = VerifyEmailViewModel(
            verifyEmail = { throw apiError("This verification link has expired.", "TOKEN_EXPIRED") },
        )

        val result = vm.doVerifyByToken(null, "old-token") as VerifyEmailState.Error

        assertEquals("TOKEN_EXPIRED", result.code)
    }

    // ── resend ───────────────────────────────────────────────────────────

    @Test
    fun doResend_success_returnsResent() = runBlocking {
        var calledWith: String? = null
        val vm = VerifyEmailViewModel(resendVerification = { email -> calledWith = email })

        val result = vm.doResend("parent@example.com")

        assertTrue(result is VerifyEmailState.Resent)
        assertEquals("parent@example.com", calledWith)
    }

    @Test
    fun doResend_rateLimited_returnsErrorWithRateLimitedCode() = runBlocking {
        val vm = VerifyEmailViewModel(
            resendVerification = { throw apiError("Too many verification emails requested.", "RATE_LIMITED") },
        )

        val result = vm.doResend("parent@example.com") as VerifyEmailState.Error

        assertEquals("RATE_LIMITED", result.code)
    }

    // ── checkStatus ("I verified in my browser") ────────────────────────

    @Test
    fun doCheckStatus_withNoStoredToken_returnsErrorWithoutCallingMe() = runBlocking {
        TokenProvider.token = null
        var meCalled = false
        val vm = VerifyEmailViewModel(fetchMe = { meCalled = true; user })

        val result = vm.doCheckStatus(null)

        assertTrue(result is VerifyEmailState.Error)
        assertTrue((result as VerifyEmailState.Error).message.contains("Enter the code"))
        assertTrue("fetchMe must not be called without a stored token", !meCalled)
    }

    @Test
    fun doCheckStatus_stillUnverified_returnsNotYetVerifiedWithoutRefreshingToken() = runBlocking {
        TokenProvider.token = "existing.unverified.jwt"
        var refreshCalled = false
        val vm = VerifyEmailViewModel(
            fetchMe = { user.copy(emailVerified = false) },
            refreshToken = { refreshCalled = true; AuthResponse("should-not-be-used", user) },
        )

        val result = vm.doCheckStatus(null)

        assertTrue(result is VerifyEmailState.NotYetVerified)
        assertTrue("refreshToken must not be called while still unverified", !refreshCalled)
    }

    @Test
    fun doCheckStatus_nowVerified_refreshesTokenAndPersistsIt() = runBlocking {
        TokenProvider.token = "existing.unverified.jwt"
        var persistedToken: String? = null
        val vm = VerifyEmailViewModel(
            fetchMe = { user.copy(emailVerified = true) },
            refreshToken = { AuthResponse("new.verified.jwt", user) },
            persistSession = { _, token, _ -> persistedToken = token },
        )

        val result = vm.doCheckStatus(null)

        assertTrue(result is VerifyEmailState.Verified)
        assertEquals("new.verified.jwt", persistedToken)
    }
}

/** Builds a real retrofit2.HttpException with a JSON error body, matching the backend's
 *  actual { "message": ..., "code": ... } error shape, so errorFrom's HttpException branch
 *  (not just its generic-Exception fallback) is genuinely exercised. */
private fun apiError(message: String, code: String): HttpException {
    val json = """{"message":"$message","code":"$code"}"""
    val body = json.toResponseBody("application/json".toMediaType())
    return HttpException(Response.error<Any>(400, body))
}
