package queue4.externalizableObjects;

import java.io.*;

/**
 * Created by cliveh on 09/06/2016.
 */
public class SwapId implements Externalizable, Comparable<SwapId>
{
    private double _tenor;
    private String _tenorLabel;
    private int _hash;

    /**
     * @param tenor The tenor (e.g. 10 for "10Y").
     */
    public SwapId(double tenor)
    {
        updateValues(tenor);
    }

    /**
     * Only provided to allow class to be serialized/de-serialized; not not use!!
     */
    public SwapId()
    {
    }

    /**
     * Copy constructor.
     *
     * @param id The id.
     */
    public SwapId(SwapId id)
    {
        updateValues(id.getTenor());
    }

    /**
     * @param tenor The tenor of the swap
     * @return The tenor of the swap.
     */
    protected static String getTenorLabel(double tenor)
    {
        // See if we have an integer

        if ((tenor == Math.floor(tenor)) && !Double.isInfinite(tenor))
        {
            return ((int) tenor) + "Y";
        }
        else
        {
            return tenor + "Y";
        }
    }

    /**
     * Sets the values and re-calculates the hash code.
     *
     * @param tenor The tenor (e.g. 10 for "10Y").
     */
    public void updateValues(double tenor)
    {
        _tenor = tenor;
        _tenorLabel = SwapId.getTenorLabel(_tenor);
        buildHashCode();
    }

    /**
     * @return The tenor label (e.g. "10Y").
     */
    public String getTenorLabel()
    {
        return _tenorLabel;
    }

    /**
     * Set the tenor label
     *
     * @param tenorLabel The tenor label (e.g. "10Y").
     */
    public void setTenorLabel(String tenorLabel)
    {
        _tenorLabel = tenorLabel;
    }

    /**
     * @return The tenor (e.g. 10 for "10Y").
     */
    public double getTenor()
    {
        return _tenor;
    }

    public void setTenor(double tenor)
    {
        _tenor = tenor;
        _tenorLabel = SwapId.getTenorLabel(_tenor);
        buildHashCode();
    }

    /**
     * @param id The ID to compare against
     * @return A positive integer if this is greater than id, 0 if they represent the same structure, or a negative integer if this is less than id
     */
    @Override
    public final int compareTo(SwapId id)
    {
        return Double.compare(_tenor, id._tenor);
    }

    /**
     * Build hashCode based on other fields in constructor and also when deserializing.
     * <p/>
     * This is required as hash codes are not the same between different JVMs.
     */
    private void buildHashCode()
    {
        _hash = 17 + 31 * (int) _tenor;
    }

    /**
     * @see Object
     */
    @Override
    public int hashCode()
    {
        return _hash;
    }

    /**
     * @see Object
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else
        {
            if (obj instanceof SwapId)
            {
                SwapId id = (SwapId) obj;

                return id._tenor == _tenor;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * @see Object
     */
    @Override
    public String toString()
    {
        return _tenorLabel;
    }

    /**
     * @see Externalizable
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeDouble(_tenor);
    }

    /**
     * @see Externalizable
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        _tenor = in.readDouble();
        updateValues(_tenor);
    }
}
