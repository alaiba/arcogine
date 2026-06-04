package com.arcogine.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot entry point used only to bootstrap the application context
 * for {@code @SpringBootTest}. The production module is built as a library
 * (bootJar disabled) and ships no main class, so the HTTP contract tests provide
 * their own. Component scanning rooted at {@code com.arcogine.api} picks up the
 * controllers, {@code WebConfig}, and {@code SimThread} bean.
 */
@SpringBootApplication
class TestApiApplication {}
