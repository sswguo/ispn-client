package org.commonjava.ispn.cache;

public class StoreKey
{

    private String keyId;

    private StoreType type;

    public StoreKey( String keyId, StoreType type )
    {
        this.keyId = keyId;
        this.type = type;
    }

    public String getKeyId()
    {
        return keyId;
    }

    public void setKeyId( String keyId )
    {
        this.keyId = keyId;
    }

    public StoreType getType()
    {
        return type;
    }

    public void setType( StoreType type )
    {
        this.type = type;
    }
}
