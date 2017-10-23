package comandos;

import java.io.IOException;

import mensajeria.Paquete;
import servidor.Servidor;

/**
 * Comando para desconexión
 *
 */
public class Salir extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        // Cierro todo
        try {
            getEscuchaCliente().getEntrada().close();
            getEscuchaCliente().getSalida().close();
            getEscuchaCliente().getSocket().close();
        } catch (final IOException e) {
            Servidor.getLog().append("Falló al intentar salir \n");

        }

        // Lo elimino de los clientes conectados
        Servidor.getClientesConectados().remove(this.getEscuchaCliente());
        final Paquete paquete = gson.fromJson(cadenaLeida, Paquete.class);
        // Indico que se desconecto
        Servidor.getLog().append(paquete.getIp() + " se ha desconectado." + System.lineSeparator());
    }

}
