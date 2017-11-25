package npc;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;

import mensajeria.Comando;
import mensajeria.PaqueteDeNPCs;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaqueteNPC;
import servidor.EscuchaCliente;
import servidor.Servidor;

/**
 * Clase para el manejo de los NPC's
 *
 */
public final class ControlNPC {
    private static final int DISTANCIA_SEGURA_A_PERSONAJE_PARA_RESPAWNEAR = 700;

    /**
     * Constructor
     */
    private ControlNPC() {

    }

    /**
     * Construye los NPC's en posiciones del mapa.
     */
    public static void generarNPCs() {
        try {
            final List<PaqueteNPC> npcs = Servidor.getConector().getNPCs();

            for (final PaqueteNPC npc : npcs) {
                Servidor.getNPsCreados().put(npc.getId(), npc);
            }
        } catch (final Exception e) {
            Servidor.getLog().append("Fallo generando NPCs." + System.lineSeparator());
        }
    }

    /**
     * Re construye los NPC's en posiciones del mapa.
     *
     * @param npc
     *            paqueteNPC
     * @return boolean isCreated
     */
    public static Boolean reGenerarNPC(final PaqueteNPC npc) {
        Boolean isCreated = false;
        try {
            // Reviso que se haya cumplido el tiempo para el respawn
            final Date date = new Date();
            final long now = date.getTime();
            final long deathTime = npc.getDeathTime().getTime();
            final int division = 1000;

            if ((now - deathTime) / division < npc.getSecsToRespawn()) {
                return isCreated;
            }

            // Me fijo que no se pise con nadie
            // float npcPosX = npc.getPosX();
            // float npcPosY = npc.getPosY();
            final float npcPosMaxX = npc.getMaxX();
            final float npcPosMinX = npc.getMinX();
            final float npcPosMaxY = npc.getMaxY();
            final float npcPosMinY = npc.getMinY();

            for (final Entry<Integer, PaqueteMovimiento> ubicacionPersonaje : Servidor.getUbicacionPersonajes()
                    .entrySet()) {
                final float personajePosX = ubicacionPersonaje.getValue().getPosX();
                final float personajePosY = ubicacionPersonaje.getValue().getPosY();

                // Calculo la distancia diagonal
                // double diagonalDis = Math
                // .sqrt(Math.pow(npcPosX - personajePosX, 2) + Math.pow(npcPosY -
                // personajePosY, 2));
                final double diagonalDis1 = Math
                        .sqrt(Math.pow(npcPosMaxX - personajePosX, 2) + Math.pow(npcPosMaxY - personajePosY, 2));
                final double diagonalDis2 = Math
                        .sqrt(Math.pow(npcPosMaxX - personajePosX, 2) + Math.pow(npcPosMinY - personajePosY, 2));
                final double diagonalDis3 = Math
                        .sqrt(Math.pow(npcPosMinX - personajePosX, 2) + Math.pow(npcPosMaxY - personajePosY, 2));
                final double diagonalDis4 = Math
                        .sqrt(Math.pow(npcPosMinX - personajePosX, 2) + Math.pow(npcPosMinY - personajePosY, 2));

                // Si hay una distancia diagonal menor a 700 no lo considero válido
                if (diagonalDis1 < DISTANCIA_SEGURA_A_PERSONAJE_PARA_RESPAWNEAR) {
                    return isCreated;
                }
                // Si hay una distancia diagonal menor a 700 no lo considero válido
                if (diagonalDis2 < DISTANCIA_SEGURA_A_PERSONAJE_PARA_RESPAWNEAR) {
                    return isCreated;
                }
                // Si hay una distancia diagonal menor a 700 no lo considero válido
                if (diagonalDis3 < DISTANCIA_SEGURA_A_PERSONAJE_PARA_RESPAWNEAR) {
                    return isCreated;
                }
                // Si hay una distancia diagonal menor a 700 no lo considero válido
                if (diagonalDis4 < DISTANCIA_SEGURA_A_PERSONAJE_PARA_RESPAWNEAR) {
                    return isCreated;
                }
            }

            // Actualizo el NPC segun la DB
            final PaqueteNPC newNPC = Servidor.getConector().getNPC(npc.getId());

            Servidor.getNPsCreados().put(newNPC.getId(), newNPC);
            Servidor.getNpcsARespawnear().remove(newNPC.getId());

            // Le aviso a todos
            final PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
            pdn.setComando(Comando.ACTUALIZARNPCS);
            for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
                try {
                    synchronized (conectado) {
                        conectado.getSalida().writeObject(new Gson().toJson(pdn));
                    }
                } catch (final IOException e) {
                    // TODO Auto-generated catch block
                    Servidor.getLog().append("Falló al intentar enviar los npcs actualizados."
                            + conectado.getPaquetePersonaje().getId() + "\n");
                }
            }

            isCreated = true;
        } catch (final Exception e) {
            Servidor.getLog().append(
                    "Fallo re-generando NPC: " + npc.getNombre() + "(" + npc.getId() + ") " + System.lineSeparator());
        }
        return isCreated;
    }
}
