package concurrency;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class RaceConditionTest {

    @Nested
    class Race_Condition_재현 {

        /**
         * Race Condition이란?
         * - 여러 스레드가 공유 자원에 동시 접근할 때
         * - 실행 순서에 따라 결과가 달라지는 현상
         * - count++ 은 사실 3단계 연산: 읽기 → 증가 → 쓰기
         *   - 1. 메모리에서 count 값을 읽음
         *   - 2. 읽은 값에 +1
         *   - 3. 다시 메모리에 씀
         *   이 세 단계는 원자적(atomic)이지 않습니다.
         */
        private int count = 0;

        @RepeatedTest(10)
        void 동기화_없이_공유_변수_접근시_데이터_유실() throws InterruptedException {
            count = 0;
            int threadCount = 100;
            int incrementPerThread = 1000;

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        for (int j = 0; j < incrementPerThread; j++) {
                            count++; // 동기화 없음 → Race Condition
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }

            int expected = threadCount * incrementPerThread; // 100,000

            // 거의 항상 기대값보다 작음 (데이터 유실)
            System.out.println("기대값: " + expected + ", 실제값: " + count);
            assertThat(count).isLessThanOrEqualTo(expected);
        }
    }

    @Nested
    class synchronized로_해결 {

        // 문을 잠그는 방식

        private int count = 0;

        private synchronized void increment() {
            count++;
        }

        @RepeatedTest(5)
        void synchronized_메서드로_동기화() throws InterruptedException {
            count = 0;
            int threadCount = 100;
            int incrementPerThread = 1000;

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        for (int j = 0; j < incrementPerThread; j++) {
                            increment();
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }

            int expected = threadCount * incrementPerThread;
            assertThat(count).isEqualTo(expected);
        }

        @RepeatedTest(5)
        void synchronized_블록으로_동기화() throws InterruptedException {
            count = 0;
            int threadCount = 100;
            int incrementPerThread = 1000;
            Object lock = new Object();

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        for (int j = 0; j < incrementPerThread; j++) {
                            synchronized (lock) {
                                count++;
                            }
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }

            int expected = threadCount * incrementPerThread;
            assertThat(count).isEqualTo(expected);
        }
    }

    @Nested
    class AtomicInteger로_해결 {

        /**
         * 서로 부딪히면 다시 계산하는 방식
         *
         * AtomicInteger
         * - CAS(Compare-And-Swap) 연산 사용
         * - 락 없이 원자적 연산 보장
         * - synchronized보다 성능 좋음
         */
        @RepeatedTest(5)
        void AtomicInteger는_원자적_연산_보장() throws InterruptedException {
            AtomicInteger count = new AtomicInteger(0);
            int threadCount = 100;
            int incrementPerThread = 1000;

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        for (int j = 0; j < incrementPerThread; j++) {
                            count.incrementAndGet();
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }

            int expected = threadCount * incrementPerThread;
            assertThat(count.get()).isEqualTo(expected);
        }
    }

    @Nested
    class 복합_연산의_위험성 {

        /**
         * 체크 후 행동(Check-Then-Act) 패턴의 위험성
         * - if 조건 확인과 실행 사이에 다른 스레드 개입 가능
         */
        private int value = 0;

        @Test
        void 체크_후_행동_패턴은_원자적이지_않다() throws InterruptedException {
            value = 0;
            int threadCount = 100;

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        // 체크와 행동 사이에 다른 스레드 개입 가능
                        if (value == 0) {
                            value = 1; // 여러 스레드가 동시에 이 조건을 통과할 수 있음
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }

            // 이 테스트는 문제를 보여주기 위한 것
            assertThat(value).isEqualTo(1);
        }

        @Test
        void compareAndSet으로_원자적_체크_후_행동() throws InterruptedException {
            AtomicInteger atomicValue = new AtomicInteger(0);
            AtomicInteger successCount = new AtomicInteger(0);
            int threadCount = 100;

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        // CAS: 기대값이 0이면 1로 변경, 아니면 실패
                        if (atomicValue.compareAndSet(0, 1)) {
                            successCount.incrementAndGet();
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }

            // 정확히 1개의 스레드만 성공
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(atomicValue.get()).isEqualTo(1);
        }
    }
}
