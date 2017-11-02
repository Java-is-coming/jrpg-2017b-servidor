package servidor;

import com.google.gson.Gson;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteDeMovimientos;

/**
 * Thread para la atencion de los movimientos de los personajes en el mapa
 *
 */
public class AtencionMovimientos extends Thread {

    private final Gson gson = new Gson();

    /**
     * Constructor
     */
    public AtencionMovimientos() {

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

                        if (conectado.getPaquetePersonaje().getEstado() == Estado.ESTADO_JUEGO) {

                            final PaqueteDeMovimientos pdp = (PaqueteDeMovimientos) new PaqueteDeMovimientos(
                                    Servidor.getUbicacionPersonajes()).clone();
                            pdp.setComando(Comando.MOVIMIENTO);
                            synchronized (conectado) {
                                conectado.getSalida().writeObject(gson.toJson(pdp));
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                Servidor.getLog().append("Falló al intentar enviar paqueteDeMovimientos \n");
            }
        }
    }
}
