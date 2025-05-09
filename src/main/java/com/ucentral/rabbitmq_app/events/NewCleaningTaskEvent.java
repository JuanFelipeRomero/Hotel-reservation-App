package com.ucentral.rabbitmq_app.events;

import com.ucentral.rabbitmq_app.dto.CleaningTaskUIDTO;
import org.springframework.context.ApplicationEvent;

public class NewCleaningTaskEvent extends ApplicationEvent {
   private final CleaningTaskUIDTO task;

   public NewCleaningTaskEvent(Object source, CleaningTaskUIDTO task) {
      super(source);
      this.task = task;
   }

   public CleaningTaskUIDTO getTask() {
      return task;
   }
}