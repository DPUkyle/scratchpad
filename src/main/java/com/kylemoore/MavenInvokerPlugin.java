package com.kylemoore;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.TaskProvider;
import org.apache.maven.project.DefaultProjectBuilder;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MavenInvokerPlugin implements Plugin<Project> {

    public void apply(Project project) {

        // TODO create a task to invoke maven
        TaskProvider<Exec> mavenInvoker = project.getTasks().register("invokeMavenTask", Exec.class, task -> {
            task.workingDir(project.getProjectDir());


            List<String> args = new ArrayList<>();
            args.add("./mvnw");
            args.add("-B"); //batch mode
            args.add("assemble");

            task.commandLine(args);

            // get stdout/stderr
            OutputStream standardOutput = new ByteArrayOutputStream();
            task.setStandardOutput(standardOutput);
            // todo repeat for stderr

            // check commandline status

            task.setIgnoreExitValue(true);

            task.doLast(t -> {
                int exitCode = ((Exec) t).getExitValue();
                if (exitCode != 0) {
                    throw new GradleException("maven execution failed because ..." + stderr.getLastLines(30));
                }
            });

        });

        // v2 invoke the main method of the maven FQCN directly
        //TaskProvider<JavaExec> betterMavenInvoker = project.getTasks().register ...


        // v3 invoke the main methdo of the maven FQCN; isolate maven deps on a named configuration
        Configuration conf = project.getConfigurations().create("isolatedMaven", c -> {
            Dependency maven = project.getDependencies().create("org.apache.maven:maven-core:3.6.1");
            c.defaultDependencies(deps -> deps.add(maven));
        });

        TaskProvider<IsolatedMavenInvoker> bestMavenInvoker = project.getTasks().register("isolatedMavenInvoker", IsolatedMavenInvoker.class, task -> {
            task.setClasspath(conf);
        });

    }
}
