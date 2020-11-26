package org.commonjava.ispn.cache;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class ISPNCache
{

    public void setup() throws Exception
    {
        EmbeddedCacheManager manager = new DefaultCacheManager("infinispan.xml");

        Cache c1 = manager.getCache( "distributed-cache");
        c1.put( "test", "tv" );

        System.out.println(c1.get( "test" ));
    }

}
