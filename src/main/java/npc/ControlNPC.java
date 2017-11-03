package npc;

import java.io.IOException;

import dominio.NonPlayableCharacter;
import estados.Estado;
import mensajeria.PaqueteNPC;
import servidor.Servidor;

/**
 * Clase para el manejo de los NPC's
 *
 */
public final class ControlNPC {

	private static final int[] x = new int[] { 10, 736, 1100, 1504, -1210, 1120, 2176, 992, -928, -10 };
	/*
	 * Wx[0] = 10; x[1] = 736; x[2] = 1100; x[3] = 1504; x[4] = -1210; x[5] = 1120;
	 * x[6] = 2176; x[7] = 992; x[8] = -928; x[9] = -10; ````````````
	 */
	private static final int[] y = new int[] { 500, 760, 672, 1104, 1038, 1456, 1120, 1744, 1456, 2100 };
	private static final String[] nombres = new String[] { "White Walker", "Paul Walker", "Johnnie Walker", "Kraken",
			"Ifrit", "Leviatan", "Minotauro", "Illidan", "Arthas", "Jon Snow" };

	private static final int[] xValidos = new int[] { 64, 192, -256, -992, -2016 };
	private static final int[] YValidos = new int[] { 1088, 1472, 1856, 1328, 1008 };

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
			for (int i = 1; i <= nombres.length; i++) {
				final PaqueteNPC npc = new PaqueteNPC();

				final NonPlayableCharacter npcChar = new NonPlayableCharacter(nombres[i - 1], i, i - 1);

				npc.setNombre(npcChar.getNombre());
				npc.setEstado(Estado.ESTADO_JUEGO);
				npc.setId(i);

				npc.setFrame(1);

				npc.setPosX(x[i - 1]);
				npc.setPosY(y[i - 1]);

				final int direccion = 6;
				npc.setDireccion(direccion);

				npc.setFuerza(npcChar.getFuerza());
				npc.setNivel(npcChar.getNivel());
				npc.setDificultad(i - 1);
				npc.setSaludTope(npcChar.getSaludTope());

				Servidor.getNPsCreados().put(i, npc);
			}

		} catch (final IOException e) {
			Servidor.getLog().append("Fallo generando NPCs." + System.lineSeparator());
		}
	}

	public static void reGenerarNPC(final int id) {
		try {
			final PaqueteNPC npc = new PaqueteNPC();

			final NonPlayableCharacter npcChar = new NonPlayableCharacter(nombres[id - 1], id, id - 1);

			npc.setNombre(npcChar.getNombre());
			npc.setEstado(Estado.ESTADO_JUEGO);
			npc.setId(id);

			npc.setFrame(1);

			for (int i = 0; i <= xValidos.length; i++) {
				int x = xValidos[i];
				int y = YValidos[i];
				/*
				 * for(PaquetePersonaje personaje: Servidor.getPersonajesConectados()) {
				 * personaje.get }
				 */
			}

			npc.setPosX(x[id - 1]);
			npc.setPosY(y[id - 1]);

			final int direccion = 6;
			npc.setDireccion(direccion);

			npc.setFuerza(npcChar.getFuerza());
			npc.setNivel(npcChar.getNivel());
			npc.setDificultad(id - 1);
			npc.setSaludTope(npcChar.getSaludTope());

			Servidor.getNPsCreados().put(id, npc);

		} catch (final IOException e) {
			Servidor.getLog().append("Fallo generando NPCs." + System.lineSeparator());
		}
	}
}
