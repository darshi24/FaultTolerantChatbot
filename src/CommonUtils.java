/**
 * A class that provides some common utility functions
 */
public class CommonUtils {
    /**
     * A method to check if the entered port number is valid or not.
     * @param arg the port number
     * @return a boolean value of true or false. true if the port number is valid, otherwise false
     */
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
