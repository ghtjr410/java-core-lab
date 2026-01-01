package object_contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.*;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comparable과 Comparator 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. Comparable: 클래스의 자연적 순서(natural ordering) 정의
 * 2. Comparator: 다양한 정렬 기준을 외부에서 제공
 * 3. compareTo와 equals의 일관성
 * 4. Java 8+ Comparator 유틸리티 메서드
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ComparableComparatorTest {

    @Nested
    class Comparable_자연적_순서 {

        /**
         * Comparable 구현 시 compareTo 규약:
         * 1. sgn(x.compareTo(y)) == -sgn(y.compareTo(x))
         * 2. 추이성: x.compareTo(y) > 0 && y.compareTo(z) > 0 → x.compareTo(z) > 0
         * 3. x.compareTo(y) == 0 → sgn(x.compareTo(z)) == sgn(y.compareTo(z))
         * 4. (권장) x.compareTo(y) == 0 → x.equals(y)
         */
        @Test
        void Comparable_구현으로_자연적_순서_정의() {
            List<Person> people =
                    new ArrayList<>(List.of(new Person("Charlie", 30), new Person("Alice", 25), new Person("Bob", 35)));

            Collections.sort(people); // 자연적 순서로 정렬

            assertThat(people).extracting(Person::name).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        void compareTo_반환값의_의미() {
            Person alice = new Person("Alice", 25);
            Person bob = new Person("Bob", 30);
            Person alice2 = new Person("Alice", 25);

            // 음수: this가 인자보다 작음
            assertThat(alice.compareTo(bob)).isNegative();

            // 양수: this가 인자보다 큼
            assertThat(bob.compareTo(alice)).isPositive();

            // 0: 같음
            assertThat(alice.compareTo(alice2)).isZero();
        }

        @Test
        void TreeSet은_Comparable을_사용해_정렬() {
            TreeSet<Person> people = new TreeSet<>();
            people.add(new Person("Charlie", 30));
            people.add(new Person("Alice", 25));
            people.add(new Person("Bob", 35));

            assertThat(people).extracting(Person::name).containsExactly("Alice", "Bob", "Charlie");
        }

        @Test
        void Comparable_미구현시_TreeSet에서_예외_발생() {
            assertThatThrownBy(() -> {
                        TreeSet<NonComparable> set = new TreeSet<>();
                        set.add(new NonComparable("A"));
                        set.add(new NonComparable("B")); // 여기서 비교 시도
                    })
                    .isInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class Comparator_커스텀_순서 {

        /**
         * 핵심 질문: Comparable과 Comparator는 언제 각각 사용하는가?
         *
         * Comparable: 클래스의 기본 정렬 순서가 명확할 때 (자연적 순서)
         * Comparator: 다양한 정렬 기준이 필요하거나, 클래스 수정이 불가능할 때
         */
        @Test
        void Comparator로_다양한_정렬_기준_적용() {
            List<Person> people =
                    new ArrayList<>(List.of(new Person("Charlie", 30), new Person("Alice", 25), new Person("Bob", 35)));

            // 나이 순 정렬
            people.sort(Comparator.comparingInt(Person::age));

            assertThat(people).extracting(Person::age).containsExactly(25, 30, 35);
        }

        @Test
        void 역순_정렬() {
            List<Person> people =
                    new ArrayList<>(List.of(new Person("Alice", 25), new Person("Bob", 30), new Person("Charlie", 35)));

            // 이름 역순
            people.sort(Comparator.comparing(Person::name).reversed());

            assertThat(people).extracting(Person::name).containsExactly("Charlie", "Bob", "Alice");
        }

        @Test
        void 다중_기준_정렬() {
            List<Employee> employees = new ArrayList<>(List.of(
                    new Employee("Alice", "Engineering", 50000),
                    new Employee("Bob", "Engineering", 60000),
                    new Employee("Charlie", "Sales", 55000),
                    new Employee("David", "Engineering", 50000)));

            // 부서 → 급여(내림차순) → 이름 순
            employees.sort(Comparator.comparing(Employee::department)
                    .thenComparing(Employee::salary, Comparator.reverseOrder())
                    .thenComparing(Employee::name));

            assertThat(employees).extracting(Employee::name).containsExactly("Bob", "Alice", "David", "Charlie");
        }

        @Test
        void null_처리_Comparator() {
            List<String> names = new ArrayList<>(List.of("Bob", "Alice"));
            names.add(null);
            names.add("Charlie");

            // null을 마지막에 배치
            names.sort(Comparator.nullsLast(Comparator.naturalOrder()));

            assertThat(names).containsExactly("Alice", "Bob", "Charlie", null);

            // null을 처음에 배치
            names.sort(Comparator.nullsFirst(Comparator.naturalOrder()));

            assertThat(names).containsExactly(null, "Alice", "Bob", "Charlie");
        }
    }

    @Nested
    class Comparator_생성_방법 {

        @Test
        void comparing_메서드로_생성() {
            Comparator<Person> byName = Comparator.comparing(Person::name);
            Comparator<Person> byAge = Comparator.comparingInt(Person::age);

            List<Person> people = new ArrayList<>(List.of(new Person("Bob", 25), new Person("Alice", 30)));

            people.sort(byName);
            assertThat(people.getFirst().name()).isEqualTo("Alice");

            people.sort(byAge);
            assertThat(people.getFirst().age()).isEqualTo(25);
        }

        @Test
        void 람다로_직접_정의() {
            /*
             * 람다로 직접 정의하는 방식은 실무에서 잘 안 씀
             * - 가독성 떨어짐
             * - 비교 로직 실수 가능성 있음 (p1, p2 순서 헷갈림)
             *
             * 대부분 Comparator.comparing() 체이닝으로 해결됨:
             * Comparator.comparing(p -> p.name().length())
             */

            Comparator<Person> byNameLength =
                    (p1, p2) -> Integer.compare(p1.name().length(), p2.name().length());

            List<Person> people = new ArrayList<>(
                    List.of(new Person("Alexander", 25), new Person("Bob", 30), new Person("Charlie", 35)));

            people.sort(byNameLength);

            assertThat(people).extracting(Person::name).containsExactly("Bob", "Charlie", "Alexander");
        }

        @Test
        void 메서드_참조로_자연순서_사용() {
            List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob"));

            // String::compareTo는 Comparable의 자연순서
            names.sort(String::compareTo);

            assertThat(names).containsExactly("Alice", "Bob", "Charlie");
        }
    }

    @Nested
    class compareTo와_equals_일관성 {

        /**
         * compareTo와 equals의 일관성:
         * x.compareTo(y) == 0 이면 x.equals(y)도 true여야 함 (권장)
         *
         * 일관성이 없으면 정렬된 컬렉션에서 예상치 못한 동작 발생
         */
        @Test
        void 일관성_있는_구현() {
            ConsistentVersion v1 = new ConsistentVersion(1, 0);
            ConsistentVersion v2 = new ConsistentVersion(1, 0);

            // compareTo와 equals가 일관됨
            assertThat(v1.compareTo(v2)).isZero();
            assertThat(v1.equals(v2)).isTrue();
        }

        @Test
        void 일관성_없는_구현의_문제점() {
            InconsistentVersion v1 = new InconsistentVersion(1, 0, "alpha");
            InconsistentVersion v2 = new InconsistentVersion(1, 0, "beta");

            // compareTo는 0 (버전만 비교)
            assertThat(v1.compareTo(v2)).isZero();

            // equals는 false (tag도 비교)
            assertThat(v1.equals(v2)).isFalse();

            // TreeSet에서 문제 발생
            TreeSet<InconsistentVersion> treeSet = new TreeSet<>();
            treeSet.add(v1);
            treeSet.add(v2); // v1.compareTo(v2) == 0이므로 중복으로 판단

            assertThat(treeSet).hasSize(1); // 2개가 아님

            // HashSet은 equals 기반이므로 다르게 동작
            java.util.HashSet<InconsistentVersion> hashSet = new java.util.HashSet<>();
            hashSet.add(v1);
            hashSet.add(v2);

            assertThat(hashSet).hasSize(2); // equals가 false이므로 2개
        }

        @Test
        void BigDecimal_일관성_문제_실례() {
            java.math.BigDecimal bd1 = new java.math.BigDecimal("1.0");
            java.math.BigDecimal bd2 = new java.math.BigDecimal("1.00");

            // compareTo는 0 (수학적으로 같음)
            assertThat(bd1.compareTo(bd2)).isZero();

            // equals는 false (스케일이 다름)
            assertThat(bd1.equals(bd2)).isFalse();

            // TreeSet vs HashSet 동작 차이
            TreeSet<java.math.BigDecimal> treeSet = new TreeSet<>();
            treeSet.add(bd1);
            treeSet.add(bd2);
            assertThat(treeSet).hasSize(1);

            java.util.HashSet<java.math.BigDecimal> hashSet = new java.util.HashSet<>();
            hashSet.add(bd1);
            hashSet.add(bd2);
            assertThat(hashSet).hasSize(2);
        }
    }

    @Nested
    class compareTo_구현_가이드라인 {

        @Test
        void 기본형은_박싱_클래스의_compare_사용() {
            // 안티패턴: 직접 빼기 연산 (오버플로우 위험)
            // return this.value - other.value;

            // 권장: 박싱 클래스의 compare 메서드
            IntWrapper w1 = new IntWrapper(Integer.MAX_VALUE);
            IntWrapper w2 = new IntWrapper(-1);

            // 올바른 결과
            assertThat(w1.compareTo(w2)).isPositive();
        }

        @Test
        void 다중_필드_비교시_중요도_순서로() {
            Version v1 = new Version(1, 9, 0);
            Version v2 = new Version(2, 0, 0);
            Version v3 = new Version(1, 10, 0);

            assertThat(v1.compareTo(v2)).isNegative(); // major가 더 중요
            assertThat(v1.compareTo(v3)).isNegative(); // minor 비교
        }

        @Test
        void Comparator_체이닝으로_간결하게_구현() {
            // Comparable 구현 시 Comparator를 활용
            VersionModern v1 = new VersionModern(1, 9, 0);
            VersionModern v2 = new VersionModern(2, 0, 0);

            assertThat(v1.compareTo(v2)).isNegative();
        }
    }

    // === 테스트용 헬퍼 클래스들 ===

    record Person(String name, int age) implements Comparable<Person> {
        @Override
        public int compareTo(Person other) {
            return this.name.compareTo(other.name); // 이름순 자연적 순서
        }
    }

    static class NonComparable {
        private final String value;

        public NonComparable(String value) {
            this.value = value;
        }
    }

    record Employee(String name, String department, int salary) {}

    /**
     * compareTo와 equals가 일관된 구현
     */
    static class ConsistentVersion implements Comparable<ConsistentVersion> {
        private final int major;
        private final int minor;

        public ConsistentVersion(int major, int minor) {
            this.major = major;
            this.minor = minor;
        }

        @Override
        public int compareTo(ConsistentVersion other) {
            int result = Integer.compare(this.major, other.major);
            if (result == 0) {
                result = Integer.compare(this.minor, other.minor);
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ConsistentVersion v)) return false;
            return major == v.major && minor == v.minor;
        }

        @Override
        public int hashCode() {
            return Objects.hash(major, minor);
        }
    }

    /**
     * compareTo와 equals가 일관되지 않은 구현 (안티패턴)
     */
    static class InconsistentVersion implements Comparable<InconsistentVersion> {
        private final int major;
        private final int minor;
        private final String tag; // equals에만 사용

        public InconsistentVersion(int major, int minor, String tag) {
            this.major = major;
            this.minor = minor;
            this.tag = tag;
        }

        @Override
        public int compareTo(InconsistentVersion other) {
            // tag는 비교하지 않음
            int result = Integer.compare(this.major, other.major);
            if (result == 0) {
                result = Integer.compare(this.minor, other.minor);
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof InconsistentVersion v)) return false;
            // tag도 비교함
            return major == v.major && minor == v.minor && Objects.equals(tag, v.tag);
        }

        @Override
        public int hashCode() {
            return Objects.hash(major, minor, tag);
        }
    }

    /**
     * 오버플로우 안전한 비교
     */
    static class IntWrapper implements Comparable<IntWrapper> {
        private final int value;

        public IntWrapper(int value) {
            this.value = value;
        }

        @Override
        public int compareTo(IntWrapper other) {
            // 안전: Integer.compare 사용
            return Integer.compare(this.value, other.value);
            // 위험: return this.value - other.value; (오버플로우)
        }
    }

    /**
     * 전통적인 다중 필드 compareTo
     */
    static class Version implements Comparable<Version> {
        private final int major;
        private final int minor;
        private final int patch;

        public Version(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public int compareTo(Version other) {
            int result = Integer.compare(this.major, other.major);
            if (result != 0) return result;

            result = Integer.compare(this.minor, other.minor);
            if (result != 0) return result;

            return Integer.compare(this.patch, other.patch);
        }
    }

    /**
     * Comparator를 활용한 모던 compareTo
     */
    static class VersionModern implements Comparable<VersionModern> {
        private static final Comparator<VersionModern> COMPARATOR = Comparator.comparingInt(
                        (VersionModern v) -> v.major)
                .thenComparingInt(v -> v.minor)
                .thenComparingInt(v -> v.patch);

        private final int major;
        private final int minor;
        private final int patch;

        public VersionModern(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public int compareTo(VersionModern other) {
            return COMPARATOR.compare(this, other);
        }
    }
}
