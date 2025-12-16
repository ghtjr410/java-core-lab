# Java Core Lab

> Java 언어의 핵심을 깊이 이해하기 위한 학습 테스트 저장소

[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![JUnit Version](https://img.shields.io/badge/JUnit-5.10-green.svg)](https://junit.org/junit5/)

## 📌 소개

이 저장소는 **Java 언어 자체의 동작 원리**를 학습 테스트(Learning Test)를 통해 탐구합니다.

프레임워크나 라이브러리가 아닌, 순수 Java 언어 스펙에 집중합니다.

```
"코드로 증명하고, 테스트로 이해한다"
```

## 🎯 학습 목표

- Java 언어의 **동작 원리**를 코드로 직접 검증
- 단순 사용법이 아닌 **"왜 이렇게 동작하는가"** 이해
- **안티패턴**과 **권장 패턴**의 차이를 실험으로 체득
- 면접에서 **깊이 있는 답변**을 할 수 있는 기반 구축

## 🛠 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Test Framework | JUnit 5 |
| Assertion | AssertJ |
| Build Tool | Gradle |

## 📁 프로젝트 구조

```
src/test/java/
├── 01_type_system/          # 제네릭, 와일드카드, 타입 추론
├── 02_object_contract/      # equals, hashCode, Comparable
├── 03_string/               # String Pool, StringBuilder
├── 04_collection/           # List, Set, Map 구현체 비교
├── 05_enum/                 # Enum 활용, 상태 머신
├── 06_exception/            # Checked vs Unchecked, 예외 처리 패턴
├── 07_nested_class/         # Inner, Static Nested, Anonymous
├── 08_immutability/         # final, 방어적 복사, 불변 패턴
├── 09_interface/            # default, static, private 메서드
├── 10_functional/           # Lambda, Method Reference, Closure
├── 11_stream/               # Stream API 심화
├── 12_optional/             # Optional 올바른 사용법
├── 13_datetime/             # Java Time API
├── 14_record/               # Record 패턴
├── 15_sealed/               # Sealed Classes
├── 16_pattern_matching/     # 패턴 매칭 (instanceof, switch)
├── 17_text_blocks/          # Text Blocks
├── 18_sequenced_collection/ # Sequenced Collections (Java 21)
├── 19_reflection/           # Reflection API
├── 20_annotation/           # Annotation 심화
├── 21_virtual_thread/       # Virtual Thread (Java 21)
└── 22_concurrency_basic/    # 동시성 기초
```

## 📚 학습 내용

### Part 1: 기본기 (01 ~ 09)

<details>
<summary><b>01. Type System</b> - 제네릭과 타입 시스템</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `GenericBasicTest` | 제네릭 클래스, 메서드, 타입 파라미터 |
| `GenericWildcardTest` | `<?>`, `<? extends T>`, `<? super T>` 차이 |
| `GenericTypeErasureTest` | 타입 소거가 발생하는 시점과 영향 |
| `TypeInferenceTest` | `var` 키워드, 다이아몬드 연산자 |

**핵심 질문**
- 왜 `List<Integer>`를 `List<Object>`에 대입할 수 없는가?
- 타입 소거(Type Erasure)란 무엇이고, 어떤 제약을 만드는가?
- PECS(Producer Extends, Consumer Super) 원칙이란?

</details>

<details>
<summary><b>02. Object Contract</b> - Object 메서드 계약</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `EqualsHashCodeTest` | equals-hashCode 계약, 위반 시 문제점 |
| `ToStringTest` | toString 구현 패턴 |
| `ComparableComparatorTest` | 자연 순서 vs 커스텀 순서 |
| `CloneTest` | 얕은 복사 vs 깊은 복사, clone의 문제점 |

**핵심 질문**
- equals를 재정의하면 hashCode도 재정의해야 하는 이유는?
- Comparable과 Comparator는 언제 각각 사용하는가?
- clone() 대신 권장되는 복사 방법은?

</details>

<details>
<summary><b>03. String</b> - 문자열 처리</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `StringPoolTest` | String Pool, intern(), 리터럴 vs new |
| `StringBuilderBufferTest` | StringBuilder vs StringBuffer, 성능 차이 |
| `StringMethodsTest` | Java 11+ 새로운 메서드들 |
| `StringFormatterTest` | formatted(), String.format() |

**핵심 질문**
- `"hello" == "hello"`는 왜 true인가?
- StringBuilder와 StringBuffer의 차이는?
- 문자열 연결 시 `+` 연산자 vs StringBuilder 성능 차이는?

</details>

<details>
<summary><b>04. Collection</b> - 컬렉션 프레임워크</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `ListImplementationTest` | ArrayList vs LinkedList 성능 특성 |
| `SetImplementationTest` | HashSet vs TreeSet vs LinkedHashSet |
| `MapImplementationTest` | HashMap vs TreeMap vs LinkedHashMap |
| `QueueDequeTest` | Queue, Deque, PriorityQueue |
| `CollectionPerformanceTest` | 시간복잡도 실험 |

**핵심 질문**
- ArrayList와 LinkedList는 각각 언제 사용하는가?
- HashMap의 시간복잡도가 O(1)인 이유는?
- TreeSet은 내부적으로 어떻게 정렬을 유지하는가?

</details>

<details>
<summary><b>05. Enum</b> - 열거형 활용</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `EnumBasicTest` | Enum 기본 사용법, 싱글톤 특성 |
| `EnumMethodTest` | 추상 메서드, 필드, 생성자 |
| `EnumSetMapTest` | EnumSet, EnumMap 성능 이점 |
| `EnumStateMachineTest` | Enum을 활용한 상태 머신 패턴 |

**핵심 질문**
- Enum이 싱글톤 구현에 권장되는 이유는?
- EnumSet이 HashSet보다 빠른 이유는?
- Enum에 추상 메서드를 정의하면 어떤 이점이 있는가?

</details>

<details>
<summary><b>06. Exception</b> - 예외 처리</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `CheckedExceptionTest` | Checked Exception 특성, 처리 강제 |
| `UncheckedExceptionTest` | Unchecked Exception, RuntimeException |
| `TryWithResourcesTest` | AutoCloseable, 리소스 자동 해제 |
| `ExceptionHandlingPatternTest` | 예외 처리 패턴, 안티패턴 |

**핵심 질문**
- Checked와 Unchecked Exception의 차이는?
- try-with-resources가 finally보다 권장되는 이유는?
- 예외를 catch하고 무시하면 안 되는 이유는?

</details>

<details>
<summary><b>07. Nested Class</b> - 중첩 클래스</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `InnerClassTest` | Inner Class, 외부 클래스 참조 |
| `StaticNestedClassTest` | Static Nested Class |
| `AnonymousClassTest` | 익명 클래스, 람다와의 차이 |
| `LocalClassTest` | 지역 클래스 |

**핵심 질문**
- Inner Class가 외부 클래스의 참조를 갖는 것이 왜 문제가 될 수 있는가?
- Static Nested Class는 언제 사용하는가?
- 익명 클래스와 람다의 차이점은?

</details>

<details>
<summary><b>08. Immutability</b> - 불변성</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `FinalKeywordTest` | final 변수, 메서드, 클래스 |
| `DefensiveCopyTest` | 방어적 복사, 불변 보장 |
| `ImmutablePatternTest` | 불변 객체 생성 패턴 |

**핵심 질문**
- final 키워드가 보장하는 것과 보장하지 않는 것은?
- 방어적 복사는 왜 필요한가?
- 불변 객체의 장점은 무엇인가?

</details>

<details>
<summary><b>09. Interface</b> - 인터페이스 진화</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `InterfaceDefaultMethodTest` | default 메서드, 다중 상속 충돌 |
| `InterfaceStaticMethodTest` | static 메서드 |
| `InterfacePrivateMethodTest` | private 메서드 (Java 9+) |

**핵심 질문**
- default 메서드가 추가된 이유는?
- 두 인터페이스의 default 메서드가 충돌하면 어떻게 되는가?
- 인터페이스에 private 메서드가 왜 필요한가?

</details>

### Part 2: 함수형 프로그래밍 (10 ~ 12)

<details>
<summary><b>10. Functional</b> - 함수형 프로그래밍 기초</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `LambdaTest` | 람다 표현식 문법, 타입 추론 |
| `MethodReferenceTest` | 메서드 참조 4가지 유형 |
| `ClosureTest` | 클로저, effectively final |
| `BuiltInFunctionalInterfaceTest` | Function, Consumer, Predicate, Supplier |

**핵심 질문**
- 람다에서 외부 변수를 사용할 때 effectively final이어야 하는 이유는?
- 메서드 참조의 4가지 유형은?
- Function, Consumer, Predicate의 차이는?

</details>

<details>
<summary><b>11. Stream</b> - Stream API 심화</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `StreamCreationTest` | Stream 생성 방법들 |
| `StreamIntermediateTest` | filter, map, flatMap 등 중간 연산 |
| `StreamTerminalTest` | collect, reduce 등 최종 연산 |
| `CollectorTest` | Collectors 유틸리티 |
| `CustomCollectorTest` | 커스텀 Collector 구현 |
| `ParallelStreamTest` | 병렬 스트림, 주의사항 |
| `StreamPitfallTest` | Stream 사용 시 흔한 실수들 |

**핵심 질문**
- Stream은 왜 재사용할 수 없는가?
- 중간 연산이 지연 평가되는 것은 어떤 이점이 있는가?
- 병렬 스트림을 사용하면 안 되는 경우는?

</details>

<details>
<summary><b>12. Optional</b> - Optional 올바른 사용법</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `OptionalCreationTest` | Optional 생성 방법 |
| `OptionalChainingTest` | map, flatMap, filter 체이닝 |
| `OptionalAntiPatternTest` | Optional 안티패턴들 |

**핵심 질문**
- Optional을 필드로 사용하면 안 되는 이유는?
- `isPresent() + get()` 대신 권장되는 방법은?
- Optional.of()와 Optional.ofNullable()의 차이는?

</details>

### Part 3: 모던 Java (13 ~ 18)

<details>
<summary><b>13. DateTime</b> - Java Time API</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `LocalDateTimeTest` | LocalDate, LocalTime, LocalDateTime |
| `ZonedDateTimeTest` | 시간대 처리, ZoneId |
| `DurationPeriodTest` | Duration vs Period |
| `DateTimeFormatterTest` | 날짜/시간 포맷팅 |

**핵심 질문**
- LocalDateTime과 ZonedDateTime은 언제 각각 사용하는가?
- Duration과 Period의 차이는?
- 레거시 Date/Calendar 대신 Java Time API를 사용해야 하는 이유는?

</details>

<details>
<summary><b>14. Record</b> - Record (Java 16+)</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `RecordBasicTest` | Record 기본 문법, 자동 생성 메서드 |
| `RecordValidationTest` | Compact Constructor, 유효성 검증 |
| `RecordPatternTest` | Record와 패턴 매칭 |
| `RecordVsClassTest` | Record vs Class 비교, 사용 시점 |

**핵심 질문**
- Record가 자동으로 생성해주는 것들은?
- Record는 언제 사용하고, 언제 사용하지 말아야 하는가?
- Compact Constructor란?

</details>

<details>
<summary><b>15. Sealed</b> - Sealed Classes (Java 17+)</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `SealedClassTest` | sealed, permits, non-sealed |
| `SealedWithPatternMatchingTest` | Sealed Class + 패턴 매칭 |

**핵심 질문**
- Sealed Class가 해결하는 문제는?
- permits 절에 명시된 클래스만 상속할 수 있는 이유는?
- Sealed Class와 패턴 매칭을 함께 사용하면 어떤 이점이 있는가?

</details>

<details>
<summary><b>16. Pattern Matching</b> - 패턴 매칭</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `InstanceofPatternTest` | instanceof 패턴 매칭 |
| `SwitchExpressionTest` | switch 표현식 |
| `SwitchPatternTest` | switch 패턴 매칭 |
| `RecordPatternDestructuringTest` | Record 패턴 분해 |

**핵심 질문**
- 패턴 매칭이 기존 instanceof보다 나은 점은?
- switch 표현식과 switch 문의 차이는?
- Record 패턴 분해란?

</details>

<details>
<summary><b>17. Text Blocks</b> - Text Blocks (Java 15+)</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `TextBlockTest` | Text Block 문법, 들여쓰기 처리 |

**핵심 질문**
- Text Block의 들여쓰기는 어떻게 처리되는가?
- Text Block에서 후행 공백을 유지하려면?

</details>

<details>
<summary><b>18. Sequenced Collection</b> - Sequenced Collections (Java 21)</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `SequencedCollectionTest` | SequencedCollection, SequencedSet, SequencedMap |

**핵심 질문**
- Sequenced Collection이 추가된 이유는?
- reversed() 메서드는 어떻게 동작하는가?

</details>

### Part 4: 메타 프로그래밍 (19 ~ 20)

<details>
<summary><b>19. Reflection</b> - Reflection API</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `ClassInspectionTest` | Class 객체, 메타데이터 조회 |
| `FieldAccessTest` | 필드 접근, private 필드 수정 |
| `MethodInvocationTest` | 메서드 동적 호출 |
| `ReflectionPerformanceTest` | Reflection 성능 오버헤드 |

**핵심 질문**
- Reflection은 어떤 상황에서 사용하는가?
- Reflection의 성능 오버헤드는 얼마나 되는가?
- Spring이 Reflection을 사용하는 이유는?

</details>

<details>
<summary><b>20. Annotation</b> - Annotation 심화</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `BuiltInAnnotationTest` | @Override, @Deprecated, @SuppressWarnings |
| `CustomAnnotationTest` | 커스텀 애노테이션 정의 |
| `MetaAnnotationTest` | @Target, @Retention, @Inherited |
| `AnnotationProcessingTest` | 런타임 애노테이션 처리 |

**핵심 질문**
- @Retention의 RetentionPolicy 종류와 차이는?
- 커스텀 애노테이션은 어떻게 만드는가?
- 애노테이션 정보를 런타임에 읽으려면?

</details>

### Part 5: 동시성 (21 ~ 22)

<details>
<summary><b>21. Virtual Thread</b> - Virtual Thread (Java 21)</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `VirtualThreadBasicTest` | Virtual Thread 생성, 기본 사용법 |
| `VirtualThreadVsPlatformTest` | Platform Thread와 성능 비교 |
| `VirtualThreadPitfallTest` | Virtual Thread 사용 시 주의사항 |

**핵심 질문**
- Virtual Thread와 Platform Thread의 차이는?
- Virtual Thread는 어떤 상황에서 효과적인가?
- Virtual Thread 사용 시 주의해야 할 점은?

</details>

<details>
<summary><b>22. Concurrency Basic</b> - 동시성 기초</summary>

| 테스트 | 학습 내용 |
|--------|-----------|
| `ThreadLifecycleTest` | Thread 생명주기, join, interrupt |
| `SynchronizedTest` | synchronized, wait, notify |
| `ExecutorServiceTest` | ExecutorService, Thread Pool |
| `CompletableFutureTest` | CompletableFuture 비동기 처리 |
| `AtomicTest` | Atomic 클래스, CAS 연산 |

**핵심 질문**
- synchronized의 동작 원리는?
- ExecutorService를 사용해야 하는 이유는?
- CompletableFuture의 thenApply와 thenCompose의 차이는?

</details>

## 📝 학습 테스트 작성 원칙

### 1. 테스트 구조
```java
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StreamIntermediateTest {

    @Nested
    class filter_조건에_맞는_요소만_통과 {

        @Test
        void 기본_사용법() {
            List numbers = List.of(1, 2, 3, 4, 5);

            List evens = numbers.stream()
                    .filter(n -> n % 2 == 0)
                    .toList();

            assertThat(evens).containsExactly(2, 4);
        }
        
        ...
    }
    
    ...
}
```

### 2. 원칙
| 원칙 | 설명 |
|------|------|
| **@DisplayNameGeneration** | 언더스코어를 공백으로 자동 변환 |
| **한글 메서드명** | `동기화_없이_공유_변수_접근시_데이터_유실()` |
| **@Nested** | 관련 테스트 그룹핑, 클래스명도 한글 스네이크케이스 |
| **given/when/then** | 구조는 유지하되 주석 생략 |
### 3. 학습 테스트가 다루는 것

```
✅ 기본 사용법
✅ 동작 원리 (왜 이렇게 동작하는가)
✅ 엣지 케이스
✅ 안티패턴 vs 권장 패턴
✅ 성능 특성 (필요한 경우)
✅ 실무에서 겪을 수 있는 함정
```

## 🚀 실행 방법

```bash
# 전체 테스트 실행
./gradlew test

# 특정 섹션만 실행
./gradlew test --tests "*.01_type_system.*"

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

## 📖 참고 자료

- [Effective Java 3rd Edition](https://www.oreilly.com/library/view/effective-java/9780134686097/)
- [Modern Java in Action](https://www.manning.com/books/modern-java-in-action)
- [Java Language Specification](https://docs.oracle.com/javase/specs/)
- [OpenJDK JDK 21](https://openjdk.org/projects/jdk/21/)



---

<div align="center">

**"이해하지 못하는 것은 소유하지 못한 것이다"**

*— 괴테*

</div>