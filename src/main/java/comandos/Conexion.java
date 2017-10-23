package comandos;

import mensajeria.PaqueteMovimiento;
import mensajeria.PaquetePersonaje;
import servidor.Servidor;

/**
 * Clase conexion, se envia cuando se conecta un cliente
 *
 */
public class Conexion extends ComandosServer {

    /**
     * Ejecución de comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente()
                .setPaquetePersonaje((PaquetePersonaje) (gson.fromJson(cadenaLeida, PaquetePersonaje.class)).clone());

        Servidor.getPersonajesConectados().put(getEscuchaCliente().getPaquetePersonaje().getId(),
                (PaquetePersonaje) getEscuchaCliente().getPaquetePersonaje().clone());
        Servidor.getUbicacionPersonajes().put(getEscuchaCliente().getPaquetePersonaje().getId(),
                (PaqueteMovimiento) new PaqueteMovimiento(getEscuchaCliente().getPaquetePersonaje().getId()).clone());

        synchronized (Servidor.getAtencionConexiones()) {
            Servidor.getAtencionConexiones().notify();
        }

    }

}
