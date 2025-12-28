package object_contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.StringJoiner;
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

    @Nested
    class toString_구현_패턴 {

        @Test
        void 간결한_형식_모든_핵심_정보_포함() {
            PersonSimple person = new PersonSimple("John", 25, "john@email.com");

            assertThat(person.toString()).isEqualTo("PersonSimple{name='John', age=25, email='john@email.com'}");
        }

        @Test
        void 포맷_명세를_문서화한_경우() {
            PhoneNumber phone = new PhoneNumber(82, 10, 12345678);

            // 포맷이 명세되어 있으면 파싱에 활용 가능
            assertThat(phone.toString()).isEqualTo("+82-10-12345678");
        }

        @Test
        void StringJoiner를_사용한_깔끔한_구현() {
            PersonWithJoiner person = new PersonWithJoiner("John", 25, "Seoul");

            assertThat(person.toString()).isEqualTo("PersonWithJoiner[name=John, age=25, city=Seoul]");
        }

        @Test
        void StringBuilder를_사용한_성능_최적화_구현() {
            PersonWithBuilder person = new PersonWithBuilder("John", 25);

            assertThat(person.toString()).isEqualTo("PersonWithBuilder{name='John', age=25}");
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

    static class PersonSimple {
        private final String name;
        private final int age;
        private final String email;

        public PersonSimple(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        @Override
        public String toString() {
            return "PersonSimple{" + "name='" + name + '\'' + ", age=" + age + ", email='" + email + '\'' + '}';
        }
    }

    /**
     * 포맷이 명세된 클래스
     */
    static class PhoneNumber {
        private final int countryCode;
        private final int areaCode;
        private final int number;

        public PhoneNumber(int countryCode, int areaCode, int number) {
            this.countryCode = countryCode;
            this.areaCode = areaCode;
            this.number = number;
        }

        /**
         * 전화번호의 문자열 표현을 반환합니다.
         * 형식: +국가코드-지역번호-전화번호
         * 예시: +82-10-12345678
         */
        @Override
        public String toString() {
            return String.format("+%d-%d-%d", countryCode, areaCode, number);
        }

        /**
         * 문자열로부터 PhoneNumber를 생성합니다.
         * @param str "+국가코드-지역번호-전화번호" 형식의 문자열
         */
        public static PhoneNumber valueOf(String str) {
            String[] parts = str.substring(1).split("-"); // +제거 후 분할
            return new PhoneNumber(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PhoneNumber p)) return false;
            return countryCode == p.countryCode && areaCode == p.areaCode && number == p.number;
        }

        @Override
        public int hashCode() {
            return 31 * 31 * countryCode + 31 * areaCode + number;
        }
    }

    static class PersonWithJoiner {
        private final String name;
        private final int age;
        private final String city;

        public PersonWithJoiner(String name, int age, String city) {
            this.name = name;
            this.age = age;
            this.city = city;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", "PersonWithJoiner[", "]")
                    .add("name=" + name)
                    .add("age=" + age)
                    .add("city=" + city)
                    .toString();
        }
    }

    static class PersonWithBuilder {
        private final String name;
        private final int age;

        public PersonWithBuilder(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("PersonWithBuilder{")
                    .append("name='")
                    .append(name)
                    .append('\'')
                    .append(", age=")
                    .append(age)
                    .append('}')
                    .toString();
        }
    }
}
