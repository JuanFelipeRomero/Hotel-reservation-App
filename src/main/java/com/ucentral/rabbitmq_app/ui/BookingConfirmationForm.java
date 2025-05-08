package com.ucentral.rabbitmq_app.ui;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import com.ucentral.rabbitmq_app.dto.RoomAvailableEventData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookingConfirmationForm extends JDialog { // Use JDialog for secondary window

   private final RoomAvailableEventData availableRoomData;
   private final RabbitTemplate rabbitTemplate;

   private JTextField guestNameField;
   private JTextField guestIdField;
   private JButton confirmButton;
   private JTextArea detailsArea;

   public BookingConfirmationForm(Frame owner, // Parent frame (can be null)
         RoomAvailableEventData availableRoomData,
         RabbitTemplate rabbitTemplate) {
      super(owner, "Confirm Booking", true); // Modal dialog
      this.availableRoomData = availableRoomData;
      this.rabbitTemplate = rabbitTemplate;

      setSize(450, 350);
      setLocationRelativeTo(owner); // Center relative to owner
      setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Close only this dialog

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
      confirmButton = new JButton("Confirm Booking");
   }

   private void populateDetails() {
      detailsArea.setText(String.format(
            "Room Available!\nNumber: %s\nType: %s\nPrice: $%.2f\nDates: %s to %s",
            availableRoomData.getRoomNumber(),
            availableRoomData.getRoomType(),
            availableRoomData.getPricePerNight(),
            availableRoomData.getCheckInDate(),
            availableRoomData.getCheckOutDate()));
   }

   private void layoutComponents() {
      JPanel detailsPanel = new JPanel(new BorderLayout(5, 5));
      detailsPanel.setBorder(BorderFactory.createTitledBorder("Available Room Details"));
      detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

      JPanel formPanel = new JPanel(new GridBagLayout());
      formPanel.setBorder(BorderFactory.createTitledBorder("Guest Information"));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.anchor = GridBagConstraints.WEST;

      gbc.gridx = 0;
      gbc.gridy = 0;
      formPanel.add(new JLabel("Guest Name:"), gbc);
      gbc.gridx = 1;
      gbc.gridy = 0;
      formPanel.add(guestNameField, gbc);

      gbc.gridx = 0;
      gbc.gridy = 1;
      formPanel.add(new JLabel("Guest ID/Number:"), gbc);
      gbc.gridx = 1;
      gbc.gridy = 1;
      formPanel.add(guestIdField, gbc);

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      buttonPanel.add(confirmButton);

      setLayout(new BorderLayout(10, 10));
      add(detailsPanel, BorderLayout.NORTH);
      add(formPanel, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.SOUTH);
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

      if (guestName.trim().isEmpty() || guestId.trim().isEmpty()) {
         JOptionPane.showMessageDialog(this, "Please enter both Guest Name and ID.", "Input Required",
               JOptionPane.WARNING_MESSAGE);
         return;
      }

      // Create the final DTO
      FinalBookingDetailsDTO finalDetails = new FinalBookingDetailsDTO(
            availableRoomData.getRoomId(),
            availableRoomData.getRoomNumber(),
            availableRoomData.getRoomType(),
            availableRoomData.getPricePerNight(),
            availableRoomData.getCheckInDate(),
            availableRoomData.getCheckOutDate(),
            guestName,
            guestId);

      // Send to RabbitMQ
      try {
         rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_PROCESSED_DETAILS,
               RabbitMQConfig.ROUTING_KEY_PROCESSED_DETAIL,
               finalDetails);
         System.out.println("BookingConfirmationForm: Sent FinalBookingDetailsDTO to RabbitMQ: " + finalDetails);
         // Close the dialog after sending
         JOptionPane.showMessageDialog(this, "Booking request sent successfully!", "Request Sent",
               JOptionPane.INFORMATION_MESSAGE);
         dispose();
      } catch (Exception ex) {
         System.err.println("BookingConfirmationForm: Error sending final booking message: " + ex.getMessage());
         JOptionPane.showMessageDialog(this, "Error sending booking request. Please check logs.", "Error",
               JOptionPane.ERROR_MESSAGE);
      }
   }
}