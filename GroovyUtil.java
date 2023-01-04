package com.yunfei.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroovyUtil {


    private static Map<String, String> nameAndMd5 = new ConcurrentHashMap<>();
    private static Map<String, Script> nameAndScript = new ConcurrentHashMap<>();
    public static void engine(String scriptText, String name, Map<String,Object> variable) {
        try{
            Binding binding = new Binding();
            variable.forEach(binding::setVariable);

            String oldMd5 = nameAndMd5.get(name);
            String newMd5 = fingerKey(scriptText);
            Script script;
            if (oldMd5!=null && oldMd5.equals(newMd5)){
                script = nameAndScript.get(name);
            }else{
                GroovyClassLoader classLoader = new GroovyClassLoader();
                Class aClass = classLoader.parseClass(scriptText);
                script = InvokerHelper.createScript(aClass, binding);
                nameAndMd5.put(name, newMd5);
                nameAndScript.put(name, script);
            }
            script.setBinding(binding);
            script.run();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("执行文件发生错误");
        }
    }

    // 为脚本代码生成md5指纹
    public static String fingerKey(String scriptText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(scriptText.getBytes(StandardCharsets.UTF_8));

            final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
            StringBuilder ret = new StringBuilder(bytes.length * 2);
            for (int i=0; i<bytes.length; i++) {
                ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
                ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
            }
            return ret.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
