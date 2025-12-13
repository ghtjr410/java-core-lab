package concurrency;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class SynchronizedTest {

    @Nested
    class synchronized_메서드 {

        /**
         * synchronized 메서드
         * - 인스턴스 메서드: this를 락으로 사용
         * - static 메서드: 클래스 객체를 락으로 사용
         * - 메서드 전체가 임계 영역
         */
        class Counter {
            private int count = 0;

            public synchronized void increment() {
                count++;
            }

            public synchronized int getCount() {
                return count;
            }
        }

        @RepeatedTest(5)
        void synchronized_메서드는_스레드_안전() throws InterruptedException {
            Counter counter = new Counter();
            int threadCount = 100;
            int incrementPerThread = 1000;

            try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
                CountDownLatch latch = new CountDownLatch(threadCount);

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        for (int j = 0; j < incrementPerThread; j++) {
                            counter.increment();
                        }
                        latch.countDown();
                    });
                }

                latch.await();
                executor.shutdown();
            }

            assertThat(counter.getCount()).isEqualTo(threadCount * incrementPerThread);
        }

        @Test
        void 같은_인스턴스의_synchronized_메서드는_같은_락_사용() throws InterruptedException {
            List<String> order = new ArrayList<>();

            class SharedResource {
                public synchronized void methodA() throws InterruptedException {
                    order.add("A-start");
                    Thread.sleep(100);
                    order.add("A-end");
                }

                public synchronized void methodB() throws InterruptedException {
                    order.add("B-start");
                    Thread.sleep(100);
                    order.add("B-end");
                }
            }

            SharedResource resource = new SharedResource();
            CountDownLatch latch = new CountDownLatch(2);

            new Thread(() -> {
                        try {
                            resource.methodA();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            latch.countDown();
                        }
                    })
                    .start();

            Thread.sleep(10); // methodA가 먼저 락 획득하도록

            new Thread(() -> {
                        try {
                            resource.methodB();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            latch.countDown();
                        }
                    })
                    .start();

            latch.await();

            // methodA가 끝나야 methodB 시작 (같은 락이라서)
            assertThat(order).containsExactly("A-start", "A-end", "B-start", "B-end");
        }
    }
}
