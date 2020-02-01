package org.sevensource.magnolia.backup.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.sevensource.magnolia.backup.SimpleBackupModule;
import org.sevensource.magnolia.backup.configuration.SimpleBackupJobConfiguration;
import org.sevensource.magnolia.backup.executor.backup.BackupExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.context.SystemContext;
import info.magnolia.importexport.command.JcrExportCommand;


public class BackupCommand extends BaseRepositoryCommand {

	private static final Logger logger = LoggerFactory.getLogger(BackupCommand.class);

	private static final String PROP_BACKUP_CONFIGURATION = "configuration";
	
    private final Provider<SimpleBackupModule> moduleConfigurationProvider;
    private final ServerConfiguration serverConfiguration;
    private final SystemContext systemCtx;
    
	
    @Inject
	public BackupCommand(
			Provider<SimpleBackupModule> moduleConfigurationProvider,
			ServerConfiguration serverConfiguration,
			SystemContext ctx) {
    	
    	this.moduleConfigurationProvider = moduleConfigurationProvider;
    	this.serverConfiguration = serverConfiguration;
    	this.systemCtx = ctx;
	}
	
	@Override
	public boolean execute(Context context) throws Exception {
		
		final String configAttribute = context.getAttribute(PROP_BACKUP_CONFIGURATION);
		if(StringUtils.isEmpty(configAttribute)) {
			throw new IllegalArgumentException("No attribute named " + PROP_BACKUP_CONFIGURATION + " found in requestContext");
		}

		final SimpleBackupJobConfiguration configuration = getBackupConfiguration(configAttribute);
		final Path backupPath = buildBackupPath(configuration.getBackupPath());
		
		final BackupExecutor executor = new BackupExecutor(
				configuration.getWorkspaces(),
				JcrExportCommand.Compression.ZIP,
				backupPath,
				systemCtx);
		
		executor.run();

		return true;
	}
    
    
    private SimpleBackupJobConfiguration getBackupConfiguration(String requestedConfiguration) {
    	
    	final SimpleBackupModule moduleConfiguration =
    			moduleConfigurationProvider.get();
    	
		final SimpleBackupJobConfiguration configuration = moduleConfiguration.getConfigurations()
				.stream()
				.filter(c -> StringUtils.equals(requestedConfiguration, c.getName()))
				.findFirst()
				.orElse(null);
		
		if(configuration == null) {
			throw new IllegalArgumentException("Cannot find configuration with name " + requestedConfiguration);
		}
		
		return configuration;
    }
    
    protected Path buildBackupPath(String basePath) {
    	
    	Path path = Paths.get(basePath);
    	if(! path.toFile().exists()) {
    		throw new IllegalArgumentException("Backupdirectory " + basePath + " does not exist");
    	}
    	
		final String instanceId = serverConfiguration.isAdmin() ? "author" : "public";
		
		path = path.resolve(instanceId);
		if(! path.toFile().exists()) {
			try {
				Files.createDirectory(path);
			} catch(Exception e) {
				logger.error("Cannot create directory: ", e);
				throw new IllegalArgumentException(e);
			}
		}
		
		return path;
    }
}
