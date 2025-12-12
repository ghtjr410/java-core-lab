package concurrency;

import static org.assertj.core.api.Assertions.*;

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
}
