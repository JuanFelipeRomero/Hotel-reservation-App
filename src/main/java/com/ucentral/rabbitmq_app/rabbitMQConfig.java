package com.ucentral.rabbitmq_app;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.FanoutExchange;
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

   // For Processed Reservation Details (Service -> New Queue)
   public static final String EXCHANGE_PROCESSED_DETAILS = "direct.reservaciones";
   public static final String QUEUE_PROCESSED_DETAILS = "cola_reservaciones";
   public static final String ROUTING_KEY_PROCESSED_DETAIL = "detalle_procesado_reserva";

   // For Confirmed Reservation Events (DB Registration Service -> Fanout to other
   // services)
   public static final String EXCHANGE_RESERVA_CONFIRMADA = "evento.reserva_confirmada";
   public static final String QUEUE_LIMPIEZA = "cola_limpieza";
   public static final String QUEUE_NOTIFICACIONES = "cola_notificaciones";

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

   // Beans for Processed Reservation Details Flow
   @Bean
   public DirectExchange processedDetailsExchange() {
      return new DirectExchange(EXCHANGE_PROCESSED_DETAILS);
   }

   @Bean
   public Queue processedDetailsQueue() {
      return new Queue(QUEUE_PROCESSED_DETAILS, true, false, false);
   }

   @Bean
   public Binding bindingProcessedDetails(Queue processedDetailsQueue,
         DirectExchange processedDetailsExchange) {
      return BindingBuilder.bind(processedDetailsQueue)
            .to(processedDetailsExchange)
            .with(ROUTING_KEY_PROCESSED_DETAIL);
   }

   // Beans for Confirmed Reservation Event Flow
   @Bean
   public FanoutExchange reservaConfirmadaExchange() {
      return new FanoutExchange(EXCHANGE_RESERVA_CONFIRMADA);
   }

   @Bean
   public Queue limpiezaQueue() {
      return new Queue(QUEUE_LIMPIEZA, true, false, false);
   }

   @Bean
   public Queue notificacionesQueue() {
      return new Queue(QUEUE_NOTIFICACIONES, true, false, false);
   }

   @Bean
   public Binding bindingLimpiezaToConfirmada(Queue limpiezaQueue, FanoutExchange reservaConfirmadaExchange) {
      return BindingBuilder.bind(limpiezaQueue).to(reservaConfirmadaExchange);
   }

   @Bean
   public Binding bindingNotificacionesToConfirmada(Queue notificacionesQueue,
         FanoutExchange reservaConfirmadaExchange) {
      return BindingBuilder.bind(notificacionesQueue).to(reservaConfirmadaExchange);
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
