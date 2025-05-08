package com.ucentral.rabbitmq_app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

   // Exchange for availability requests
   public static final String EXCHANGE_DISPONIBILIDAD = "direct.disponibilidad";

   // Queue that AvailabilityService listens to
   public static final String QUEUE_RESERVACIONES_PARA_DISPONIBILIDAD = "cola_disponibilidad";

   // Routing key from form to this queue via the exchange
   public static final String ROUTING_KEY_VERIFICAR_DISPONIBILIDAD = "verificar_disponibilidad";

   @Bean
   public DirectExchange disponibilidadExchange() {
      return new DirectExchange(EXCHANGE_DISPONIBILIDAD);
   }

   @Bean
   public Queue colaReservacionesParaDisponibilidad() {

      return new Queue(QUEUE_RESERVACIONES_PARA_DISPONIBILIDAD, true, false, false);
   }

   @Bean
   public Binding bindingDisponibilidadToColaReservaciones(Queue colaReservacionesParaDisponibilidad,
         DirectExchange disponibilidadExchange) {
      return BindingBuilder.bind(colaReservacionesParaDisponibilidad) // Source is the queue bean
            .to(disponibilidadExchange) // Target is the exchange bean
            .with(ROUTING_KEY_VERIFICAR_DISPONIBILIDAD); // Routing key
   }

   // Define the MessageConverter Bean that RabbitTemplate will use
   @Bean
   public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
      return new Jackson2JsonMessageConverter(objectMapper);
   }

   // RabbitTemplate will automatically use the MessageConverter bean defined
   // above.
   @Bean
   public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory,
         final MessageConverter messageConverter) {
      final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
      rabbitTemplate.setMessageConverter(messageConverter);
      return rabbitTemplate;
   }

   // Define your Queues, Exchanges, and Bindings as beans here.
   // For example:
   /*
    * public static final String MY_QUEUE_NAME = "myExampleQueue";
    * 
    * @Bean
    * public Queue myQueue() {
    * // durable: true, exclusive: false, autoDelete: false
    * return new Queue(MY_QUEUE_NAME, true, false, false);
    * }
    */

   // You would also define Exchanges and Bindings if you're using them.
}
