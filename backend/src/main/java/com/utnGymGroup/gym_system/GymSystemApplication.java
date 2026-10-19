package com.utnGymGroup.gym_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@SpringBootApplication
public class GymSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(GymSystemApplication.class, args);
	}

}
