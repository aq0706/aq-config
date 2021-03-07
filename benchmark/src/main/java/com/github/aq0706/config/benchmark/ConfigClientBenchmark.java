package com.github.aq0706.config.benchmark;

import com.github.aq0706.config.client.ConfigClient;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class ConfigClientBenchmark {

    private ConfigClient configClient;

    @Setup
    public void setUp() {
        configClient = new ConfigClient("http://127.0.0.1", 17060);
    }

    @Benchmark
    @Fork(value = 2)
    @Measurement(iterations = 10, time = 5)
    @Warmup(iterations = 5, time = 5)
    @Threads(10)
    public void get() {
        configClient.get("namespace", "appName", "key");
    }
}
