package com.ieum.infra.redis;

import com.ieum.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("RedisService")
class RedisServiceTest extends AbstractIntegrationTest {

    private static final String KEY = "test:key";
    private static final String VALUE = "test-value";
    private static final Duration TTL_SHORT = Duration.ofSeconds(1);

    @Autowired
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        redisService.delete(KEY);
    }

    @Nested
    @DisplayName("set")
    class Set {

        @Test
        @DisplayName("TTL 없이 저장한 값을 get으로 조회하면 동일한 값이 반환된다")
        void set_withoutTtl_returnsValue() {
            // when
            redisService.set(KEY, VALUE);

            // then
            assertThat(redisService.get(KEY)).isEqualTo(VALUE);
        }

        @Test
        @DisplayName("TTL을 지정해 저장한 값을 TTL 만료 전에 조회하면 동일한 값이 반환된다")
        void set_withTtl_returnsValueBeforeExpiry() {
            // when
            redisService.set(KEY, VALUE, Duration.ofSeconds(10));

            // then
            assertThat(redisService.get(KEY)).isEqualTo(VALUE);
        }

        @Test
        @DisplayName("TTL 만료 후 조회하면 null이 반환된다")
        void set_withTtl_returnsNullAfterExpiry() throws InterruptedException {
            // when
            redisService.set(KEY, VALUE, TTL_SHORT);
            Thread.sleep(TTL_SHORT.toMillis() + 200);

            // then
            assertThat(redisService.get(KEY)).isNull();
        }
    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("존재하지 않는 키를 조회하면 null이 반환된다")
        void get_nonExistentKey_returnsNull() {
            assertThat(redisService.get("non:existent:key")).isNull();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("저장된 키를 삭제하면 이후 get 조회 시 null이 반환된다")
        void delete_existingKey_returnsNullOnGet() {
            // given
            redisService.set(KEY, VALUE);

            // when
            redisService.delete(KEY);

            // then
            assertThat(redisService.get(KEY)).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 키를 삭제해도 예외가 발생하지 않는다")
        void delete_nonExistentKey_noException() {
            assertThatNoException().isThrownBy(() -> redisService.delete("non:existent:key"));
        }
    }

    @Nested
    @DisplayName("hasKey")
    class HasKey {

        @Test
        @DisplayName("존재하는 키에 대해 true를 반환한다")
        void hasKey_existingKey_returnsTrue() {
            // given
            redisService.set(KEY, VALUE);

            // then
            assertThat(redisService.hasKey(KEY)).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 키에 대해 false를 반환한다")
        void hasKey_nonExistentKey_returnsFalse() {
            assertThat(redisService.hasKey("non:existent:key")).isFalse();
        }
    }
}
