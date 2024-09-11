package com.udacity.webcrawler.profiler;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import javax.inject.Inject;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

    private final Clock clock;
    private final ProfilingState state = new ProfilingState();
    private final ZonedDateTime startTime;

    @Inject
    ProfilerImpl(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "Clock must not be null.");
        this.startTime = ZonedDateTime.now(clock);
    }

    @Override
    public <T> T wrap(Class<T> klass, T delegate) throws IllegalArgumentException {
        Objects.requireNonNull(klass, "Class must not be null.");
        if (!Arrays.stream(klass.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(Profiled.class))) {
            throw new IllegalArgumentException("Profiled annotation not found in " + klass.getName());
        }

        // Create a dynamic proxy to wrap the delegate with ProfilingMethodInterceptor
        ProfilingMethodInterceptor handler = new ProfilingMethodInterceptor(clock, state, delegate);
        Object proxy = Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                new Class<?>[] { klass }, handler);
        return klass.cast(proxy);
    }

    @Override
    public void writeData(Path path) {
        // Write the ProfilingState data to the specified file path, appending if the file exists
        try (Writer writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            writeData(writer);
        } catch (IOException e) {
            // Handle exception (could log it or rethrow)
            System.err.println("Error writing profiling data: " + e.getMessage());
        }
    }

    @Override
    public void writeData(Writer writer) throws IOException {
        // Write the start time and profiling state to the writer
        writer.write("Run at " + DateTimeFormatter.RFC_1123_DATE_TIME.format(startTime));
        writer.write(System.lineSeparator());
        state.write(writer);
        writer.write(System.lineSeparator());
    }
}
