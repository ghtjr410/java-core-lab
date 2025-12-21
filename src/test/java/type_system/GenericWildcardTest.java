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

    @Nested
    class 상한_경계_와일드카드_Upper_Bounded {

        /**
         * <? extends T>: Producer Extends
         * - T 또는 T의 하위 타입을 담은 컬렉션
         * - 읽기(produce) 전용으로 사용
         * - 쓰기 불가 (정확한 타입을 모르므로)
         */
        @Test
        void extends_와일드카드는_읽기_전용이다() {
            List<Integer> integers = List.of(1, 2, 3);
            List<Double> doubles = List.of(1.1, 2.2, 3.3);

            // List<? extends Number>는 Number 또는 그 하위 타입의 리스트
            double sumIntegers = sumNumbers(integers);
            double sumDoubles = sumNumbers(doubles);

            assertThat(sumIntegers).isEqualTo(6.0);
            assertThat(sumDoubles).isEqualTo(6.6, within(0.01));
        }

        @Test
        void extends_와일드카드_리스트에는_쓰기가_불가능하다() {
            List<Integer> integers = new ArrayList<>(List.of(1, 2, 3));
            List<? extends Number> numbers = integers;

            // 읽기는 가능 - Number로 읽음
            Number first = numbers.get(0);
            assertThat(first).isEqualTo(1);

            // 쓰기는 불가능!
            // numbers.add(1);       // 컴파일 에러
            // numbers.add(1.0);     // 컴파일 에러
            // numbers.add(1L);      // 컴파일 에러

            // 왜? List<? extends Number>는 List<Integer>일 수도, List<Double>일 수도 있다.
            // 만약 List<Integer>인데 Double을 넣으면 타입 안전성 깨짐!
        }

        @Test
        void extends_와일드카드로_여러_하위타입_컬렉션을_유연하게_처리() {
            List<Integer> integers = List.of(1, 2, 3);
            List<Long> longs = List.of(10L, 20L, 30L);
            List<Double> doubles = List.of(0.1, 0.2, 0.3);

            Number maxInt = findMax(integers);
            Number maxLong = findMax(longs);
            Number maxDouble = findMax(doubles);

            assertThat(maxInt.intValue()).isEqualTo(3);
            assertThat(maxLong.longValue()).isEqualTo(30L);
            assertThat(maxDouble.doubleValue()).isEqualTo(0.3);
        }

        private double sumNumbers(List<? extends Number> numbers) {
            double sum = 0;
            for (Number n : numbers) {
                sum += n.doubleValue();
            }
            return sum;
        }

        private Number findMax(List<? extends Number> numbers) {
            if (numbers.isEmpty()) return null;
            Number max = numbers.get(0);
            for (Number n : numbers) {
                if (n.doubleValue() > max.doubleValue()) {
                    max = n;
                }
            }
            return max;
        }

        private static org.assertj.core.data.Offset<Double> within(double value) {
            return org.assertj.core.data.Offset.offset(value);
        }
    }
}
