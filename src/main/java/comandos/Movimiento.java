package comandos;

import mensajeria.PaqueteMovimiento;
import servidor.Servidor;

/**
 * Comando que mueve al personaje en el mapa y notifica al resto de los usuarios
 * conectados.
 *
 */
public class Movimiento extends ComandosServer {

    /**
     * Ejecución del comando.
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaqueteMovimiento((gson.fromJson(cadenaLeida, PaqueteMovimiento.class)));

        Servidor.getUbicacionPersonajes().get(getEscuchaCliente().getPaqueteMovimiento().getIdPersonaje())
                .setPosX(getEscuchaCliente().getPaqueteMovimiento().getPosX());
        Servidor.getUbicacionPersonajes().get(getEscuchaCliente().getPaqueteMovimiento().getIdPersonaje())
                .setPosY(getEscuchaCliente().getPaqueteMovimiento().getPosY());
        Servidor.getUbicacionPersonajes().get(getEscuchaCliente().getPaqueteMovimiento().getIdPersonaje())
                .setDireccion(getEscuchaCliente().getPaqueteMovimiento().getDireccion());
        Servidor.getUbicacionPersonajes().get(getEscuchaCliente().getPaqueteMovimiento().getIdPersonaje())
                .setFrame(getEscuchaCliente().getPaqueteMovimiento().getFrame());

        synchronized (Servidor.getAtencionMovimientos()) {
            Servidor.getAtencionMovimientos().notify();
        }

    }

}
