package comandos;

import java.io.IOException;
import java.util.Map;

import mensajeria.Comando;
import mensajeria.PaqueteMensaje;
import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando para hablar
 *
 */
public class Talk extends ComandosServer {

    @Override
    public void ejecutar() {
        int idUser = 0;
        final PaqueteMensaje paqueteMensaje = (gson.fromJson(cadenaLeida, PaqueteMensaje.class));

        if (paqueteMensaje.getUserReceptor() != null) {
            if (Servidor.mensajeAUsuario(paqueteMensaje)) {

                paqueteMensaje.setComando(Comando.TALK);

                for (final Map.Entry<Integer, PaquetePersonaje> personaje : Servidor.getPersonajesConectados()
                        .entrySet()) {
                    if (personaje.getValue().getNombre().equals(paqueteMensaje.getUserReceptor())) {
                        idUser = personaje.getValue().getId();
                        break;
                    }
                }

                for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
                    if (conectado.getIdPersonaje() == idUser) {
                        try {
                            conectado.getSalida().writeObject(gson.toJson(paqueteMensaje));
                        } catch (final IOException e) {
                            Servidor.getLog().append("Falló al intentar enviar mensaje a:"
                                    + conectado.getPaquetePersonaje().getId() + "\n");
                        }
                    }
                }
            } else {
                Servidor.getLog().append("No se envió el mensaje \n");
            }
        } else {
            for (final Map.Entry<Integer, PaquetePersonaje> personaje : Servidor.getPersonajesConectados().entrySet()) {
                if (personaje.getValue().getNombre().equals(paqueteMensaje.getUserEmisor())) {
                    idUser = personaje.getValue().getId();
                    break;
                }
            }

            int contador = 1;
            for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
                if (conectado.getIdPersonaje() != idUser) {
                    try {
                        conectado.getSalida().writeObject(gson.toJson(paqueteMensaje));

                        contador++;
                    } catch (final IOException e) {
                        final int idPj = conectado.getPaquetePersonaje().getId();
                        Servidor.getLog().append("Falló al intentar enviar mensaje a:" + idPj + "\n");
                    }
                }
            }
            Servidor.mensajeAAll(paqueteMensaje, contador);
        }
    }
}
