package org.sevensource.magnolia.backup.magnolia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.Context;
import info.magnolia.context.ContextDecorator;

public class SameJcrSessionContextDecorator extends ContextDecorator {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(SameJcrSessionContextDecorator.class);

	private final transient Map<String, Session> sessions = new HashMap<>();

	public SameJcrSessionContextDecorator(Context ctx, List<String> workspaces) {
		super(ctx);

		for(String workspace : workspaces) {
			try {
				final Session session = ctx.getJCRSession(workspace);
				sessions.put(workspace, session);
			} catch(Exception e) {
				logger.error("Cannot cache JCR session for workspace {}: {}", workspace, e);
			}
		}
	}

	@Override
	public Session getJCRSession(String workspaceName) throws RepositoryException {
		if(sessions.containsKey(workspaceName)) {
			logger.debug("Returning cached session for {}", workspaceName);
			return sessions.get(workspaceName);
		} else {
			logger.info("Requested workspace {} does not have a coresponding cached session", workspaceName);
			return super.getJCRSession(workspaceName);
		}
	}
	public Map<String, Session> getCachedSessions() {
		return sessions;
	}
}
