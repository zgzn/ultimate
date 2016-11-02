package de.uni_freiburg.informatik.ultimate.deltadebugger.core.search.speculation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

import de.uni_freiburg.informatik.ultimate.deltadebugger.core.exceptions.MissingTestResultException;
import de.uni_freiburg.informatik.ultimate.deltadebugger.core.exceptions.UncheckedInterruptedException;
import de.uni_freiburg.informatik.ultimate.deltadebugger.core.search.ISearchStep;

/**
 * Runs a speculative search on an arbitrary number of worker threads with a given test function.
 *
 * @param <T>
 *            search step type
 */
public class ParallelSearchIteratorIterator<T extends ISearchStep<?, T>> {
	private final SpeculativeSearchIterator<T> mSearchIterator;
	private final CancelableStepTest<T> mCancelableTest;
	private final List<Future<?>> mPendingWorkers = new ArrayList<>();
	private volatile boolean mStopRequested;

	/**
	 * Constructs a new instance that can be used for one iteration.
	 *
	 * Note that all concurrent access to the SpeculativeSearchIterator is synchronized on the searchIterator instance
	 * and only the test function if executed without any synchronization.
	 *
	 * @param searchIterator
	 *            speculative iterator to work on
	 * @param cancelableTest
	 *            test function that is called to determine test results
	 */
	public ParallelSearchIteratorIterator(final SpeculativeSearchIterator<T> searchIterator,
			final CancelableStepTest<T> cancelableTest) {
		this.mSearchIterator = searchIterator;
		this.mCancelableTest = cancelableTest;
	}

	/**
	 * Start iteration using the given number of worker threads that are started by the given executor service.
	 *
	 * @param executorService
	 * @param workerCount
	 */
	public void beginIteration(final ExecutorService executorService, final int workerCount) {
		if (workerCount < 1) {
			throw new IllegalArgumentException();
		}
		if (!mPendingWorkers.isEmpty()) {
			throw new IllegalStateException("beginIteration already called");
		}
		for (int i = 0; i != workerCount; ++i) {
			mPendingWorkers.add(executorService.submit(this::worker));
		}
	}

	/**
	 * Wait until iteration has ended and return the result.
	 *
	 * @return current result.
	 * @throws InterruptedException
	 */
	public T endIteration() throws InterruptedException {
		if (mPendingWorkers.isEmpty()) {
			throw new IllegalStateException("beginIteration has not been called");
		}

		// Wait for all workers to return.
		// This is important to ensure that
		// - no exceptions are swallowed
		// - no new (potentially expensive) tests are started before the
		// previous ones have completed
		// - all parallel execution is limited to this method
		try {
			for (final Future<?> f : mPendingWorkers) {
				f.get();
			}
		} catch (final ExecutionException e) {
			final Throwable inner = e.getCause();
			if (inner instanceof Error) {
				throw (Error) inner;
			}
			if (inner instanceof RuntimeException) {
				throw (RuntimeException) inner;
			}
			throw new RuntimeException("unexpected sneaky exception", e);
		}

		return getCurrentStep();
	}

	/**
	 * @return the current step of the non-speculative iteration
	 */
	public T getCurrentStep() {
		synchronized (mSearchIterator) {
			return mSearchIterator.getCurrentStep();
		}
	}

	private ISpeculativeTask<T> getNextTask() {
		while (true) {
			synchronized (mSearchIterator) {
				final ISpeculativeTask<T> task = mSearchIterator.getNextTask();
				if (task != null) {
					return task;
				}
			}

			// Currently there is no speculative step available,
			// but not all pending tasks have completed yet so there
			// may be further steps available later
			// -> wait for more tasks to complete
			// Should use a more sophisticated event mechanism to wake
			// up once another task has completed, and also ensure
			// that there is another task pending...
			try {
				TimeUnit.MILLISECONDS.sleep(10);
			} catch (final InterruptedException e) {
				// There is no expected interruption, if this happens it's
				// like any other unexpected runtime exception
				Thread.currentThread().interrupt();
				throw new UncheckedInterruptedException(e);
			}
		}
	}

	public boolean isStopRequested() {
		return mStopRequested;
	}

	/**
	 * Begin and wait for iteration to end, then return the step.
	 *
	 * @param executorService
	 * @param workerCount
	 * @return the step reached at the end of iteration
	 */
	public T iterateToEnd(final ExecutorService executorService, final int workerCount) {
		beginIteration(executorService, workerCount);
		try {
			return endIteration();
		} catch (final InterruptedException unexpected) {
			Thread.currentThread().interrupt();
			throw new UncheckedInterruptedException(unexpected);
		}
	}

	/**
	 * Checks if iteration has ended (either successfully or non-successfully) without blocking.
	 *
	 * @return true if iteration has ended
	 */
	public boolean pollIsDone() {
		if (mPendingWorkers.isEmpty()) {
			throw new IllegalStateException("beginIteration has not been called");
		}
		return mPendingWorkers.stream().allMatch(Future::isDone);
	}

	private void runTestAndCompleteTask(final ISpeculativeTask<T> task) {
		final BooleanSupplier isCanceled = () -> task.isCanceled() || isStopRequested();
		final Optional<Boolean> result = mCancelableTest.test(task.getStep(), isCanceled);
		if (!result.isPresent()) {
			// A test is only allowed to return no result if cancelation
			// was actually requested. To handle this case we could only
			// abort the whole iteration or repeat the test.
			// Iteration control is not the responsibility of the test
			// function and repeating the test with a broken
			// test function sounds like bad idea.
			if (!isCanceled.getAsBoolean()) {
				throw new MissingTestResultException();
			}
			return;
		}
		synchronized (mSearchIterator) {
			task.complete(result.get());
		}
	}

	/**
	 * Request workers to stop the iteration.
	 */
	public void stopWorkers() {
		mStopRequested = true;
	}

	/**
	 * Wait until iteration has ended for a limited timespan only.
	 *
	 * @param timeout
	 * @param unit
	 * @return true if all workers have ended at the time of return
	 * @throws InterruptedException
	 */
	public boolean waitForEnd(final long timeout, final TimeUnit unit) throws InterruptedException {
		long nanosLeft = unit.toNanos(timeout);
		final long deadline = System.nanoTime() + nanosLeft;

		for (final Future<?> f : mPendingWorkers) {
			if (!f.isDone()) {
				if (nanosLeft <= 0L) {
					return false;
				}
				try {
					f.get(nanosLeft, TimeUnit.NANOSECONDS);
				} catch (CancellationException | ExecutionException e) {
					// deferr exception to endIteration
				} catch (final TimeoutException e) {
					return false;
				}
				nanosLeft = deadline - System.nanoTime();
			}
		}
		return true;
	}

	private void worker() {
		try {
			while (!isStopRequested()) {
				final ISpeculativeTask<T> task = getNextTask();
				if (task.isDone()) {
					return;
				}
				runTestAndCompleteTask(task);
			}
		} catch (final Exception e) {
			stopWorkers();
			throw e;
		}
	}
}
