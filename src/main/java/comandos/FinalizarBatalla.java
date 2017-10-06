package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteDeNPCs;
import mensajeria.PaqueteFinalizarBatalla;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class FinalizarBatalla extends ComandosServer {

	@Override
	public void ejecutar() {
		
		PaqueteFinalizarBatalla paqueteFinalizarBatalla = (PaqueteFinalizarBatalla) gson.fromJson(cadenaLeida, PaqueteFinalizarBatalla.class);
		
		escuchaCliente.setPaqueteFinalizarBatalla(paqueteFinalizarBatalla);
		
		if (paqueteFinalizarBatalla.getTipoBatalla() == PaqueteBatalla.batallarPersonaje) {
			Servidor.getConector().actualizarInventario(paqueteFinalizarBatalla.getGanadorBatalla());
			Servidor.getPersonajesConectados().get(paqueteFinalizarBatalla.getIdEnemigo()).setEstado(Estado.estadoJuego);
		} else {
			
			//Si gana el NPC, lo volvemos a estado juego
			if (paqueteFinalizarBatalla.getGanadorBatalla() == paqueteFinalizarBatalla.getIdEnemigo())
				Servidor.getNPsCreados().get(paqueteFinalizarBatalla.getIdEnemigo()).setEstado(Estado.estadoJuego);
			else //Gana el usuario, volamos al NPC
				Servidor.getNPsCreados().remove(paqueteFinalizarBatalla.getIdEnemigo());
		}
			
		
		Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteFinalizarBatalla().getId()).setEstado(Estado.estadoJuego);
		
		
		
		for(EscuchaCliente conectado : Servidor.getClientesConectados()) {
			
			if (paqueteFinalizarBatalla.getTipoBatalla() == PaqueteBatalla.batallarPersonaje) {
				if(conectado.getIdPersonaje() == escuchaCliente.getPaqueteFinalizarBatalla().getIdEnemigo()) {
					try {
						conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteFinalizarBatalla()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Servidor.log.append("Falló al intentar enviar finalizarBatalla a:" + conectado.getPaquetePersonaje().getId() + "\n");
					}
				}
			} else {
				
				PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
				pdn.setComando(Comando.ACTUALIZARNPCS);				
				try {
					conectado.getSalida().writeObject(gson.toJson(pdn));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Servidor.log.append("Falló al intentar enviar los npcs actualizados." + conectado.getPaquetePersonaje().getId() + "\n");
				}
			}
			
		}
		
		
		synchronized(Servidor.atencionConexiones){
			Servidor.atencionConexiones.notify();
		}

	}

}
