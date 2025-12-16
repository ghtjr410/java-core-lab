package type_system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
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

    @Nested
    class 제네릭_메서드 {

        @Test
        void 제네릭_메서드는_호출_시점에_타입이_결정된다() {
            String first = getFirst(List.of("a", "b", "c"));
            Integer firstNum = getFirst(List.of(1, 2, 3));

            assertThat(first).isEqualTo("a");
            assertThat(firstNum).isEqualTo(1);
        }

        @Test
        void 타입_파라미터는_명시할_수도_있고_추론에_맡길_수도_있다() {
            // 타입 추론
            String inferred = getFirst(List.of("hello"));

            // 명시적 타입 지정 (보통 필요 없지만 가능)
            String explicit = GenericBasicTest.<String>getFirst(List.of("hello"));

            assertThat(inferred).isEqualTo(explicit);
        }

        @Test
        void 제네릭_메서드와_제네릭_클래스의_타입_파라미터는_독립적이다() {
            Box<String> stringBox = new Box<>();

            // Box<String>이지만, transform 메서드는 다른 타입으로 변환 가능
            Integer length = stringBox.transform("hello", String::length);

            assertThat(length).isEqualTo(5);
        }
    }

    @Nested
    class 제네릭의_불공변성_Invariance {

        /**
         * 핵심 질문: 왜 List<Integer>를 List<Object>에 대입할 수 없는가?
         *
         * 답: 타입 안전성을 보장하기 위해서다.
         * 만약 가능하다면, List<Integer>에 String을 넣을 수 있게 되어버린다.
         */
        @Test
        void 제네릭은_불공변이므로_하위타입_관계가_성립하지_않는다() {
            List<Integer> integers = new ArrayList<>();
            integers.add(1);

            // 컴파일 에러! List<Integer>는 List<Object>의 하위 타입이 아님
            // List<Object> objects = integers;

            // 만약 위 코드가 가능했다면...
            // objects.add("hello"); // Object니까 String 넣기 가능
            // Integer num = integers.get(1); // String을 Integer로? ClassCastException

            assertThat(integers).containsExactly(1);
        }

        @Test
        void 반면_배열은_공변이므로_하위타입_관계가_성립한다() {
            Integer[] integers = {1, 2, 3};

            // 컴파일 OK! Integer[]는 Object[]의 하위 타입
            Object[] objects = integers;

            // 하지만 런타임에 문제 발생!
            assertThatThrownBy(() -> {
                        objects[0] = "hello"; // ArrayStoreException
                    })
                    .isInstanceOf(ArrayStoreException.class);
        }

        @Test
        void 배열의_공변성은_런타임_예외를_유발하지만_제네릭의_불공변성은_컴파일_타임에_잡아낸다() {
            // 배열: 컴파일 OK, 런타임 에러 가능
            Object[] objectArray = new Integer[3];
            // objectArray[0] = "string"; // ArrayStoreException at runtime

            // 제네릭: 컴파일 에러로 사전 차단
            // List<Object> objectList = new ArrayList<Integer>(); // 컴파일 에러

            // 제네릭이 더 안전하다 - 문제를 컴파일 타임에 발견
            assertThat(true).isTrue();
        }
    }

    @Nested
    class Bounded_타입_파라미터 {

        @Test
        void extends로_상한_경계를_지정할_수_있다() {
            // T extends Number: T는 Number 또는 그 하위 타입이어야 함
            NumberBox<Integer> intBox = new NumberBox<>(42);
            NumberBox<Double> doubleBox = new NumberBox<>(3.14);

            // NumberBox<String> stringBox = new NumberBox<>("hello"); // 컴파일 에러!

            assertThat(intBox.doubleValue()).isEqualTo(42.0);
            assertThat(doubleBox.doubleValue()).isEqualTo(3.14);
        }

        @Test
        void 다중_경계를_지정할_수_있다() {
            // T extends Number & Comparable<T>
            ComparableNumberBox<Integer> box1 = new ComparableNumberBox<>(10);
            ComparableNumberBox<Integer> box2 = new ComparableNumberBox<>(20);

            assertThat(box1.isLessThan(box2)).isTrue();
        }
    }

    // === 헬퍼 클래스 ===

    static class Box<T> {
        private T value;

        public void set(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        // 제네릭 메서드: 클래스의 T와 독립적인 R
        public <R> R transform(T input, java.util.function.Function<T, R> transformer) {
            return transformer.apply(input);
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

    static class NumberBox<T extends Number> {
        private final T value;

        public NumberBox(T value) {
            this.value = value;
        }

        public double doubleValue() {
            return value.doubleValue(); // Number의 메서드 사용 가능
        }
    }

    static class ComparableNumberBox<T extends Number & Comparable<T>> {
        private final T value;

        public ComparableNumberBox(T value) {
            this.value = value;
        }

        public boolean isLessThan(ComparableNumberBox<T> other) {
            return this.value.compareTo(other.value) < 0;
        }
    }

    static <T> T getFirst(List<T> list) {
        return list.isEmpty() ? null : list.getFirst();
    }
}
