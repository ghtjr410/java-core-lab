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
public class GenericWildCardTest {

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
}
