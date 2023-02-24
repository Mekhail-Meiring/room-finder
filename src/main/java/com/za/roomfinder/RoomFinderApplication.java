package com.za.roomfinder;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class RoomFinderApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomFinderApplication.class, args);
	}

}
