/*
 * 
 * 
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 * $
 */
package org.sipfoundry.sipxconfig.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

/**
 * Common utility methods for unit testing with neoconf. Somewhat controversial being in codebase,
 * however unit test library would be uglier, IMHO.
 */
public final class TestUtil {

    private static final String EOL = System.getProperty("line.separator");

    private static final String FORWARD_SLASH = "/";

    private static final DateFormat ENGLISH_DATE = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.FULL, Locale.ENGLISH);
    static {
        ENGLISH_DATE.setLenient(true);
    }

    private TestUtil() {
        // empty - to prevent instantiation
    }

    public static final boolean isWindows() {
        return File.separatorChar == '\\';
    }

    /**
     * If you want to use a date in a unit test that eventually
     * 
     * @param usDate typical US date representation
     * @return local date
     */
    public static final Date localizeDateTime(String usDate) {
        try {
            return ENGLISH_DATE.parse(usDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Change "\n" the the native end-of-line char. On unix, this does nothing on windows, this
     * add "\r\n"
     */
    public static final String cleanEndOfLines(String s) {
        String clean = s.replaceAll("\n", EOL);
        return clean;
    }

    public static final String currentDrive() {
        if (!isWindows()) {
            return "";
        }

        String drive = new File(FORWARD_SLASH).getAbsolutePath().substring(0, 2);
        return drive;
    }

    /**
     * The directory that is part of the classpath that a class was loaded from
     */
    public static String getClasspathDirectory(Class testClass) {
        // create file on classpath
        CodeSource code = testClass.getProtectionDomain().getCodeSource();
        URL classpathUrl = code.getLocation();
        File classpathDir = new File(classpathUrl.getFile());

        return classpathDir.getAbsolutePath();
    }

    /**
     * Where to direct test output, cleaned on 'ant clean' and ignored by subversion
     */
    public static String getTestOutputDirectory(String project) {
        return getBuildDirectory(project) + "/test-results";
    }

    /**
     * Use ClassLoader.getSystemResource() when you can gets you a stream and can work from jars,
     * but when you need a filename use this. Example:
     * 
     * <pre>
     *              
     *               # Test file in same directory as JUnit test source file 
     *               String testFile = TestUtil.getTestSourceDirectory(getClass()) + &quot;/test-file&quot;;
     *               
     * </pre>
     */
    public static String getTestSourceDirectory(Class testClass) {
        StringBuffer sb = new StringBuffer(TestUtil.getProjectDirectory()).append("/test/")
                .append(testClass.getPackage().getName().replace('.', '/'));
        String path = sb.toString();

        return path;
    }

    public static String getProjectDirectory() {
        // eclipse
        String userDir = System.getProperty("user.dir");
        // ant
        return System.getProperty("basedir", userDir);
    }

    /**
     * Get the directory all autoconf and ant build output gets sent
     */
    public static String getBuildDirectory(String project) {
        try {
            String propName = "top.build.dir";
            File dir = new File(getProjectDirectory());
            while (dir != null) {
                File propFile = new File(dir, propName);
                if (propFile.exists()) {
                    Properties props = new Properties();
                    FileInputStream topBuildDirProperties = new FileInputStream(propFile);
                    props.load(topBuildDirProperties);
                    return props.getProperty(propName) + '/' + project;
                }
                dir = dir.getParentFile();
            }
            throw new RuntimeException(String.format(
                    "Cannot find %s in any of the parent of %s.", propName, dir.getPath()));
        } catch (IOException ioe) {
            throw new RuntimeException("Could not find top build directory", ioe);
        }
    }

    /**
     * Create a sysdir.properties file in the classpath. Uses a trick that will only work if unit
     * tests are unjar-ed. We could do this in ant, but this approach avoids setup and works in
     * IDEs like Eclipse where bin.eclipse is the classpath.
     */
    public static void setSysDirProperties(Properties sysProps, String etcDirectory,
            String outputDirectory) {

        // HACK: sysdir.bin is not a real directory when testing
        final String domainName = "sipfoundry.org";
        final String realm = "realm";
        sysProps.setProperty("sysdir.bin", outputDirectory + "/bin");
        sysProps.setProperty("sysdir.etc", etcDirectory);
        sysProps.setProperty("sysdir.data", outputDirectory);
        sysProps.setProperty("sysdir.share", outputDirectory);
        sysProps.setProperty("sysdir.var", outputDirectory);
        sysProps.setProperty("sysdir.phone", outputDirectory);
        sysProps.setProperty("sysdir.tmp", outputDirectory + "/tmp");
        sysProps.setProperty("sysdir.log", outputDirectory);
        sysProps.setProperty("phoneDefaults.tftpServer", "tftp.sipfoundry.org");
        sysProps.setProperty("phoneDefaults.authorizationRealm", realm);
        sysProps.setProperty("phoneDefaults.fullyQualifiedDomainName", "pbx." + domainName);
        sysProps.setProperty("dataSource.jdbcUrl", "jdbc:postgresql://localhost/SIPXCONFIG_TEST");
        sysProps.setProperty("acdHistoryDataSource.jdbcUrl",
                "jdbc:postgresql://localhost/SIPXACD_HISTORY_TEST");
        sysProps.setProperty("acdHistoricalStatsImpl.enabled", Boolean.toString(true));
        sysProps.setProperty("adminContextImpl.backupDirectory", outputDirectory + "/backup");
        sysProps.setProperty("coreContextImpl.authorizationRealm", realm);
        sysProps.setProperty("orbitsGenerator.audioDirectory", outputDirectory
                + "/parkserver/music");
        sysProps.setProperty("replicationTrigger.replicateOnStartup", Boolean.toString(false));
        sysProps.setProperty("whacker.enabled", Boolean.toString(false));
        sysProps.setProperty("acdContextImpl.enabled", Boolean.toString(true));
        sysProps.setProperty("indexTrigger.enabled", Boolean.toString(false));
        sysProps.setProperty("upload.uploadRootDirectory", outputDirectory + "/upload");
        sysProps.setProperty("upload.destinationDirectory", outputDirectory + "/tftproot");
        sysProps.setProperty("phonebookManagerImpl.externalUsersDirectory", outputDirectory
                + "/phonebook");
        File vmDir = new File(outputDirectory + "/mailstore");
        if (!vmDir.exists()) {
            if (!vmDir.mkdirs()) {
                throw new RuntimeException("Could not create voicemail store "
                        + vmDir.getAbsolutePath());
            }
        }
        sysProps.setProperty("mailboxManagerImpl.mailstoreDirectory", vmDir.getAbsolutePath());
    }

    public static void saveSysDirProperties(Properties sysProps, String classpathDirectory) {

        File sysdirPropsFile = new File(classpathDirectory, "sipxconfig.properties");
        FileOutputStream sysdirPropsStream;
        try {
            sysdirPropsStream = new FileOutputStream(sysdirPropsFile);
            // store them so spring's application context file find it
            // in classpath
            sysProps.store(sysdirPropsStream, null);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("could not create system dir properties file", e);
        } catch (IOException e) {
            throw new RuntimeException("could not store system dir properties", e);
        }
    }
}
