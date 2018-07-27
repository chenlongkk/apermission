package com.cck.apermission.utils;

import java.util.Arrays;
import java.util.Iterator;

public class Strings {
    public static String join(Object[] arrays,String separator) {
        return join((arrays == null) ? null : Arrays.asList(arrays),separator);
    }


    public static String join(Iterable<?> iterable,String separator) {
        StringBuilder sb = new StringBuilder();
        if(separator != null && iterable != null) {
            Iterator<?> iterator = iterable.iterator();
            if(iterator.hasNext()) {
                sb.append(String.valueOf(iterator.next()));
                while (iterator.hasNext()) {
                    sb.append(separator);
                    sb.append(String.valueOf(iterator.next()));
                }
            }
        }
        return sb.toString();
    }
}
