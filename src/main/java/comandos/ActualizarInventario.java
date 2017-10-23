package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando para actualizar inventario de un personaje
 *
 */
public class ActualizarInventario extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaquetePersonaje(gson.fromJson(cadenaLeida, PaquetePersonaje.class));

        Servidor.getConector().actualizarInventario(getEscuchaCliente().getPaquetePersonaje());
        Servidor.getPersonajesConectados().remove(getEscuchaCliente().getPaquetePersonaje().getId());
        Servidor.getPersonajesConectados().put(getEscuchaCliente().getPaquetePersonaje().getId(),
                getEscuchaCliente().getPaquetePersonaje());

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
