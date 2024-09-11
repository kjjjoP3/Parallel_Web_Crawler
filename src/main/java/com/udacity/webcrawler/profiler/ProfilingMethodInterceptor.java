package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with
 * the {@link Profiled} annotation. If they are, the method interceptor records
 * how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

	private final Clock clock;
	private final ProfilingState state;
	private final Object delegate;

	// Additional instance fields can be added if necessary.
	ProfilingMethodInterceptor(Clock clock, ProfilingState state, Object delegate) {
		this.clock = Objects.requireNonNull(clock, "Clock cannot be null.");
		this.delegate = Objects.requireNonNull(delegate, "Delegate cannot be null.");
		this.state = Objects.requireNonNull(state, "ProfilingState cannot be null.");
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// Check if the invoked method is annotated with @Profiled
		boolean isProfiled = method.isAnnotationPresent(Profiled.class);
		Instant startTime = clock.instant(); // Record the start time

		try {
			return method.invoke(delegate, args); // Invoke the actual method
		} catch (InvocationTargetException e) {
			throw e.getTargetException(); // Rethrow the underlying exception
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Method access error: " + e.getMessage(), e);
		} finally {
			// If the method is profiled, record the duration
			if (isProfiled) {
				Instant endTime = clock.instant();
				Duration duration = Duration.between(startTime, endTime);
				state.record(delegate.getClass(), method, duration); // Log the duration
			}
		}
	}
}
