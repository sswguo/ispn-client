package org.commonjava.ispn.cache;

import org.infinispan.protostream.EnumMarshaller;

public class StoreTypeMarshaller implements EnumMarshaller<StoreType>
{

    @Override
    public Class getJavaClass()
    {
        return StoreType.class;
    }

    @Override
    public String getTypeName()
    {
        return "maven.StoreKey.StoreType";
    }

    @Override
    public StoreType decode( int enumValue )
    {
        if ( enumValue == 1 )
        {
            return StoreType.group;
        }
        else if ( enumValue == 2 )
        {
            return StoreType.remote;
        }
        else
        {
            return StoreType.hosted;
        }
    }

    @Override
    public int encode( StoreType storeType ) throws IllegalArgumentException
    {
        if ( storeType.equals( StoreType.group ) )
        {
            return 1;
        }
        else if ( storeType.equals( StoreType.remote ) )
        {
            return 2;
        }
        else
        {
            return 3;
        }
    }
}
