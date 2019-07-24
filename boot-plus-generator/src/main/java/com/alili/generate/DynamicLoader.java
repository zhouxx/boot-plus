package com.alili.generate;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.CharBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Required JDK >= 1.6<br><br>
 * This class can help you create the Java byte code dynamically through the string and load it into memory.<br><br>
 *
 * HOW TO:<br>
 * First step. <code>Map<String, byte[]> bytecode = DynamicLoader.compile("TestClass.java", javaSrc);</code><br>
 * Second step. <code>DynamicLoader.MemoryClassLoader classLoader = new DynamicLoader.MemoryClassLoader(bytecode);</code><br>
 * Third step. <code>Class clazz = classLoader.loadClass("TestClass");</code><br>
 * <br>
 * Then just like the normal use of the call this class can be.
 */
public class DynamicLoader {

    public static URL[] classpaths;

    /**
     * auto fill in the java-name with code, return null if cannot find the public class
     * @param javaSrc source code string
     * @return return the Map, the KEY means ClassName, the VALUE means bytecode.
     */
    public static Map<String, byte[]> compile(String javaSrc) {
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");

        Matcher matcher = pattern.matcher(javaSrc);

        if (matcher.find())
            return compile(matcher.group(1) + ".java", javaSrc);
        return null;
    }

    /**
     * @param javaName the name of your public class,eg: <code>TestClass.java</code>
     * @param javaSrc source code string
     * @return return the Map, the KEY means ClassName, the VALUE means bytecode.
     */
    public static Map<String, byte[]> compile(String javaName, String javaSrc) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);

        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
            JavaFileObject javaFileObject = manager.makeStringSource(javaName, javaSrc);
            List<String> options = null;
            if(classpaths != null) {
                List<String> list = new ArrayList<>();
                for(URL url : classpaths) {
                    list.add(url.getPath());
                }
                String message = String.join(";", list);
                options = Arrays.asList("-classpath", message);
            }
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, options, null, Arrays.asList(javaFileObject));
            if (task.call()) {
                return manager.getClassBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    public static Map<String, byte[]> compile(List<JavaSrc> javaSrcs) {

        Map<String, byte[]> retMap = new HashMap<>();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(null, null, null);

        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {

            List<JavaFileObject> javaFileObjects = new ArrayList<>();
            for(JavaSrc javaSrc : javaSrcs) {
                JavaFileObject javaFileObject = manager.makeStringSource(javaSrc.getJavaName(), javaSrc.getJavaSrc());
                javaFileObjects.add(javaFileObject);
            }

            List<String> options = null;
            if(classpaths != null) {
                List<String> list = new ArrayList<>();
                for(URL url : classpaths) {
                    list.add(url.getPath());
                }
                String message = String.join(";", list);
                options = Arrays.asList("-classpath", message);
            }
            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, options, null, javaFileObjects);
            if (task.call()) {
                retMap.putAll(manager.getClassBytes()); ;
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return retMap;
    }

    public static class MemoryClassLoader extends URLClassLoader {

        Map<String, byte[]> classBytes = new HashMap<String, byte[]>();

        public MemoryClassLoader(Map<String, byte[]> classBytes) {
            super(new URL[0], MemoryClassLoader.class.getClassLoader());
            this.classBytes.putAll(classBytes);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] buf = classBytes.get(name);
            if (buf == null) {
                return super.findClass(name);
            }
            classBytes.remove(name);
            return defineClass(name, buf, 0, buf.length);
        }
    }
}

/*
 * MemoryJavaFileManager.java
 * @author A. Sundararajan
 */

/**
 * JavaFileManager that keeps compiled .class bytes in memory.
 */
@SuppressWarnings("unchecked")
final class MemoryJavaFileManager extends ForwardingJavaFileManager {

    /**
     * Java source file extension.
     */
    private final static String EXT = ".java";

    private Map<String, byte[]> classBytes;

    public MemoryJavaFileManager(JavaFileManager fileManager) {
        super(fileManager);
        classBytes = new HashMap<String, byte[]>();
    }

    public Map<String, byte[]> getClassBytes() {
        return classBytes;
    }

    public void close() throws IOException {
        classBytes = new HashMap<String, byte[]>();
    }

    public void flush() throws IOException {
    }

    /**
     * A file object used to represent Java source coming from a string.
     */
    private static class StringInputBuffer extends SimpleJavaFileObject {
        final String code;

        StringInputBuffer(String name, String code) {
            super(toURI(name), Kind.SOURCE);
            this.code = code;
        }

        public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
            return CharBuffer.wrap(code);
        }

        public Reader openReader() {
            return new StringReader(code);
        }
    }

    /**
     * A file object that stores Java bytecode into the classBytes map.
     */
    private class ClassOutputBuffer extends SimpleJavaFileObject {
        private String name;

        ClassOutputBuffer(String name) {
            super(toURI(name), Kind.CLASS);
            this.name = name;
        }

        public OutputStream openOutputStream() {
            return new FilterOutputStream(new ByteArrayOutputStream()) {
                public void close() throws IOException {
                    out.close();
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                    classBytes.put(name, bos.toByteArray());
                }
            };
        }
    }

    public JavaFileObject getJavaFileForOutput(Location location,
                                               String className,
                                               JavaFileObject.Kind kind,
                                               FileObject sibling) throws IOException {
        if (kind == JavaFileObject.Kind.CLASS) {
            return new ClassOutputBuffer(className);
        } else {
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
    }

    static JavaFileObject makeStringSource(String name, String code) {
        return new StringInputBuffer(name, code);
    }

    static URI toURI(String name) {
        File file = new File(name);
        if (file.exists()) {
            return file.toURI();
        } else {
            try {
                final StringBuilder newUri = new StringBuilder();
                newUri.append("mfm:///");
                newUri.append(name.replace('.', '/'));
                if (name.endsWith(EXT)) newUri.replace(newUri.length() - EXT.length(), newUri.length(), EXT);
                return URI.create(newUri.toString());
            } catch (Exception exp) {
                return URI.create("mfm:///com/sun/script/java/java_source");
            }
        }
    }
}

class JavaSrc {

    private String javaName;
    private String javaSrc;

    public JavaSrc(String javaName, String javaSrc) {
        this.javaName = javaName;
        this.javaSrc = javaSrc;
    }

    public String getJavaName() {
        return javaName;
    }

    public void setJavaName(String javaName) {
        this.javaName = javaName;
    }

    public String getJavaSrc() {
        return javaSrc;
    }

    public void setJavaSrc(String javaSrc) {
        this.javaSrc = javaSrc;
    }
}
