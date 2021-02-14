package co.laptev.logproducer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunner implements CommandLineRunner {
    private final LogProducer logProducer;

    public ApplicationRunner(LogProducer logProducer) {
        this.logProducer = logProducer;
    }

    @Override
    public void run(String... args) {
        logProducer.run();
    }
}