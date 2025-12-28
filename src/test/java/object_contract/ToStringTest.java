package object_contract;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * toString 메서드 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. toString은 객체의 유익한 문자열 표현을 반환해야 한다
 * 2. 디버깅과 로깅에 매우 유용하다
 * 3. 포맷을 문서화할지 여부를 결정해야 한다
 * 4. 핵심 정보를 모두 포함하되, 너무 길지 않게 해야 한다
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ToStringTest {

    @Nested
    class toString_기본_동작 {

        @Test
        void Object의_기본_toString은_클래스명과_해시코드를_반환() {
            Object obj = new Object();
            String str = obj.toString();

            // 형식: 클래스명@16진수해시코드
            assertThat(str).matches("java\\.lang\\.Object@[0-9a-f]+");
        }

        @Test
        void 재정의하지_않으면_유용한_정보를_얻을_수_없다() {
            NoToString person = new NoToString("John", 25);
            String str = person.toString();

            // 객체 내용이 아닌 클래스명@해시코드
            assertThat(str).doesNotContain("John");
            assertThat(str).doesNotContain("25");
            assertThat(str).matches(".*NoToString@[0-9a-f]+");
        }

        @Test
        void toString을_재정의하면_유용한_정보를_얻을_수_있다() {
            Person person = new Person("John", 25);
            String str = person.toString();

            assertThat(str).contains("John");
            assertThat(str).contains("25");
        }
    }

    // === 테스트용 헬퍼 클래스들 ===

    static class NoToString {
        private final String name;
        private final int age;

        public NoToString(String name, int age) {
            this.name = name;
            this.age = age;
        }
        // toString 재정의 안함
    }

    static class Person {
        private final String name;
        private final int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Person p)) return false;
            return age == p.age && name.equals(p.name);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + age;
        }
    }
}
