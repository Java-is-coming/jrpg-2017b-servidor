package comandos;

import java.io.IOException;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteDeNPCs;
import mensajeria.PaqueteNPC;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando para indicar a los clientes que actualicen el estado de los NPc's
 */
public class ActualizarNPCs extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaqueteNPC(gson.fromJson(cadenaLeida, PaqueteNPC.class));

        // Voy a mandarle todos los NPC's a los usuarios logueados en estado juego
        for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {

            if (conectado.getPaquetePersonaje().getEstado() == Estado.ESTADO_JUEGO) {

                try {

                    final PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
                    pdn.setComando(Comando.ACTUALIZARNPCS);
                    synchronized (conectado) {
                        conectado.getSalida().writeObject(gson.toJson(pdn));
                    }

                } catch (final IOException e) {
                    final int idPersonaje = conectado.getPaquetePersonaje().getId();
                    Servidor.getLog().append("Falló al intentar enviar ataque a:" + idPersonaje + "\n");
                }
            }
        }

    }
}
