package com.ucentral.rabbitmq_app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

   // Intercambio para solicitudes de disponibilidad
   public static final String EXCHANGE_DISPONIBILIDAD = "direct.disponibilidad";

   public static final String QUEUE_RESERVACIONES_PARA_DISPONIBILIDAD = "cola_disponibilidad";

   public static final String ROUTING_KEY_VERIFICAR_DISPONIBILIDAD = "verificar_disponibilidad";

   // Para Detalles de Reserva Procesados
   public static final String EXCHANGE_PROCESSED_DETAILS = "direct.reservaciones";
   public static final String QUEUE_PROCESSED_DETAILS = "cola_reservaciones";
   public static final String ROUTING_KEY_PROCESSED_DETAIL = "detalle_procesado_reserva";

   // Para Eventos de Reserva Confirmada
   public static final String EXCHANGE_RESERVA_CONFIRMADA = "evento.reserva_confirmada";
   public static final String QUEUE_LIMPIEZA = "cola_limpieza";
   public static final String QUEUE_NOTIFICACIONES = "cola_notificaciones";

   // Confirmed Reservation Fanout Exchange and Admin Queue (NEW)
   public static final String QUEUE_ADMIN = "cola_admin";

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
      return BindingBuilder.bind(colaReservacionesParaDisponibilidad)
            .to(disponibilidadExchange)
            .with(ROUTING_KEY_VERIFICAR_DISPONIBILIDAD);
   }

   // Flujo de Detalles de Reserva Procesados
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

   // Flujo de Eventos de Reserva Confirmada
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
   // Confirmed Reservation Fanout Exchange and Admin Queue (NEW)
   @Bean
   public Queue adminQueue() {
      return new Queue(QUEUE_ADMIN, true, false, false);
   }

   @Bean
   public Binding adminBinding(Queue adminQueue, FanoutExchange reservaConfirmadaExchange) {
      return BindingBuilder.bind(adminQueue).to(reservaConfirmadaExchange);
   }

   @Bean
   public MessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
      return new Jackson2JsonMessageConverter(objectMapper);
   }

   @Bean
   public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory,
         final MessageConverter messageConverter) {
      final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
      rabbitTemplate.setMessageConverter(messageConverter);
      return rabbitTemplate;
   }

   @Bean
   public ObjectMapper objectMapper() {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      return mapper;
   }

}
