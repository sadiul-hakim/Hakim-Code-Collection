import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


@BenchmarkMode(Mode.AverageTime) // Measure average execution time
@OutputTimeUnit(TimeUnit.MILLISECONDS) // Results in milliseconds
@State(Scope.Thread) // Each thread gets its own instance
class MyBenchMark {

    @Benchmark
    public int computeSum() {
        int sum = 0;
        for (int i = 0; i < 100_000_000; i++) {
            sum += i;
        }
        return sum;
    }
}


class Main {
    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(MyBenchMark.class.getSimpleName()) // Select benchmark class
                .forks(1) // Number of JVM forks
                .build();

        new Runner(opt).run();
    }
}

/*

        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-core</artifactId>
            <version>1.37</version>
        </dependency>
        <dependency>
            <groupId>org.openjdk.jmh</groupId>
            <artifactId>jmh-generator-annprocess</artifactId>
            <version>1.37</version>
        </dependency>

*/
