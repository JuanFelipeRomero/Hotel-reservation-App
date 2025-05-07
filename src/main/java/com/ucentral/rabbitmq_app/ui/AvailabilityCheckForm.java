package com.ucentral.rabbitmq_app.ui;

import com.ucentral.rabbitmq_app.model.RoomType;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class AvailabilityCheckForm extends JFrame {

   private JDatePickerImpl checkInDatePicker;
   private JDatePickerImpl checkOutDatePicker;
   private JComboBox<RoomType> roomTypeComboBox;
   private JButton checkAvailabilityButton;
   private JTextArea resultsArea;

   public AvailabilityCheckForm() {
      setTitle("Hotel Room Availability Check");
      setSize(500, 450); // Adjusted size slightly for date pickers
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setLocationRelativeTo(null); // Center the window

      initComponents();
      layoutComponents();
      addEventListeners();
   }

   private void initComponents() {
      // Date picker setup
      UtilDateModel checkInModel = new UtilDateModel();
      UtilDateModel checkOutModel = new UtilDateModel();
      Properties p = new Properties();
      p.put("text.today", "Today");
      p.put("text.month", "Month");
      p.put("text.year", "Year");
      JDatePanelImpl checkInDatePanel = new JDatePanelImpl(checkInModel, p);
      JDatePanelImpl checkOutDatePanel = new JDatePanelImpl(checkOutModel, p);

      // Arguments: JDatePanelImpl, DateLabelFormatter
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

      // Check-in Date
      gbc.gridx = 0;
      gbc.gridy = 0;
      inputPanel.add(new JLabel("Check-in Date:"), gbc);
      gbc.gridx = 1;
      inputPanel.add(checkInDatePicker, gbc);

      // Check-out Date
      gbc.gridx = 0;
      gbc.gridy = 1;
      inputPanel.add(new JLabel("Check-out Date:"), gbc);
      gbc.gridx = 1;
      inputPanel.add(checkOutDatePicker, gbc);

      // Room Type
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
      Date selectedCheckInDate = (Date) checkInDatePicker.getModel().getValue();
      Date selectedCheckOutDate = (Date) checkOutDatePicker.getModel().getValue();
      RoomType selectedRoomType = (RoomType) roomTypeComboBox.getSelectedItem();

      if (selectedCheckInDate == null || selectedCheckOutDate == null) {
         resultsArea.setText("Please select both check-in and check-out dates.");
         return;
      }

      // Optional: Convert to java.time.LocalDate if your service layer uses it
      // LocalDate checkIn =
      // selectedCheckInDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      // LocalDate checkOut =
      // selectedCheckOutDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String checkInDateStr = sdf.format(selectedCheckInDate);
      String checkOutDateStr = sdf.format(selectedCheckOutDate);

      resultsArea.setText("Checking availability for:\n" +
            "Check-in: " + checkInDateStr + "\n" +
            "Check-out: " + checkOutDateStr + "\n" +
            "Room Type: " + selectedRoomType);

      // TODO: Call AvailabilityService
      System.out.println("Check-in: " + checkInDateStr);
      System.out.println("Check-out: " + checkOutDateStr);
      System.out.println("Room Type: " + selectedRoomType);
   }

   // Helper class for formatting the date in the JDatePicker
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
      SwingUtilities.invokeLater(() -> new AvailabilityCheckForm().setVisible(true));
   }
}