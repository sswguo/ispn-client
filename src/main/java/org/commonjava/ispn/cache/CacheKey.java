package org.commonjava.ispn.cache;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.io.Serializable;
/**
 * This class is annotated with the infinispan Protostream support annotations.
 * With this method, you don't need to define a protobuf file and a marshaller for the object.
 */
//https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.0/html/data_grid_developer_guide/protostream
//@Indexed
@ProtoDoc("@Indexed")
public class CacheKey implements Serializable
{

    //@Field(index = Index.YES, analyze = Analyze.NO)
    @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.NO)")
    @ProtoField(number = 1)
    public String key;

    //@Field(index = Index.NO, analyze = Analyze.NO)
    @ProtoDoc("@Field(index=Index.NO, analyze = Analyze.NO, store = Store.NO)")
    @ProtoField(number = 2)
    public String value;

    @ProtoFactory
    public CacheKey( String key, String value )
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "CacheKey{" + "key='" + key + '\'' + ", value='" + value + '\'' + '}';
    }
}
