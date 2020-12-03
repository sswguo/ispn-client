package org.commonjava.ispn.cache;

import org.infinispan.protostream.annotations.ProtoField;

import java.io.Serializable;

public class CacheItem implements Serializable
{

    @ProtoField( number = 1 )
    public String itemId;

    public CacheItem() {}

    public CacheItem( String itemId )
    {
        this.itemId = itemId;
    }

}
