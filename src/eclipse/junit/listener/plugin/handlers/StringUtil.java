package eclipse.junit.listener.plugin.handlers;

public class StringUtil
{
    public static boolean isBlank(final Object string) {
        return isBlank(string, false);
    }

    public static boolean isNotBlank(final Object string) {
        return !isBlank(string);
    }

    public static boolean isBlank(final Object string, final boolean enabledNull) {
        boolean blank = false;
        if (string == null) {
            blank = true;
        }
        else if (string instanceof String) {
            blank = string.toString().length() == 0 || !enabledNull && "null".equalsIgnoreCase(string.toString())
                    || string.toString().trim().length() == 0;
        }

        return blank;
    }
}
