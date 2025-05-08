package com.ucentral.rabbitmq_app.ui;

import com.ucentral.rabbitmq_app.RabbitMQConfig;
import com.ucentral.rabbitmq_app.dto.AvailabilityRequestDTO;
import com.ucentral.rabbitmq_app.model.RoomType;
import com.ucentral.rabbitmq_app.services.AvailabilityService;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class AvailabilityCheckForm extends JFrame {

   private JDatePickerImpl checkInDatePicker;
   private JDatePickerImpl checkOutDatePicker;
   private JComboBox<RoomType> roomTypeComboBox;
   private JButton checkAvailabilityButton;
   private JTextArea resultsArea;
   private final AvailabilityService availabilityService;
   private final RabbitTemplate rabbitTemplate;
   private static final DateTimeFormatter DTO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

   public AvailabilityCheckForm(AvailabilityService availabilityService, RabbitTemplate rabbitTemplate) {
      this.availabilityService = availabilityService;
      this.rabbitTemplate = rabbitTemplate;

      setTitle("Hotel Room Availability Check");
      setSize(500, 450);
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLocationRelativeTo(null);

      initComponents();
      layoutComponents();
      addEventListeners();
   }

   private void initComponents() {
      UtilDateModel checkInModel = new UtilDateModel();
      UtilDateModel checkOutModel = new UtilDateModel();
      Properties p = new Properties();
      p.put("text.today", "Today");
      p.put("text.month", "Month");
      p.put("text.year", "Year");
      JDatePanelImpl checkInDatePanel = new JDatePanelImpl(checkInModel, p);
      JDatePanelImpl checkOutDatePanel = new JDatePanelImpl(checkOutModel, p);

      checkInDatePicker = new JDatePickerImpl(checkInDatePanel, new DateLabelFormatter());
      checkOutDatePicker = new JDatePickerImpl(checkOutDatePanel, new DateLabelFormatter());

      roomTypeComboBox = new JComboBox<>(RoomType.values());
      checkAvailabilityButton = new JButton("Check Availability");
      resultsArea = new JTextArea(10, 40);
      resultsArea.setEditable(false);
   }

   private void layoutComponents() {
      JPanel inputPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.anchor = GridBagConstraints.WEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;

      gbc.gridx = 0;
      gbc.gridy = 0;
      inputPanel.add(new JLabel("Check-in Date:"), gbc);
      gbc.gridx = 1;
      inputPanel.add(checkInDatePicker, gbc);

      gbc.gridx = 0;
      gbc.gridy = 1;
      inputPanel.add(new JLabel("Check-out Date:"), gbc);
      gbc.gridx = 1;
      inputPanel.add(checkOutDatePicker, gbc);

      gbc.gridx = 0;
      gbc.gridy = 2;
      inputPanel.add(new JLabel("Room Type:"), gbc);
      gbc.gridx = 1;
      inputPanel.add(roomTypeComboBox, gbc);

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      buttonPanel.add(checkAvailabilityButton);
      JScrollPane scrollPane = new JScrollPane(resultsArea);

      setLayout(new BorderLayout(10, 10));
      add(inputPanel, BorderLayout.NORTH);
      add(buttonPanel, BorderLayout.CENTER);
      add(scrollPane, BorderLayout.SOUTH);
   }

   private void addEventListeners() {
      checkAvailabilityButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            handleCheckAvailability();
         }
      });
   }

   private void handleCheckAvailability() {
      resultsArea.setText("Checking availability...");

      Date selectedCheckInUtilDate = (Date) checkInDatePicker.getModel().getValue();
      Date selectedCheckOutUtilDate = (Date) checkOutDatePicker.getModel().getValue();
      RoomType selectedRoomType = (RoomType) roomTypeComboBox.getSelectedItem();

      if (selectedCheckInUtilDate == null || selectedCheckOutUtilDate == null) {
         resultsArea.setText("Please select both check-in and check-out dates.");
         return;
      }

      LocalDate checkInLocalDate = selectedCheckInUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate checkOutLocalDate = selectedCheckOutUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

      if (checkOutLocalDate.isBefore(checkInLocalDate) || checkOutLocalDate.isEqual(checkInLocalDate)) {
         resultsArea.setText("Check-out date must be after check-in date.");
         return;
      }

      try {
         if (this.rabbitTemplate == null) {
            resultsArea.setText("Error: RabbitMQ connection not available.");
            return;
         }
         AvailabilityRequestDTO requestDTO = new AvailabilityRequestDTO(
               checkInLocalDate.format(DTO_DATE_FORMATTER),
               checkOutLocalDate.format(DTO_DATE_FORMATTER),
               selectedRoomType.name());

         rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DISPONIBILIDAD,
               RabbitMQConfig.ROUTING_KEY_VERIFICAR_DISPONIBILIDAD,
               requestDTO);
         System.out.println("UI Form: Data sent to RabbitMQ (Exchange: " + RabbitMQConfig.EXCHANGE_DISPONIBILIDAD
               + ", RoutingKey: " + RabbitMQConfig.ROUTING_KEY_VERIFICAR_DISPONIBILIDAD + ") -> " + requestDTO);
         resultsArea.setText("Availability check request sent. Waiting for results...");
      } catch (Exception ex) {
         System.err.println("AvailabilityCheckForm: Error sending initial request DTO to RabbitMQ: " + ex.getMessage());
         resultsArea.setText("Error sending availability request. Please check logs.");
      }
   }

   public static class DateLabelFormatter extends JFormattedTextField.AbstractFormatter {
      private final String datePattern = "yyyy-MM-dd";
      private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

      @Override
      public Object stringToValue(String text) throws ParseException {
         return dateFormatter.parseObject(text);
      }

      @Override
      public String valueToString(Object value) throws ParseException {
         if (value != null) {
            Calendar cal = (Calendar) value;
            return dateFormatter.format(cal.getTime());
         }
         return "";
      }
   }

   public static void main(String[] args) {
      SwingUtilities.invokeLater(() -> new AvailabilityCheckForm(null, null).setVisible(true));
   }

   // Add a getter for the results area
   public JTextArea getResultsArea() {
      return resultsArea;
   }
}