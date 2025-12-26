package object_contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * equals와 hashCode 계약 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. equals 메서드는 동치 관계(equivalence relation)를 구현해야 한다
 * 2. equals를 재정의하면 hashCode도 반드시 재정의해야 한다
 * 3. hashCode 계약을 위반하면 Hash 기반 컬렉션에서 문제가 발생한다
 * 4. equals 구현 시 흔히 저지르는 실수들을 알아야 한다
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class EqualsHashCodeTest {

    @Nested
    class equals_기본_계약 {

        /**
         * equals 메서드가 만족해야 하는 동치 관계:
         * 1. 반사성(Reflexive): x.equals(x) == true
         * 2. 대칭성(Symmetric): x.equals(y) == y.equals(x)
         * 3. 추이성(Transitive): x.equals(y) && y.equals(z) → x.equals(z)
         * 4. 일관성(Consistent): 변경 없으면 항상 같은 결과
         * 5. null-아님: x.equals(null) == false
         */
        @Test
        void 반사성_자기_자신과_같아야_한다() {
            Person person = new Person("John", 25);

            assertThat(person.equals(person)).isTrue();
        }

        @Test
        void 대칭성_양방향으로_같아야_한다() {
            Person p1 = new Person("John", 25);
            Person p2 = new Person("John", 25);

            assertThat(p1.equals(p2)).isTrue();
            assertThat(p2.equals(p1)).isTrue();
        }

        @Test
        void 추이성_연쇄적으로_같아야_한다() {
            Person p1 = new Person("John", 25);
            Person p2 = new Person("John", 25);
            Person p3 = new Person("John", 25);

            assertThat(p1.equals(p2)).isTrue();
            assertThat(p2.equals(p3)).isTrue();
            assertThat(p1.equals(p3)).isTrue(); // 추이성
        }

        @Test
        void 일관성_반복_호출해도_결과가_같아야_한다() {
            Person p1 = new Person("John", 25);
            Person p2 = new Person("John", 25);

            // 여러 번 호출해도 동일한 결과
            for (int i = 0; i < 100; i++) {
                assertThat(p1.equals(p2)).isTrue();
            }
        }

        @Test
        void null과_비교하면_false를_반환해야_한다() {
            Person person = new Person("John", 25);

            assertThat(person.equals(null)).isFalse();
        }
    }

    @Nested
    class equals_대칭성_위반_사례 {
        // -- 다른 타입과 비교하려고 욕심부리지 마라가 핵심 --

        /**
         * 대칭성 위반의 대표적 사례:
         * 상위 클래스와 하위 클래스 간의 equals 비교
         */
        @Test
        void 대칭성_위반_예시_CaseInsensitiveString() {
            CaseInsensitiveString cis = new CaseInsensitiveString("Hello");
            String s = "hello";

            // 대칭성 위반
            // cis.equals(s) == true (CaseInsensitiveString이 String과 비교)
            // s.equals(cis) == false (String은 CaseInsensitiveString을 모름)
            assertThat(cis.equals(s)).isTrue();
            assertThat(s.equals(cis)).isFalse(); // 대칭성 위반
        }

        @Test
        void 대칭성_위반시_컬렉션에서_예측_불가능한_동작() {
            CaseInsensitiveString cis = new CaseInsensitiveString("Hello");
            String s = "hello";

            Set<Object> set = new HashSet<>();
            set.add(cis);

            // 구현에 따라 결과가 달라질 수 있음 - 예측 불가능
            // JDK 버전, HashSet 구현에 따라 true/false가 달라질 수 있다
            boolean contains = set.contains(s);

            // 이런 불확실성이 문제
            assertThat(contains).isIn(true, false);
        }
    }

    // === 테스트용 헬퍼 클래스들 ===

    /**
     * 올바르게 equals와 hashCode를 구현한 클래스
     */
    static class Person {
        private final String name;
        private final int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Person person)) return false;
            return age == person.age && Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    /**
     * 대칭성 위반 예시
     */
    static class CaseInsensitiveString {
        private final String s;

        public CaseInsensitiveString(String s) {
            this.s = Objects.requireNonNull(s);
        }

        // 잘못된 구현: String과의 비교를 시도
        @Override
        public boolean equals(Object o) {
            if (o instanceof CaseInsensitiveString cis) {
                return s.equalsIgnoreCase(cis.s);
            }
            if (o instanceof String str) { // 대칭성 위반의 원인
                return s.equalsIgnoreCase(str);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return s.toLowerCase().hashCode();
        }
    }
}
