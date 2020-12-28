package org.commonjava.ispn.cache;

import java.util.HashSet;
import java.util.Set;

public enum StoreType
{
    group( false, "group", "groups", "g" ), remote( false, "remote", "remotes", "repository", "repositories",
                                                                 "r" ), hosted( true, "hosted", "hosted", "deploy", "deploys", "deploy_point", "h", "d" );

    //    private static final Logger logger = new Logger( StoreType.class );

    private final boolean writable;

    private final String singular;

    private final String plural;

    private final Set<String> aliases;

    public String getSingular()
    {
        return singular;
    }

    public String getPlural()
    {
        return plural;
    }

    public Set<String> getAliases()
    {
        return aliases;
    }

    StoreType( final boolean writable, final String singular,
               final String plural, final String... aliases )
    {
        this.writable = writable;
        this.singular = singular;
        this.plural = plural;

        final Set<String> a = new HashSet<String>();
        a.add( singular );
        a.add( plural );
        for ( final String alias : aliases )
        {
            a.add( alias.toLowerCase() );
        }

        this.aliases = a;
    }

    public String pluralEndpointName()
    {
        return plural;
    }

    public String singularEndpointName()
    {
        return singular;
    }

    public boolean isWritable()
    {
        return writable;
    }

    public static StoreType get( final String typeStr )
    {
        if ( typeStr == null )
        {
            return null;
        }

        final String type = typeStr.trim()
                                   .toLowerCase();
        if ( type.length() < 1 )
        {
            return null;
        }

        for ( final StoreType st : values() )
        {
            //            logger.info( "Checking '{}' vs name: '{}' and aliases: {}", type, st.name(), join( st.aliases, ", " ) );
            if ( st.name()
                   .equalsIgnoreCase( type ) || st.aliases.contains( type ) )
            {
                return st;
            }
        }

        return null;
    }

    public static StoreType get( final Class<?> storeCls )
    {
        if ( storeCls == null )
        {
            return null;
        }


        return null;
    }

}
