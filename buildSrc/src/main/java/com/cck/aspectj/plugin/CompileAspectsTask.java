package com.cck.aspectj.plugin;

import org.gradle.util.CollectionUtils;

import java.io.File;
import java.util.*;

public class CompileAspectsTask extends BaseAspectsTask{
    private List<String> mAllAspectsFiles;
    public static Map<String,Object> args() {
        Map<String,Object> args = new HashMap<>();
        args.put("type",CompileAspectsTask.class);
        args.put("group","Aspects");
        args.put("overwrite",true);
        args.put("description","compile *.aj file in *.class");
        return args;
    }

    @Override
    public void preDoTask() {
        mLogger.lifecycle(LOG_PREFIX+"begin compile aspects"+LOG_PREFIX);
    }

    @Override
    public void afterDoTask() {
        mLogger.lifecycle(LOG_PREFIX+"compile aspects success"+LOG_PREFIX);
    }

    @Override
    public void doTask() {
        mAllAspectsFiles = produceAllSourceSets();
        List<String> base = Arrays.asList(
                "-d", aspectsDestinationPath,
                "-source","1.7",
                "-target","1.7",
                "-classpath", javaClassPath + File.pathSeparator+javaDestinationPath
        );
        List<String> argList = new ArrayList<>(base);
        argList.addAll(mAllAspectsFiles);
        String[] args = new String[argList.size()];
        argList.toArray(args);
        String argsStr = CollectionUtils.join(" ",argList);
        mLogger.lifecycle(argsStr);
        compileTool.run(args,messageHandler);
    }


}
