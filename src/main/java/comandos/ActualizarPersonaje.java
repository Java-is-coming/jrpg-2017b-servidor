package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando de actualización de personaje
 *
 */
public class ActualizarPersonaje extends ComandosServer {

    /**
     * Ejecución de comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaquetePersonaje(gson.fromJson(cadenaLeida, PaquetePersonaje.class));

        Servidor.getConector().actualizarPersonaje(getEscuchaCliente().getPaquetePersonaje());
        final int personajeId = getEscuchaCliente().getPaquetePersonaje().getId();
        Servidor.getPersonajesConectados().remove(personajeId);
        Servidor.getPersonajesConectados().put(personajeId, getEscuchaCliente().getPaquetePersonaje());

        for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
            try {
                conectado.getSalida().writeObject(gson.toJson(getEscuchaCliente().getPaquetePersonaje()));
            } catch (final IOException e) {
                Servidor.getLog().append("Falló al intentar enviar paquetePersonaje a:"
                        + conectado.getPaquetePersonaje().getId() + "\n");
            }
        }

    }

}
