package com.ucentral.rabbitmq_app.ui;

import com.ucentral.rabbitmq_app.dto.CleaningTaskUIDTO;
import com.ucentral.rabbitmq_app.services.CleaningService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;
import javax.swing.ListSelectionModel;

public class CleaningServiceUI extends JFrame {

   private final CleaningService cleaningService;
   private JButton markCleanedButton;

   private DefaultTableModel pendingTableModel;
   private DefaultTableModel cleanedTableModel;
   private JTable pendingTasksTable;
   private JTable cleanedTasksTable;

   public CleaningServiceUI(CleaningService cleaningService) {
      this.cleaningService = cleaningService;
      setTitle("Servicio de Limpieza - Tareas");
      setSize(800, 500);
      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
      setLocationByPlatform(true);

      initComponents();
      layoutComponents();
      addEventListeners();
      loadInitialData();
   }

   private void initComponents() {
      String[] columnNames = { "ID Reserva/Tarea", "Habitación No.", "Fecha Limpieza" };

      pendingTableModel = new DefaultTableModel(columnNames, 0) {
         @Override
         public boolean isCellEditable(int row, int column) {
            return false;
         }
      };
      pendingTasksTable = new JTable(pendingTableModel);
      pendingTasksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      pendingTasksTable.setAutoCreateRowSorter(true);

      cleanedTableModel = new DefaultTableModel(columnNames, 0) {
         @Override
         public boolean isCellEditable(int row, int column) {
            return false;
         }
      };
      cleanedTasksTable = new JTable(cleanedTableModel);
      cleanedTasksTable.setAutoCreateRowSorter(true);
      cleanedTasksTable.setRowSelectionAllowed(false);
      cleanedTasksTable.setFocusable(false);

      markCleanedButton = new JButton("Marcar como Limpiada");
   }

   private void layoutComponents() {
      JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
      mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      JScrollPane pendingScrollPane = new JScrollPane(pendingTasksTable);
      pendingScrollPane.setBorder(BorderFactory.createTitledBorder("Tareas Pendientes"));

      JScrollPane cleanedScrollPane = new JScrollPane(cleanedTasksTable);
      cleanedScrollPane.setBorder(BorderFactory.createTitledBorder("Tareas Completadas"));

      JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pendingScrollPane, cleanedScrollPane);
      splitPane.setDividerLocation(200);
      splitPane.setResizeWeight(0.5);

      mainPanel.add(splitPane, BorderLayout.CENTER);

      JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      actionPanel.add(markCleanedButton);
      mainPanel.add(actionPanel, BorderLayout.SOUTH);

      add(mainPanel);
   }

   private void addEventListeners() {
      markCleanedButton.addActionListener(e -> {
         int selectedRow = pendingTasksTable.getSelectedRow();
         if (selectedRow >= 0) {
            int modelRow = pendingTasksTable.convertRowIndexToModel(selectedRow);
            Long taskId = (Long) pendingTableModel.getValueAt(modelRow, 0);
            if (taskId != null) {
               System.out.println("UI: Marking task ID " + taskId + " as cleaned.");
               cleaningService.markTaskAsCleaned(taskId);
            }
         } else {
            JOptionPane.showMessageDialog(this,
                  "Por favor, seleccione una tarea pendiente para marcarla como limpiada.",
                  "Selección Requerida", JOptionPane.WARNING_MESSAGE);
         }
      });
   }

   private void loadInitialData() {
      pendingTableModel.setRowCount(0);
      cleanedTableModel.setRowCount(0);

      List<CleaningTaskUIDTO> pending = cleaningService.getPendingCleaningTasks();
      System.out.println("UI: Loading " + pending.size() + " pending tasks.");
      for (CleaningTaskUIDTO task : pending) {
         addTaskToTableModel(pendingTableModel, task);
      }

      List<CleaningTaskUIDTO> cleaned = cleaningService.getCleanedTasks();
      System.out.println("UI: Loading " + cleaned.size() + " cleaned tasks.");
      for (CleaningTaskUIDTO task : cleaned) {
         addTaskToTableModel(cleanedTableModel, task);
      }
   }

   private void addTaskToTableModel(DefaultTableModel model, CleaningTaskUIDTO task) {
      Vector<Object> rowData = new Vector<>();
      rowData.add(task.getReservationId());
      rowData.add(task.getRoomNumber());
      rowData.add(task.getCheckOutDate() != null ? task.getCheckOutDate().toString() : "N/A");
      model.addRow(rowData);
   }

   public void handleNewTask(CleaningTaskUIDTO task) {
      SwingUtilities.invokeLater(() -> {
         System.out.println("UI: Received new task event: " + task);
         addTaskToTableModel(pendingTableModel, task);
      });
   }

   public void handleTaskUpdated(CleaningTaskUIDTO task) {
      SwingUtilities.invokeLater(() -> {
         System.out.println("UI: Received task updated event: " + task);
         if ("CLEANED".equals(task.getStatus())) {
            for (int i = 0; i < pendingTableModel.getRowCount(); i++) {
               Long rowId = (Long) pendingTableModel.getValueAt(i, 0);
               if (task.getReservationId().equals(rowId)) {
                  pendingTableModel.removeRow(i);
                  System.out.println("UI: Removed task ID " + task.getReservationId() + " from pending table.");
                  break;
               }
            }
            addTaskToTableModel(cleanedTableModel, task);
            System.out.println("UI: Added task ID " + task.getReservationId() + " to cleaned table.");
         }
      });
   }
}