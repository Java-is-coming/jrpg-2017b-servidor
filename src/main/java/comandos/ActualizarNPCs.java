package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteDeMovimientos;
import mensajeria.PaqueteDeNPCs;
import mensajeria.PaqueteNPC;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class ActualizarNPCs extends ComandosServer {

	@Override
	public void ejecutar() {
		escuchaCliente.setPaqueteNPC((PaqueteNPC) gson.fromJson(cadenaLeida, PaqueteNPC.class));
		
		
		//Voy a mandarle todos los NPC's a los usuarios logueados en estado juego
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			
			if(conectado.getPaquetePersonaje().getEstado() == Estado.estadoJuego) {
				
				try {
					
					PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
					pdn.setComando(Comando.ACTUALIZARNPCS);
					synchronized (conectado) {
						conectado.getSalida().writeObject(gson.toJson(pdn));									
					}
					
				} catch (IOException e) {
					Servidor.log.append("Falló al intentar enviar ataque a:" + conectado.getPaquetePersonaje().getId() + "\n");
				}
			}
		}
		
	}

}
