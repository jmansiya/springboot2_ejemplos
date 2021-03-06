package com.jmansilla.management;

import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.notify.MailNotifier;
import reactor.core.publisher.Mono;

@Configuration
public class MailNotifierCustom extends MailNotifier{
	
	@Value("${franja.horaria.notificaciones.permitidas:0-23}")
	private String horarioNotificacionesPermitidas;
	
	private final int POSICION_HORA_INICIO_PERMISO_NOTIFICACIONES = 0;
	
	private final int POSICION_HORA_FIN_PERMISO_NOTIFICACIONES = 1;

	public MailNotifierCustom(JavaMailSender mailSender, InstanceRepository repository, TemplateEngine templateEngine) {
		super(mailSender, repository, templateEngine);
		// TODO Auto-generated constructor stub
	}

	 protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
		if (horarioNotificacionesPermitidas()) {
			System.out.println("Ejecutamos notificación la notificación.");
			return super.doNotify(event, instance);
		} else {
			 System.out.println("fuera de hora no lanzamos la notificación");
		}
		 
		return Mono.empty();
	 }
	 
	 private boolean horarioNotificacionesPermitidas() {
		 boolean resultado = true;
		 
		 try{
			 String[] rangoHorario = getHoraInicioAndHoraFin();
			 
			 LocalTime hora = LocalTime.now();
			 
   			 return ((hora.getHour() < Integer.parseInt(rangoHorario[POSICION_HORA_FIN_PERMISO_NOTIFICACIONES]))
   					 && (hora.getHour() >= Integer.parseInt(rangoHorario[POSICION_HORA_INICIO_PERMISO_NOTIFICACIONES])));
		 } catch (Exception e) {
			System.out.println("Excepcion al obtener el rango de horas permitido para la generación de notificaciones.");
			e.printStackTrace();
		 }
	
		 return resultado;
	 }
	 
	 private String[] getHoraInicioAndHoraFin() {
		 System.out.println("Franja horaria establecia: " + horarioNotificacionesPermitidas);
		 return horarioNotificacionesPermitidas.split("-");
	 }
	  		
}

	 