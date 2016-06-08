package domains;


public interface Action {
    int id();  //  Usages: hashCode, ordering actions, checking equality

    String name();  // Usages: looking at output
}
