package comandos;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import dominio.Casta;
import dominio.NonPlayableCharacter;
import dominio.Personaje;
import mensajeria.PaqueteAtacarNPC;
import mensajeria.PaqueteNPC;
import mensajeria.PaquetePersonaje;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Comando para atacar en una batallla
 *
 *
 */
public class AtacarNPC extends ComandosServer {

    /**
     * Ejecución de comando
     */
    @Override
    public void ejecutar() {
        getEscuchaCliente().setPaqueteAtacarNPC(gson.fromJson(cadenaLeida, PaqueteAtacarNPC.class));

        for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {

            if (conectado.getIdPersonaje() == getEscuchaCliente().getPaqueteAtacarNPC().getId()) {

                for (final Entry<Integer, PaqueteNPC> paqueteNPC : Servidor.getNPsCreados().entrySet()) {

                    if (paqueteNPC.getValue().getId() == getEscuchaCliente().getPaqueteAtacarNPC().getIdNPCEnemigo()) {

                        final NonPlayableCharacter enemigo = new NonPlayableCharacter(paqueteNPC.getValue().getNombre(),
                                paqueteNPC.getValue().getNivel(), paqueteNPC.getValue().getDificultad());

                        enemigo.setSalud(getEscuchaCliente().getPaqueteAtacarNPC().getNuevaSaludEnemigo());

                        for (final Entry<Integer, PaquetePersonaje> paquetePersonaje : Servidor
                                .getPersonajesConectados().entrySet()) {

                            if (paquetePersonaje.getValue().getId() == conectado.getIdPersonaje()) {
                                final String nombre = paquetePersonaje.getValue().getNombre();
                                final int salud = getEscuchaCliente().getPaqueteAtacarNPC().getNuevaSaludPersonaje();
                                final int energia = getEscuchaCliente().getPaqueteAtacarNPC()
                                        .getNuevaEnergiaPersonaje();
                                final int fuerza = paquetePersonaje.getValue().getFuerza();
                                final int destreza = paquetePersonaje.getValue().getDestreza();
                                final int inteligencia = paquetePersonaje.getValue().getInteligencia();
                                final int experiencia = paquetePersonaje.getValue().getExperiencia();
                                final int nivel = paquetePersonaje.getValue().getNivel();
                                final int id = paquetePersonaje.getValue().getId();

                                Personaje personaje = null;
                                Casta casta = null;
                                try {
                                    casta = (Casta) Class
                                            .forName("dominio" + "." + paquetePersonaje.getValue().getCasta())
                                            .newInstance();
                                    personaje = (Personaje) Class
                                            .forName("dominio" + "." + paquetePersonaje.getValue().getRaza())
                                            .getConstructor(String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE,
                                                    Integer.TYPE, Integer.TYPE, Casta.class, Integer.TYPE, Integer.TYPE,
                                                    Integer.TYPE)
                                            .newInstance(nombre, salud, energia, fuerza, destreza, inteligencia, casta,
                                                    experiencia, nivel, id);
                                } catch (InstantiationException | IllegalAccessException | ClassNotFoundException
                                        | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                                        | SecurityException e) {
                                    JOptionPane.showMessageDialog(null, "Error al crear la batalla");
                                }

                                if (enemigo.estaVivo()) {
                                    enemigo.atacar(personaje);
                                }

                                try {
                                    final PaqueteAtacarNPC paqueteAtacarNPC = new PaqueteAtacarNPC(
                                            paquetePersonaje.getValue().getId(), paqueteNPC.getValue().getId(),
                                            personaje.getSalud(), personaje.getEnergia(), enemigo.getSalud(),
                                            personaje.getDefensa(), personaje.getCasta().getProbabilidadEvitarDanio());

                                    getEscuchaCliente().setPaqueteAtacarNPC(paqueteAtacarNPC);

                                    conectado.getSalida()
                                            .writeObject(gson.toJson(getEscuchaCliente().getPaqueteAtacarNPC()));
                                } catch (final IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
