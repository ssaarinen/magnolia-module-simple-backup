package org.sevensource.magnolia.backup.command;

import org.sevensource.magnolia.backup.executor.gc.RepositoryGarbageCollectorExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.SystemContext;

public class RepositoryGarbageCollectionCommand extends MgnlCommand {

	private static final Logger logger = LoggerFactory.getLogger(RepositoryGarbageCollectionCommand.class);

    private final RepositoryGarbageCollectorExecutor garbageCollector;
    
    @Inject
	public RepositoryGarbageCollectionCommand(SystemContext ctx) {
    	this.garbageCollector = new RepositoryGarbageCollectorExecutor(ctx);
	}
	
	@Override
	public boolean execute(Context context) throws Exception {
		logger.info("Executing Repository Garbage Collection command");
		this.garbageCollector.run();
		return true;
	}
}
