package it.sky.keplero.exception;

public class RepricingGenericException extends RuntimeException {
    public RepricingGenericException() { super(); }
    public RepricingGenericException(String message) { super(message); }
    public RepricingGenericException(String message, Throwable cause) { super(message, cause); }
    public RepricingGenericException(Throwable cause) { super(cause); }
}
