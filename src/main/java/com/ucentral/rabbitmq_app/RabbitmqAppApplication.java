package com.ucentral.rabbitmq_app;

import com.ucentral.rabbitmq_app.dto.RoomAvailableEventData;
import com.ucentral.rabbitmq_app.dto.RoomNotAvailableEventData;
import com.ucentral.rabbitmq_app.services.AvailabilityService;
import com.ucentral.rabbitmq_app.ui.AvailabilityCheckForm;
import com.ucentral.rabbitmq_app.ui.BookingConfirmationForm;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.swing.*;
import java.awt.*;

// @EnableAsync // Consider adding if event listeners might block
@SpringBootApplication
public class RabbitmqAppApplication {

	private AvailabilityCheckForm mainForm;
	private final RabbitTemplate rabbitTemplate;

	public RabbitmqAppApplication(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	public static void main(String[] args) {
		// SpringApplication.run(RabbitmqAppApplication.class, args);
		// Modified to run in a non-headless environment for Swing UI
		new SpringApplicationBuilder(RabbitmqAppApplication.class)
				.headless(false) // Allow AWT/Swing
				.run(args);
	}

	@Bean
	// Re-inject RabbitTemplate here
	public CommandLineRunner launchSwingUI(AvailabilityService availabilityService, RabbitTemplate rabbitTemplateCL) {
		return args -> {
			// Ensure UI updates are on the Event Dispatch Thread (EDT)
			SwingUtilities.invokeLater(() -> {
				// Pass the actual RabbitTemplate instance needed by the form
				mainForm = new AvailabilityCheckForm(availabilityService, rabbitTemplateCL);
				mainForm.setVisible(true);
			});
		};
	}

	// Event Listener to handle RoomAvailableEvent
	@EventListener
	public void handleRoomAvailable(RoomAvailableEventData eventData) {
		System.out.println("Application Event Listener: Received RoomAvailableEvent: " + eventData);
		// Ensure UI update happens on the EDT
		SwingUtilities.invokeLater(() -> {
			// Use the class-level rabbitTemplate for the booking form
			BookingConfirmationForm bookingForm = new BookingConfirmationForm(mainForm, eventData, this.rabbitTemplate);
			bookingForm.setVisible(true);
		});
	}

	// New Event Listener for Room Not Available
	@EventListener
	public void handleRoomNotAvailable(RoomNotAvailableEventData eventData) {
		System.out.println("Application Event Listener: Received RoomNotAvailableEvent: " + eventData);
		// Ensure UI update happens on the EDT
		SwingUtilities.invokeLater(() -> {
			String message = String.format(
					"Sorry, no rooms of type '%s' are available between %s and %s.",
					eventData.getRoomType(),
					eventData.getCheckInDate(),
					eventData.getCheckOutDate());
			JOptionPane.showMessageDialog(mainForm, // Parent component
					message,
					"No Availability",
					JOptionPane.INFORMATION_MESSAGE);
			// Also update the main form's text area
			if (mainForm != null) {
				// Assuming AvailabilityCheckForm has a method like this:
				// mainForm.updateResultArea(message);
				// Or access it directly if needed and safe
				JTextArea resultsArea = mainForm.getResultsArea(); // Need a getter for this
				if (resultsArea != null) {
					resultsArea.setText(message);
				}
			}
		});
	}
}
