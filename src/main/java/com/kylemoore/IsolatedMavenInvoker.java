package com.kylemoore;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkerExecutor;

import javax.inject.Inject;

public class IsolatedMavenInvoker extends DefaultTask {

    private final WorkerExecutor executor;
    private FileCollection classpath;

    @Inject
    public IsolatedMavenInvoker(WorkerExecutor executor) {
        this.executor = executor;
    }

    @Classpath
    public FileCollection getClasspath() {
      return this.classpath;
    }


    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }


    @TaskAction
    public void invokeMaven() {

        executor.classLoaderIsolation(spec -> {
            spec.getClasspath().from(getClasspath());
        }).submit(MyWorkAction.class);
    }

    static class MyWorkAction implements WorkAction<ParamObject> {

    }
}
