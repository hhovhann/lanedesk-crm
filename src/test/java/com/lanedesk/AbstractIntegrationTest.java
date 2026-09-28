package com.lanedesk;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestcontainersConfig.class)
@TestPropertySource(properties = {"lanedesk.security.user=test", "lanedesk.security.password=test"})
@Transactional
public abstract class AbstractIntegrationTest {
}
