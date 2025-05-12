package com.ucentral.rabbitmq_app.ui;

import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import com.ucentral.rabbitmq_app.events.NewReservationConfirmedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

@Component
public class AdminDashboardForm extends JFrame {

   private JTable reservationTable;
   private DefaultTableModel tableModel;

   public AdminDashboardForm() {
      setTitle("Panel de Administración - Reservas");
      setSize(800, 400);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setLocationRelativeTo(null);

      initComponents();
      layoutComponents();
   }

   private void initComponents() {
      String[] columnNames = { "Nombre Huésped", "Identificacion", "Email Huésped", "Habitación No.", "Tipo Habitación",
            "Precio/Noche", "Check-In", "Check-Out" };

      tableModel = new DefaultTableModel(columnNames, 0) {
         @Override
         public boolean isCellEditable(int row, int column) {
            return false;
         }
      };

      reservationTable = new JTable(tableModel);
      reservationTable.setFillsViewportHeight(true);
      reservationTable.setAutoCreateRowSorter(true);
   }

   private void layoutComponents() {
      JTabbedPane tabbedPane = new JTabbedPane();
      JPanel reservationPanel = new JPanel(new BorderLayout());
      reservationPanel.add(new JScrollPane(reservationTable), BorderLayout.CENTER);
      tabbedPane.addTab("Reservas Confirmadas", null, reservationPanel, "Ver reservas confirmadas");

      add(tabbedPane, BorderLayout.CENTER);
   }

   @EventListener
   public void handleNewReservation(NewReservationConfirmedEvent event) {
      FinalBookingDetailsDTO details = event.getBookingDetails();
      System.out.println("AdminDashboardForm: Evento recibido para nueva reserva: " + details);

      SwingUtilities.invokeLater(() -> {
         Vector<Object> rowData = new Vector<>();
         rowData.add(details.getGuestName());
         rowData.add(details.getGuestId());
         rowData.add(details.getGuestEmail());
         rowData.add(details.getRoomNumber());
         rowData.add(details.getRoomType() != null ? details.getRoomType().name() : "N/A");
         rowData.add(details.getPricePerNight());
         rowData.add(details.getCheckInDate());
         rowData.add(details.getCheckOutDate());

         tableModel.addRow(rowData);
         System.out.println("AdminDashboardForm: Fila añadida a la tabla.");
      });
   }

   public void display() {
      SwingUtilities.invokeLater(() -> {
         setVisible(true);
      });
   }
}