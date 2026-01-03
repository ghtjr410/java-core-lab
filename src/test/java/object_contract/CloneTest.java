package object_contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Clone 메서드 학습 테스트
 *
 * 핵심 학습 포인트:
 * 1. clone()의 동작 방식과 Cloneable 인터페이스
 * 2. 얕은 복사(shallow copy)와 깊은 복사(deep copy)
 * 3. clone()의 문제점과 대안
 * 4. 복사 생성자와 복사 팩터리 패턴 권장
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class CloneTest {

    @Nested
    class clone_기본_동작 {

        @Test
        void Cloneable_미구현시_CloneNotSupportedException_발생() {
            NonCloneable obj = new NonCloneable("test");

            assertThatThrownBy(() -> obj.clone()).isInstanceOf(CloneNotSupportedException.class);
        }

        @Test
        void Cloneable_구현시_clone_가능() throws CloneNotSupportedException {
            SimpleCloneable original = new SimpleCloneable("test", 42);
            SimpleCloneable cloned = (SimpleCloneable) original.clone();

            // 다른 객체
            assertThat(cloned).isNotSameAs(original);

            // 같은 값
            assertThat(cloned.getName()).isEqualTo(original.getName());
            assertThat(cloned.getValue()).isEqualTo(original.getValue());

            // equals (올바르게 구현됐다면)
            assertThat(cloned).isEqualTo(original);
        }

        @Test
        void clone_규약_x_clone_ne_x() throws CloneNotSupportedException {
            SimpleCloneable x = new SimpleCloneable("test", 42);
            SimpleCloneable clone = (SimpleCloneable) x.clone();

            // x.clone() != x (항상 참)
            assertThat(clone).isNotSameAs(x);
        }

        @Test
        void clone_규약_x_clone_getClass_eq_x_getClass() throws CloneNotSupportedException {
            SimpleCloneable x = new SimpleCloneable("test", 42);
            SimpleCloneable clone = (SimpleCloneable) x.clone();

            // x.clone().getClass() == x.getClass() (일반적으로 참)
            assertThat(clone.getClass()).isEqualTo(x.getClass());
        }

        @Test
        void clone_규약_x_clone_equals_x() throws CloneNotSupportedException {
            SimpleCloneable x = new SimpleCloneable("test", 42);
            SimpleCloneable clone = (SimpleCloneable) x.clone();

            // x.clone().equals(x) (일반적으로 참, 필수는 아님)
            assertThat(clone).isEqualTo(x);
        }
    }

    @Nested
    class 얕은_복사_Shallow_Copy {

        @Test
        void Object_clone은_얕은_복사를_수행() throws CloneNotSupportedException {
            List<String> list = new ArrayList<>(List.of("a", "b", "c"));
            ShallowCloneable original = new ShallowCloneable("test", list);

            ShallowCloneable cloned = original.clone();

            // 객체는 다름
            assertThat(cloned).isNotSameAs(original);

            // 하지만 내부 리스트는 같은 참조
            assertThat(cloned.getItems()).isSameAs(original.getItems());

            // 원본 수정하면 복사본도 영향받음
            original.getItems().add("d");
            assertThat(cloned.getItems()).contains("d"); // 복사본도 변경됨
        }

        @Test
        void 얕은_복사의_문제점() throws CloneNotSupportedException {
            Address address = new Address("Seoul", "123");
            PersonWithAddress original = new PersonWithAddress("John", address);

            PersonWithAddress cloned = original.clone();

            // 주소 객체가 공유됨
            assertThat(cloned.getAddress()).isSameAs(original.getAddress());

            // 원본 주소 변경 시 복사본도 영향받음
            original.getAddress().setCity("Busan");
            assertThat(cloned.getAddress().getCity()).isEqualTo("Busan"); // 영향받음
        }
    }

    // === 테스트용 헬퍼 클래스들 ===

    static class NonCloneable {
        private final String value;

        public NonCloneable(String value) {
            this.value = value;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException();
        }
    }

    static class SimpleCloneable implements Cloneable {
        private final String name;
        private final int value;

        public SimpleCloneable(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone(); // 필드가 모두 불변/기본형이면 충분
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SimpleCloneable s)) return false;
            return value == s.value && name.equals(s.name);
        }

        @Override
        public int hashCode() {
            return 31 * name.hashCode() + value;
        }
    }

    /**
     * 얕은 복사 문제 시연
     */
    static class ShallowCloneable implements Cloneable {
        private final String name;
        private final List<String> items;

        public ShallowCloneable(String name, List<String> items) {
            this.name = name;
            this.items = items;
        }

        public List<String> getItems() {
            return items;
        }

        @Override
        public ShallowCloneable clone() throws CloneNotSupportedException {
            return (ShallowCloneable) super.clone(); // 얕은 복사만!
        }
    }

    static class Address {
        private String city;
        private String zipCode;

        public Address(String city, String zipCode) {
            this.city = city;
            this.zipCode = zipCode;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getZipCode() {
            return zipCode;
        }

        public Address copy() {
            return new Address(this.city, this.zipCode);
        }
    }

    static class PersonWithAddress implements Cloneable {
        private final String name;
        private final Address address;

        public PersonWithAddress(String name, Address address) {
            this.name = name;
            this.address = address;
        }

        public Address getAddress() {
            return address;
        }

        @Override
        public PersonWithAddress clone() throws CloneNotSupportedException {
            return (PersonWithAddress) super.clone(); // 얕은 복사 - Address 공유됨
        }
    }
}
