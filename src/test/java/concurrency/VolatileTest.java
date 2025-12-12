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
}
