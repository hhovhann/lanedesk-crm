package com.lanedesk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(TestcontainersConfig.class)
@TestPropertySource(properties = {
        "lanedesk.security.user=test",
        "lanedesk.security.password=test"
})
class LaneDeskApplicationTests {

    @Test
    void contextLoads() {
    }
}
