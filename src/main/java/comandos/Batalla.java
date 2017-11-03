package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteDeNPCs;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando de batalla
 *
 */
public class Batalla extends ComandosServer {
	/**
	 * Ejecucion del comando
	 */
	@Override
	public void ejecutar() {
		// Le reenvio al id del personaje batallado que quieren pelear
		getEscuchaCliente().setPaqueteBatalla(gson.fromJson(cadenaLeida, PaqueteBatalla.class));

		Servidor.getLog().append(getEscuchaCliente().getPaqueteBatalla().getId() + " quiere batallar con "
				+ getEscuchaCliente().getPaqueteBatalla().getIdEnemigo() + System.lineSeparator());
		try {

			// seteo estado de batalla
			Servidor.getPersonajesConectados().get(getEscuchaCliente().getPaqueteBatalla().getId())
					.setEstado(Estado.ESTADO_BATALLA);

			getEscuchaCliente().getPaqueteBatalla().setMiTurno(true);
			getEscuchaCliente().getSalida().writeObject(gson.toJson(getEscuchaCliente().getPaqueteBatalla()));

			if (getEscuchaCliente().getPaqueteBatalla().getTipoBatalla() == PaqueteBatalla.BATALLAR_PERSONAJE) {
				Servidor.getPersonajesConectados().get(getEscuchaCliente().getPaqueteBatalla().getIdEnemigo())
						.setEstado(Estado.ESTADO_BATALLA);

			} else {

				Servidor.getNPsCreados().get(getEscuchaCliente().getPaqueteBatalla().getIdEnemigo())
						.setEstado(Estado.ESTADO_BATALLA);
			}

			for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {

				if (getEscuchaCliente().getPaqueteBatalla().getTipoBatalla() == PaqueteBatalla.BATALLAR_PERSONAJE) {
					if (conectado.getIdPersonaje() == getEscuchaCliente().getPaqueteBatalla().getIdEnemigo()) {
						final int aux = getEscuchaCliente().getPaqueteBatalla().getId();
						final int idEnemigo = getEscuchaCliente().getPaqueteBatalla().getIdEnemigo();
						getEscuchaCliente().getPaqueteBatalla().setId(idEnemigo);
						getEscuchaCliente().getPaqueteBatalla().setIdEnemigo(aux);
						getEscuchaCliente().getPaqueteBatalla().setMiTurno(false);
						conectado.getSalida().writeObject(gson.toJson(getEscuchaCliente().getPaqueteBatalla()));
						break;
					}
				} else {

					final PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
					pdn.setComando(Comando.ACTUALIZARNPCS);
					conectado.getSalida().writeObject(gson.toJson(pdn));

				}
			}

		} catch (final IOException e) {
			Servidor.getLog().append("Falló al intentar enviar Batalla \n");
		}

		synchronized (Servidor.getAtencionConexiones()) {
			Servidor.getAtencionConexiones().notify();
		}
	}
}
