package type_system;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 제네릭 타입 소거 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. 타입 소거(Type Erasure)란 컴파일 타임에만 제네릭 타입 정보가 존재하고,
 *    런타임에는 소거되는 것을 말한다.
 * 2. Java 제네릭은 하위 호환성을 위해 타입 소거 방식으로 구현되었다.
 * 3. 타입 소거로 인해 발생하는 제약사항들을 이해해야 한다.
 *
 * 분명히 타입을 명시했는데 왜 무시당해야 하나라는 의문이 들 수 있다.
 * Java는 레거시 호환성을 위해 "최고"가 아니라 "최선"의 선택을 했다.
 * Java의 타입 소거는 20년전의 기술 부채가 지금까지 이어지는 대표적 사례이다.
 *
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class GenericTypeErasureTest {

    @Nested
    class 타입_소거의_기본_원리 {

        @Test
        void 런타임에는_제네릭_타입_정보가_사라진다() {
            List<String> stringList = new ArrayList<>();
            List<Integer> integerList = new ArrayList<>();

            // 런타임에는 둘 다 그냥 ArrayList
            assertThat(stringList.getClass()).isEqualTo(integerList.getClass());
            assertThat(stringList.getClass().getName()).isEqualTo("java.util.ArrayList");
        }

        @Test
        void instanceof_검사에서_제네릭_타입_파라미터를_사용할_수_없다() {
            List<String> list = new ArrayList<>();

            // 컴파일 에러: 타입 소거로 인해 런타임에 타입 정보 없음
            // if (list instanceof List<String>) { }

            // 이것만 가능
            assertThat(list instanceof List<?>).isTrue();
            assertThat(list instanceof ArrayList<?>).isTrue();
        }

        @Test
        void Raw_타입으로_캐스팅하면_타입_안전성이_깨질_수_있다() {
            List<String> stringList = new ArrayList<>();
            stringList.add("hello");

            // Raw 타입으로 캐스팅 (경고 발생하지만 컴파일됨)
            @SuppressWarnings("unchecked")
            List<Integer> integerList = (List<Integer>) (List<?>) stringList;

            // 런타임에 타입 정보가 없으므로 캐스팅 성공!
            // 하지만 실제 사용 시 ClassCastException
            assertThatThrownBy(() -> {
                        Integer first = integerList.get(0); // ClassCastException!
                    })
                    .isInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class 타입_소거로_인한_제약사항 {

        @Test
        void 제네릭_타입으로_인스턴스를_생성할_수_없다() {
            // new T()가 불가능한 이유:
            // 런타임에 T가 무엇인지 모르기 때문

            // 컴파일 에러
            // T instance = new T();

            // 대안: Class<T> 또는 Supplier<T>를 받아서 생성
            Box<String> box = new Box<>(String::new);
            assertThat(box.createNew()).isEqualTo("");
        }

        @Test
        void 제네릭_타입으로_배열을_생성할_수_없다() {
            // 컴파일 에러
            // T[] array = new T[10];

            // 배열은 런타임에 타입 정보를 유지하는데,
            // 제네릭은 타입 소거로 런타임에 타입 정보가 없어서 충돌

            // 대안 1: Object 배열 사용 후 캐스팅 (안전하지 않음)
            // 대안 2: Array.newInstance 사용
            // 대안 3: List 사용 (권장)

            GenericArray<String> arr = new GenericArray<>(String.class, 5);
            arr.set(0, "hello");
            assertThat(arr.get(0)).isEqualTo("hello");
        }

        @Test
        void 같은_제네릭_클래스의_다른_타입_파라미터로_오버로딩할_수_없다() {
            // 컴파일 에러:
            // - 타입 소거 후 시그니처가 동일해짐
            // void process(List<String> list) { }
            // void process(List<Integer> list) { }

            // 타입 소거 후 둘 다:
            // void process(List list) { }
            // 가 되어 메서드 시그니처 충돌!

            Processor processor = new Processor();
            assertThat(processor.processStrings(List.of("a"))).isEqualTo("String: a");
            assertThat(processor.processIntegers(List.of(1))).isEqualTo("Integer: 1");
        }

        @Test
        void 제네릭_예외_클래스를_만들_수_없다() {
            // 컴파일 에러:
            // class GenericException<T> extends Exception { }

            // 왜? catch 블록에서 타입 파라미터를 구분할 수 없음
            // catch (GenericException<String> e) { }  // 불가능
            // catch (GenericException<Integer> e) { } // 불가능

            // 타입 소거 후 둘 다 GenericException이 됨
        }

        @Test
        void static_컨텍스트에서_타입_파라미터를_사용할_수_없다() {
            // class Box<T> {
            //     static T value;           // 컴파일 에러
            //     static void process(T t); // 컴파일 에러
            // }

            // 왜? static 멤버는 모든 인스턴스가 공유하는데,
            // 타입 파라미터는 인스턴스마다 다를 수 있음
            // Box<String>, Box<Integer>가 같은 static 필드를 공유해야 하는데
            // T가 String인지 Integer인지 정할 수 없음
        }
    }

    @Nested
    class 타입_소거의_구체적_동작 {

        @Test
        void 비한정_타입_파라미터는_Object로_소거된다() throws Exception {
            // class Container<T> { T value; }
            // 소거 후: class Container { Object value; }

            Class<?> clazz = Container.class;
            java.lang.reflect.Field field = clazz.getDeclaredField("value");

            // 런타임에는 Object 타입
            assertThat(field.getType()).isEqualTo(Object.class);
        }

        @Test
        void 한정_타입_파라미터는_첫_번째_경계_타입으로_소거된다() throws Exception {
            // class NumberContainer<T extends Number> { T value; }
            // 소거 후: class NumberContainer { Number value; }

            Class<?> clazz = NumberContainer.class;
            java.lang.reflect.Field field = clazz.getDeclaredField("value");

            // 런타임에는 Number 타입
            assertThat(field.getType()).isEqualTo(Number.class);
        }

        @Test
        void 다중_경계의_경우_첫_번째_경계로_소거된다() throws Exception {
            // class MultiContainer<T extends Number & Comparable<T>> { T value; }
            // 소거 후: class MultiContainer { Number value; }

            Class<?> clazz = MultiContainer.class;
            java.lang.reflect.Field field = clazz.getDeclaredField("value");

            // 첫 번째 경계인 Number로 소거
            assertThat(field.getType()).isEqualTo(Number.class);
        }

        @Test
        void 컴파일러는_필요한_곳에_자동으로_캐스트를_삽입한다() {
            Container<String> container = new Container<>();
            container.value = "hello";

            // 우리 코드: String s = container.value;
            // 컴파일 후: String s = (String) container.value;

            String value = container.value;
            assertThat(value).isEqualTo("hello");
        }
    }

    @Nested
    class 타입_소거_우회_방법 {

        @Test
        void Class_객체를_통한_타입_정보_전달() {
            TypeAwareContainer<String> container = new TypeAwareContainer<>(String.class);
            container.setValue("hello");

            assertThat(container.getType()).isEqualTo(String.class);
            assertThat(container.getValue()).isEqualTo("hello");

            // 타입 안전한 인스턴스 생성 가능
            String newInstance = container.createNew();
            assertThat(newInstance).isEqualTo("");
        }

        @Test
        void 익명_클래스를_통한_타입_토큰_패턴() {
            // 슈퍼 타입 토큰 패턴
            TypeReference<List<String>> typeRef = new TypeReference<>() {};

            Type type = typeRef.getType();
            assertThat(type.getTypeName()).isEqualTo("java.util.List<java.lang.String>");
        }

        @Test
        void 리플렉션으로_제네릭_메서드_시그니처_조회() throws Exception {
            Method method = TypeErasureExample.class.getMethod("process", List.class);

            // 파라미터의 제네릭 타입 정보는 메타데이터로 유지됨
            Type[] paramTypes = method.getGenericParameterTypes();
            ParameterizedType paramType = (ParameterizedType) paramTypes[0];

            assertThat(paramType.getRawType()).isEqualTo(List.class);
            assertThat(paramType.getActualTypeArguments()[0]).isEqualTo(String.class);
        }
    }

    @Nested
    class 브릿지_메서드 {

        // 타입 소거로 인해 런타임에 끊어질 오버라이드 관계를
        // 컴파일러가 컴파일 시점에 브릿지 메서드를 생성해서 복구한다
        @Test
        void 컴파일러는_타입_소거로_인한_다형성_유지를_위해_브릿지_메서드를_생성한다() throws Exception {
            // IntegerNode는 Node<Integer>를 상속
            // Node<Integer>의 setData(Integer)가 타입 소거 후
            // setData(Object)가 되므로 브릿지 메서드 필요

            Class<?> clazz = IntegerNode.class;
            Method[] methods = clazz.getDeclaredMethods();

            // setData가 2개: 원본 + 브릿지 메서드
            long setDataCount = java.util.Arrays.stream(methods)
                    .filter(m -> m.getName().equals("setData"))
                    .count();

            assertThat(setDataCount).isEqualTo(2);

            // 브릿지 메서드 확인
            Method bridgeMethod = clazz.getMethod("setData", Object.class);
            assertThat(bridgeMethod.isBridge()).isTrue();

            // 실제 메서드
            Method realMethod = clazz.getMethod("setData", Integer.class);
            assertThat(realMethod.isBridge()).isFalse();
        }

        @Test
        void 브릿지_메서드는_다형성_호출을_실제_메서드로_위임한다() {
            Node<Integer> node = new IntegerNode();
            node.setData(42); // Object 버전이 호출되지만, 브릿지가 Integer 버전으로 위임

            assertThat(node.getData()).isEqualTo(42);
        }
    }

    // === 테스트용 헬퍼 클래스들 ===

    static class Box<T> {
        private final java.util.function.Supplier<T> supplier;

        public Box(java.util.function.Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T createNew() {
            return supplier.get();
        }
    }

    static class GenericArray<T> {
        private final T[] array;

        @SuppressWarnings("unchecked")
        public GenericArray(Class<T> clazz, int size) {
            array = (T[]) java.lang.reflect.Array.newInstance(clazz, size);
        }

        public void set(int index, T value) {
            array[index] = value;
        }

        public T get(int index) {
            return array[index];
        }
    }

    static class Processor {
        public String processStrings(List<String> list) {
            return "String: " + list.get(0);
        }

        public String processIntegers(List<Integer> list) {
            return "Integer: " + list.get(0);
        }
    }

    static class Container<T> {
        T value;
    }

    static class NumberContainer<T extends Number> {
        T value;
    }

    static class MultiContainer<T extends Number & Comparable<T>> {
        T value;
    }

    static class TypeAwareContainer<T> {
        private final Class<T> type;
        private T value;

        public TypeAwareContainer(Class<T> type) {
            this.type = type;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public Class<T> getType() {
            return type;
        }

        public T createNew() {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    abstract static class TypeReference<T> {
        private final Type type;

        protected TypeReference() {
            Type superClass = getClass().getGenericSuperclass();
            type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }

        public Type getType() {
            return type;
        }
    }

    /**
     * 슈퍼 타입 토큰 패턴
     * 익명 클래스를 통해 제네릭 타입 정보를 런타임에 유지
     */
    static class TypeErasureExample {
        public void process(List<String> list) {}
    }

    // 브릿지 메서드 예시용 클래스
    static class Node<T> {
        private T data;

        public void setData(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }

    static class IntegerNode extends Node<Integer> {
        @Override
        public void setData(Integer data) {
            super.setData(data);
        }
    }
}
