import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.StructuredTaskScope.Joiner.allSuccessfulOrThrow;
import static java.util.concurrent.StructuredTaskScope.Joiner.anySuccessfulResultOrThrow;

public class Java25StructuredTaskScope {
    public static void main(String[] args) {
        twoSuccessfulJoiner();
    }

    private static void anySuccessfulResultOrThrowTest() {
        try (var scope = StructuredTaskScope.open(anySuccessfulResultOrThrow())) {
            scope.fork(() -> task(4, false));
            scope.fork(() -> task(3, false));
            scope.fork(() -> task(5, false));
            scope.fork(() -> task(1, false));

            Object result = scope.join(); // It's a race of who can finish faster.

            System.out.println(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void allSuccessfulOrThrowTest() {
        try (var scope = StructuredTaskScope.open(allSuccessfulOrThrow())) {
            var task1 = scope.fork(() -> task(2, false));
            var task2 = scope.fork(() -> task(3, true));

            scope.join();

            String text1 = task1.get();
            String text2 = task2.get();

            System.out.println(text1); // No Result
            System.out.println(text2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void defaultTest() {
        try (var scope = StructuredTaskScope.open()) {
            var task1 = scope.fork(() -> task(2, false));
            var task2 = scope.fork(() -> task(3, true));

            scope.join();

            String text1 = task1.get();
            String text2 = task2.get();

            System.out.println(text1); // No Result
            System.out.println(text2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void withTimeout() {
        try (
                var scope = StructuredTaskScope.open(allSuccessfulOrThrow(),
                        cf -> cf.withTimeout(Duration.ofSeconds(4)))
        ) {
            var task1 = scope.fork(() -> task(4, false));
            var task2 = scope.fork(() -> task(3, false));

            scope.join();

            String text1 = task1.get();
            String text2 = task2.get();

            System.out.println(text1); // No Result
            System.out.println(text2);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void twoSuccessfulJoiner() {
        try (
                var scope = StructuredTaskScope.open(new TargetSuccessJoiner<String>(2))
        ) {
            scope.fork(() -> task(4, true));
            scope.fork(() -> task(3, true));
            scope.fork(() -> task(2, true));
            scope.fork(() -> task(5, false));

            List<String> results = scope.join();
            System.out.println("✅ Collected: " + results);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String task(int sec, boolean fail) {
        try {
            TimeUnit.SECONDS.sleep(sec);

            if (fail) {
                throw new RuntimeException("Task Failed");
            }

            return String.format("Task finished in %s sec", sec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class TargetSuccessJoiner<T> implements StructuredTaskScope.Joiner<T, List<T>> {

        private final int targetCount;
        private final List<T> successfulResults =
                Collections.synchronizedList(new ArrayList<>());
        private final List<Throwable> exceptions =
                Collections.synchronizedList(new ArrayList<>());

        TargetSuccessJoiner(int targetCount) {
            this.targetCount = targetCount;
        }

        @Override
        public boolean onComplete(StructuredTaskScope.Subtask<? extends T> subtask) {
            switch (subtask.state()) {
                case SUCCESS -> {
                    System.out.println("✅ Success: " + subtask.get());
                    successfulResults.add(subtask.get());
                    if (successfulResults.size() >= targetCount) {
                        System.out.println("🚦 Reached " + targetCount + " successes — shutting down scope!");
                        return true;
                    }
                }
                case FAILED -> {
                    System.out.println("❌ Failure: " + subtask.exception());
                    exceptions.add(subtask.exception());
                }
                case UNAVAILABLE -> {
                }
            }

            return false;
        }

        @Override
        public List<T> result() throws Throwable {
            if (successfulResults.size() >= targetCount) {
                return List.copyOf(successfulResults);
            } else {
                Exception ex = new Exception("Fewer than " + targetCount + " tasks succeeded");

                // This line attaches all those subtask exceptions to your main exception as suppressed exceptions.
                // That means if two subtasks failed with different stack traces, you don’t lose that information
                // it’s all preserved inside ex.
                exceptions.forEach(ex::addSuppressed);
                throw ex;
            }
        }
    }
}
