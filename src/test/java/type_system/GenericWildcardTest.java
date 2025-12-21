package type_system;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 제네릭 와일드카드 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. <?> - 비한정 와일드카드: 모든 타입 허용, 읽기만 가능 (Object로)
 * 2. <? extends T> - 상한 경계: T 또는 T의 하위 타입, Producer(읽기) 용도
 * 3. <? super T> - 하한 경계: T 또는 T의 상위 타입, Consumer(쓰기) 용도
 * 4. PECS: Producer-Extends, Consumer-Super
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class GenericWildcardTest {

    @Nested
    class 비한정_와일드카드_Unbounded {

        @Test
        void 비한정_와일드카드는_모든_타입의_리스트를_받을_수_있다() {
            List<String> strings = List.of("a", "b", "c");
            List<Integer> integers = List.of(1, 2, 3);
            List<Object> objects = List.of("mixed", 1, 2.0);

            assertThat(countElements(strings)).isEqualTo(3);
            assertThat(countElements(integers)).isEqualTo(3);
            assertThat(countElements(objects)).isEqualTo(3);
        }

        @Test
        void 비한정_와일드카드_리스트에서는_Object로만_읽을_수_있다() {
            List<String> strings = List.of("hello", "world");

            Object first = getFirstAsObject(strings);

            assertThat(first).isEqualTo("hello");
            // String first = getFirstAsObject(strings); // 컴파일 에러
        }

        @Test
        void 비한정_와일드카드_리스트에는_null만_추가할_수_있다() {
            List<String> strings = new ArrayList<>();
            strings.add("hello");

            // addToList(strings, "world"); // 컴파일 에러
            // 왜? List<?>는 "어떤 타입인지 모름"이므로 타입 안전성 보장 불가

            addNullOnly(strings); // null은 모든 참조 타입에 대입 가능하므로 OK

            assertThat(strings).containsExactly("hello", null);
        }

        private int countElements(List<?> list) {
            return list.size();
        }

        private Object getFirstAsObject(List<?> list) {
            return list.isEmpty() ? null : list.get(0);
        }

        private void addNullOnly(List<?> list) {
            // list.add("something"); // 컴파일 에러
            list.add(null); // null만 가능
        }
    }
}
