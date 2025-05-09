package com.ucentral.rabbitmq_app.events;

import com.ucentral.rabbitmq_app.dto.CleaningTaskUIDTO;
import org.springframework.context.ApplicationEvent;

public class CleaningTaskUpdatedEvent extends ApplicationEvent {

   private final CleaningTaskUIDTO task;

   public CleaningTaskUpdatedEvent(Object source, CleaningTaskUIDTO task) {
      super(source);
      this.task = task;
   }

   public CleaningTaskUIDTO getTask() {
      return task;
   }
}