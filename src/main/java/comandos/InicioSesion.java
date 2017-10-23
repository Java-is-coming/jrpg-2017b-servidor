package comandos;

import java.io.IOException;

import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteDeNPCs;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;
import servidor.Servidor;

/**
 * Comando para inciar sesión
 *
 */
public class InicioSesion extends ComandosServer {

    /**
     * Ejecución del comando
     */
    @Override
    public void ejecutar() {
        final Paquete paqueteSv = new Paquete(null, 0);
        paqueteSv.setComando(Comando.INICIOSESION);

        // Recibo el paquete usuario
        final PaqueteUsuario paqueteUsuario = gson.fromJson(cadenaLeida, PaqueteUsuario.class);
        this.getEscuchaCliente().setPaqueteUsuario(paqueteUsuario);

        // Si se puede loguear el usuario le envio un mensaje de exito y el paquete
        // personaje con los datos
        try {
            if (Servidor.getConector().loguearUsuario(getEscuchaCliente().getPaqueteUsuario())) {

                PaquetePersonaje paquetePersonaje = new PaquetePersonaje();
                paquetePersonaje = Servidor.getConector().getPersonaje(getEscuchaCliente().getPaqueteUsuario());
                paquetePersonaje.setComando(Comando.INICIOSESION);
                paquetePersonaje.setMensaje(Paquete.msjExito);
                getEscuchaCliente().setIdPersonaje(paquetePersonaje.getId());

                getEscuchaCliente().getSalida().writeObject(gson.toJson(paquetePersonaje));

                final PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
                pdn.setComando(Comando.ACTUALIZARNPCS);
                getEscuchaCliente().getSalida().writeObject(gson.toJson(pdn));

            } else {
                paqueteSv.setMensaje(Paquete.msjFracaso);
                getEscuchaCliente().getSalida().writeObject(gson.toJson(paqueteSv));
            }
        } catch (final IOException e) {
            Servidor.getLog().append("Falló al intentar iniciar sesión \n");
        }

    }
}
