package comandos;

import mensajeria.PaquetePersonaje;
import servidor.Servidor;

/**
 * Comando para mostrar la lista de mapas.
 *
 */
public class MostrarMapas extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaquetePersonaje(gson.fromJson(cadenaLeida, PaquetePersonaje.class));
        final int mapa = getEscuchaCliente().getPaquetePersonaje().getMapa();
        final String hostAddress = getEscuchaCliente().getSocket().getInetAddress().getHostAddress();
        Servidor.getLog().append(hostAddress + " ha elegido el mapa " + mapa + System.lineSeparator());

    }

}
