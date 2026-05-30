package com.nikhil.rateLimiter;

import com.nikhil.rateLimiter.annotation.RateLimit;
import com.nikhil.rateLimiter.service.SlidingWindowRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(RateLimiterApplicationTests.RateLimiterController.class)
class RateLimiterApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SlidingWindowRateLimiter slidingWindowRateLimiter;

	@RestController
	static class RateLimiterController {
		@GetMapping("/api/test-limit")
		@RateLimit(limit = 5, windowSeconds = 60)
		public String testLimit() {
			return "test";
		}
	}
	@Test
	void contextLoads() {
	}
	@Test
	void shouldReturnTestWhenRateLimiterAllowsRequests() throws Exception {
		String expectedKey = "rate_limit:127.0.0.1:RateLimiterApplicationTests.RateLimiterController.testLimit()";
		when(slidingWindowRateLimiter.checkRateLimiting(anyString(), eq(5), eq(60)))
				.thenReturn(true);
		mockMvc.perform(get("/api/test-limit")
				.with(request -> {
					request.setRemoteAddr("127.0.0.1");
					return request;
				}))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturnTooManyRequestsWhenRateLimiterAllowsRequests() throws Exception {
		String expectedKey = "rate_limit:203.0.113.195:RateLimiterApplicationTests.RateLimiterController.testLimit()";
		when(slidingWindowRateLimiter.checkRateLimiting(anyString(), eq(5), eq(60)))
				.thenReturn(false);
		mockMvc.perform(get("/api/test-limit")
						.header("x-forwarded-for", "203.0.113.195"))
						.andExpect(status().isTooManyRequests());
	}

}
