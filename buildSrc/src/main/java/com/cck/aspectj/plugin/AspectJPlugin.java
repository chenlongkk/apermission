package com.cck.aspectj.plugin;


import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.Set;
import java.util.function.Consumer;

public class AspectJPlugin implements Plugin<Project> {
    private Logger mLogger;

    @Override
    public void apply(Project project) {
        mLogger = project.getLogger();
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                afterEvaluate(project);
            }
        });
    }

    private void afterEvaluate(Project project) {
        SourceSetContainer sourceSets = (SourceSetContainer) project.getProperties().get("sourceSets");
        JavaCompile compileJava = CommonUtils.findJavaCompileTask(project);
        if(sourceSets == null) {
            mLogger.error("sourceSets is NULL.");
            return ;
        }
        if(compileJava == null) {
            mLogger.error("JavaCompile Task is NULL.");
            return ;
        }
        sourceSets.forEach(new Consumer<SourceSet>() {
            @Override
            public void accept(SourceSet sourceSet) {
                Set<File> srcDirs = sourceSet.getAllJava().getSrcDirs();
                File parentDir = srcDirs.iterator().next().getParentFile();
                sourceSet.getJava().srcDir(new File(parentDir, "aspects"));
            }
        });
        project.task(CompileAspectsTask.args(), "compileAspects");

        final Task jtask =  project.getTasks().findByName("jar");
        if(jtask instanceof Jar) {
            File destDir = new File(project.getBuildDir(),"aspects/classes");
            ((Jar) jtask).from(destDir.getAbsolutePath());
            jtask.dependsOn("compileAspects");
        }

    }



}
