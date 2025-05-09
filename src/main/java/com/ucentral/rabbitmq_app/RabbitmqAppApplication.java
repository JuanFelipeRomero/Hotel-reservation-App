package com.ucentral.rabbitmq_app;

import com.ucentral.rabbitmq_app.dto.RoomAvailableEventData;
import com.ucentral.rabbitmq_app.dto.RoomNotAvailableEventData;
import com.ucentral.rabbitmq_app.events.CleaningTaskUpdatedEvent;
import com.ucentral.rabbitmq_app.events.NewCleaningTaskEvent;
import com.ucentral.rabbitmq_app.services.AvailabilityService;
import com.ucentral.rabbitmq_app.services.CleaningService;
import com.ucentral.rabbitmq_app.ui.AvailabilityCheckForm;
import com.ucentral.rabbitmq_app.ui.BookingConfirmationForm;
import com.ucentral.rabbitmq_app.ui.CleaningServiceUI;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import javax.swing.*;

@SpringBootApplication
public class RabbitmqAppApplication {

	private AvailabilityCheckForm mainForm;
	private CleaningServiceUI cleaningForm;
	private final RabbitTemplate rabbitTemplate;
	private final CleaningService cleaningService;

	public RabbitmqAppApplication(RabbitTemplate rabbitTemplate, CleaningService cleaningService) {
		this.rabbitTemplate = rabbitTemplate;
		this.cleaningService = cleaningService;
	}

	public static void main(String[] args) {
		new SpringApplicationBuilder(RabbitmqAppApplication.class)
				.headless(false)
				.run(args);
	}

	@Bean
	// Re-inject RabbitTemplate here
	public CommandLineRunner launchSwingUIs(AvailabilityService availabilityService, RabbitTemplate rabbitTemplateCL) {
		return args -> {
			SwingUtilities.invokeLater(() -> {
				mainForm = new AvailabilityCheckForm(availabilityService, rabbitTemplateCL);
				mainForm.setVisible(true);

				// Launch Cleaning Service UI
				cleaningForm = new CleaningServiceUI(this.cleaningService);
				cleaningForm.setVisible(true);
			});
		};
	}

	// Event Listener to handle RoomAvailableEvent
	@EventListener
	public void handleRoomAvailable(RoomAvailableEventData eventData) {
		System.out.println("Listener de Eventos de Aplicación: RoomAvailableEvent recibido: " + eventData);
		SwingUtilities.invokeLater(() -> {
			// Use the class-level rabbitTemplate for the booking form
			BookingConfirmationForm bookingForm = new BookingConfirmationForm(mainForm, eventData, this.rabbitTemplate);
			bookingForm.setVisible(true);
		});
	}

	// New Event Listener for Room Not Available
	@EventListener
	public void handleRoomNotAvailable(RoomNotAvailableEventData eventData) {
		System.out.println("Listener de Eventos de Aplicación: RoomNotAvailableEvent recibido: " + eventData);
		SwingUtilities.invokeLater(() -> {
			String message = String.format(
					"Lo sentimos, no hay habitaciones de tipo '%s' disponibles entre %s y %s.",
					eventData.getRoomType(),
					eventData.getCheckInDate(),
					eventData.getCheckOutDate());
			JOptionPane.showMessageDialog(mainForm, // Parent component
					message,
					"Sin Disponibilidad",
					JOptionPane.INFORMATION_MESSAGE);
		});
	}

	@EventListener
	public void handleNewCleaningTask(NewCleaningTaskEvent event) {
		System.out.println("Listener de Eventos de Aplicación: NewCleaningTaskEvent recibido: " + event.getTask());
		if (cleaningForm != null) {
			cleaningForm.handleNewTask(event.getTask());
		}
	}

	@EventListener
	public void handleCleaningTaskUpdated(CleaningTaskUpdatedEvent event) {
		System.out.println("Listener de Eventos de Aplicación: CleaningTaskUpdatedEvent recibido: " + event.getTask());
		if (cleaningForm != null) {
			cleaningForm.handleTaskUpdated(event.getTask());
		}
	}
}
