package com.cck.aspectj.plugin;

import java.util.HashMap;
import java.util.Map;

public class WaveAspectsTask extends BaseAspectsTask{
    public static Map<String,Object> args() {
        Map<String,Object> args = new HashMap<>();
        args.put("type",WaveAspectsTask.class);
        args.put("group","Aspects");
        args.put("description","wave aspects");
        return args;
    }

    @Override
    public void doTask() {
        mLogger.lifecycle("execute waveAspects");
        String[] args = new String[]{
                "-showWeaveInfo",
                "-source","1.7",
                "-target","1.7",
                "-inpath", javaDestinationPath,
                "-aspectpath", aspectsDestinationPath,
                "-d", javaDestinationPath,
                "-classpath", javaClassPath
        };
        compileTool.run(args,messageHandler);
    }
}
