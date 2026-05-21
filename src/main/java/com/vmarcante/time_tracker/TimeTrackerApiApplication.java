package com.vmarcante.time_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TimeTrackerApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeTrackerApiApplication.class, args);
	}

}
