package rso.dfs.client.handlers.error;

/**
 * Class that represents exceptions like (so provide good comment): - file not
 * found in client's file system (probably unable to read file before sending or
 * whatever) - inability to save file in clients file system - ...
 * 
 * @author Adam Papros <adam.papros@gmail.com>
 * */
public class FileOperationError extends RSOErrorBase {

	private static final long serialVersionUID = -4393860890419844842L;

	/**
	 * HAHAHAH : Comment your error please
	 * */
	private FileOperationError() {
		// TODO Auto-generated constructor stub
	}

	public FileOperationError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public FileOperationError(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public FileOperationError(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public FileOperationError(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
