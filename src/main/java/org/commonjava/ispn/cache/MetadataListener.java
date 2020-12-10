package org.commonjava.ispn.cache;

import org.infinispan.client.hotrod.annotation.ClientCacheEntryCreated;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryExpired;
import org.infinispan.client.hotrod.annotation.ClientCacheEntryRemoved;
import org.infinispan.client.hotrod.annotation.ClientListener;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;

@ClientListener
public class MetadataListener
{

    @ClientCacheEntryExpired
    public void metadataExpired( ClientCacheEntryExpiredEvent event)
    {
        System.out.println("Expired :" + event.getKey());
    }

    @ClientCacheEntryCreated
    public void metadataCreated( ClientCacheEntryCreatedEvent event )
    {
        System.out.println("Created :" + event.getKey());
    }

    @ClientCacheEntryRemoved
    public void metadataRemoved( ClientCacheEntryRemovedEvent event )
    {
        System.out.println("Removed :" + event.getKey());
    }

}
