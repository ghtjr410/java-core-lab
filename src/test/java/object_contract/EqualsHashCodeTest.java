package object_contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
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

    @Nested
    class equals_추이성_위반_사례 {
        // 상속하면서 필드 추가하면 equals 계약 지키기 불가능하다.
        // 욕심부리지말고 컴포지션 써라가 핵심

        /**
         * 추이성 위반의 대표적 사례:
         * 상속 관계에서 필드를 추가할 때 발생
         */
        @Test
        void 추이성_위반_예시_상속과_필드_추가() {
            Point p1 = new Point(1, 2);
            ColorPoint cp1 = new ColorPoint(1, 2, "red");
            ColorPoint cp2 = new ColorPoint(1, 2, "blue");

            // ColorPoint.equals가 Point와 비교할 때 color를 무시한다면:
            // p1.equals(cp1) == true (좌표만 비교)
            // p1.equals(cp2) == true (좌표만 비교)
            // 그런데 cp1.equals(cp2) == false (색상이 다름)
            // → 추이성 위반

            // 이 테스트는 "잘못된 구현"을 보여주는 것
            assertThat(p1.equals(cp1)).isTrue();
            assertThat(p1.equals(cp2)).isTrue();
            assertThat(cp1.equals(cp2)).isFalse(); // 추이성 위반
        }

        @Test
        void 해결책_상속_대신_컴포지션_사용() {
            Point p = new Point(1, 2);
            ColorPointComposition cp1 = new ColorPointComposition(new Point(1, 2), "red");
            ColorPointComposition cp2 = new ColorPointComposition(new Point(1, 2), "blue");

            // 서로 다른 타입이므로 비교 자체가 false
            assertThat(p.equals(cp1)).isFalse();
            assertThat(cp1.equals(p)).isFalse();

            // 같은 타입끼리만 비교
            assertThat(cp1.equals(cp2)).isFalse(); // 색상이 다름

            // Point 비교가 필요하면 명시적으로 추출
            assertThat(cp1.asPoint()).isEqualTo(cp2.asPoint());
        }
    }

    @Nested
    class hashCode_계약 {

        /**
         * hashCode 계약:
         * 1. equals가 true인 두 객체는 반드시 같은 hashCode를 가져야 한다
         * 2. equals가 false인 두 객체가 다른 hashCode를 가질 필요는 없다 (권장사항)
         * 3. 같은 객체에 대해 hashCode를 여러 번 호출해도 같은 값을 반환해야 한다
         */
        @Test
        void equals가_true면_hashCode도_같아야_한다() {
            Person p1 = new Person("John", 25);
            Person p2 = new Person("John", 25);

            assertThat(p1.equals(p2)).isTrue();
            assertThat(p1.hashCode()).isEqualTo(p2.hashCode()); // 필수
        }

        @Test
        void hashCode가_달라도_equals는_false일_수_있다() {
            Person p1 = new Person("John", 25);
            Person p2 = new Person("Jane", 30);

            // hashCode가 다르면 equals도 false (최적화 가능)
            // 하지만 hashCode가 같아도 equals가 false일 수 있음 (충돌)
            assertThat(p1.equals(p2)).isFalse();
            // hashCode는 같을 수도 다를 수도 있음
        }
    }

    @Nested
    class hashCode_미구현시_문제 {

        // hashCode 재정의 안 함
        // → 주소값 기반
        // → 같은 값이어도 다른 hashCode
        // → HashMap/HashSet에서 못 찾음

        // hashCode 필드값 기반으로 재정의
        // → 같은 값이면 같은 hashCode
        // → HashMap/HashSet에서 정상 동작

        /**
         * 핵심 질문: equals를 재정의하면 hashCode도 재정의해야 하는 이유는?
         *
         * 답: Hash 기반 컬렉션(HashMap, HashSet, Hashtable)이
         * hashCode를 사용해 버킷을 결정하기 때문이다.
         * hashCode가 다르면 equals를 호출하지도 않고 다른 객체로 판단한다.
         */
        @Test
        void hashCode_미구현시_HashMap에서_객체를_찾을_수_없다() {
            // hashCode를 구현하지 않은 클래스
            BadPerson p1 = new BadPerson("John", 25);
            BadPerson p2 = new BadPerson("John", 25);

            assertThat(p1.equals(p2)).isTrue(); // equals는 true

            Map<BadPerson, String> map = new HashMap<>();
            map.put(p1, "person1");

            // p1과 p2는 equals가 true지만, hashCode가 다르므로 찾지 못함
            String value = map.get(p2);
            assertThat(value).isNull(); // 찾지 못함
        }

        @Test
        void hashCode_미구현시_HashSet에서_중복_판단_실패() {
            BadPerson p1 = new BadPerson("John", 25);
            BadPerson p2 = new BadPerson("John", 25);

            Set<BadPerson> set = new HashSet<>();
            set.add(p1);
            set.add(p2); // 논리적으로 같은 객체

            // equals는 true인데 hashCode가 달라서 중복으로 판단하지 않음
            assertThat(set).hasSize(2); // 2개로 저장됨
        }

        @Test
        void hashCode_올바르게_구현시_정상_동작() {
            Person p1 = new Person("John", 25);
            Person p2 = new Person("John", 25);

            Map<Person, String> map = new HashMap<>();
            map.put(p1, "person1");

            // hashCode도 같으므로 정상적으로 찾음
            String value = map.get(p2);
            assertThat(value).isEqualTo("person1");

            Set<Person> set = new HashSet<>();
            set.add(p1);
            set.add(p2);

            assertThat(set).hasSize(1); // 중복 제거됨
        }
    }

    @Nested
    class hashCode_구현_전략 {

        @Test
        void Objects_hash_사용_권장() {
            Person person = new Person("John", 25);

            // Objects.hash()는 내부적으로 Arrays.hashCode() 사용
            int expected = Objects.hash("John", 25);
            assertThat(person.hashCode()).isEqualTo(expected);
        }

        @Test
        void 성능이_중요하면_직접_계산() {
            PersonOptimized person = new PersonOptimized("John", 25);

            // 31을 곱하는 이유: 홀수이면서 소수, 시프트와 뺄셈으로 최적화 가능
            // 31 * i == (i << 5) - i
            int expected = 31 * "John".hashCode() + 25;
            assertThat(person.hashCode()).isEqualTo(expected);
        }

        @Test
        void 불변_객체는_hashCode를_캐싱할_수_있다() {
            PersonCached person = new PersonCached("John", 25);

            int hash1 = person.hashCode();
            int hash2 = person.hashCode();

            // 같은 값 반환, 두 번째 호출부터는 캐시된 값 사용
            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    class equals_구현_가이드라인 {

        @Test
        void instanceof_패턴_매칭으로_간결하게_구현() {
            PersonModern p1 = new PersonModern("John", 25);
            PersonModern p2 = new PersonModern("John", 25);
            String notPerson = "not a person";

            assertThat(p1.equals(p2)).isTrue();
            assertThat(p1.equals(notPerson)).isFalse();
            assertThat(p1.equals(null)).isFalse();
        }

        @Test
        void getClass_vs_instanceof_차이() {
            Point point = new Point(1, 2);
            ColorPoint colorPoint = new ColorPoint(1, 2, "red");

            // instanceof: 하위 클래스도 비교 가능 (리스코프 치환 원칙)
            // getClass: 정확히 같은 클래스만 비교 (더 엄격)

            // Point.equals가 instanceof 사용시
            assertThat(point.equals(colorPoint)).isTrue(); // ColorPoint도 Point

            // 엄격한 비교가 필요하면 getClass 사용
            StrictPoint sp1 = new StrictPoint(1, 2);
            StrictColorPoint scp = new StrictColorPoint(1, 2, "red");
            assertThat(sp1.equals(scp)).isFalse(); // 클래스가 다름
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
     * hashCode를 구현하지 않은 잘못된 클래스
     */
    static class BadPerson {
        private final String name;
        private final int age;

        public BadPerson(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BadPerson that)) return false;
            return age == that.age && Objects.equals(name, that.name);
        }

        // hashCode 미구현 Object.hashCode() 사용 (객체 주소 기반)
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

    /**
     * 추이성 위반 예시를 위한 Point
     */
    static class Point {
        protected final int x;
        protected final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point p)) return false;
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * 추이성 위반을 일으키는 하위 클래스
     */
    static class ColorPoint extends Point {
        private final String color;

        public ColorPoint(int x, int y, String color) {
            super(x, y);
            this.color = color;
        }

        // 추이성 위반 가능한 구현
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;

            // Point와 비교할 때는 좌표만 비교
            if (!(o instanceof ColorPoint cp)) {
                return o.equals(this); // 위험한 코드
            }

            // ColorPoint끼리는 색상도 비교
            return super.equals(o) && Objects.equals(color, cp.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), color);
        }
    }

    /**
     * 컴포지션으로 해결한 ColorPoint
     */
    static class ColorPointComposition {
        private final Point point;
        private final String color;

        public ColorPointComposition(Point point, String color) {
            this.point = point;
            this.color = color;
        }

        public Point asPoint() {
            return point;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ColorPointComposition cp)) return false;
            return point.equals(cp.point) && Objects.equals(color, cp.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(point, color);
        }
    }

    /**
     * 직접 hashCode 계산 (성능 최적화)
     */
    static class PersonOptimized {
        private final String name;
        private final int age;

        public PersonOptimized(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + age;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PersonOptimized p)) return false;
            return age == p.age && Objects.equals(name, p.name);
        }
    }

    /**
     * hashCode 캐싱 (불변 객체)
     */
    static class PersonCached {
        private final String name;
        private final int age;
        private int hashCode; // 캐시

        public PersonCached(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public int hashCode() {
            int result = hashCode;
            if (result == 0) {
                result = Objects.hash(name, age);
                hashCode = result;
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof PersonCached p)) return false;
            return age == p.age && Objects.equals(name, p.name);
        }
    }

    /**
     * Java 16+ instanceof 패턴 매칭 사용
     */
    static class PersonModern {
        private final String name;
        private final int age;

        public PersonModern(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof PersonModern p && age == p.age && Objects.equals(name, p.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    /**
     * getClass를 사용한 엄격한 비교
     */
    static class StrictPoint {
        protected final int x;
        protected final int y;

        public StrictPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            StrictPoint that = (StrictPoint) o;
            return x == that.x && y == that.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    static class StrictColorPoint extends StrictPoint {
        private final String color;

        public StrictColorPoint(int x, int y, String color) {
            super(x, y);
            this.color = color;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            StrictColorPoint that = (StrictColorPoint) o;
            return x == that.x && y == that.y && Objects.equals(color, that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, color);
        }
    }
}
