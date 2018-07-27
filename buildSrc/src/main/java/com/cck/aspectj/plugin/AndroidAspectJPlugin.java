package com.cck.aspectj.plugin;

import com.android.build.gradle.BaseExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;


public class AndroidAspectJPlugin implements Plugin<Project> {
    private Logger mLogger;
    @Override
    public void apply(Project project) {
        mLogger = project.getLogger();
        mLogger.lifecycle("apply android aspectj plugin");
        project.getDependencies().add("implementation","org.aspectj:aspectjrt:1.9.1");
        Object andObj = project.getExtensions().getByName("android");
        if(andObj instanceof BaseExtension) {
            mLogger.lifecycle("register aspectj transform.");
            ((BaseExtension) andObj).registerTransform(new AspectJTransform("aspectJ",project,mLogger));
        }else{
            mLogger.lifecycle("not support for "+andObj.getClass().getName());
//
        }
    }
}
