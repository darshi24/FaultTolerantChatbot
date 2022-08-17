public class CommonUtils {
    public static boolean isPortValid(String arg) {
        try {
            int port = Integer.parseInt(arg);
            if (port < 1024 || port > 65535) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}
