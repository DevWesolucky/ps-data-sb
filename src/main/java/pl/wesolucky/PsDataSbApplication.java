package pl.wesolucky;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PsDataSbApplication 
{

	static Logger log = LoggerFactory.getLogger(PsDataSbApplication.class);
	
	public static void main(String[] args) 
	{
		SpringApplication.run(PsDataSbApplication.class, args);
		log.info("\n--- startup completed ---");
	}
	
	
}
