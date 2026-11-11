package com.company.librarymanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LibrarymanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibrarymanagementApplication.class, args);
	}

}
