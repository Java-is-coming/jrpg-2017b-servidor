package comandos;

import mensajeria.Comando;
import servidor.EscuchaCliente;

/**
 * Comandos server
 *
 */
public abstract class ComandosServer extends Comando {
    private EscuchaCliente escuchaCliente;

    /**
     * Escucha cliente
     *
     * @param escuchaCliente
     *            escucha cliente
     */
    public void setEscuchaCliente(final EscuchaCliente escuchaCliente) {
        this.escuchaCliente = escuchaCliente;
    }

    /**
     * Escucha cliente
     *
     * @return escuchaCliente cliente
     */
    public EscuchaCliente getEscuchaCliente() {
        return escuchaCliente;
    }
}
