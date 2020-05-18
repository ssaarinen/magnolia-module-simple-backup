package org.sevensource.magnolia.backup;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.sevensource.magnolia.backup.configuration.SimpleBackupJobConfiguration;

import java.util.ArrayList;
import java.util.List;


public class SimpleBackupModule implements ModuleLifecycle {

	private List<SimpleBackupJobConfiguration> configurations = new ArrayList<>();

	public List<SimpleBackupJobConfiguration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<SimpleBackupJobConfiguration> configurations) {
		this.configurations = configurations;
	}

	@Override
	public void start(ModuleLifecycleContext moduleLifecycleContext) {
		// no-op
	}

	@Override
	public void stop(ModuleLifecycleContext moduleLifecycleContext) {
		// no-op
	}
}

