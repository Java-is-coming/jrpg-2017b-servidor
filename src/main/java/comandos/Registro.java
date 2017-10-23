package comandos;

import java.io.IOException;

import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteUsuario;
import servidor.Servidor;

/**
 * Comando para registrar un nuevo usuario
 *
 */
public class Registro extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        final Paquete paqueteSv = new Paquete(null, 0);
        paqueteSv.setComando(Comando.REGISTRO);

        final PaqueteUsuario paquete = (PaqueteUsuario) (gson.fromJson(cadenaLeida, PaqueteUsuario.class)).clone();
        getEscuchaCliente().setPaqueteUsuario(paquete);

        // Si el usuario se pudo registrar le envio un msj de exito
        try {
            if (Servidor.getConector().registrarUsuario(getEscuchaCliente().getPaqueteUsuario())) {
                paqueteSv.setMensaje(Paquete.msjExito);
                getEscuchaCliente().getSalida().writeObject(gson.toJson(paqueteSv));

                // Si el usuario no se pudo registrar le envio un msj de fracaso
            } else {
                paqueteSv.setMensaje(Paquete.msjFracaso);
                getEscuchaCliente().getSalida().writeObject(gson.toJson(paqueteSv));
            }
        } catch (final IOException e) {
            Servidor.getLog().append("Falló al intentar enviar registro\n");
        }

    }

}
