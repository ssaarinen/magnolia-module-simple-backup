package org.sevensource.magnolia.backup.executor.gc;

import java.util.concurrent.locks.ReentrantLock;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.gc.GarbageCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import info.magnolia.context.SystemContext;
import info.magnolia.jcr.wrapper.DelegateSessionWrapper;

public class RepositoryGarbageCollectorExecutor {

	private static final Logger logger = LoggerFactory.getLogger(RepositoryGarbageCollectorExecutor.class);
	
	private static final ReentrantLock lock = new ReentrantLock();
	
	private final SystemContext ctx;
	
	@Inject
	public RepositoryGarbageCollectorExecutor(SystemContext ctx) {
		this.ctx = ctx;
	}
	
    public void run() {
    	if(! lock.tryLock()) {
    		logger.warn("Cannot acquire lock in order to run Garbage Collection on Jackrabbit Repository");
    	}
    	
		try {
			logger.debug("Running Garbage Collection on JCR Repository");
			final Session session = ctx.getJCRSession("default");	
			final SessionImpl jrSession = getJackrabbitSession( session );
			final GarbageCollector gc = jrSession.createDataStoreGarbageCollector();
			gc.mark();
			gc.sweep();
			
			logger.info("Successfully executed Garbage Collection on JCR Repository");
			
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
