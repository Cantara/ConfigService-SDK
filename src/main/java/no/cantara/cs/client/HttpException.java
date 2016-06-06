package no.cantara.cs.client;

/**
 * Exception for
 *
 * @author Asbjørn Willersrud
 */
public class HttpException extends RuntimeException {

    private final int statusCode;

    public HttpException(int statusCode, String responseMessage) {
        super(responseMessage);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
