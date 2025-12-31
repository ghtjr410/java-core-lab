package object_contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
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
}
