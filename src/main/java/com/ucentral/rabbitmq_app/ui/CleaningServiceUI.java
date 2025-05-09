package com.ucentral.rabbitmq_app.ui;

import com.ucentral.rabbitmq_app.dto.CleaningTaskUIDTO;
import com.ucentral.rabbitmq_app.services.CleaningService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CleaningServiceUI extends JFrame {

   private final CleaningService cleaningService;
   private JPanel pendingTasksPanel;
   private JPanel cleanedTasksPanel;

   public CleaningServiceUI(CleaningService cleaningService) {
      this.cleaningService = cleaningService;
      setTitle("Servicio de Limpieza del Hotel");
      setSize(800, 600);
      setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
      setLocationRelativeTo(null);
      initComponents();
      layoutComponents();
      refreshAllTaskDisplays();
   }

   private void initComponents() {
      pendingTasksPanel = new JPanel();
      pendingTasksPanel.setLayout(new BoxLayout(pendingTasksPanel, BoxLayout.Y_AXIS));
      JScrollPane pendingScrollPane = new JScrollPane(pendingTasksPanel);
      pendingScrollPane.setPreferredSize(new Dimension(350, 500));

      cleanedTasksPanel = new JPanel();
      cleanedTasksPanel.setLayout(new BoxLayout(cleanedTasksPanel, BoxLayout.Y_AXIS));
      JScrollPane cleanedScrollPane = new JScrollPane(cleanedTasksPanel);
      cleanedScrollPane.setPreferredSize(new Dimension(350, 500));
   }

   private void layoutComponents() {
      setLayout(new BorderLayout(10, 10));
      JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0));

      JScrollPane pendingScrollPaneForLayout = new JScrollPane(pendingTasksPanel);
      pendingScrollPaneForLayout.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      pendingScrollPaneForLayout.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      JPanel leftPanel = new JPanel(new BorderLayout());
      leftPanel.setBorder(BorderFactory.createTitledBorder("Habitaciones pendientes por limpieza"));
      leftPanel.add(pendingScrollPaneForLayout, BorderLayout.CENTER);

      JScrollPane cleanedScrollPaneForLayout = new JScrollPane(cleanedTasksPanel);
      cleanedScrollPaneForLayout.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      cleanedScrollPaneForLayout.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      JPanel rightPanel = new JPanel(new BorderLayout());
      rightPanel.setBorder(BorderFactory.createTitledBorder("Habitaciones Limpiadas"));
      rightPanel.add(cleanedScrollPaneForLayout, BorderLayout.CENTER);

      mainPanel.add(leftPanel);
      mainPanel.add(rightPanel);
      add(mainPanel, BorderLayout.CENTER);
   }

   public void handleNewTask(CleaningTaskUIDTO task) {
      SwingUtilities.invokeLater(() -> {
         addPendingTaskToDisplay(task);
         pendingTasksPanel.revalidate();
         pendingTasksPanel.repaint();
      });
   }

   public void handleTaskUpdated(CleaningTaskUIDTO task) {
      SwingUtilities.invokeLater(() -> {
         if ("CLEANED".equals(task.getStatus())) {
            refreshPendingTasksDisplay();
            refreshCleanedTasksDisplay();
         } else {
            refreshAllTaskDisplays();
         }
      });
   }

   public void refreshAllTaskDisplays() {
      SwingUtilities.invokeLater(() -> {
         refreshPendingTasksDisplay();
         refreshCleanedTasksDisplay();
      });
   }

   private void refreshPendingTasksDisplay() {
      pendingTasksPanel.removeAll();
      List<CleaningTaskUIDTO> pending = cleaningService.getPendingCleaningTasks();
      if (pending.isEmpty()) {
         pendingTasksPanel.add(new JLabel(" No hay tareas pendientes."));
      }
      for (CleaningTaskUIDTO task : pending) {
         addPendingTaskToDisplay(task);
      }
      pendingTasksPanel.revalidate();
      pendingTasksPanel.repaint();
   }

   private void refreshCleanedTasksDisplay() {
      cleanedTasksPanel.removeAll();
      List<CleaningTaskUIDTO> cleaned = cleaningService.getCleanedTasks();
      if (cleaned.isEmpty()) {
         cleanedTasksPanel.add(new JLabel(" Aún no hay habitaciones limpiadas."));
      }
      for (CleaningTaskUIDTO task : cleaned) {
         addCleanedTaskToDisplay(task);
      }
      cleanedTasksPanel.revalidate();
      cleanedTasksPanel.repaint();
   }

   private void addPendingTaskToDisplay(CleaningTaskUIDTO task) {
      JPanel taskPanel = new JPanel(new BorderLayout(10, 0));
      taskPanel.setBorder(BorderFactory.createEtchedBorder());
      taskPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
      taskPanel.setName("task-" + task.getReservationId());

      JTextArea taskText = new JTextArea(String.format("Habitación: %s\nSalida: %s\n(ID Res: %d)", task.getRoomNumber(),
            task.getCheckOutDate(), task.getReservationId()));
      taskText.setEditable(false);
      taskText.setOpaque(false);
      taskText.setLineWrap(true);
      taskText.setWrapStyleWord(true);

      JButton cleanedButton = new JButton("Marcar como Limpiada");
      cleanedButton.addActionListener(e -> cleaningService.markTaskAsCleaned(task.getReservationId()));

      taskPanel.add(taskText, BorderLayout.CENTER);
      taskPanel.add(cleanedButton, BorderLayout.EAST);
      pendingTasksPanel.add(taskPanel);
      pendingTasksPanel.add(Box.createRigidArea(new Dimension(0, 5)));
   }

   private void addCleanedTaskToDisplay(CleaningTaskUIDTO task) {
      JPanel taskPanel = new JPanel(new BorderLayout(10, 0));
      taskPanel.setBorder(BorderFactory.createEtchedBorder());
      taskPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
      taskPanel.setName("task-" + task.getReservationId());

      JTextArea cleanedText = new JTextArea(
            String.format("Habitación: %s\nSalida: %s\nEstado: LIMPIADA", task.getRoomNumber(),
                  task.getCheckOutDate()));
      cleanedText.setEditable(false);
      cleanedText.setOpaque(false);
      cleanedText.setLineWrap(true);
      cleanedText.setWrapStyleWord(true);
      taskPanel.add(cleanedText, BorderLayout.CENTER);
      cleanedTasksPanel.add(taskPanel);
      cleanedTasksPanel.add(Box.createRigidArea(new Dimension(0, 5)));
   }
}