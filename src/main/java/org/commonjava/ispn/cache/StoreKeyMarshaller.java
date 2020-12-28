package org.commonjava.ispn.cache;

import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class StoreKeyMarshaller implements MessageMarshaller<StoreKey>
{
    @Override
    public StoreKey readFrom( ProtoStreamReader reader ) throws IOException
    {
        String keyId = reader.readString( "keyId" );
        StoreType type = reader.readEnum( "type", StoreType.class );
        return new StoreKey( keyId, type );
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, StoreKey storeKey ) throws IOException
    {
        writer.writeString( "keyId", storeKey.getKeyId() );
        writer.writeEnum( "type", storeKey.getType() );
    }

    @Override
    public Class<? extends StoreKey> getJavaClass()
    {
        return StoreKey.class;
    }

    @Override
    public String getTypeName()
    {
        return "maven.StoreKey";
    }
}
