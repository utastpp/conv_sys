package cmcrdr.logger;

//import dialog.gui.UserGUI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is used to define Logger
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class Logger {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Logger.class);

    /**
     *
     */
    public static SimpleDateFormat loggerFormat = new SimpleDateFormat("[yyyy-MM-dd,HH:mm:ss]");

    /**
     *
     * @return
     */
    public static String getLoggingTime() {
        return loggerFormat.format(new Date());
    }

    /**
     *
     * @return
     */
    public static String getCallerInfo() {
        Throwable th = new Throwable();
        StackTraceElement[] ste = th.getStackTrace();
        String className = ste[2].getClassName();
        int idx = className.lastIndexOf('.');
        if (idx > 0) {
            className = className.substring(idx + 1);
        }
        return "[" + className + "." + ste[2].getMethodName() + ":" + ste[2].getLineNumber() + "]";
    }

    /**
     * Print informative data
     *
     * @param msg
     */
    public static void info(String msg) {
        //for developer
        //DPH
        //log.info(getLoggingTime() + "[I][" + getCallerInfo() + "] " + msg);
        log.info("[I][" + getCallerInfo() + "] " + msg);

        //for user
//        log.info(getLoggingTime() + "[I] " + msg);
//        if(UserGUI.isDefaultLookAndFeelDecorated()){
//            UserGUI.logTextArea.append(msg+ "\n");
//        }
    }

    /**
     * Print debug data
     *
     * @param msg
     */
    public static void debug(String msg) {
        log.debug(getLoggingTime() + "[D][" + getCallerInfo() + "] " + msg);
    }

    /**
     *
     * @param msg
     */
    public static void error(String msg) {
        log.error(getLoggingTime() + "[E][" + getCallerInfo() + "] " + msg);
    }

    /**
     *
     * @param msg
     */
    public static void warn(String msg) {
        log.warn(getLoggingTime() + "[W][" + getCallerInfo() + "] " + msg);
    }
}
