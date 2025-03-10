package cviettel.loginservice.util;

public class ThreadLocalUtil {

    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(String userId) {
        currentUser.set(userId);
    }

    public static String getCurrentUser() {
        return currentUser.get();
    }

    public static void remove() {
        currentUser.remove();
    }

}
