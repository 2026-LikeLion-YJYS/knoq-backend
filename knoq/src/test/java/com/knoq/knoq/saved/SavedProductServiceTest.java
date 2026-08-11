package com.knoq.knoq.saved;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SavedProductServiceTest {

    @Autowired SavedProductService service;

    @Test
    void 같은_제품을_두_번_저장하면_하나만_남는다() {
        service.saveFromCamera("sess_1", "prod_12");
        service.saveFromCamera("sess_1", "prod_12");
        assertThat(service.findAll("sess_1")).hasSize(1);
    }

    @Test
    void 최근_저장순으로_반환된다() {
        service.saveFromCamera("sess_2", "prod_12");
        service.saveFromRecommend("sess_2", "prod_33");
        assertThat(service.findAll("sess_2").get(0).getProductId()).isEqualTo("prod_33");
    }
}
