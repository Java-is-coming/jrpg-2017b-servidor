package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando para actualizar el nivel de un personaje
 */
public class ActualizarPersonajeLvl extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaquetePersonaje(gson.fromJson(cadenaLeida, PaquetePersonaje.class));

        Servidor.getConector().actualizarPersonajeSubioNivel(getEscuchaCliente().getPaquetePersonaje());

        final int personajeId = getEscuchaCliente().getPaquetePersonaje().getId();
        Servidor.getPersonajesConectados().remove(personajeId);
        Servidor.getPersonajesConectados().put(personajeId, getEscuchaCliente().getPaquetePersonaje());
        getEscuchaCliente().getPaquetePersonaje().ponerBonus();
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
