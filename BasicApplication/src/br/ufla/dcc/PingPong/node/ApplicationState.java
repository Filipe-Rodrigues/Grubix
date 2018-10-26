package br.ufla.dcc.PingPong.node;

/**
 * state of an application.
 * @author dmeister
 *
 */
public enum ApplicationState implements Comparable<ApplicationState> {
	/** 
	 * application is started, but didn't do anything. 
	 */
	CREATED(false),
	
	/** 
	 * application is started and waits for something.
	  */
	WAITING(false),
	
	/** 
	 * application is started and did something.
	  */
	RUNNING(false),
	
	/**
	 *  application is completed, but failed.
	  */
	FAILED(true),
	
	/**
	 *  application completed successfully.
	  */
	DONE(true);
	
	/**
	 * flag indicated that the application is completed.
	 */
	private final boolean completed;

	/**
	 * constructor .
	 * @param completed flag indicated that the application is completed
	 */
	ApplicationState(boolean completed) {
		this.completed = completed;
	}
	
	/**
	 * returns the higher application state.
	 * @param state1 an application state
	 * @param state2 an application state
	 * @return the higher application state
	 */
	public static ApplicationState higherState(ApplicationState state1, ApplicationState state2) {
		if (state1.compareTo(state2) >= 0) {
			return state1;
		}
		return state2;
	}

	/**
	 * returns true if the state indicates a completion.
	 * @return true if completed or false
	 */
	public boolean isCompleted() {
		return completed;
	}
}
