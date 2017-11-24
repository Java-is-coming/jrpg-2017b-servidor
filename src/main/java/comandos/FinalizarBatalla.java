package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteDeNPCs;
import mensajeria.PaqueteFinalizarBatalla;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando que se utilizar para terminar una batalla.
 *
 */
public class FinalizarBatalla extends ComandosServer {

	/**
	 * Ejecución del comando.
	 */
	@Override
	public void ejecutar() {
		try {

			final PaqueteFinalizarBatalla paqueteFinalizarBatalla = gson.fromJson(cadenaLeida,
					PaqueteFinalizarBatalla.class);

			getEscuchaCliente().setPaqueteFinalizarBatalla(paqueteFinalizarBatalla);

			if (paqueteFinalizarBatalla.getTipoBatalla() == PaqueteBatalla.BATALLAR_PERSONAJE) {

				Servidor.getConector().actualizarInventario(paqueteFinalizarBatalla.getGanadorBatalla());

				Servidor.getPersonajesConectados().get(paqueteFinalizarBatalla.getIdEnemigo())
						.setEstado(Estado.ESTADO_JUEGO);
			} else {

				// Si gana el NPC, lo volvemos a estado juego
				if (paqueteFinalizarBatalla.getGanadorBatalla() == paqueteFinalizarBatalla.getIdEnemigo()) {
					Servidor.getNPsCreados().get(paqueteFinalizarBatalla.getIdEnemigo()).setEstado(Estado.ESTADO_JUEGO);
				} else {
					Servidor.getNpcsARespawnear().put(paqueteFinalizarBatalla.getIdEnemigo(),
							Servidor.getNPsCreados().get(paqueteFinalizarBatalla.getIdEnemigo()));
					Servidor.getNPsCreados().remove(paqueteFinalizarBatalla.getIdEnemigo());
				}
			}

			Servidor.getPersonajesConectados().get(getEscuchaCliente().getPaqueteFinalizarBatalla().getId())
					.setEstado(Estado.ESTADO_JUEGO);

			for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {

				if (paqueteFinalizarBatalla.getTipoBatalla() == PaqueteBatalla.BATALLAR_PERSONAJE) {
					if (conectado.getIdPersonaje() == getEscuchaCliente().getPaqueteFinalizarBatalla().getIdEnemigo()) {
						try {
							final String paquete = gson.toJson(getEscuchaCliente().getPaqueteFinalizarBatalla());
							conectado.getSalida().writeObject(paquete);
						} catch (final IOException e) {

							Servidor.getLog().append("Falló al intentar enviar finalizarBatalla a:"
									+ conectado.getPaquetePersonaje().getId() + "\n");
						}
					}
				} else {

					final PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
					pdn.setComando(Comando.ACTUALIZARNPCS);
					try {
						conectado.getSalida().writeObject(gson.toJson(pdn));
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						Servidor.getLog().append("Falló al intentar enviar los npcs actualizados."
								+ conectado.getPaquetePersonaje().getId() + "\n");
					}
				}

			}

		} catch (final Exception e) {
			Servidor.getLog().append("Falló al intentar enviar finalizarBatalla ");
		}

		synchronized (Servidor.getAtencionConexiones()) {
			Servidor.getAtencionConexiones().notify();
		}

	}

}
