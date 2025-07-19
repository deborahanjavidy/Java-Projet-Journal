import java.io.*;

public class SessionManager {
    private static final String SESSION_FILE = "session.txt";

    public static void saveUserId(Integer userId) {
        try (PrintWriter out = new PrintWriter(new FileWriter(SESSION_FILE))) {
            if (userId != null) {
                out.println(userId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Integer loadUserId() {
        try (BufferedReader in = new BufferedReader(new FileReader(SESSION_FILE))) {
            String line = in.readLine();
            if (line != null && !line.isEmpty()) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            
        }
        return null;
    }

    public static void clearSession() {
        File f = new File(SESSION_FILE);
        if (f.exists()) f.delete();
    }
} 