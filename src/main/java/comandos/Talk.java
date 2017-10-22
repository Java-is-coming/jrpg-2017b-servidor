package comandos;

import java.io.IOException;
import java.util.Map;

import mensajeria.Comando;
import mensajeria.PaqueteMensaje;
import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Talk extends ComandosServer {

	@Override
	public void ejecutar() {
		int idUser = 0;
		PaqueteMensaje paqueteMensaje = (PaqueteMensaje) (gson.fromJson(cadenaLeida, PaqueteMensaje.class));
		
		if (paqueteMensaje.getUserReceptor() != null) {
			if (Servidor.mensajeAUsuario(paqueteMensaje)) {
				
				paqueteMensaje.setComando(Comando.TALK);
				
				for (Map.Entry<Integer, PaquetePersonaje> personaje : Servidor.getPersonajesConectados().entrySet()) {
					if(personaje.getValue().getNombre().equals(paqueteMensaje.getUserReceptor())) {
						idUser = personaje.getValue().getId();
						break;
					}
				}
				
				for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
					if(conectado.getIdPersonaje() == idUser) {
						try {
							conectado.getSalida().writeObject(gson.toJson(paqueteMensaje));
						} catch (IOException e) {
							Servidor.log.append("Falló al intentar enviar mensaje a:" + conectado.getPaquetePersonaje().getId() + "\n");
						}
					}
				}			
			} else {
				Servidor.log.append("No se envió el mensaje \n");
			}
		} else {				
			for (Map.Entry<Integer, PaquetePersonaje> personaje : Servidor.getPersonajesConectados().entrySet()) {
				if(personaje.getValue().getNombre().equals(paqueteMensaje.getUserEmisor())) {
					idUser = personaje.getValue().getId();
					break;
				}
			}
			
			int contador = 1;
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				if(conectado.getIdPersonaje() != idUser) {
					try {
						conectado.getSalida().writeObject(gson.toJson(paqueteMensaje));	
						
						contador++;					
					} catch (IOException e) {
						Servidor.log.append("Falló al intentar enviar mensaje a:" + conectado.getPaquetePersonaje().getId() + "\n");
					}
				}
			}		
			Servidor.mensajeAAll(paqueteMensaje, contador);	
		}
	}
}
