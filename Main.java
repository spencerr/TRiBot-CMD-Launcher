import asm.assembly.Assembly;
import asm.assembly.Mask;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by Spencer on 9/8/2016, modified by GOD Deluxe, finalized by Swagg.
 */
public class Main {

    public static String cArgClass = null;
    public static String var6Class = null;
    public static String var6ArrayList = null;
    public static String encodeClass = null;
    public static String encodeMethod = null;

    public static void main(String... sargs) {
        try {
            loadLibrary(Paths.get(getAppDataDirectory().toString(), "dependancies", "TRiBot.jar").toFile());
            loadHooks();

            if (cArgClass == null) {
                generateHooks();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, String> parsedArguments = new HashMap<>();
        for (String arg : sargs) {
            String[] split = arg.split("~");
            if (split.length == 2)
                parsedArguments.put(split[0], split[1]);
        }

        String accountSession = "";
        try {
            List<String> lines = Files.readAllLines(Paths.get(Main.getAppDataDirectory().toString(), "settings", "startup.ini"));
            for (String line : lines) {
                if (line.contains("<sid>")) {
                    String[] splits = line.split("<sid>");
                    String[] splits2 = splits[1].split("</sid");
                    accountSession = "SID" + splits2[0];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String accountName = getArgument(parsedArguments, "accountName");
        String scriptName = getArgument(parsedArguments, "scriptName").replace("_", " ");
        String scriptCommand = getArgument(parsedArguments, "scriptCommand").replace("_", " ");
        int world = Integer.parseInt(getArgument(parsedArguments, "world").equals("") ? "0" : getArgument(parsedArguments, "world"));
        String breakProfile = getArgument(parsedArguments, "breakProfile");
        String proxyIP = getArgument(parsedArguments, "proxyIP");
        String proxyPort = getArgument(parsedArguments, "proxyPort");
        String proxyUsername = getArgument(parsedArguments, "proxyUsername");
        String proxyPassword = getArgument(parsedArguments, "proxyPassword");
        String heapSize = getArgument(parsedArguments, "heapSize");

        String os = java.lang.System.getProperty("os.name").toLowerCase();
        String sep = os.contains("win") ? ";" : ":";
        String dir = os.contains("win") ? "\\" : "/";
        String q = os.contains("win") ? "\"" : "";

        ArrayList<String> args = new ArrayList<>();
        String TRIBOT_PATH = Main.getAppDataDirectory().toString().replace("\\", dir);

        args.add("java");
        args.add("-Xmx386m -Xss2m -Dsun.java2d.noddraw=true -XX:CompileThreshold=1500 -Xincgc");
        args.add("-XX:+UseConcMarkSweepGC -XX:+UseParNewGC -Xbootclasspath/p:" + q + TRIBOT_PATH + dir + "dependancies" + dir + "xboot860.jar" + q);
        args.add("-classpath "
                + q + Main.getJavaBinDirectory().getParent().replace("\\", dir) + dir + "lib" + dir + "tools.jar" + q + sep
                + q + TRIBOT_PATH + dir + "bin" + q + sep
                + q + TRIBOT_PATH + dir + "dependancies" + dir + "substance.jar" + q  + sep
                + q + TRIBOT_PATH + dir + "dependancies" + dir + "trident.jar" + q + sep
                + q + TRIBOT_PATH + dir + "dependancies" + dir + "TRiBot.jar" + q + sep);
        args.add("org.tribot.TRiBot");
        args.add(accountSession);
        args.add("MEM" + heapSize);

        try {

            Class<?> cArgClazz = Class.forName(cArgClass);
            Constructor<?> constructor = cArgClazz.getConstructor(String.class, String.class, String.class, int.class, String.class, int.class);
            Object o = constructor.newInstance(accountName, scriptName, scriptCommand, world, breakProfile, 1472396136);

            Class<?> var6Clazz = Class.forName(var6Class);
            Constructor<?> constructor1 = var6Clazz.getConstructor(ArrayList.class, String.class);
            Object var6 = constructor1.newInstance(new ArrayList<>(), null);

            Field f = var6.getClass().getField(var6ArrayList);
            f.setAccessible(true);
            Object o2 = f.get(var6);
            Method m = f.getType().getDeclaredMethod("add", Object.class);
            m.invoke(o2, o);

            int crand = (int) (Math.random() * 10000000);
            final String bootname = "/client" + crand + ".boot";

            if (proxyIP != null && !proxyIP.equals(""))
                args.add("PROXY_HOST" + encodeString(proxyIP));
            if (proxyPort != null && !proxyPort.equals(""))
                args.add("PROXY_PORT" + encodeString(proxyPort));
            if (proxyUsername != null && !proxyUsername.equals(""))
                args.add("PROXY_USERNAME" + encodeString(proxyUsername));
            if (proxyPassword != null && !proxyPassword.equals(""))
                args.add("PROXY_PASSWORD" + encodeString(proxyPassword));

            args.add("BOOT_INFO" + encodeString(bootname));
            args.add("OLDSCHOOL");

            String exe = "";
            for (String sarg : args) {
                exe += sarg + " ";
            }

            Path path = Paths.get(TRIBOT_PATH + bootname);
            try {
                Path tfile = Files.createFile(path);
                if (!tfile.toFile().exists())
                    throw new Error("Clientboot file does not exist.");
                else
                    System.out.println("Created boot: " + path);

                FileOutputStream fos = new FileOutputStream(tfile.toFile());
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos);

                oos.writeObject(var6);
                oos.close();
                bos.close();
                fos.close();

                try {
                    Runtime.getRuntime().exec(exe);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

        } catch (ClassNotFoundException | InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (int i=0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }


    public static synchronized void loadLibrary(java.io.File jar)
    {
        try {
            /*We are using reflection here to circumvent encapsulation; addURL is not public*/
            java.net.URLClassLoader loader = (java.net.URLClassLoader)ClassLoader.getSystemClassLoader();
            java.net.URL url = jar.toURI().toURL();
            /*Disallow if already loaded*/
            for (java.net.URL it : java.util.Arrays.asList(loader.getURLs())){
                if (it.equals(url)){
                    return;
                }
            }
            java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
            method.setAccessible(true); /*promote the method to public access*/
            method.invoke(loader, new Object[]{url});
        } catch (final java.lang.NoSuchMethodException |
                java.lang.IllegalAccessException |
                java.net.MalformedURLException |
                java.lang.reflect.InvocationTargetException e){

        }
    }

    public static void generateHooks() throws Exception {
        HashMap<String, String> foundClasses = new HashMap<>();
        HashMap<String, byte[]> classData = new HashMap<>();

        Path jarPath = Paths.get(getAppDataDirectory().toString(), "dependancies", "TRiBot.jar");
        JarFile jar = new JarFile(jarPath.toFile());
        Enumeration<JarEntry> entries = jar.entries();
        //URLClassLoader loader = URLClassLoader.newInstance(new URL[] { new URL("file:" + jarPath.toUri().toURL().toString() + "") }, ClassLoader.getSystemClassLoader());

        try{
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class"))
                    continue;

                try {
                    String className = entry.getName().substring(0, entry.getName().length() - 6);
                    className = className.replace('/', '.');

                    byte[] buffer = new byte[1024];
                    int read;

                    InputStream is = jar.getInputStream(entry);
                    byte[] allByteData = new byte[0];
                    while ((read = is.read(buffer)) != -1) {
                        byte[] tempBuff = new byte[read+allByteData.length];
                        for(int i=0;i<allByteData.length;++i)
                            tempBuff[i]=allByteData[i];

                        for(int i=0;i<read;++i)
                            tempBuff[i+allByteData.length]=buffer[i];

                        allByteData=tempBuff;
                    }

                    classData.put(entry.getName(), allByteData);

                    Class clazz = Class.forName(className);

                    try {
                        if (className.equals("obf.fI")) continue;
                        Field uid = clazz.getDeclaredField("serialVersionUID");
                        if (uid != null) {
                            uid.setAccessible(true);

                            if ((long) uid.get(null) == 8230292901933089929L) {
                                for (Field field : clazz.getDeclaredFields()) {
                                    if (field.getType().equals(ArrayList.class)) {
                                        foundClasses.put("var6ArrayList", field.getName());
                                    }
                                }
                                foundClasses.put("var6Class", className);
                            } else if ((long) uid.get(null) == -7292378165840294914L) {
                                foundClasses.put("cArgClass", className);
                            }
                        }
                    } catch (ExceptionInInitializerError | Exception e) {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, ClassNode> classNodes = new HashMap<>();
        for (String className : classData.keySet()) {
            byte[] bytes = classData.get(className);
            String name = className.substring(0, className.indexOf(".class"));
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            classNodes.put(name, cn);
        }

        ClassNode node = classNodes.get("org/tribot/TRiBot");
        out:
        for (MethodNode methodNode : node.methods) {
            if (methodNode.name.equals("main")) {
                System.out.println("Found TRiBot main method.");

                List<AbstractInsnNode> pattern = Assembly.find(methodNode, Mask.DUP_X1, Mask.INVOKEVIRTUAL, Mask.INVOKEVIRTUAL, Mask.INVOKESTATIC, Mask.INVOKESPECIAL, Mask.ASTORE);
                if (pattern != null) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) pattern.get(3);

                    ClassNode node1 = classNodes.get(methodInsnNode.owner);
                    for (MethodNode methodNode1 : node1.methods) {
                        if (methodNode1.desc.equals("([B)Ljava/lang/String;")) {
                            foundClasses.put("encodeClass", methodInsnNode.owner.replace('/', '.'));
                            foundClasses.put("encodeMethod", methodNode1.name);
                            break out;
                        }
                    }
                }
            }
        }

        List<String> lines = new ArrayList<>();
        lines.add(getMD5Checksum(Paths.get(Main.getAppDataDirectory().toString(), "dependancies", "TRiBot.jar").toString()));
        lines.add(foundClasses.get("cArgClass"));
        lines.add(foundClasses.get("var6Class"));
        lines.add(foundClasses.get("var6ArrayList"));
        lines.add(foundClasses.get("encodeClass"));
        lines.add(foundClasses.get("encodeMethod"));

        cArgClass = foundClasses.get("cArgClass");
        var6Class = foundClasses.get("var6Class");
        var6ArrayList = foundClasses.get("var6ArrayList");
        encodeClass = foundClasses.get("encodeClass");
        encodeMethod = foundClasses.get("encodeMethod");

        System.out.println("Wrote md5+hooks to hookData.ini");

        Files.write(Paths.get(Main.getAppDataDirectory().toString(), "settings", " hookData.ini"), lines);
    }

    public static void loadHooks() {

        try {
            String md5 = getMD5Checksum(Paths.get(Main.getAppDataDirectory().toString(), "dependancies", "TRiBot.jar").toString());

            List<String> hookData = Files.readAllLines(Paths.get(Main.getAppDataDirectory().toString(), "settings", " hookData.ini"));
            if (hookData.size() > 0) {
                if (hookData.get(0).equals(md5)) {
                    cArgClass = hookData.get(1);
                    var6Class = hookData.get(2);
                    var6ArrayList = hookData.get(3);
                    encodeClass = hookData.get(4);
                    encodeMethod = hookData.get(5);
                }
            }
        } catch (Exception e) {

        }
    }

    public static String encodeString(String toEncode) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = Class.forName(encodeClass);
        Method method = clazz.getDeclaredMethod(encodeMethod, byte[].class);
        return (String) method.invoke(clazz, (Object) toEncode.getBytes());
    }

    public static String getArgument(HashMap<String, String> set, String key) {
        return set.containsKey(key) ? set.get(key) : "";
    }

    public static File getJavaBinDirectory() {
        String[] a;
        String a2;
        String a3 = System.getProperty("os.name").toLowerCase();
        String a4 = System.getenv("JAVA_HOME");
        if (a4 != null && a4.length() > 0) {
            return new File(a4, "bin");
        }
        if (!a3.contains("win")) {
            a3.contains("mac");
            return null;
        }
        if (bc("HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit") != null && (a2=iB("HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit", "CurrentVersion")) != null && (a2 = iB(("HKEY_LOCAL_MACHINE\\SOFTWARE\\JavaSoft\\Java Development Kit\\" + a2), "JavaHome")) != null) {
            return new File(a2, "bin");
        }
        a = bc("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\JavaSoft\\Java Runtime Environment");
        if (a == null) return null;
        a2 = iB("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\JavaSoft\\Java Runtime Environment", "CurrentVersion");
        if (a2 == null) return null;
        String a5 = iB(("HKEY_LOCAL_MACHINE\\SOFTWARE\\Wow6432Node\\JavaSoft\\Java Runtime Environment\\" + a2), "JavaHome");
        if (a5 == null) return null;
        return new File(a5, "bin");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static String[] bc(String a) {
        try {
            Process var1 = Runtime.getRuntime().exec("reg query \"" + a + "\"");
            StreamReader var2 = new StreamReader(var1.getInputStream());
            var2.start();
            var1.waitFor();
            var2.join();
            String var8 = var2.getResult();
            if(var8.length() < 1) {
                return null;
            } else {
                String[] var9 = var8.split(a.replace("\\", "\\\\") + "\\\\");
                ArrayList var3 = new ArrayList();
                String[] var6 = var9;
                int var5 = var9.length;

                int var4;
                for(int var10000 = var4 = 0; var10000 < var5; var10000 = var4) {
                    if(!(var8 = var6[var4]).contains(a)) {
                        var3.add(var8);
                    }

                    ++var4;
                }

                String[] var10001 = new String[var3.size()];
                return var10001;
            }
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }


    private static String iB(String a, String a1) {
        try {
            Process a2 = Runtime.getRuntime().exec("reg query \"" + a + "\" /v " + a1);
            StreamReader var2 = new StreamReader(a2.getInputStream());
            var2.start();
            a2.waitFor();
            var2.join();
            String var5 = var2.getResult();
            if(var5.length() < 1) {
                return null;
            } else {
                var5 = var5.substring(var5.indexOf("REG_SZ") + "REG_SZ".length() + 1, var5.length()).trim();
                return var5;
            }
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }


    @SuppressWarnings("unused")
    public static File getAppDataDirectory() {
        File file;
        File file2 = null;
        File a = null;
        String a2 = System.getProperty("user.home");
        String a3 = System.getProperty("os.name").toLowerCase();
        if (a3.contains("win")) {
            String a4 = System.getenv("APPDATA");
            file2 = a4 == null || a4.length() < 1 ? (a = new File(a2, ".tribot" + File.separatorChar)) : (a = new File(a4, ".tribot" + File.separatorChar));
        } else if (a3.contains("solaris") || a3.contains("linux") || a3.contains("sunos") || a3.contains("unix")) {
            file2 = a = new File(a2, ".tribot" + File.separatorChar);
        } else if (a3.contains("mac")) {
            file = new File(a2, "Library" + File.separatorChar + "Application Support" + File.separatorChar + "tribot");
            File file3 = a = file;
        } else {
            file = new File(a2, "tribot" + File.separatorChar);
            File file4 = a = file;
        }
        if (file2 != null) {
            if (a.exists()) return a;
            if (a.mkdirs()) return a;
        }
        a = new File("data");
        System.out.println("Couldn't create seperate application data directory. Using application data directory as: " + a.getAbsolutePath());
        return a;
    }
}