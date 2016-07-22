package queue4.externalizableObjects;

import java.io.Externalizable;

/**
 * Created by cliveh on 10/05/2016.
 */
/**
 * Should be implemented by all classes representing configuration settings.
 */
public interface ConfigSetting extends Externalizable
{
    /**
     * @return The ID for the executor that this is of relevance to.
     */
    String getExecutor();

    /**
     * Set the ID for the executor that this is of relevance to.
     *
     * @param executor The executor id
     */
    void setExecutor(String executor);

    /**
     * @return The unique identifier for the configuration setting.
     */
    String getId();

    /**
     * Set the unique identifier for the configuration setting.
     *
     * @param id The unique ID
     */
    void setId(String id);

    /**
     * @return true if this is for retransmit, false otherwise.
     */
    boolean isRetransmit();

    /**
     * Used to sets the retransmit property.
     *
     * @param isRetransmit true if this is for retransmit, false otherwise.
     */
    void setRetransmit(boolean isRetransmit);
}