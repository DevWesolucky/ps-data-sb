package pl.wesolucky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PsDataSbApplication 
{

	public static void main(String[] args) 
	{
		SpringApplication.run(PsDataSbApplication.class, args);
		System.out.println("--- startup completed ---");
	}
	
	
}
