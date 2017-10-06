package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteDeNPCs;
import servidor.EscuchaCliente;
import servidor.Servidor;

public class Batalla extends ComandosServer {

	@Override
	public void ejecutar() {
		// Le reenvio al id del personaje batallado que quieren pelear
		escuchaCliente.setPaqueteBatalla((PaqueteBatalla) gson.fromJson(cadenaLeida, PaqueteBatalla.class));

		Servidor.log.append(escuchaCliente.getPaqueteBatalla().getId() + " quiere batallar con "
				+ escuchaCliente.getPaqueteBatalla().getIdEnemigo() + System.lineSeparator());
		try {

			// seteo estado de batalla
			Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteBatalla().getId())
					.setEstado(Estado.estadoBatalla);

			escuchaCliente.getPaqueteBatalla().setMiTurno(true);
			escuchaCliente.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteBatalla()));
			
			if (escuchaCliente.getPaqueteBatalla().getTipoBatalla() == PaqueteBatalla.batallarPersonaje)
			{
				Servidor.getPersonajesConectados().get(escuchaCliente.getPaqueteBatalla().getIdEnemigo())
				.setEstado(Estado.estadoBatalla);
			} else {
				Servidor.getNPsCreados().get(escuchaCliente.getPaqueteBatalla().getIdEnemigo())
				.setEstado(Estado.estadoBatalla);
			}
			
			
			
			for (EscuchaCliente conectado : Servidor.getClientesConectados()) {
				
				if (escuchaCliente.getPaqueteBatalla().getTipoBatalla() == PaqueteBatalla.batallarPersonaje)
				{
					if (conectado.getIdPersonaje() == escuchaCliente.getPaqueteBatalla().getIdEnemigo()) {
						int aux = escuchaCliente.getPaqueteBatalla().getId();
						escuchaCliente.getPaqueteBatalla().setId(escuchaCliente.getPaqueteBatalla().getIdEnemigo());
						escuchaCliente.getPaqueteBatalla().setIdEnemigo(aux);
						escuchaCliente.getPaqueteBatalla().setMiTurno(false);
						conectado.getSalida().writeObject(gson.toJson(escuchaCliente.getPaqueteBatalla()));
						break;
					}
				} else {
				
					PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
					pdn.setComando(Comando.ACTUALIZARNPCS);				
					conectado.getSalida().writeObject(gson.toJson(pdn));
					
				}
			}
			
		} catch (IOException e) {
			Servidor.log.append("Falló al intentar enviar Batalla \n");
		}

		synchronized (Servidor.atencionConexiones) {
			Servidor.atencionConexiones.notify();
		}

	}

}
