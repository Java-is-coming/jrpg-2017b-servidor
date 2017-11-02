package servidor;

import com.google.gson.Gson;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteDePersonajes;

/**
 * Thread para atender cada conexión de los clientes
 *
 */
public class AtencionConexiones extends Thread {

    private final Gson gson = new Gson();

    /**
     * Constructor
     */
    public AtencionConexiones() {

    }

    /**
     * Ejecución del thread
     */
    @Override
    public void run() {

        synchronized (this) {
            try {
                while (true) {

                    // Espero a que se conecte alguien
                    wait();

                    // Le reenvio la conexion a todos
                    for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {

                        if (conectado.getPaquetePersonaje().getEstado() != Estado.ESTADO_OFFLINE) {

                            final PaqueteDePersonajes pdp = (PaqueteDePersonajes) new PaqueteDePersonajes(
                                    Servidor.getPersonajesConectados()).clone();
                            pdp.setComando(Comando.CONEXION);
                            synchronized (conectado) {
                                conectado.getSalida().writeObject(gson.toJson(pdp));
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                Servidor.getLog().append("Falló al intentar enviar paqueteDePersonajes\n");
            }
        }
    }
}
