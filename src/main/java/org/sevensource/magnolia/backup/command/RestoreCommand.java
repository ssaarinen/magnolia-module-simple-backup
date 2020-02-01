package org.sevensource.magnolia.backup.command;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.sevensource.magnolia.backup.executor.restore.RestoreExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;

public class RestoreCommand extends MgnlCommand {

	private static final Logger logger = LoggerFactory.getLogger(RestoreCommand.class);

	private static final String PROP_PATH = "path";
	

	@Override
	public boolean execute(Context context) throws Exception {
		
		final String pathAttribute = context.getAttribute(PROP_PATH);
		if(StringUtils.isEmpty(pathAttribute)) {
			throw new IllegalArgumentException("No attribute named " + PROP_PATH + " found in requestContext");
		}
		
		final Path restorePath = Paths.get(pathAttribute);
		
		RestoreExecutor restoreManager = new RestoreExecutor(restorePath);
		restoreManager.run();
		
		return true;
	}
}
