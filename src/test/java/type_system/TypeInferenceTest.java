package type_system;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 타입 추론 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. var (Java 10+): 지역 변수의 타입을 컴파일러가 추론
 * 2. 다이아몬드 연산자 (Java 7+): 제네릭 인스턴스 생성 시 타입 생략
 * 3. 메서드 타입 추론: 제네릭 메서드 호출 시 타입 파라미터 추론
 * 4. 람다와 메서드 참조의 타입 추론
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class TypeInferenceTest {

    @Nested
    class var_키워드_Java_10 {

        @Test
        void var는_지역_변수의_타입을_추론한다() {
            var message = "Hello"; // String으로 추론
            var count = 42; // int로 추론
            var pi = 3.14; // double로 추론
            var flag = true; // boolean으로 추론

            assertThat(message).isInstanceOf(String.class);
            assertThat(count).isInstanceOf(Integer.class);
            assertThat(pi).isInstanceOf(Double.class);
            assertThat(flag).isInstanceOf(Boolean.class);
        }

        @Test
        void var는_컬렉션_타입도_추론한다() {
            var list = List.of("a", "b", "c"); // List<String>
            var map = Map.of("key", 1); // Map<String, Integer>
            var set = java.util.Set.of(1, 2, 3); // Set<Integer>

            assertThat(list.get(0)).isInstanceOf(String.class);
            assertThat(map.get("key")).isInstanceOf(Integer.class);
        }

        @Test
        void var는_복잡한_제네릭_타입에서_가독성을_높인다() {
            // var 없이: 타입 선언이 장황함
            Map<String, List<Map<String, Integer>>> withoutVar = new HashMap<String, List<Map<String, Integer>>>();

            // var 사용: 훨씬 간결함
            var withVar = new HashMap<String, List<Map<String, Integer>>>();

            assertThat(withVar).isEmpty();
        }

        @Test
        void var는_스트림_파이프라인에서_중간_결과를_저장할_때_유용하다() {
            var names = List.of("Alice", "Bob", "Charlie", "David");

            // 복잡한 중간 타입을 명시하지 않아도 됨
            var nameLengthMap = names.stream().collect(Collectors.toMap(Function.identity(), String::length));
            // Map<String, Integer>로 추론됨

            assertThat(nameLengthMap.get("Alice")).isEqualTo(5);
        }

        @Test
        void var는_for_each_루프에서_사용_가능하다() {
            var numbers = List.of(1, 2, 3, 4, 5);
            var sum = 0;

            for (var number : numbers) {
                sum += number;
            }

            assertThat(sum).isEqualTo(15);
        }

        @Test
        void var는_try_with_resources에서_사용_가능하다() throws Exception {
            var content = "";

            try (var reader = new java.io.StringReader("Hello")) {
                var buffer = new char[5];
                reader.read(buffer);
                content = new String(buffer);
            }

            assertThat(content).isEqualTo("Hello");
        }
    }

    @Nested
    class var_사용_불가_케이스 {

        @Test
        void var는_초기화_없이_사용할_수_없다() {
            // 컴파일 에러:
            // 추론할 타입 정보가 없음
            // var unintialized;
            // unintialized = "hello";

            // 반드시 선언과 동시에 초기화
            var initialized = "hello";
            assertThat(initialized).isEqualTo("hello");
        }

        @Test
        void var는_null로_초기화할_수_없다() {
            // 컴파일 에러
            // - null만으로는 타입 추론 불가
            // var nullVar = null;

            // 명시적 캐스팅으로 타입 힌트 제공 가능
            var nullable = (String) null;
            assertThat(nullable).isNull();
        }

        @Test
        void var는_람다_표현식에_직접_사용할_수_없다() {
            // 컴파일 에러:
            // - 람다의 타입은 컨텍스트에서 결정됨
            // var lambda = (x) -> x * 2;

            // 타겟 타입을 명시하면 가능
            var lambda = (Function<Integer, Integer>) x -> x * 2;

            assertThat(lambda.apply(5)).isEqualTo(10);
        }

        @Test
        void var는_배열_초기화에_직접_사용할_수_없다() {
            // 컴파일 에러
            // var arr = {1, 2, 3};

            // 명시적으로 new 사용하면 가능
            var arr = new int[] {1, 2, 3};

            assertThat(arr).containsExactly(1, 2, 3);
        }

        @Test
        void var는_필드에_사용할_수_없다() {
            // class Example {
            //     var field = "hello"; // 컴파일 에러
            // }

            // var는 지역 변수 전용
        }

        @Test
        void var는_메서드_파라미터에_사용할_수_없다() {
            // void method(var param) { } // 컴파일 에러

            // 파라미터는 명시적 타입 필요
        }

        @Test
        void var는_반환_타입에_사용할_수_없다() {
            // var method() { return "hello"; } // 컴파일 에러

            // 반환 타입은 명시적으로 지정해야 함
        }
    }

    @Nested
    class var_사용_가이드라인 {

        @Test
        void 타입이_명확할_때_var_사용() {
            // 좋은 예: 우측에서 타입이 명확히 보임
            var list = new ArrayList<String>();
            var map = new HashMap<String, Integer>();
            var name = "John";

            assertThat(list).isEmpty();
            assertThat(map).isEmpty();
            assertThat(name).isEqualTo("John");
        }

        @Test
        void 타입이_불명확할_때_var_사용을_피해야_한다() {
            // 안 좋은 예: result가 무슨 타입인지 불명확
            // var result = service.process(data);

            // 좋은 예: 타입 명시로 코드 가독성 향상
            // ProcessResult result = service.process(data);
        }

        @Test
        void 의미_있는_변수명으로_var의_단점을_보완() {
            var userNames = List.of("Alice", "Bob"); // 복수형 -> List
            var userNameToAge = Map.of("Alice", 30); // To -> Map
            var activeUserCount = 42; // Count -> 숫자형

            assertThat(userNames).hasSize(2);
            assertThat(userNameToAge).containsKey("Alice");
            assertThat(activeUserCount).isEqualTo(42);
        }
    }

    @Nested
    class 다이아몬드_연산자_Java_7 {

        @Test
        void 다이아몬드_연산자로_제네릭_인스턴스_생성_시_타입_생략() {
            // Java 7 이전
            List<String> oldStyle = new ArrayList<String>();

            // Java 7+: 다이아몬드 연산자
            List<String> newStyle = new ArrayList<>();

            // 컴파일러가 좌측 선언부를 보고 타입 추론
            assertThat(oldStyle.getClass()).isEqualTo(newStyle.getClass());
        }

        @Test
        void 중첩_제네릭에서_다이아몬드_연산자_효과() {
            // 타입 선언이 장황한 경우
            Map<String, List<Integer>> oldStyle = new HashMap<String, List<Integer>>();

            // 다이아몬드 연산자로 간결하게
            Map<String, List<Integer>> newStyle = new HashMap<>();

            assertThat(newStyle).isEmpty();
        }

        @Test
        void 익명_클래스에서_다이아몬드_연산자_사용_Java_9() {
            // Java 9부터 익명 클래스에서도 다이아몬드 연산자 사용 가능
            java.util.Comparator<String> comparator = new java.util.Comparator<>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.length() - s2.length();
                }
            };

            assertThat(comparator.compare("ab", "abc")).isLessThan(0);
        }
    }

    @Nested
    class 메서드_타입_추론 {

        @Test
        void 제네릭_메서드는_인자로부터_타입을_추론한다() {
            // 타입 파라미터를 명시하지 않아도 추론됨
            String first = firstElement(List.of("a", "b", "c"));
            Integer firstNum = firstElement(List.of(1, 2, 3));

            assertThat(first).isEqualTo("a");
            assertThat(firstNum).isEqualTo(1);
        }

        @Test
        void 반환_타입_컨텍스트로부터_타입을_추론한다() {
            // 좌변의 타입으로부터 추론
            List<String> emptyStrings = emptyList();
            List<Integer> emptyIntegers = emptyList();

            assertThat(emptyStrings).isEmpty();
            assertThat(emptyIntegers).isEmpty();
        }

        @Test
        void 체이닝된_메서드에서의_타입_추론() {
            var result = emptyList().stream().map(Object::toString).toList();

            // Object로 추론됨 (컨텍스트 정보 부족)
            assertThat(result).isEmpty();
        }

        @Test
        void 명시적_타입_힌트가_필요한_경우() {
            // 컴파일러가 추론하기 어려운 경우 명시적으로 타입 지정
            List<String> explicitType = GenericMethodExample.<String>emptyList();

            // 또는 타겟 타입을 통한 힌트
            List<String> targetType = emptyList();
            String first = targetType.isEmpty() ? "default" : targetType.getFirst();

            assertThat(explicitType).isEmpty();
            assertThat(first).isEqualTo("default");
        }

        private <T> T firstElement(List<T> list) {
            return list.isEmpty() ? null : list.getFirst();
        }

        private <T> List<T> emptyList() {
            return new ArrayList<>();
        }
    }

    static class GenericMethodExample {
        public static <T> List<T> emptyList() {
            return new ArrayList<>();
        }
    }
}
