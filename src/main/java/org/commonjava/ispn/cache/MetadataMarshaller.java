package org.commonjava.ispn.cache;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.infinispan.protostream.MessageMarshaller;

import java.io.IOException;

//https://access.redhat.com/documentation/en-us/red_hat_data_grid/7.1/html/developer_guide/remote_querying
public class MetadataMarshaller
                implements MessageMarshaller<Metadata>
{
    @Override
    public Metadata readFrom( ProtoStreamReader reader ) throws IOException
    {
        String artifactId = reader.readString("artifactId");
        String groupId = reader.readString( "groupId" );
        String version = reader.readString( "version" );
        Versioning versioning = reader.readObject( "versioning", Versioning.class );

        Metadata metadata = new Metadata();
        metadata.setArtifactId( artifactId );
        metadata.setGroupId( groupId );
        metadata.setVersion( version );
        metadata.setVersioning( versioning );

        return metadata;
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, Metadata metadata ) throws IOException
    {
        writer.writeString("artifactId", metadata.getArtifactId());
        writer.writeString( "groupId", metadata.getGroupId() );
        writer.writeString( "version", metadata.getVersion() );
        writer.writeObject( "versioning", metadata.getVersioning(), Versioning.class );
    }

    @Override
    public Class<? extends Metadata> getJavaClass()
    {
        return Metadata.class;
    }

    @Override
    public String getTypeName()
    {
        return "maven.Metadata";
    }
}
