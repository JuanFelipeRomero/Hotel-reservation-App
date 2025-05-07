package com.ucentral.rabbitmq_app;

import com.ucentral.rabbitmq_app.services.AvailabilityService;
import com.ucentral.rabbitmq_app.ui.AvailabilityCheckForm;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.swing.*;

@SpringBootApplication
public class RabbitmqAppApplication {

	public static void main(String[] args) {
		// SpringApplication.run(RabbitmqAppApplication.class, args);
		// Modified to run in a non-headless environment for Swing UI
		new SpringApplicationBuilder(RabbitmqAppApplication.class)
				.headless(false) // Allow AWT/Swing
				.run(args);
	}

	@Bean
	public CommandLineRunner launchSwingUI(AvailabilityService availabilityService) {
		return args -> {
			// Ensure UI updates are on the Event Dispatch Thread (EDT)
			SwingUtilities.invokeLater(() -> {
				// Pass the service to the form
				AvailabilityCheckForm form = new AvailabilityCheckForm(availabilityService);
				form.setVisible(true);
			});
		};
	}
}
