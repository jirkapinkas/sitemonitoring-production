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
        Throwable cause = ex.getCause();
        while(cause != null) {
	        if(cause.getMessage() != null) {
	            if(result.length() != 0) {
	                result.append(", ");
	            }
	            result.append(cause.getLocalizedMessage());
	        }
	        cause = cause.getCause();
        }
        return result.toString();
    }
}
