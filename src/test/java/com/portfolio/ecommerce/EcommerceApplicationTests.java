package com.portfolio.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — verifies the entire Spring context loads without errors.
 */
@SpringBootTest
class EcommerceApplicationTests {

    @Test
    void contextLoads() {
        // If the context fails to start (missing beans, config errors, etc.)
        // this test will fail with a descriptive message.
    }
}
