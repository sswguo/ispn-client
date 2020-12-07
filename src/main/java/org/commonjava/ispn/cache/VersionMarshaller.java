package org.commonjava.ispn.cache;

import org.apache.maven.artifact.repository.metadata.Versioning;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

public class VersionMarshaller implements MessageMarshaller<Versioning>
{
    @Override
    public Versioning readFrom( ProtoStreamReader reader ) throws IOException
    {
        Versioning versioning = new Versioning();
        versioning.setRelease( reader.readString( "release" ) );
        return versioning;
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, Versioning versioning ) throws IOException
    {
        writer.writeString( "release", versioning.getRelease() );
    }

    @Override
    public Class<? extends Versioning> getJavaClass()
    {
        return Versioning.class;
    }

    @Override
    public String getTypeName()
    {
        return "maven.Versioning";
    }
}
