package org.commonjava.ispn.cache;

public class CacheEAnnotation
{

    public String id;

    public CacheEAnnotation(){}

    public CacheEAnnotation( String id )
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    @Override
    public String toString()
    {
        return "CacheEAnnotation{" + "id='" + id + '\'' + '}';
    }
}
