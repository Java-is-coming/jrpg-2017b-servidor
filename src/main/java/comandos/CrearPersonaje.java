package comandos;

import java.io.IOException;

import mensajeria.PaquetePersonaje;
import servidor.Servidor;

/**
 * Comando para creacion de personaje
 *
 */
public class CrearPersonaje extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        // Casteo el paquete personaje
        getEscuchaCliente().setPaquetePersonaje((gson.fromJson(cadenaLeida, PaquetePersonaje.class)));
        // Guardo el personaje en ese usuario
        Servidor.getConector().registrarPersonaje(getEscuchaCliente().getPaquetePersonaje(),
                getEscuchaCliente().getPaqueteUsuario());
        try {
            PaquetePersonaje paquetePersonaje;
            paquetePersonaje = new PaquetePersonaje();
            paquetePersonaje = Servidor.getConector().getPersonaje(getEscuchaCliente().getPaqueteUsuario());
            getEscuchaCliente().setIdPersonaje(paquetePersonaje.getId());
            getEscuchaCliente().getSalida().writeObject(gson.toJson(getEscuchaCliente().getPaquetePersonaje(),
                    getEscuchaCliente().getPaquetePersonaje().getClass()));
        } catch (final IOException e1) {
            Servidor.getLog().append("Falló al intentar enviar personaje creado \n");
        }

    }

}
