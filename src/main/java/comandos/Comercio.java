package comandos;

import java.io.IOException;

import mensajeria.PaqueteComerciar;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando para comerciar
 *
 */
public class Comercio extends ComandosServer {

    /**
     * Ejecución de comando
     */
    @Override
    public void ejecutar() {
        PaqueteComerciar paqueteComerciar;
        paqueteComerciar = gson.fromJson(cadenaLeida, PaqueteComerciar.class);

        // BUSCO EN LAS ESCUCHAS AL QUE SE LO TENGO QUE MANDAR
        for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
            if (conectado.getPaquetePersonaje().getId() == paqueteComerciar.getIdEnemigo()) {
                try {
                    conectado.getSalida().writeObject(gson.toJson(paqueteComerciar));
                } catch (final IOException e) {
                    final int personajeId = conectado.getPaquetePersonaje().getId();
                    Servidor.getLog().append("Falló al intentar enviar comercio a:" + personajeId + "\n");
                }
            }
        }

    }
}
