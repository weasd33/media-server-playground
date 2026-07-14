package com.github.weasd33.playground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MediaPlaygroundApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediaPlaygroundApplication.class, args);
	}

}
