package queue4;


/**
 * Should be implemented by all EventManagers.
 */
public interface EventManagerBase
{
    /**
     * @return The ID associated with this.
     */
    String getExecutor();


    /**
     * Triggered if a failover event is fired for the EventManager. Ignored by default.
     *
     * @param executor        executor that has failed over.
     * @param hasBeenExecuted Indicates whether or not the failover event has been executed. When triggered from
     *                        outside the queue the flag should be set to false to trigger a failover, but the event
     *                        written to queue should have the flag set to true to prevent components reading it
     *                        from triggering another failover.
     */
    default void failover(String executor, boolean hasBeenExecuted)
    {
    }
}