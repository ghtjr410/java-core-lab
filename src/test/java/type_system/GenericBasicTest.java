package type_system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 제네릭 기본 개념 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. 제네릭은 컴파일 타임 타입 안전성을 제공한다
 * 2. 제네릭은 불공변(invariant)이다 - List<Integer>는 List<Object>의 하위 타입이 아니다
 * 3. 배열은 공변(covariant)이다 - Integer[]는 Object[]의 하위 타입이다
 * 4. 이 차이가 타입 안전성에 미치는 영향을 이해해야 한다
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class GenericBasicTest {

    @Nested
    class 제네릭_클래스_기본 {

        @Test
        void 제네릭_클래스는_타입_파라미터를_받아_타입_안전성을_제공한다() {
            Box<String> stringBox = new Box<>();
            stringBox.set("Hello");

            String value = stringBox.get(); // 캐스팅 불필요

            assertThat(value).isEqualTo("Hello");
        }

        @Test
        void 제네릭이_없던_시절에는_Object와_캐스팅을_사용했다() {
            RawBox rawBox = new RawBox();
            rawBox.set("Hello");

            // 캐스팅 필요 - 런타임에 ClassCastException 위험
            String value = (String) rawBox.get();

            assertThat(value).isEqualTo("Hello");
        }

        @Test
        void Raw_타입_사용시_잘못된_타입을_넣어도_컴파일_에러가_발생하지_않는다() {
            RawBox rawBox = new RawBox();
            rawBox.set("Hello");
            rawBox.set(123); // 컴파일 OK, 하지만 위험!

            // 런타임에서야 문제 발견
            assertThatThrownBy(() -> {
                        String value = (String) rawBox.get();
                    })
                    .isInstanceOf(ClassCastException.class);
        }

        @Test
        void 다중_타입_파라미터를_사용할_수_있다() {
            Pair<String, Integer> pair = new Pair<>("age", 25);

            assertThat(pair.first()).isEqualTo("age");
            assertThat(pair.second()).isEqualTo(25);
        }
    }

    static class Box<T> {
        private T value;

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }
    }

    static class RawBox {
        private Object value;

        public void set(Object value) {
            this.value = value;
        }

        public Object get() {
            return value;
        }
    }

    record Pair<F, S>(F first, S second) {}
}
