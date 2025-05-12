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
import com.ucentral.rabbitmq_app.ui.AdminDashboardForm;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

import javax.swing.*;

@SpringBootApplication
public class RabbitmqAppApplication {

	@Autowired
	private ApplicationContext applicationContext;

	private AvailabilityCheckForm availabilityCheckForm;
	private CleaningServiceUI cleaningForm;
	private final RabbitTemplate rabbitTemplate;
	private final CleaningService cleaningService;

	public RabbitmqAppApplication(RabbitTemplate rabbitTemplate, CleaningService cleaningService) {
		this.rabbitTemplate = rabbitTemplate;
		this.cleaningService = cleaningService;
	}

	public static void main(String[] args) {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(RabbitmqAppApplication.class)
				.headless(false)
				.run(args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(AvailabilityService availabilityService, RabbitTemplate rabbitTemplate,
			ApplicationContext ctx) {
		return args -> {
			// Launch Availability Check Form
			SwingUtilities.invokeLater(() -> {
				try {
					System.out.println("Attempting to launch AvailabilityCheckForm...");
					this.availabilityCheckForm = new AvailabilityCheckForm(availabilityService, rabbitTemplate);
					this.availabilityCheckForm.setVisible(true);
					System.out.println("AvailabilityCheckForm launched successfully.");
				} catch (Exception e) {
					System.err.println("Error launching AvailabilityCheckForm: " + e.getMessage());
					e.printStackTrace();
				}
			});

			// Launch Cleaning Service UI
			SwingUtilities.invokeLater(() -> {
				try {
					System.out.println("Attempting to launch CleaningServiceUI...");
					if (this.cleaningService == null) {
						System.err.println("Error: CleaningService is null. Cannot launch CleaningServiceUI.");
						return;
					}
					cleaningForm = new CleaningServiceUI(this.cleaningService);
					cleaningForm.setVisible(true);
					System.out.println("CleaningServiceUI launched successfully.");
				} catch (Exception e) {
					System.err.println("Error launching CleaningServiceUI: " + e.getMessage());
					e.printStackTrace();
				}
			});

			// Launch Admin Dashboard Form
			SwingUtilities.invokeLater(() -> {
				try {
					System.out.println("Attempting to launch AdminDashboardForm...");
					AdminDashboardForm adminDashboard = ctx.getBean(AdminDashboardForm.class);
					adminDashboard.display(); // Calls setVisible(true) via display()
					System.out.println("AdminDashboardForm launched successfully.");
				} catch (Exception e) {
					System.err.println("Error launching AdminDashboardForm: " + e.getMessage());
					e.printStackTrace();
				}
			});
		};
	}

	@EventListener
	public void handleRoomAvailable(RoomAvailableEventData eventData) {
		System.out.println("Application Listener: Received RoomAvailableEventData: " + eventData);
		SwingUtilities.invokeLater(() -> {
			RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);

			BookingConfirmationForm bookingForm = new BookingConfirmationForm(
					this.availabilityCheckForm,
					eventData,
					rabbitTemplate);
			bookingForm.setVisible(true);
		});
	}

	@EventListener
	public void handleRoomNotAvailable(RoomNotAvailableEventData eventData) {
		System.out.println("Listener de Eventos de Aplicación: RoomNotAvailableEvent recibido: " + eventData);
		SwingUtilities.invokeLater(() -> {
			String message = String.format(
					"Lo sentimos, no hay habitaciones de tipo '%s' disponibles entre %s y %s.",
					eventData.getRoomType(),
					eventData.getCheckInDate(),
					eventData.getCheckOutDate());
			JOptionPane.showMessageDialog(availabilityCheckForm,
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
