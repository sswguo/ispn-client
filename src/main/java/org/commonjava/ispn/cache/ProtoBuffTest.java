package org.commonjava.ispn.cache;

import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;


public class ProtoBuffTest
{
    //https://protostuff.github.io/docs/
    //https://protostuff.github.io/docs/protostuff-runtime/
    public static void main(String[] args)
    {
        Schema schema = RuntimeSchema.getSchema( CacheEAnnotation.class);
        System.out.println(schema.newMessage().toString());

        RuntimeSchema.register( CacheEAnnotation.class, schema );
    }

}
