package org.commonjava.ispn.cache;

import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

//https://access.redhat.com/documentation/en-us/red_hat_data_grid/7.1/html/developer_guide/remote_querying
public class CacheEMarshaller implements MessageMarshaller<CacheEAnnotation>
{
    @Override
    public CacheEAnnotation readFrom( ProtoStreamReader reader ) throws IOException
    {
        String id = reader.readString("id");

        return new CacheEAnnotation(id);
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, CacheEAnnotation cacheEAnnotation ) throws IOException
    {
        writer.writeString("id", cacheEAnnotation.getId());
    }

    @Override
    public Class<? extends CacheEAnnotation> getJavaClass()
    {
        return CacheEAnnotation.class;
    }

    @Override
    public String getTypeName()
    {
        return "cache.CacheEAnnotation";
    }
}
