package rso.dfs.client.handlers.error;

/**
 * Base class for supported errors.
 * 
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public abstract class RSOErrorBase extends Exception {

	private static final long serialVersionUID = -8758451960664610609L;

	public RSOErrorBase() {
		// TODO Auto-generated constructor stub
	}

	public RSOErrorBase(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public RSOErrorBase(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RSOErrorBase(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RSOErrorBase(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
