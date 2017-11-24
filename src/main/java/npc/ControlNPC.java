package npc;

import java.io.IOException;
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
	/*
	 * private static final int[] X = new int[] { 10, 736, 1100, 1504, -1210, 1120,
	 * 2176, 992, -928, -10 }; private static final int[] Y = new int[] { 500, 760,
	 * 672, 1104, 1038, 1456, 1120, 1744, 1456, 2100 }; private static final
	 * String[] NOMBRES = new String[] { "White Walker", "Paul Walker",
	 * "Johnnie Walker", "Kraken", "Ifrit", "Leviatan", "Minotauro", "Illidan",
	 * "Arthas", "Jon Snow" };
	 */
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
			// Actualizo el NPC segun la DB
			PaqueteNPC newNPC = Servidor.getConector().getNPC(npc.getId());

			// Me fijo que no se pise con nadie
			float npcPosX = newNPC.getPosX();
			float npcPosY = newNPC.getPosY();

			Boolean seVen = false;
			for (final Entry<Integer, PaqueteMovimiento> ubicacionPersonaje : Servidor.getUbicacionPersonajes()
					.entrySet()) {
				float personajePosX = ubicacionPersonaje.getValue().getPosX();
				float personajePosY = ubicacionPersonaje.getValue().getPosY();

				// Calculo la distancia diagonal
				double diagonalDis = Math
						.sqrt(Math.pow(npcPosX - personajePosX, 2) + Math.pow(npcPosY - personajePosY, 2));

				// Si hay una distancia diagonal menor a 100 no lo considero válido
				if (diagonalDis < 100) {
					seVen = true;
					break;
				}
			}

			if (!seVen) {
				Servidor.getNPsCreados().put(newNPC.getId(), newNPC);
				Servidor.getNpcsARespawnear().remove(newNPC.getId());

				// Le aviso a todos
				for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
					final PaqueteDeNPCs pdn = (PaqueteDeNPCs) new PaqueteDeNPCs(Servidor.getNPsCreados()).clone();
					pdn.setComando(Comando.ACTUALIZARNPCS);

					try {
						conectado.getSalida().writeObject(new Gson().toJson(pdn));
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						Servidor.getLog().append("Falló al intentar enviar los npcs actualizados."
								+ conectado.getPaquetePersonaje().getId() + "\n");
					}
				}

				isCreated = true;
			}
		} catch (final Exception e) {
			Servidor.getLog().append(
					"Fallo re-generando NPC: " + npc.getNombre() + "(" + npc.getId() + ") " + System.lineSeparator());
		}
		return isCreated;
	}
}
