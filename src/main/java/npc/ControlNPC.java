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
			List<PaqueteNPC> npcs = Servidor.getConector().getNPCs();

			for (PaqueteNPC npc : npcs)
				Servidor.getNPsCreados().put(npc.getId(), npc);
			/*
			 * for (int i = 1; i <= NOMBRES.length; i++) { final PaqueteNPC npc = new
			 * PaqueteNPC();
			 * 
			 * final NonPlayableCharacter npcChar = new NonPlayableCharacter(NOMBRES[i - 1],
			 * i, i - 1);
			 * 
			 * npc.setNombre(npcChar.getNombre()); npc.setEstado(Estado.ESTADO_JUEGO);
			 * npc.setId(i);
			 * 
			 * npc.setFrame(1);
			 * 
			 * npc.setPosX(X[i - 1]); npc.setPosY(Y[i - 1]);
			 * 
			 * final int direccion = 6; npc.setDireccion(direccion);
			 * 
			 * npc.setFuerza(npcChar.getFuerza()); npc.setNivel(npcChar.getNivel());
			 * npc.setDificultad(i - 1); npc.setSaludTope(npcChar.getSaludTope());
			 * 
			 * Servidor.getNPsCreados().put(i, npc); }
			 */
		} catch (final Exception e) {
			Servidor.getLog().append("Fallo generando NPCs." + System.lineSeparator());
		}
	}

	/**
	 * Re construye los NPC's en posiciones del mapa.
	 * 
	 * @param PaqueteNPC
	 *            npc
	 * 
	 * @return Boolean isCreated
	 */
	public static Boolean reGenerarNPC(PaqueteNPC npc) {
		Boolean isCreated = false;
		try {
			// Reviso que se haya cumplido el tiempo para el respawn
			Date date = new Date();
			long now = date.getTime();
			long deathTime = npc.getDeathTime().getTime();

			if ((now - deathTime) / 1000 < npc.getSecsToRespawn()) {
				return isCreated;
			}

			// Me fijo que no se pise con nadie
			// float npcPosX = npc.getPosX();
			// float npcPosY = npc.getPosY();
			float npcPosMaxX = npc.getMaxX();
			float npcPosMinX = npc.getMinX();
			float npcPosMaxY = npc.getMaxY();
			float npcPosMinY = npc.getMinY();

			for (final Entry<Integer, PaqueteMovimiento> ubicacionPersonaje : Servidor.getUbicacionPersonajes()
					.entrySet()) {
				float personajePosX = ubicacionPersonaje.getValue().getPosX();
				float personajePosY = ubicacionPersonaje.getValue().getPosY();

				// Calculo la distancia diagonal
				// double diagonalDis = Math
				// .sqrt(Math.pow(npcPosX - personajePosX, 2) + Math.pow(npcPosY -
				// personajePosY, 2));
				double diagonalDis1 = Math
						.sqrt(Math.pow(npcPosMaxX - personajePosX, 2) + Math.pow(npcPosMaxY - personajePosY, 2));
				double diagonalDis2 = Math
						.sqrt(Math.pow(npcPosMaxX - personajePosX, 2) + Math.pow(npcPosMinY - personajePosY, 2));
				double diagonalDis3 = Math
						.sqrt(Math.pow(npcPosMinX - personajePosX, 2) + Math.pow(npcPosMaxY - personajePosY, 2));
				double diagonalDis4 = Math
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
			PaqueteNPC newNPC = Servidor.getConector().getNPC(npc.getId());

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
