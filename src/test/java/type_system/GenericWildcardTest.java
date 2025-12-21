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

    @Nested
    class 하한_경계_와일드카드_Lower_Bounded {

        /**
         * <? super T>: Consumer Super
         * - T 또는 T의 상위 타입을 담은 컬렉션
         * - 쓰기(consume) 전용으로 사용
         * - 읽기 시 Object로만 받을 수 있음
         */
        @Test
        void super_와일드카드는_쓰기_전용이다() {
            List<Object> objects = new ArrayList<>();
            List<Number> numbers = new ArrayList<>();
            List<Integer> integers = new ArrayList<>();

            // Integer를 추가하는 메서드
            addIntegers(objects); // List<Object>도 Integer를 담을 수 있음
            addIntegers(numbers); // List<Number>도 Integer를 담을 수 있음
            addIntegers(integers); // List<Integer>는 당연히 가능

            assertThat(objects).containsExactly(1, 2, 3);
            assertThat(numbers).containsExactly(1, 2, 3);
            assertThat(integers).containsExactly(1, 2, 3);
        }

        @Test
        void super_와일드카드_리스트에서_읽기는_Object로만_가능하다() {
            List<Number> numbers = new ArrayList<>(List.of(1, 2.0, 3L));
            List<? super Integer> superInteger = numbers;

            // 쓰기는 가능 - Integer 또는 그 하위 타입
            superInteger.add(100);

            // 읽기는 Object로만
            Object first = superInteger.get(0);
            // Integer first = superInteger.get(0); // 컴파일 에러!
            // Number first = superInteger.get(0);  // 컴파일 에러!

            assertThat(first).isEqualTo(1);
        }

        @Test
        void super_와일드카드로_상위타입_컬렉션에_유연하게_추가() {
            List<Object> objects = new ArrayList<>();

            // Object 리스트에 String을 추가하는 것도 가능
            fillWithStrings(objects);

            assertThat(objects).containsExactly("A", "B", "C");
        }

        private void addIntegers(List<? super Integer> list) {
            list.add(1);
            list.add(2);
            list.add(3);
        }

        private void fillWithStrings(List<? super String> list) {
            list.add("A");
            list.add("B");
            list.add("C");
        }
    }

    @Nested
    class PECS_원칙_Producer_Extends_Consumer_Super {

        /**
         * PECS 원칙:
         * - Producer(데이터를 꺼내는 쪽): extends 사용
         * - Consumer(데이터를 넣는 쪽): super 사용
         *
         * Collections.copy(dest, src)가 대표적인 예:
         * - src는 데이터를 제공(produce)하므로 extends
         * - dest는 데이터를 받아들이므로(consume) super
         */
        @Test
        void PECS_원칙_적용_예시_복사() {
            List<Integer> source = List.of(1, 2, 3);
            List<Number> destination = new ArrayList<>(List.of(0, 0, 0));

            copy(destination, source);

            assertThat(destination).containsExactly(1, 2, 3);
        }

        @Test
        void PECS_원칙_적용_예시_필터링_후_추가() {
            List<Integer> source = List.of(1, 2, 3, 4, 5, 6);
            List<Number> destination = new ArrayList<>();

            // source에서 짝수만 꺼내서 destination에 추가
            filterAndAdd(destination, source, n -> n % 2 == 0);

            assertThat(destination).containsExactly(2, 4, 6);
        }

        @Test
        void PECS가_없으면_코드_재사용성이_떨어진다() {
            List<Integer> integers = List.of(1, 2, 3);
            List<Number> numbers = new ArrayList<>(List.of(0, 0, 0));

            // PECS 없이 단순 제네릭으로는 이게 불가능
            // copyStrictly(numbers, integers); // 컴파일 에러

            // PECS로 유연성 확보
            copy(numbers, integers); // OK

            assertThat(numbers).containsExactly(1, 2, 3);
        }

        /**
         * PECS가 적용된 copy 메서드
         * @param dest 대상 리스트 (Consumer - super)
         * @param src 소스 리스트 (Producer - extends)
         */
        private <T> void copy(List<? super T> dest, List<? extends T> src) {
            for (int i = 0; i < src.size(); i++) {
                dest.set(i, src.get(i));
            }
        }

        private <T> void filterAndAdd(
                List<? super T> dest, // Consumer: 데이터를 받음
                List<? extends T> src, // Producer: 데이터를 제공
                java.util.function.Predicate<T> predicate) {
            for (T item : src) {
                if (predicate.test(item)) {
                    dest.add(item);
                }
            }
        }

        // PECS 없는 버전 - 유연성이 떨어짐
        private <T> void copyStrictly(List<T> dest, List<T> src) {
            for (int i = 0; i < src.size(); i++) {
                dest.set(i, src.get(i));
            }
        }
    }

    @Nested
    class 와일드카드_캡처 {
        /**
         * 와일드카드 캡처 헬퍼 패턴:
         * - public API는 <?> 로 간결하게 노출
         * - 내부 헬퍼는 <T> 로 실제 작업 수행
         * - 호출자는 타입을 명시할 필요 없이 편리하게 사용
         *
         * 만약 <?> 대신 <T>를 직접 노출하면:
         * - swap(strings, 0, 1)           → 대부분 OK
         * - Utils.<String>swap(list, 0, 1) → 가끔 타입 명시 필요
         * - 메서드 시그니처가 복잡해 보임: <T> void swap(List<T> list, ...)
         *
         * <?> + 헬퍼 패턴을 쓰면:
         * - swap(list, 0, 1) → 항상 간단
         * - 메서드 시그니처가 깔끔: void swap(List<?> list, ...)
         */
        @Test
        void 와일드카드와_헬퍼로_간결한_API_제공() {
            List<String> strings = new ArrayList<>(List.of("a", "b", "c"));

            // 호출자는 타입 신경 쓸 필요 없이 그냥 호출
            // 첫 번째와 두 번째 요소 교환
            swap(strings, 0, 1);

            assertThat(strings).containsExactly("b", "a", "c");
        }

        @Test
        void 다양한_타입에_동일한_API로_동작() {
            List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3));

            // Integer든 String이든 같은 방식으로 호출
            reverse(numbers);

            assertThat(numbers).containsExactly(3, 2, 1);
        }

        // <?> 로 받아서 호출자가 타입 명시 없이 사용 가능
        // 와일드카드를 사용하는 public 메서드
        private void swap(List<?> list, int i, int j) {
            swapHelper(list, i, j);
        }

        // 와일드카드 캡처를 위한 private 헬퍼
        // 내부에서 <?> -> <T>로 캡처하여 타입 안전한 연산 수행
        private <T> void swapHelper(List<T> list, int i, int j) {
            T temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }

        private void reverse(List<?> list) {
            reverseHelper(list);
        }

        private <T> void reverseHelper(List<T> list) {
            int left = 0;
            int right = list.size() - 1;
            while (left < right) {
                T temp = list.get(left);
                list.set(left, list.get(right));
                list.set(right, temp);
                left++;
                right--;
            }
        }
    }
}
