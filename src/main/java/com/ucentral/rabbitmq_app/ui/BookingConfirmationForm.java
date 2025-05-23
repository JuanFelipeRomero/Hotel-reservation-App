package com.ucentral.rabbitmq_app.ui;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import com.ucentral.rabbitmq_app.dto.RoomAvailableEventData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookingConfirmationForm extends JDialog {

   private final RoomAvailableEventData availableRoomData;
   private final RabbitTemplate rabbitTemplate;

   private JTextField guestNameField;
   private JTextField guestIdField;
   private JTextField guestEmailField;
   private JButton confirmButton;
   private JTextArea detailsArea;

   public BookingConfirmationForm(Frame owner,
         RoomAvailableEventData availableRoomData,
         RabbitTemplate rabbitTemplate) {
      super(owner, "Confirmar Reserva", true);
      this.availableRoomData = availableRoomData;
      this.rabbitTemplate = rabbitTemplate;

      setSize(450, 400);
      setLocationRelativeTo(owner);
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

      initComponents();
      layoutComponents();
      populateDetails();
      addEventListeners();
   }

   private void initComponents() {
      detailsArea = new JTextArea(5, 30);
      detailsArea.setEditable(false);
      detailsArea.setLineWrap(true);
      detailsArea.setWrapStyleWord(true);

      guestNameField = new JTextField(20);
      guestIdField = new JTextField(20);
      guestEmailField = new JTextField(20);
      confirmButton = new JButton("Confirmar Reserva");
   }

   private void populateDetails() {
      detailsArea.setText(String.format(
            "****Habitación Disponible****\nNúmero: %s\nTipo: %s\nPrecio: $%.2f\nFechas: %s a %s",
            availableRoomData.getRoomNumber(),
            availableRoomData.getRoomType(),
            availableRoomData.getPricePerNight(),
            availableRoomData.getCheckInDate(),
            availableRoomData.getCheckOutDate()));
   }

   private void layoutComponents() {
      // Panel principal con padding
      JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
      mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

      // Panel para mostrar detalles de la habitación (arriba)
      JPanel detailsPanel = new JPanel(new BorderLayout(5, 5));
      detailsPanel.setBorder(BorderFactory.createTitledBorder("Detalles de la Habitación Disponible"));
      detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

      // Panel para el formulario de información del huésped
      JPanel formPanel = new JPanel(new GridBagLayout());
      formPanel.setBorder(BorderFactory.createTitledBorder("Información del Huésped"));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.anchor = GridBagConstraints.WEST;

      // Labels - Column 0
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weightx = 0.0; // Labels don't expand horizontally
      gbc.fill = GridBagConstraints.NONE;
      formPanel.add(new JLabel("Nombre del Huésped:"), gbc);

      gbc.gridy = 1;
      formPanel.add(new JLabel("Identificacion del Huesped"), gbc);

      gbc.gridy = 2;
      formPanel.add(new JLabel("Email del Huésped:"), gbc);

      // TextFields - Column 1
      gbc.gridx = 1;
      gbc.gridy = 0;
      gbc.weightx = 1.0; // <<< Give this column horizontal weight to expand
      gbc.fill = GridBagConstraints.HORIZONTAL; // <<< Make fields fill horizontally
      formPanel.add(guestNameField, gbc);

      gbc.gridy = 1;
      formPanel.add(guestIdField, gbc);

      gbc.gridy = 2;
      formPanel.add(guestEmailField, gbc);

      // Panel para los botones
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      buttonPanel.add(confirmButton);

      // Añadir componentes al panel principal
      mainPanel.add(detailsPanel, BorderLayout.NORTH);
      mainPanel.add(formPanel, BorderLayout.CENTER);
      mainPanel.add(buttonPanel, BorderLayout.SOUTH);

      // Añadir panel principal al diálogo
      add(mainPanel, BorderLayout.CENTER);
   }

   private void addEventListeners() {
      confirmButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            handleConfirmBooking();
         }
      });
   }

   private void handleConfirmBooking() {
      String guestName = guestNameField.getText();
      String guestId = guestIdField.getText();
      String guestEmail = guestEmailField.getText();

      if (guestName.trim().isEmpty() || guestId.trim().isEmpty() || guestEmail.trim().isEmpty()) {
         JOptionPane.showMessageDialog(this, "Por favor, ingrese Nombre, ID y Email del huésped.", "Entrada Requerida",
               JOptionPane.WARNING_MESSAGE);
         return;
      }

      FinalBookingDetailsDTO finalDetails = new FinalBookingDetailsDTO(
            availableRoomData.getRoomId(),
            availableRoomData.getRoomNumber(),
            availableRoomData.getRoomType(),
            availableRoomData.getPricePerNight(),
            availableRoomData.getCheckInDate(),
            availableRoomData.getCheckOutDate(),
            guestName,
            guestId,
            guestEmail);

      try {
         rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_PROCESSED_DETAILS,
               RabbitMQConfig.ROUTING_KEY_PROCESSED_DETAIL,
               finalDetails);
         System.out.println("FinalBookingDetailsDTO enviado a RabbitMQ: " + finalDetails);
         JOptionPane.showMessageDialog(this, "¡Solicitud de reserva enviada exitosamente!", "Solicitud Enviada",
               JOptionPane.INFORMATION_MESSAGE);
         dispose();
      } catch (Exception ex) {
         System.err.println("Error al enviar mensaje final de reserva: " + ex.getMessage());
         JOptionPane.showMessageDialog(this, "Error al enviar la solicitud de reserva. Por favor, revise los logs.",
               "Error",
               JOptionPane.ERROR_MESSAGE);
      }
   }
}