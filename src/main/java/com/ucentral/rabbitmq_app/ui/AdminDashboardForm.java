package com.ucentral.rabbitmq_app.ui;

import com.ucentral.rabbitmq_app.dto.FinalBookingDetailsDTO;
import com.ucentral.rabbitmq_app.events.NewReservationConfirmedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

@Component // Make this a Spring component so @EventListener works
public class AdminDashboardForm extends JFrame {

   private JTable reservationTable;
   private DefaultTableModel tableModel;

   public AdminDashboardForm() {
      setTitle("Admin Dashboard - Reservas Confirmadas");
      setSize(800, 400);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Don't exit app when admin closes
      setLocationRelativeTo(null); // Center on screen

      initComponents();
      layoutComponents();
   }

   private void initComponents() {
      // Define table columns
      String[] columnNames = { "Guest Name", "Guest ID", "Guest Email", "Room No.", "Room Type", "Price/Night",
            "Check-In", "Check-Out" };

      // Create a non-editable table model
      tableModel = new DefaultTableModel(columnNames, 0) {
         @Override
         public boolean isCellEditable(int row, int column) {
            return false; // Make table read-only
         }
      };

      reservationTable = new JTable(tableModel);
      reservationTable.setFillsViewportHeight(true);
      reservationTable.setAutoCreateRowSorter(true); // Enable sorting
   }

   private void layoutComponents() {
      // Use JTabbedPane as requested for future expansion
      JTabbedPane tabbedPane = new JTabbedPane();

      // Create panel for the reservations table
      JPanel reservationPanel = new JPanel(new BorderLayout());
      reservationPanel.add(new JScrollPane(reservationTable), BorderLayout.CENTER);

      // Add the reservation panel as the first tab
      tabbedPane.addTab("Reservas Confirmadas", null, reservationPanel,
            "Ver reservas confirmadas");

      // Add the tabbed pane to the frame
      add(tabbedPane, BorderLayout.CENTER);
   }

   // Listener for Spring Application Events
   @EventListener
   public void handleNewReservation(NewReservationConfirmedEvent event) {
      FinalBookingDetailsDTO details = event.getBookingDetails();
      System.out.println("AdminDashboardForm: Received event for new reservation: " + details);

      // Ensure UI updates happen on the Swing Event Dispatch Thread
      SwingUtilities.invokeLater(() -> {
         Vector<Object> rowData = new Vector<>();
         rowData.add(details.getGuestName());
         rowData.add(details.getGuestId());
         rowData.add(details.getGuestEmail());
         rowData.add(details.getRoomNumber());
         rowData.add(details.getRoomType() != null ? details.getRoomType().name() : "N/A"); // Handle potential null
         rowData.add(details.getPricePerNight());
         rowData.add(details.getCheckInDate());
         rowData.add(details.getCheckOutDate());

         tableModel.addRow(rowData);
         System.out.println("AdminDashboardForm: Added row to table.");
      });
   }

   // Optional: Method to make the frame visible, called from Application runner
   public void display() {
      // Ensure visibility toggling happens on the EDT
      SwingUtilities.invokeLater(() -> setVisible(true));
   }
}