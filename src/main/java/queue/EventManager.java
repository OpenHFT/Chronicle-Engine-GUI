package queue;

public interface EventManager
{
    void start(String component);

    void update(String component, String eventId);
}