package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando para actualizar el trueque
 *
 */
public class ActualizarTrueque extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaquetePersonaje(gson.fromJson(cadenaLeida, PaquetePersonaje.class));

        Servidor.getConector().actualizarInventario(getEscuchaCliente().getPaquetePersonaje());
        Servidor.getConector().actualizarPersonaje(getEscuchaCliente().getPaquetePersonaje());

        final int personajeId = getEscuchaCliente().getPaquetePersonaje().getId();
        Servidor.getPersonajesConectados().remove(personajeId);
        Servidor.getPersonajesConectados().put(personajeId, getEscuchaCliente().getPaquetePersonaje());

        for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
            try {
                conectado.getSalida().writeObject(gson.toJson(getEscuchaCliente().getPaquetePersonaje()));
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                Servidor.getLog().append("Falló al intentar enviar actualizacion de trueque a:"
                        + conectado.getPaquetePersonaje().getId() + "\n");
            }
        }

    }

}
