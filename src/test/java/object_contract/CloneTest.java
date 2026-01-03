package object_contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Clone 메서드 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. clone()의 동작 방식과 Cloneable 인터페이스
 * 2. 얕은 복사(shallow copy)와 깊은 복사(deep copy)
 * 3. clone()의 문제점과 대안
 * 4. 복사 생성자와 복사 팩터리 패턴 권장
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class CloneTest {

    @Nested
    class clone_기본_동작 {

        @Test
        void Cloneable_미구현시_CloneNotSupportedException_발생() {
            NonCloneable obj = new NonCloneable("test");

            assertThatThrownBy(() -> obj.clone()).isInstanceOf(CloneNotSupportedException.class);
        }

        @Test
        void Cloneable_구현시_clone_가능() throws CloneNotSupportedException {
            SimpleCloneable original = new SimpleCloneable("test", 42);
            SimpleCloneable cloned = (SimpleCloneable) original.clone();

            // 다른 객체
            assertThat(cloned).isNotSameAs(original);

            // 같은 값
            assertThat(cloned.getName()).isEqualTo(original.getName());
            assertThat(cloned.getValue()).isEqualTo(original.getValue());

            // equals (올바르게 구현됐다면)
            assertThat(cloned).isEqualTo(original);
        }

        @Test
        void clone_규약_x_clone_ne_x() throws CloneNotSupportedException {
            SimpleCloneable x = new SimpleCloneable("test", 42);
            SimpleCloneable clone = (SimpleCloneable) x.clone();

            // x.clone() != x (항상 참)
            assertThat(clone).isNotSameAs(x);
        }

        @Test
        void clone_규약_x_clone_getClass_eq_x_getClass() throws CloneNotSupportedException {
            SimpleCloneable x = new SimpleCloneable("test", 42);
            SimpleCloneable clone = (SimpleCloneable) x.clone();

            // x.clone().getClass() == x.getClass() (일반적으로 참)
            assertThat(clone.getClass()).isEqualTo(x.getClass());
        }

        @Test
        void clone_규약_x_clone_equals_x() throws CloneNotSupportedException {
            SimpleCloneable x = new SimpleCloneable("test", 42);
            SimpleCloneable clone = (SimpleCloneable) x.clone();

            // x.clone().equals(x) (일반적으로 참, 필수는 아님)
            assertThat(clone).isEqualTo(x);
        }
    }

    // === 테스트용 헬퍼 클래스들 ===

    static class NonCloneable {
        private final String value;

        public NonCloneable(String value) {
            this.value = value;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }

    static class SimpleCloneable implements Cloneable {
        private final String name;
        private final int value;

        public SimpleCloneable(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone(); // 필드가 모두 불변/기본형이면 충분
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SimpleCloneable s)) return false;
            return value == s.value && name.equals(s.name);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + value;
        }
    }
}
