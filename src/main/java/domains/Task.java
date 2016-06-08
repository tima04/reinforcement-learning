package domains;

public interface Task {
	
	double getReward(State s, Action a, State sPrime);

	boolean taskEnds(State s); // true if state s is terminal state of task or domain.
	default boolean taskEnds(Features f){
		return false;
	} // true if state s is terminal state of task or domain.

	State startState();

	double gamma();

    void init();
}
