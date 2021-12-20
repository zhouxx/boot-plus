package com.alilitech.generate;

import org.apache.maven.plugin.logging.Log;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private DynamicLoader() {
    }

    protected static final List<String> classpaths = new ArrayList<>();

    public static Log log;

    /**
     * auto fill in the java-name with code, return null if cannot find the public class
     * @param javaSrc source code string
     * @return return the Map, the KEY means ClassName, the VALUE means bytecode.
     */
    public static Map<String, byte[]> compile(String javaSrc) {
        Pattern pattern = Pattern.compile("public\\s+class\\s+(\\w+)");

        Matcher matcher = pattern.matcher(javaSrc);

        if (matcher.find())
            return compile(matcher.group(1) + MemoryJavaFileManager.EXT, javaSrc);
        return Collections.emptyMap();
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

            // 设置classpath
            List<File> files = classpaths.stream().map(File::new).collect(Collectors.toList());
            stdManager.setLocation(StandardLocation.CLASS_PATH, files);

            JavaFileObject javaFileObject = MemoryJavaFileManager.makeStringSource(javaName, javaSrc);

            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, null, null, Arrays.asList(javaFileObject));
            if (Boolean.TRUE.equals(task.call())) {
                return manager.getClassBytes();
            } else {
                log.warn("动态编译失败！");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new DynamicCompileException(e);
        }
        return Collections.emptyMap();
    }

    public static Map<String, byte[]> compile(List<JavaSrc> javaSrcs) {

        Map<String, byte[]> retMap = new HashMap<>();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        //创建诊断信息监听器
        DiagnosticCollector<JavaFileObject> diagnosticListeners = new DiagnosticCollector<>();
        StandardJavaFileManager stdManager = compiler.getStandardFileManager(diagnosticListeners, Locale.getDefault(), StandardCharsets.UTF_8);

        try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {

            // #3 设置classpath
            List<File> files = classpaths.stream().map(File::new).collect(Collectors.toList());
            stdManager.setLocation(StandardLocation.CLASS_PATH, files);

            List<JavaFileObject> javaFileObjects = new ArrayList<>();
            for(JavaSrc javaSrc : javaSrcs) {
                JavaFileObject javaFileObject = MemoryJavaFileManager.makeStringSource(javaSrc.getJavaName(), javaSrc.getJavaSrcStr());
                javaFileObjects.add(javaFileObject);
            }

            JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnosticListeners, null, null, javaFileObjects);
            if (Boolean.TRUE.equals(task.call())) {
                retMap.putAll(manager.getClassBytes());
            } else {
                log.warn("动态编译失败");
                //输出诊断信息
                for(Diagnostic<? extends JavaFileObject> diagnostic : diagnosticListeners.getDiagnostics()) {
                    if(log.isDebugEnabled()) {
                        log.debug(diagnostic.getMessage(Locale.getDefault()));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new DynamicCompileException(e);
        }
        return retMap;
    }

    public static class MemoryClassLoader extends URLClassLoader {

        Map<String, byte[]> classBytes = new HashMap<>();

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
final class MemoryJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

    /**
     * Java source file extension.
     */
    protected static final String EXT = ".java";

    private Map<String, byte[]> classBytes;

    public MemoryJavaFileManager(StandardJavaFileManager fileManager) {
        super(fileManager);
        classBytes = new HashMap<>();
    }

    public Map<String, byte[]> getClassBytes() {
        return classBytes;
    }

    @Override
    public void close() throws IOException {
        classBytes = new HashMap<>();
    }

    @Override
    public void flush() throws IOException {
        // do nothing
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

        @Override
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

        @Override
        public OutputStream openOutputStream() {
            return new FilterOutputStream(new ByteArrayOutputStream()) {
                @Override
                public void close() throws IOException {
                    out.close();
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                    classBytes.put(name, bos.toByteArray());
                }
            };
        }
    }

    @Override
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
    private String javaSrcStr;

    public JavaSrc(String javaName, String javaSrcStr) {
        this.javaName = javaName;
        this.javaSrcStr = javaSrcStr;
    }

    public String getJavaName() {
        return javaName;
    }

    public void setJavaName(String javaName) {
        this.javaName = javaName;
    }

    public String getJavaSrcStr() {
        return javaSrcStr;
    }

    public void setJavaSrcStr(String javaSrcStr) {
        this.javaSrcStr = javaSrcStr;
    }
}

class DynamicCompileException extends RuntimeException {

    public DynamicCompileException() {
        super();
    }

    public DynamicCompileException(String message) {
        super(message);
    }

    public DynamicCompileException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicCompileException(Throwable cause) {
        super(cause);
    }

    protected DynamicCompileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
