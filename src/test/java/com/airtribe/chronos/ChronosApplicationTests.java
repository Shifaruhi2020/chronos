package com.airtribe.chronos;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@DataMongoTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class ChronosApplicationTests {

    @Test
    void contextLoads() {
        // This will only load MongoDB-related configuration
    }

}
