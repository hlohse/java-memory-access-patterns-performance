import org.jetbrains.annotations.NotNull;
import org.openjdk.jol.info.ClassLayout;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Main {

    private static final long RANDOM_SEED = 123456789;
    private static final @NotNull Random RANDOM = new Random(RANDOM_SEED);

    private enum DataStructure {
        PRIMITIVE_ARRAY,
        BOXED_ARRAY,
        ARRAY_LIST,
        LINKED_LIST_CREATION_ORDER,
        LINKED_LIST_RANDOM_ORDER,
        TREE_SET;

        private static @NotNull DataStructure of(final int ordinal) {
            Objects.checkIndex(ordinal, DataStructure.values().length);
            return DataStructure.values()[ordinal];
        }
    }

    /**
     * @param args dataStructure numValues numIterations
     *             dataStructure  int  Ordinal of the DataStructure enum to use.
     *             numValues      int  Number of Values to generate randomly and sum up.
     *             numIterations  int  Number of benchmark warmups & iterations to run and calculate the mean runtime of.
     */
    public static void main(final String @NotNull [] args) throws IOException {
        //System.out.println(ClassLayout.parseClass(Long.class).toPrintable());
        //final Integer[] boxedIntegerArray = new Integer[10];
        //System.out.println(ClassLayout.parseInstance(boxedIntegerArray).toPrintable());
        /*
        final ArrayList<Long> arrayList = new ArrayList<>(2);
        arrayList.add(3234523523L);
        arrayList.add(5647676557L);
        System.out.println(ClassLayout.parseInstance(arrayList).toPrintable());
         */
        /*
        final LinkedList<Object> linkedList = new LinkedList<>();
        linkedList.add(3234523523L);
        linkedList.add(5647676557L);
        System.out.println(ClassLayout.parseInstance(linkedList).toPrintable());
         */

        if (args.length != 3) {
            throw new IllegalArgumentException("Expected 3 arguments but got: " + args.length);
        }
        final DataStructure dataStructure = DataStructure.of(Integer.parseInt(args[0]));
        final int numValues = Integer.parseInt(args[1]);
        if (numValues <= 0) {
            throw new IllegalArgumentException("numValues must be > 0 but was: " + numValues);
        }
        final int numIterations = Integer.parseInt(args[2]);
        if (numIterations < 1) {
            throw new IllegalArgumentException("numIterations must be >= 1 but was: " + numIterations);
        }
        System.out.println(dataStructure + " with " + numValues + " values and " + numIterations + " iterations.");

        switch (dataStructure) {

            case PRIMITIVE_ARRAY: {
                final long[] values = new long[numValues];
                fillWithRandomValues((i, value) -> values[i] = value, numValues);
                runBenchmark(
                        () -> {
                            long sum = 0;
                            for (long l : values) {
                                sum += l;
                            }
                            return sum;
                        },
                        numIterations);
            } break;

            case BOXED_ARRAY: {
                final Long[] values = new Long[numValues];
                fillWithRandomValues((i, value) -> values[i] = value, numValues);
                runBenchmark(
                        () -> {
                            long sum = 0;
                            for (long l : values) {
                                sum += l;
                            }
                            return sum;
                        },
                        numIterations);
            } break;

            case ARRAY_LIST: {
                final Collection<Long> values = new ArrayList<>(numValues);
                fillWithRandomValues(values, numValues);
                runBenchmark(newSumSupplierFor(values), numIterations);
            } break;

            case LINKED_LIST_CREATION_ORDER: {
                final Collection<Long> values = new LinkedList<>();
                fillWithRandomValues(values, numValues);
                runBenchmark(newSumSupplierFor(values), numIterations);
            } break;

            case LINKED_LIST_RANDOM_ORDER: {
                final List<Long> shuffledValues = new ArrayList<>(numValues);
                fillWithRandomValues(shuffledValues, numValues);
                Collections.shuffle(shuffledValues, RANDOM);
                final Collection<Long> values = new LinkedList<>(shuffledValues);
                runBenchmark(newSumSupplierFor(values), numIterations);
            } break;

            case TREE_SET: {
                final Collection<Long> values = new TreeSet<>();
                fillWithRandomValues(values, numValues);
                runBenchmark(newSumSupplierFor(values), numIterations);
            } break;

            default:
                throw new UnsupportedOperationException("Unknown DataStructure: " + dataStructure);
        }
    }

    private static void fillWithRandomValues(final @NotNull Collection<Long> collection, final int numValues) {
        fillWithRandomValues((i, value) -> collection.add(value), numValues);
    }

    private static void fillWithRandomValues(final @NotNull BiConsumer<Integer, Long> consumer, final int numValues) {
        IntStream.range(0, numValues).forEach(i -> consumer.accept(i, RANDOM.nextLong()));
    }

    private static Supplier<Long> newSumSupplierFor(final @NotNull Collection<Long> values) {
        return () -> {
            long sum = 0;
            for (final long value : values) {
                sum += value;
            }
            return sum;
        };
    }

    private static void runBenchmark(final @NotNull Supplier<Long> sumSupplier, final int numIterations) throws IOException {
        System.out.println("Warming up...");
        final long warmupSum = calculateSum(sumSupplier, numIterations);
        System.out.println("Sum " + warmupSum);
        System.out.println("Press enter to start benchmark.");
        @SuppressWarnings("unused")
        final int readReturnCode = System.in.read();
        final long startMs = System.currentTimeMillis();
        final long benchmarkSum = calculateSum(sumSupplier, numIterations);
        final long stopMs = System.currentTimeMillis();
        final long totalRuntimeMs = stopMs - startMs;
        final long averageRuntimeMs = totalRuntimeMs / numIterations;
        System.out.println("Sum " + benchmarkSum + " @ " + averageRuntimeMs + " ms mean runtime per iteration.");
    }

    private static long calculateSum(final @NotNull Supplier<Long> sumSupplier, final int numIterations) {
        long sum = 0;
        for (int i = 0; i < numIterations; i++) {
            sum += sumSupplier.get();
        }
        return sum;
    }
}