package queue;


/**
 * Should be implemented by all classes that deal with absorbing events from an event source and invoking some form of action.
 */
public interface EventWorkflow
{
    /**
     * Call to start the event flow.
     */
    void start();


    /**
     * Call to stop the event flow.
     */
    void stop();


    /**
     * Call to failover the even workflow.
     */
    default void failover()
    {
    }


    /**
     * @return The ID.
     */
    String getExecutor();


    /**
     * Call to process a single event.
     *
     * @return true if there was an event to process, false otherwise.
     * @throws Exception if an unexpected error occurred when processing the event.
     */
    boolean runOnce() throws Exception;


    /**
     * Call when there are no more events to process and some form of action should be performed.
     *
     * @return true if some action was performed when this method was called, false otherwise.
     */
    boolean onIdle();
}
