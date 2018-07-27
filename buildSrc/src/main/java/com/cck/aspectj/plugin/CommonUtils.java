package com.cck.aspectj.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GUtil;

public class CommonUtils {

    public static boolean isEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    public static JavaCompile findJavaCompileTask(Project project) {
        Task task = project.getTasks().getByName("compileJava");
        if(task instanceof JavaCompile) {
            return (JavaCompile)task;
        }
        return null;
    }

}
