# Magnolia module SimpleBackup
[![GitHub Tag](https://img.shields.io/github/tag/sevensource/magnolia-module-simple-backup.svg?maxAge=3600)](https://github.com/sevensource/magnolia-module-simple-backup/tags)
[![Maven Central](https://img.shields.io/maven-central/v/org.sevensource.magnolia/magnolia-module-simple-backup.svg?maxAge=3600)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.sevensource.magnolia%22%20AND%20a%3A%22magnolia-module-simple-backup%22)
[![License](https://img.shields.io/github/license/sevensource/magnolia-module-simple-backup.svg)](https://github.com/sevensource/magnolia-module-simple-backup/blob/master/LICENSE)

## [Magnolia CMS](http://www.magnolia-cms.com) module adding backup and restore functionality
.
### Installation
The module is available on [Maven central](https://search.maven.org/artifact/org.sevensource.magnolia/magnolia-module-simple-backup) - add it to your war file:
```xml
<dependency>
  <groupId>org.sevensource.magnolia</groupId>
  <artifactId>magnolia-module-simple-backup</artifactId>
  <version>${magnolia-module-simple-backup.version}</version>
</dependency>
```
### Configuration
- Module configuration is done in Magnolias config workspace in `/modules/simplebackup/config`
- add a backup job definition beneith `/modules/simplebackup/config/configurations`:
```yml
    example:
      backupPath: "/var/mgnl-backup"  # base path where backups are stored
      enabled: true                   # will only be executed if set to true
      name: example                   # name of the job
      workspaces:                     # a list of workspaces to backup
        website:
          workspace: website          # name of the workspace
          path: /                     # root path within the workspace to backup
          split: true                 # if true, a new file will be written for every subfolder
        config:
          workspace: config
          path: /
          split: false
```
### Execution
Backup and Restore is triggered via [Magnolia Commands](https://documentation.magnolia-cms.com/display/DOCS/Commands)
Available commands:

| Catalog  | Command | Parameters | Description |
| ------------- | ------------- | ------------- | ------------- |
| simplebackup  | backup  | configuration => _name of configured backup job_  | Triggers the execution of a backup job with the name specified in _configuration_  |
| simplebackup  | restore  | path => _path to the directory that contains the backup files_  | Triggers the execution of a restore |
| simplebackup  | garbage-collection  | _none_  | Triggers the execution of a JackRabbit Repository Garbage Collection |

#### Executing backups from Java/Groovy
```groovy
map  = new java.util.HashMap<String, String>()
map.put("configuration", "example")        // name of the backup job created above
map.put("backup-subdirectory", "nightly")  // optional parameter, if omitted a timestamp will be used
cm = info.magnolia.commands.CommandsManager.getInstance()
cm.executeCommand('simplebackup','backup',map)
```

#### Executing restores from Java/Groovy
```groovy
map  = new java.util.HashMap<String, String>()
map.put("path", "/var/magnolia-backups/author/2020-01-25T120000") // path to the directory in which the backup files are stored
cm = info.magnolia.commands.CommandsManager.getInstance()
cm.executeCommand('simplebackup','restore',map)
```

#### Executing garbage collections from Java/Groovy
```groovy
cm = info.magnolia.commands.CommandsManager.getInstance()
cm.executeCommand('simplebackup','garbage-collection', null)
```

#### via Magnolia's scheduler
Using [Magnolia Scheduler Module](https://documentation.magnolia-cms.com/display/DOCS/Scheduler+module), regular
backups can be executed - see [Commands-Scheduling](https://documentation.magnolia-cms.com/display/DOCS/Commands#Commands-Scheduling)
for details.


#### via REST services
Using [Magnolia Rest Module](https://documentation.magnolia-cms.com/display/DOCS/REST+module) backups can be initiated
via the REST API:
- enable the backup command in
`/modules/rest-services/rest-endpoints/commands/enabledCommands`:

_config.modules.rest-services.rest-endpoints.commands.enabledCommands.backup.yaml_
```yml
backup:
  catalogName: simplebackup
  commandName: backup
  access:
    roles:
      rest: rest-editor
```
