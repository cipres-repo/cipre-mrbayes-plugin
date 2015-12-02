package dr.app.tracer;

/**
 * Catches an error when trace values are infinite.
 *
 * @author Jonas Kuhn
 *         Created on 11/04/14 3:05 PM
 */
public class TraceRuntimeException extends RuntimeException {

    public TraceRuntimeException () {
        super();
    }

    public TraceRuntimeException (String message) {
        super(message);
    }
}
