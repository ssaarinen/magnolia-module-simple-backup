package org.sevensource.magnolia.backup.magnolia;

import java.util.ArrayList;
import java.util.List;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

public class MagnoliaSimpleBackupVersionHandler extends DefaultModuleVersionHandler {

	@Override
	protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<>();
        tasks.addAll(super.getExtraInstallTasks(installContext));

        return tasks;
	}
	
//	@Override
//	protected List<Task> getStartupTasks(InstallContext installContext) {
//        final List<Task> tasks = getExtraInstallTasks(installContext);
//        tasks.add(new ModuleBootstrapTask());
//        tasks.add(new ModuleFilesExtraction());
//        return tasks;
//	}
}
