package org.commonjava.ispn;

import org.commonjava.ispn.cache.HotPodClient;
import org.commonjava.ispn.cache.ISPNCache;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
				DataSourceAutoConfiguration.class,
				DataSourceTransactionManagerAutoConfiguration.class,
				HibernateJpaAutoConfiguration.class
})
@ComponentScan({"org.commonjava"})
public class ISPNCacheApplication
{

	public static void main(String[] args) {
		SpringApplication.run( ISPNCacheApplication.class, args);
		System.setProperty( "java.net.preferIPv4Stack", "true" );
		System.out.println(System.getProperty( "java.net.preferIPv4Stack" ));
		/*ISPNCache ds = new ISPNCache();
		try
		{
			ds.setup();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}*/
		HotPodClient client = new HotPodClient();
		try
		{
			client.setup();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

}
