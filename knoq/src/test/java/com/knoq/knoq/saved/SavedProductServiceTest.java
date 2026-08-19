package com.knoq.knoq.saved;

import com.knoq.knoq.saved.service.SavedProductService;
import com.knoq.knoq.sessions.entity.Session;
import com.knoq.knoq.sessions.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SavedProductServiceTest {

    @Autowired
    SavedProductService service;

    @Autowired
    SessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        sessionRepository.save(Session.of(
                "sess_test_1", "token_saved_test_1", 1L, LocalDateTime.now().plusMinutes(30)
        ));
        sessionRepository.save(Session.of(
                "sess_test_2", "token_saved_test_2", 1L, LocalDateTime.now().plusMinutes(30)
        ));
    }

    @Test
    void 같은_제품을_두_번_저장하면_하나만_남는다() {

        service.saveFromCamera(
                "sess_test_1",
                "prod_1"
        );

        service.saveFromCamera(
                "sess_test_1",
                "prod_1"
        );

        assertThat(
                service.findAll("sess_test_1")
        ).hasSize(1);
    }

    @Test
    void 최근_저장순으로_반환된다() {

        service.saveFromCamera(
                "sess_test_2",
                "prod_1"
        );

        service.saveFromRecommend(
                "sess_test_2",
                "prod_2"
        );

        assertThat(
                service.findAll("sess_test_2")
                        .get(0)
                        .getProductId()
        ).isEqualTo("prod_2");
    }
}
