package filelogger;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class FileLogger {

    private FileLogger() {}

    private static String LOG_ROOT_LOCATION = "";
    private static BufferedWriter d = null;
    private static String NL = "\n";

    private static class FileLoggerInstance {
        private static final FileLogger FILELOGGER = new FileLogger();
    }

    public static FileLogger getInstance() {
        return FileLoggerInstance.FILELOGGER;
    }

    public static String getDateString() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat date = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        date.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = date.format(calendar.getTime());
        return dateString;
    }

    public static void info(String toLog) {
        log("[INFO] ", toLog);
    }

    public static void debug(String toLog) {
        log("[DEBUG] ", toLog);
    }

    public static void error(String toLog) {
        log("[ERROR] ", toLog);
    }

    public static void warn(String toLog) {
        log("[WARN] ", toLog);
    }

    public static void init(String path) {
        d = null;
        try {
            LOG_ROOT_LOCATION = path;
            // check if folder exists, if not - create it
            new File(path).mkdirs();
            d = new BufferedWriter(new FileWriter(LOG_ROOT_LOCATION));
            d.write("Server log: " + getDateString() + NL + NL);
            d.flush();
        } catch (FileNotFoundException ex) {

        } catch (IOException e) {

        }
    }

    public static void closeFileLogger() {
        try {
            d.close();
        } catch (IOException e) {

        }
    }

    private static synchronized void log(String level, String toLog) {
        try {
            d.write(level + getDateString() + ": "+ toLog + NL);
            d.flush();
        } catch (IOException e) {

        }
    }
}

