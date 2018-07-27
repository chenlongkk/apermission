package com.cck.aspectj.plugin;

import com.android.build.api.transform.*;
import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.android.build.gradle.LibraryExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.api.LibraryVariant;
import com.android.build.gradle.internal.CompileOptions;
import com.android.build.gradle.internal.pipeline.TransformManager;
import joptsimple.internal.Strings;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.tools.ajc.Main;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.internal.impldep.aQute.bnd.build.Run;
import org.gradle.util.CollectionUtils;
import org.gradle.util.GUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class AspectJTransform extends Transform {
    private static final Set<QualifiedContent.ContentType> sInput =
            Collections.singleton(QualifiedContent.DefaultContentType.CLASSES);
    private static final Set<? super QualifiedContent.Scope> sScopes=
            CollectionUtils.toSet(Arrays.asList(
                    QualifiedContent.Scope.PROJECT,
                    QualifiedContent.Scope.SUB_PROJECTS,
                    QualifiedContent.Scope.EXTERNAL_LIBRARIES
            ));
    private String mName;
    private Logger mLogger;
    private Project mProject;
    public AspectJTransform(String name,Project project,Logger logger) {
        mName = name;
        mLogger = logger;
        mProject = project;
    }
    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return sInput;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }


    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Context context = transformInvocation.getContext();
        String variantName = context.getVariantName();
        String classPath = findJavaClassPath(variantName);
        String bootClassPath = findBootClassPath();
        Collection<TransformInput> inputList = transformInvocation.getInputs();
        TransformOutputProvider provider = transformInvocation.getOutputProvider();

        for(TransformInput in : inputList) {
            Collection<DirectoryInput> dirInputs = in.getDirectoryInputs();
            if(dirInputs != null) {
                for (DirectoryInput dir : dirInputs) {
                    File root = dir.getFile();
                    mLogger.lifecycle("dir:"+dir.getName()+",path:"+root.getAbsolutePath());
                    waveAspectDir(root, classPath, bootClassPath, provider);
                }
            }

            Collection<JarInput> jarInputs = in.getJarInputs();
            if(jarInputs != null) {
                for (JarInput jar : jarInputs) {
                    mLogger.lifecycle("jar:" + jar.getName() + ",path:" + jar.getFile().getAbsolutePath()+"status:"+jar.getStatus().toString());
                    waveAspectJar(jar, classPath, bootClassPath, provider);
                }
            }

        }
    }

    private String findJavaClassPath(String variantName) {
        Object andObj = mProject.getExtensions().getByName("android");
        if(andObj instanceof AppExtension) {
            Iterator<ApplicationVariant> it = ((AppExtension) andObj).getApplicationVariants().iterator();
            while (it.hasNext()) {
                ApplicationVariant variant = it.next();
                if(variant != null) {
                    if(variant.getName().equals(variantName)) {
                        return variant.getCompileClasspath(null).getAsPath();
                    }
                }
            }
        }else if(andObj instanceof LibraryExtension) {
            Iterator<LibraryVariant> it = ((LibraryExtension) andObj).getLibraryVariants().iterator();
            while (it.hasNext()) {
                LibraryVariant variant = it.next();
                if(variant != null) {
                    if(variant.getName().equals(variantName)) {
                        return variant.getCompileClasspath(null).getAsPath();
                    }
                }
            }
        }
        return "";
    }

    private String findBootClassPath() {
        Object andObj = mProject.getExtensions().getByName("android");
        if(andObj instanceof BaseExtension) {
            return GUtil.asPath(((BaseExtension) andObj).getBootClasspath());
        }
        return "";
    }

    private CompileOptions findCompileOptions() {
        Object andObj = mProject.getExtensions().getByName("android");
        if(andObj instanceof BaseExtension) {
            return ((BaseExtension) andObj).getCompileOptions();
        }
        return null;
    }

    private void waveAspectDir(File root,String classpath,String bootClasspath,TransformOutputProvider provider) {
        if(root == null || !root.exists()){
            mLogger.lifecycle("root file is not valid,skip this wave");
            return;
        }
        File out = provider.getContentLocation(root.getName(),sInput,sScopes,Format.DIRECTORY);
        log("in dir:"+root.getName()+"out:"+out.getName());
        List<String> args = new ArrayList<>(produceCommonArgs());
        List<String> more = Arrays.asList(
                "-inpath", root.getAbsolutePath(),
                "-aspectpath", classpath,
                "-d", out.getAbsolutePath(),
                "-classpath", classpath,
                "-bootclasspath",bootClasspath
        );
        args.addAll(more);
        invokeWaveImpl(args);
    }

    private void waveAspectJar(JarInput in,String classpath,String bootClasspath,TransformOutputProvider provider) {
        if(in == null || in.getFile() == null ||!in.getFile().exists()){
            mLogger.lifecycle("jar file is not valid,skip this wave");
            return;
        }
        File out = provider.getContentLocation(in.getName(),sInput,sScopes,Format.JAR);
        log("in jar:"+in.getName()+",file:"+in.getFile()+",out:"+out.getName());
        List<String> args = new ArrayList<>(produceCommonArgs());
        List<String> more = Arrays.asList(
                "-injars", in.getFile().getAbsolutePath(),
                "-aspectpath", classpath,
                "-outjar", out.getAbsolutePath(),
                "-classpath", classpath,
                "-bootclasspath",bootClasspath
        );

        args.addAll(more);
        invokeWaveImpl(args);
    }

    private List<String> produceCommonArgs() {
        CompileOptions compileOptions = findCompileOptions();
        String source = "";
        String target = "";
        if(compileOptions != null) {
            source = compileOptions.getSourceCompatibility().toString();
            target = compileOptions.getTargetCompatibility().toString();
        }

         return Arrays.asList(
                "-showWeaveInfo",
                "-source",CommonUtils.isEmpty(source) ? "1.7":source,
                "-target",CommonUtils.isEmpty(target) ? "1.7":target
         );
    }

    private void invokeWaveImpl(List<String> argList) {
        mLogger.lifecycle("execute waveAspects ,args:"+ Strings.join(argList," "));
        String[] args = new String[argList.size()];
        argList.toArray(args);
        Main compileTool = new Main();
        MessageHandler messageHandler = new MessageHandler();
        compileTool.run(args,messageHandler);
        for (IMessage message : messageHandler.getMessages(null, true)) {
            if(message.isAbort()|| message.isError() || message.isFailed()) {
                mLogger.error(message.getMessage()+",detail:"+message.getDetails());
                throw new RuntimeException(message.getThrown());
            }else{
                mLogger.lifecycle(message.getMessage());
            }
        }
        compileTool.quit();
    }

    private void log(String msg) {
        mLogger.lifecycle("--------------->"+msg);
    }
}
