package com.whoiszxl.rpc.core.spi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExtensionLoader {

    public static String EXTENSION_LOADER_DIR_PREFIX = "META-INF/zxl-rpc/";
    public static Map<String, LinkedHashMap<String, Class>> EXTENSION_LOADER_CLASS_CACHE = new ConcurrentHashMap<>();
    public static ExtensionLoader LOADER_INSTANCE = new ExtensionLoader();

    /**
     * 加载扩展类
     * @param clazz
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void loadExtension(Class clazz) throws IOException, ClassNotFoundException {
        if(clazz == null) {
            throw new IllegalArgumentException("class不能为空");
        }

        //获取的spi文件的全路径，并使用类加载器加载资源
        String spiFilePath = EXTENSION_LOADER_DIR_PREFIX + clazz.getName();
        ClassLoader classLoader = this.getClass().getClassLoader();
        Enumeration<URL> enumeration = null;
        enumeration = classLoader.getResources(spiFilePath);

        //循环迭代，每次读一行，将每行通过等于号切分，将对象名与接口权限定名保存到EXTENSION_LOADER_CLASS_CACHE中
        while (enumeration.hasMoreElements()) {
            URL url = enumeration.nextElement();
            InputStreamReader inputStreamReader = null;
            inputStreamReader = new InputStreamReader(url.openStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            LinkedHashMap<String, Class> classMap = new LinkedHashMap<>();
            while((line = bufferedReader.readLine()) != null) {
                if(line.startsWith("#")) {
                    continue;
                }

                String[] lineArr = line.split("=");
                String implClassName = lineArr[0];
                String interfaceName = lineArr[1];

                classMap.put(implClassName, Class.forName(interfaceName));
            }

            if(EXTENSION_LOADER_CLASS_CACHE.containsKey(clazz.getName())) {
                EXTENSION_LOADER_CLASS_CACHE.get(clazz.getName()).putAll(classMap);
            }else {
                EXTENSION_LOADER_CLASS_CACHE.put(clazz.getName(), classMap);
            }
        }

    }
}
