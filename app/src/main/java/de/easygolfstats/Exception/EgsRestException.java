package de.easygolfstats.Exception;

public class EgsRestException extends Exception {
    public EgsRestException (String message) {
        super(message);
    }
    public EgsRestException (Exception e) {
        super (e);
    }

    public EgsRestException (String message, Throwable t) {
        super (message, t);
    }
}
