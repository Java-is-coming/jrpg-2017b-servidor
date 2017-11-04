package comandos;

import java.io.IOException;

import mensajeria.PaqueteAtacar;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando para atacar en una batallla
 *
 *
 */
public class Atacar extends ComandosServer {

    /**
     * Ejecución de comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaqueteAtacar(gson.fromJson(cadenaLeida, PaqueteAtacar.class));
        for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
            if (conectado.getIdPersonaje() == getEscuchaCliente().getPaqueteAtacar().getIdEnemigo()) {
                try {
                    conectado.getSalida().writeObject(gson.toJson(getEscuchaCliente().getPaqueteAtacar()));
                } catch (final IOException e) {
                    final int idPj = conectado.getPaquetePersonaje().getId();
                    Servidor.getLog().append("Falló al intentar enviar ataque a:" + idPj + "\n");
                }
            }

        }

    }

}
