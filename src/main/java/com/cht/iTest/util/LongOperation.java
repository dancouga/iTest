package com.cht.iTest.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.sys.DesktopCache;
import org.zkoss.zk.ui.sys.DesktopCtrl;
import org.zkoss.zk.ui.sys.WebAppCtrl;

public abstract class LongOperation implements Runnable {

	private String desktopId;
	private DesktopCache desktopCache;
	private Thread thread;
	private AtomicBoolean cancelled = new AtomicBoolean(false);
	private UUID taskId = UUID.randomUUID();

	/**
	 * asynchronous callback for your long operation code
	 * 
	 * @throws InterruptedException
	 */
	protected abstract void execute() throws InterruptedException;

	/**
	 * optional callback method when the task has completed successfully
	 */
	protected void onFinish() {
	};

	/**
	 * optional callback method when the task has been cancelled or was
	 * interrupted otherwise
	 */
	protected void onCancel() {
	};

	/**
	 * optional callback method when the task has completed with an uncaught
	 * RuntimeException
	 * 
	 * @param exception
	 */
	protected void onException(RuntimeException exception) {
	};

	/**
	 * optional callback method when the task has completed (always called)
	 */
	protected void onCleanup() {
	};

	public final void cancel() {
		cancelled.set(true);
		thread.interrupt();
	}

	public final boolean isCancelled() {
		return cancelled.get();
	}

	protected final void activate() throws InterruptedException {
		Executions.activate(getDesktop());
	}

	protected final void deactivate() {
		Executions.deactivate(getDesktop());
	}

	protected final void checkCancelled() throws InterruptedException {
		if (Thread.currentThread() != this.thread) {
			throw new IllegalStateException("this method can only be called in the worker thread (i.e. during execute)");
		}

		boolean interrupted = Thread.interrupted();

		if (interrupted || cancelled.get()) {
			cancelled.set(true);
			throw new InterruptedException();
		}
	}

	public final void start() {
		this.desktopId = Executions.getCurrent().getDesktop().getId();
		this.desktopCache = ((WebAppCtrl) WebApps.getCurrent()).getDesktopCache(Sessions.getCurrent());
		serverPushForThisTask(true);
		thread = new Thread(this);
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
	}

	@Override
	public final void run() {
		try {
			try {
				checkCancelled();
				execute();
				checkCancelled();
				activate();
				onFinish();
				deactivate();
			} catch (InterruptedException e) {
				try {
					cancelled.set(true);
					activate();
					onCancel();
					deactivate();
				} catch (InterruptedException e1) {
					throw new RuntimeException("interrupted onCancel handling", e1);
				}
			} catch (RuntimeException rte) {
				try {
					activate();
					onException(rte);
					deactivate();
				} catch (InterruptedException e1) {
					throw new RuntimeException("interrupted onException handling", e1);
				}
				throw rte;
			}
		} finally {
			try {
				activate();
				onCleanup();
				deactivate();
			} catch (InterruptedException e1) {
				throw new RuntimeException("interrupted onCleanup handling", e1);
			} finally {
				serverPushForThisTask(false);
			}
		}
	}

	private void serverPushForThisTask(boolean enable) {
		((DesktopCtrl) getDesktop()).enableServerPush(enable, taskId);
	}

	private Desktop getDesktop() {
		return desktopCache.getDesktop(desktopId);
	}
}