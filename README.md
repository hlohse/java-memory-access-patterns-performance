# Memory Access Patterns & Performance

A demo for memory access patterns & performance in Java for a lightning talk at HiveMQ.

## Build

```
mvn package
```

## Run

```
java -jar target/lightning-talk-demo.jar <dataStructure> <numValues> <numIterations>
    dataStructure  int  Ordinal of the DataStructure enum to use. See Main.java.
    numValues      int  Number of Values to generate randomly and sum up.
    numIterations  int  Number of benchmark iterations to run and calculate the mean runtime of.
```
Then hit enter when requested.

For example:
```
hlohse@Hennings-MBP java-memory-access-patterns-performance % java -jar target/lightning-talk-demo.jar 0 1000000 10
PRIMITIVE_ARRAY with 1000000 values and 10 iterations.
Press enter to start.

Sum -3297155703793952598 @ 1 ms mean runtime per iteration.
```

## Profiling (Linux/CentOS)

Allow user mode access to performance counters:
```
sudo sysctl -w kernel.perf_event_paranoid=-1
```

Find the PID of the running process (that's waiting for you to hit enter), for example via:
```
ps -faux | grep lightning
```

Then use attach `perf` to the process, for example via:
```
sudo perf stat -e L1-dcache-loads,L1-dcache-load-misses,cache-references,cache-misses,branches -p <PID>
```

Then hit enter in the waiting process.
