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
   private final AvailabilityService availabilityService;
   private final RabbitTemplate rabbitTemplate;
   private static final DateTimeFormatter DTO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

   public AvailabilityCheckForm(AvailabilityService availabilityService, RabbitTemplate rabbitTemplate) {
      this.availabilityService = availabilityService;
      this.rabbitTemplate = rabbitTemplate;

      setTitle("Verificación de Disponibilidad de Habitaciones");
      setSize(500, 250);
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
      p.put("text.today", "Hoy");
      p.put("text.month", "Mes");
      p.put("text.year", "Año");
      JDatePanelImpl checkInDatePanel = new JDatePanelImpl(checkInModel, p);
      JDatePanelImpl checkOutDatePanel = new JDatePanelImpl(checkOutModel, p);

      checkInDatePicker = new JDatePickerImpl(checkInDatePanel, new DateLabelFormatter());
      checkOutDatePicker = new JDatePickerImpl(checkOutDatePanel, new DateLabelFormatter());

      roomTypeComboBox = new JComboBox<>(RoomType.values());
      checkAvailabilityButton = new JButton("Verificar Disponibilidad");
   }

   private void layoutComponents() {
      JPanel inputPanel = new JPanel(new GridBagLayout());
      inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(5, 5, 5, 5);
      gbc.anchor = GridBagConstraints.WEST;
      gbc.fill = GridBagConstraints.HORIZONTAL;

      gbc.gridx = 0;
      gbc.gridy = 0;
      inputPanel.add(new JLabel("Fecha de Entrada:"), gbc);
      gbc.gridx = 1;
      inputPanel.add(checkInDatePicker, gbc);

      gbc.gridx = 0;
      gbc.gridy = 1;
      inputPanel.add(new JLabel("Fecha de Salida:"), gbc);
      gbc.gridx = 1;
      inputPanel.add(checkOutDatePicker, gbc);

      gbc.gridx = 0;
      gbc.gridy = 2;
      inputPanel.add(new JLabel("Tipo de Habitación:"), gbc);
      gbc.gridx = 1;
      inputPanel.add(roomTypeComboBox, gbc);

      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      buttonPanel.add(checkAvailabilityButton);

      setLayout(new BorderLayout(10, 10));
      add(inputPanel, BorderLayout.NORTH);
      add(buttonPanel, BorderLayout.CENTER);
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
      Date selectedCheckInUtilDate = (Date) checkInDatePicker.getModel().getValue();
      Date selectedCheckOutUtilDate = (Date) checkOutDatePicker.getModel().getValue();
      RoomType selectedRoomType = (RoomType) roomTypeComboBox.getSelectedItem();

      if (selectedCheckInUtilDate == null || selectedCheckOutUtilDate == null) {
         JOptionPane.showMessageDialog(this, "Por favor, seleccione ambas fechas, de entrada y salida.",
               "Error de Validación", JOptionPane.ERROR_MESSAGE);
         return;
      }

      LocalDate checkInLocalDate = selectedCheckInUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
      LocalDate checkOutLocalDate = selectedCheckOutUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

      if (checkOutLocalDate.isBefore(checkInLocalDate) || checkOutLocalDate.isEqual(checkInLocalDate)) {
         JOptionPane.showMessageDialog(this, "La fecha de salida debe ser posterior a la fecha de entrada.",
               "Error de Validación", JOptionPane.ERROR_MESSAGE);
         return;
      }

      final JDialog verifyingDialog = new JDialog(this, "Procesando", false);
      JLabel label = new JLabel("Verificando disponibilidad...", SwingConstants.CENTER);
      label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      verifyingDialog.getContentPane().add(label);
      verifyingDialog.pack();
      verifyingDialog.setLocationRelativeTo(this);
      verifyingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
      verifyingDialog.setVisible(true);

      try {
         if (this.rabbitTemplate == null) {
            verifyingDialog.dispose();
            JOptionPane.showMessageDialog(this, "Error: Conexión con RabbitMQ no disponible.", "Error de Conexión",
                  JOptionPane.ERROR_MESSAGE);
            return;
         }
         AvailabilityRequestDTO requestDTO = new AvailabilityRequestDTO(
               checkInLocalDate.format(DTO_DATE_FORMATTER),
               checkOutLocalDate.format(DTO_DATE_FORMATTER),
               selectedRoomType.name());

         rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_DISPONIBILIDAD,
               RabbitMQConfig.ROUTING_KEY_VERIFICAR_DISPONIBILIDAD,
               requestDTO);

         verifyingDialog.dispose();

         System.out
               .println(
                     "Formulario UI: Datos enviados a RabbitMQ (Intercambio: " + RabbitMQConfig.EXCHANGE_DISPONIBILIDAD
                           + ", Clave de Enrutamiento: " + RabbitMQConfig.ROUTING_KEY_VERIFICAR_DISPONIBILIDAD + ") -> "
                           + requestDTO);

      } catch (Exception ex) {
         verifyingDialog.dispose();
         System.err.println(
               "AvailabilityCheckForm: Error al enviar DTO de solicitud inicial a RabbitMQ: " + ex.getMessage());
         JOptionPane.showMessageDialog(this, "Error al enviar solicitud de disponibilidad.",
               "Error", JOptionPane.ERROR_MESSAGE);
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
}