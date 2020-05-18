package org.sevensource.magnolia.backup.executor.gc;

import com.google.inject.Inject;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.wrapper.DelegateSessionWrapper;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.gc.GarbageCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class RepositoryGarbageCollectorExecutor {

	private static final Logger logger = LoggerFactory.getLogger(RepositoryGarbageCollectorExecutor.class);

	private static final ReentrantLock lock = new ReentrantLock();

	private final SystemContext ctx;

	@Inject
	public RepositoryGarbageCollectorExecutor(SystemContext ctx) {
		this.ctx = ctx;
	}

    public void run() {
		final StopWatch stopWatch = StopWatch.createStarted();

    	if(! lock.tryLock()) {
    		logger.warn("Another instance of this GarbageCollector seems to be running, aborting...");
    		return;
    	}

		try {
			logger.info("Running Garbage Collection on JCR Repository");
			final Session session = ctx.getJCRSession("default");
			final SessionImpl jrSession = getJackrabbitSession( session );
			final GarbageCollector gc = jrSession.createDataStoreGarbageCollector();
			gc.mark();
			gc.sweep();
			gc.close();

			logger.info("Successfully finished executing Garbage Collection on JCR Repository in {} seconds", stopWatch.getTime(TimeUnit.SECONDS));

		} catch (LoginException e) {
			throw new RuntimeException("Cannot Login", e);
		} catch (RepositoryException e) {
			throw new RuntimeException("Cannot run Garbage Collection on Repository", e);
		} finally {
			lock.unlock();
			ctx.release();
		}
    }

	private SessionImpl getJackrabbitSession(Session session) {
		while (session instanceof DelegateSessionWrapper) {
			session = ((DelegateSessionWrapper) session).getWrappedSession();
		}

		if(! (session instanceof SessionImpl)) {
			throw new IllegalArgumentException("Session is not of type " + SessionImpl.class.getName());
		}

		return (SessionImpl) session;
	}
}
