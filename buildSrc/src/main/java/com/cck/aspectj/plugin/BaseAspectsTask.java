package com.cck.aspectj.plugin;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryTree;
import org.gradle.api.logging.Logger;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.compile.JavaCompile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseAspectsTask extends DefaultTask {
    protected static final String LOG_PREFIX = "=======================";
    protected Logger mLogger;
    protected String bootClassPath;
    protected String javaDestinationPath;
    protected String javaClassPath;
    protected String javaSourceDir;
    protected String aspectsDestinationPath;
    protected MessageHandler messageHandler;
    protected Main compileTool;

    public BaseAspectsTask() {
        initTask();
    }

    public abstract void doTask();

    public void preDoTask() {

    }

    public void afterDoTask() {

    }

    public void setJavaDestinationPath(String javaDestinationPath) {
        this.javaDestinationPath = javaDestinationPath;
    }

    public void setJavaSourceDir(String javaSourceDir) {
        this.javaSourceDir = javaSourceDir;
    }

    public void setJavaClassPath(String javaClassPath) {
        this.javaClassPath = javaClassPath;
    }

    public void setBootClassPath(String bootClassPath) {
        this.bootClassPath = bootClassPath;
    }

    @TaskAction
    public void taskRun() {
        if(CommonUtils.isEmpty(javaDestinationPath)) javaDestinationPath = findJavaDestinationPath();
        if(CommonUtils.isEmpty(javaClassPath)) javaClassPath = findJavaClassPath();
        if(CommonUtils.isEmpty(bootClassPath)) bootClassPath = findBootClassPath();
        if(CommonUtils.isEmpty(javaSourceDir)) javaSourceDir = findAllJavaSourceDir();
        if(CommonUtils.isEmpty(aspectsDestinationPath)) aspectsDestinationPath = findAspectsDestination(getProject());
        preDoTask();
        doTask();
        for (IMessage message : messageHandler.getMessages(null, true)) {
            if(message.isAbort()||message.isError()||message.isFailed()) {
                mLogger.error(message.getMessage());
            }else{
                mLogger.lifecycle(message.getMessage());
            }
        }
        afterDoTask();
    }

    protected String findBootClassPath() {
        return "";
    }

    protected String findJavaDestinationPath() {
        JavaCompile javaCompile = CommonUtils.findJavaCompileTask(getProject());
        if(javaCompile != null) {
            return javaCompile.getDestinationDir().getAbsolutePath();
        }
        return null;
    }

    protected String findJavaClassPath() {
        JavaCompile javaCompile = CommonUtils.findJavaCompileTask(getProject());
        if(javaCompile != null) {
            return javaCompile.getClasspath().getAsPath();
        }
        return null;
    }

    protected List<String> produceAllSourceSets() {
        List<String> source = new ArrayList<>();
        Object sourceSetsObj = getProject().getProperties().get("sourceSets");
        if(sourceSetsObj instanceof SourceSetContainer) {
            SourceSetContainer sourceSets = (SourceSetContainer)sourceSetsObj;
            SourceSet mainSourceSet = sourceSets.getAt("main");
            for(DirectoryTree tree : mainSourceSet.getJava().getSrcDirTrees()) {
                if(tree.getDir().getName().equals("aspects")) {
                    findAllAjFile(tree.getDir(),source);
                    break;
                }
            }
        }
        return source;
    }

    protected String findAllJavaSourceDir() {
        Object sourceSetsObj = getProject().getProperties().get("sourceSets");
        if(sourceSetsObj instanceof SourceSetContainer) {
            SourceSetContainer sourceSets = (SourceSetContainer)sourceSetsObj;
            SourceSet mainSourceSet = sourceSets.getAt("main");
            return mainSourceSet.getJava().getSourceDirectories().filter(new Spec<File>() {
                @Override
                public boolean isSatisfiedBy(File file) {
                    return file != null && !file.getName().equals("aspects");
                }
            }).getAsPath();
        }
        return "";
    }

    public static String findAspectsDestination(Project project) {
        File aspectsBuild = new File(project.getBuildDir(),"aspects/classes");
        if(!aspectsBuild.exists()) aspectsBuild.mkdirs();
        return aspectsBuild.getAbsolutePath();
    }

    private void findAllAjFile(File file, List<String> source) {
        if(file == null) {
            return ;
        }
        if(file.isFile()) {
            if(file.getName().endsWith(".aj")){
                source.add(file.getAbsolutePath());
            }
        }else if(file.isDirectory()){
            File[] subDir = file.listFiles();
            if(subDir != null) {
                for (File sub : subDir){
                    findAllAjFile(sub,source);
                }
            }
        }
    }

    private void initTask() {
        mLogger = getLogger();
        messageHandler = new MessageHandler();
        compileTool = new Main();

    }



}
