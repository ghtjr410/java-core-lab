package concurrency;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class VolatileTest {

    @Nested
    class 가시성_문제 {

        /**
         * 한 스레드가 값을 바꿨다고 해서
         * 다른 스레드가 그 변경을 “반드시” 보는 것은 아니다.
         *
         * 가시성(Visibility) 문제란?
         * - 한 스레드가 변경한 값을 다른 스레드가 보지 못하는 현상
         * - 각 스레드는 CPU 캐시에 변수 복사본을 가질 수 있음
         * - 메인 메모리와 동기화되지 않으면 변경 사항 못 봄
         *
         * [Thread A CPU Cache] ← 복사 → [Main Memory] ← 복사 → [Thread B CPU Cache]
         *      flag = true             flag = false             flag = false
         *                              (동기화 안됨)
         */
        private boolean running = true;

        @Test
        @Timeout(value = 3, unit = TimeUnit.SECONDS)
        void volatile_없으면_무한루프_가능성() throws InterruptedException {
            // 이 테스트는 환경에 따라 다르게 동작할 수 있음
            // JVM 최적화로 인해 무한루프에 빠질 수 있음

            running = true;
            AtomicInteger loopCount = new AtomicInteger(0);

            Thread worker = new Thread(() -> {
                while (running) {
                    loopCount.incrementAndGet();
                    // 빈 루프 - JVM이 running 값을 캐싱할 수 있음
                }
            });

            worker.start();
            Thread.sleep(100);

            running = false; // 메인 스레드에서 변경
            worker.join(1000); // 1초 대기

            System.out.println("루프 횟수: " + loopCount.get());
            System.out.println("스레드 상태: " + worker.getState());

            // 환경에 따라 스레드가 종료되지 않을 수 있음
            // @Timeout으로 테스트 보호
        }
    }

    @Nested
    class volatile로_가시성_보장 {

        private volatile boolean running = true;

        @Test
        @Timeout(value = 3, unit = TimeUnit.SECONDS)
        void volatile_키워드로_가시성_보장() throws InterruptedException {
            running = true;
            AtomicInteger loopCount = new AtomicInteger(0);

            Thread worker = new Thread(() -> {
                while (running) {
                    loopCount.incrementAndGet();
                }
            });

            worker.start();
            Thread.sleep(100);

            running = false; // volatile이라 다른 스레드에서 즉시 보임
            worker.join(1000);

            System.out.println("루프 횟수: " + loopCount.get());
            assertThat(worker.getState()).isEqualTo(Thread.State.TERMINATED);
        }
    }

    @Nested
    class volatile은_원자성을_보장하지_않는다 {

        /**
         * volatile의 한계
         * - 가시성만 보장, 원자성은 보장하지 않음
         * - volatile int count; count++ 는 여전히 위험
         * - 읽기 → 수정 → 쓰기가 원자적이지 않음
         */
        private volatile int count = 0;

        @Test
        void volatile이어도_복합연산은_Race_Condition_발생() throws InterruptedException {
            count = 0;
            int threadCount = 100;
            int incrementPerThread = 1000;

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        for (int j = 0; j < incrementPerThread; j++) {
                            count++; // volatile이지만 원자적이지 않음!
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }

            int expected = threadCount * incrementPerThread;
            System.out.println("기대값: " + expected + ", 실제값: " + count);

            // 거의 항상 기대값보다 작음
            assertThat(count).isLessThanOrEqualTo(expected);
        }
    }

    @Nested
    class volatile_올바른_사용_케이스 {

        /**
         * volatile을 써야 하는 경우:
         * 1. 단순 플래그 (boolean)
         * 2. 한 스레드만 쓰고, 여러 스레드가 읽는 경우
         * 3. 독립적인 값 (다른 변수와 연관 없음)
         */
        @Nested
        class 종료_플래그 {

            private volatile boolean stopRequested = false;

            @Test
            void 종료_플래그로_사용() throws InterruptedException {
                stopRequested = false;
                AtomicInteger workCount = new AtomicInteger(0);

                Thread worker = new Thread(() -> {
                    while (!stopRequested) {
                        workCount.incrementAndGet();
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                });

                worker.start();
                Thread.sleep(100);

                stopRequested = true; // 종료 요청
                worker.join(1000);

                assertThat(worker.getState()).isEqualTo(Thread.State.TERMINATED);
                assertThat(workCount.get()).isGreaterThan(0);
            }
        }

        @Nested
        class 단일_쓰기_다중_읽기 {

            private volatile String latestValue = "";

            @Test
            void 한_스레드만_쓰고_여러_스레드가_읽기() throws InterruptedException {
                int readerCount = 5;
                CountDownLatch startLatch = new CountDownLatch(1);
                CountDownLatch endLatch = new CountDownLatch(readerCount + 1);
                AtomicInteger readCount = new AtomicInteger(0);

                // Reader 스레드들
                for (int i = 0; i < readerCount; i++) {
                    new Thread(() -> {
                                try {
                                    startLatch.await();
                                    for (int j = 0; j < 100; j++) {
                                        String value = latestValue; // 읽기만
                                        if (!value.isEmpty()) {
                                            readCount.incrementAndGet();
                                        }
                                        Thread.sleep(1); // Reader도 약간의 딜레이
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } finally {
                                    endLatch.countDown();
                                }
                            })
                            .start();
                }

                // Writer 스레드 (단일)
                new Thread(() -> {
                            try {
                                startLatch.await();
                                for (int i = 0; i < 100; i++) {
                                    latestValue = "value-" + i; // 쓰기
                                    Thread.sleep(1);
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            } finally {
                                endLatch.countDown(); // Writer도 완료 신호
                            }
                        })
                        .start();

                startLatch.countDown(); // 시작
                endLatch.await(); // 모든 스레드 완료 대기

                assertThat(readCount.get()).isGreaterThan(0);
            }
        }
    }

    @Nested
    class volatile_vs_synchronized_vs_Atomic {

        /**
         * 비교:
         * - volatile: 가시성만 보장, 원자성 X, 가장 가벼움
         * - synchronized: 가시성 + 원자성, 락 사용, 무거움
         * - Atomic: 가시성 + 원자성, CAS 사용, 중간
         */
        private volatile int volatileCount = 0;

        private int syncCount = 0;
        private final AtomicInteger atomicCount = new AtomicInteger(0);

        private synchronized void syncIncrement() {
            syncCount++;
        }

        @Test
        void 세_가지_방식_비교() throws InterruptedException {
            int threadCount = 10;
            int incrementPerThread = 10000;

            // volatile (원자성 없음 - 부정확한 결과)
            volatileCount = 0;
            runThreads(threadCount, incrementPerThread, () -> volatileCount++);
            int volatileResult = volatileCount;

            // synchronized (정확하지만 느림)
            syncCount = 0;
            runThreads(threadCount, incrementPerThread, this::syncIncrement);
            int syncResult = syncCount;

            // Atomic (정확하고 빠름)
            atomicCount.set(0);
            runThreads(threadCount, incrementPerThread, atomicCount::incrementAndGet);
            int atomicResult = atomicCount.get();

            int expected = threadCount * incrementPerThread;

            System.out.println("Expected: " + expected);
            System.out.println("volatile: " + volatileResult + " (원자성 없음)");
            System.out.println("synchronized: " + syncResult);
            System.out.println("Atomic: " + atomicResult);

            assertThat(syncResult).isEqualTo(expected);
            assertThat(atomicResult).isEqualTo(expected);
            // volatileResult는 expected보다 작을 가능성 높음
        }

        private void runThreads(int threadCount, int incrementPerThread, Runnable task) throws InterruptedException {
            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        for (int j = 0; j < incrementPerThread; j++) {
                            task.run();
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }
        }
    }
}
