# Race Condition 실험 문서 (count++ 내부 분석)

이 문서는 `count++` 연산이 멀티 스레드 환경에서\
왜 Race Condition(경쟁 조건)을 일으키는지,\
직접 클래스를 만들고 컴파일 결과를 확인하는 방식으로 설명한다.

---

## 1. Counter 클래스 작성

먼저 가장 단순한 카운터 클래스를 하나 만든다.

```java
public class Counter {
    int count;

    public void inc() {
        count++;
    }
}
```
- count는 인스턴스 필드
- inc()는 count++를 수행하는 메서드
- 동기화(synchronized, atomic 등)는 전혀 사용하지 않는다
- 
## 2. 컴파일 방법 
- 터미널에서 Counter.java 파일이 있는 디렉터리로 이동한 뒤
- 다음 명령어를 실행한다.
``` bash
javac Counter.java
```
정상적으로 컴파일되면 다음 파일이 생성된다.
``` cpp
Counter.class
```
이 파일이 JVM이 실제로 실행하는 바이트코드다.

## 3. 컴파일 결과(바이트코드) 확인
이제 `javap` 명령어를 이용해 `count++`가 실제로 어떤 명령으로 구성되어 있는지 확인한다.
``` bash
javap -c Counter
```
출력 결과는 다음과 같다.
```text
Compiled from "Counter.java"
public class Counter {
  int count;

  public Counter();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public void inc();
    Code:
       0: aload_0
       1: dup
       2: getfield      #7                  // Field count:I
       5: iconst_1
       6: iadd
       7: putfield      #7                  // Field count:I
      10: return
}
```

## 4. count++ 바이트코드 해석
`inc()` 메서드의 핵심 부분만 정리하면 다음과 같다.
```text
getfield count   // count 값을 읽는다 (read)
iconst_1
iadd             // 1을 더한다 (add)
putfield count   // 다시 count에 쓴다 (write)
```
즉, count++는 하나의 연산이 아니라 \
읽기 → 계산 → 쓰기의 3단계로 나뉜다.
## 5. 왜 경쟁 조건(Race Condition)이 발생하는가
멀티 스레드 환경에서 두 스레드 A, B가 동시에 inc()를 실행한다고 가정한다.\
초기 상태:
```text
count = 10
```
실제 실행 순서는 다음과 같이 섞일 수 있다.
```text
A: getfield count → 10
B: getfield count → 10
A: iadd → 11
B: iadd → 11
A: putfield count = 11
B: putfield count = 11
```
결과:
```text
count = 11
```
- 두 번 증가가 발생했지만
- 최종 결과는 한 번만 증가
이 현상을 **Lost Update(갱신 손실)** 라고 한다.
## 6. 핵심 정리
- count++는 원자적(atomic) 연산이 아니다
- 여러 스레드가 동시에 실행하면 
  - 같은 값을 읽고 
  - 각자 계산한 결과를 
  - 서로 덮어쓸 수 있다
- 실행 순서에 따라 결과가 달라진다 (비결정적)

이것이 Race Condition의 본질이다.
## 7. 결론
- 단일 스레드 환경에서는 문제가 없다 
- 멀티 스레드 환경에서는 count++는 절대 안전하지 않다 
- 공유 변수에는 반드시 동기화 전략이 필요하다
  - synchronized 
  - AtomicInteger 
  - Lock

이 문서는 Race Condition을 코드와 컴파일 결과로 직접 확인하기 위한 실험 기록이다.

***

| 문제 | 원인 | 해결책 |
|------|------|--------|
| Race Condition | 여러 스레드가 공유 변수 동시 접근 | synchronized, AtomicInteger |
| Check-Then-Act | 조건 확인과 실행이 분리됨 | compareAndSet (CAS) |
| 데이터 유실 | count++가 원자적이지 않음 | incrementAndGet() |