package com.knoq.knoq.staff.service;

import com.knoq.knoq.global.exception.ApiException;
import com.knoq.knoq.staff.dto.StaffLoginRequest;
import com.knoq.knoq.staff.dto.StaffLoginResponse;
import com.knoq.knoq.store.entity.Store;
import com.knoq.knoq.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class StaffServiceTest {

    @Autowired
    private StaffService staffService;

    @Autowired
    private StoreRepository storeRepository;

    @BeforeEach
    void setUp() {
        storeRepository.save(Store.of("TEST-001", "테스트 매장"));
    }

    @Test
    void 올바른_storeCode와_PIN이면_로그인에_성공한다() {
        StaffLoginResponse response = staffService.login(new StaffLoginRequest("TEST-001", "1234"));

        assertThat(response.staffToken()).isNotBlank();
        assertThat(response.storeName()).isEqualTo("테스트 매장");
    }

    @Test
    void 존재하지_않는_storeCode면_예외를_던진다() {
        assertThatThrownBy(() -> staffService.login(new StaffLoginRequest("NOT-EXIST", "1234")))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void PIN이_틀리면_예외를_던진다() {
        assertThatThrownBy(() -> staffService.login(new StaffLoginRequest("TEST-001", "0000")))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 로그인_후_받은_토큰으로_로그아웃하면_정상_처리된다() {
        StaffLoginResponse loginResponse = staffService.login(new StaffLoginRequest("TEST-001", "1234"));

        staffService.logout("Bearer " + loginResponse.staffToken());
        // 예외 없이 끝나면 성공 (별도로 확인할 상태값이 없음 - stateless라서)
    }

    @Test
    void Authorization_헤더가_없으면_로그아웃시_예외를_던진다() {
        assertThatThrownBy(() -> staffService.logout(null))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void 잘못된_형식의_토큰이면_로그아웃시_예외를_던진다() {
        assertThatThrownBy(() -> staffService.logout("Bearer 이상한값"))
                .isInstanceOf(ApiException.class);
    }
}