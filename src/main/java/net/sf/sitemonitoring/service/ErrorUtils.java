package net.sf.sitemonitoring.service;

public final class ErrorUtils {

    public static final String getError(Throwable ex) {
        if (ex == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        if (ex.getMessage() != null) {
            result.append(ex.getLocalizedMessage());
        }
        if(ex.getCause() != null && ex.getCause().getMessage() != null) {
            if(result.length() != 0) {
                result.append(", ");
            }
            result.append(ex.getCause().getLocalizedMessage());
        }
        return result.toString();
    }
}
