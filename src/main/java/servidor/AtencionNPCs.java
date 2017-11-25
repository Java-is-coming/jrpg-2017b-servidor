package servidor;

import java.util.Map.Entry;

import mensajeria.PaqueteNPC;
import npc.ControlNPC;

/**
 * Thread para atender cada conexión de los clientes
 *
 */
public class AtencionNPCs extends Thread {

    /**
     * Constructor
     */
    public AtencionNPCs() {

    }

    /**
     * Ejecución del thread
     */
    @Override
    public void run() {

        synchronized (this) {
            try {
                while (true) {

                    for (final Entry<Integer, PaqueteNPC> paqueteNPC : Servidor.getNpcsARespawnear().entrySet()) {
                        ControlNPC.reGenerarNPC(paqueteNPC.getValue());
                    }
                }
            } catch (final Exception e) {
                Servidor.getLog().append("Falló al intentar enviar paqueteDePersonajes\n");
            }
        }
    }
}
