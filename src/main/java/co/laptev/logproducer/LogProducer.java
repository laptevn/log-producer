package co.laptev.logproducer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class LogProducer implements DisposableBean {
    private static final int DESTROY_TIMEOUT = 2;

    private static final Logger logger = LogManager.getLogger(LogProducer.class);

    private final int workersCount;
    private final int itemsPerWorker;
    private final int pauseBetweenProducing;
    private final ExecutorService executorService;

    public LogProducer(
            @Value("${workers}") int workersCount,
            @Value("${items-per-worker}") int itemsPerWorker,
            @Value("${pause-between-producing}") int pauseBetweenProducing) {

        this.workersCount = workersCount;
        this.itemsPerWorker = itemsPerWorker;
        this.pauseBetweenProducing = pauseBetweenProducing;

        executorService = Executors.newFixedThreadPool(workersCount);
    }

    public void run() {
        CompletableFuture[] futures = new CompletableFuture[workersCount];
        for (int taskIndex = 0; taskIndex < workersCount; ++taskIndex) {
            futures[taskIndex] = (CompletableFuture.runAsync(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    for (int i = 0; i < itemsPerWorker; ++i) {
                        logger.info("Sample log entry {}", i);
                    }
                    try {
                        Thread.sleep(pauseBetweenProducing);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, executorService));
        }
        CompletableFuture.allOf(futures).join();
    }

    @Override
    public void destroy() {
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(DESTROY_TIMEOUT, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}