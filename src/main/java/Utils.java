import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Utils {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?!-)[A-Za-z0-9-]+([\\-\\.]{1}[a-z0-9]+)*\\.[A-Za-z]{2,6}$"
    );
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$"
    );

    public static boolean isNotValidDomainNorIP(String host) {
        return !(DOMAIN_PATTERN.matcher(host).matches() || IP_PATTERN.matcher(host).matches());
    }

    public static boolean isValidRegex(String pattern) {
        if (pattern == null) return false;
        try {
            Pattern.compile(pattern);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    public static Pattern getRegex(String pattern) {
        if (pattern == null) return null;
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }
}