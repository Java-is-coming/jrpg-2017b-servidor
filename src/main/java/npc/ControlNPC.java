package npc;

import java.io.IOException;

import dominio.NonPlayableCharacter;
import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteNPC;
import servidor.Servidor;

public class ControlNPC {

	public static void generarNPCs() {
		try {			
			int[] x = new int[] {10, 736, 1100, 1504, -1056, 1120, 2176, 992, -928, 0};
			int[] y = new int[] {500, 760, 672, 1104, 1008, 1456, 1120, 1744, 1456, 2240};
			String[] nombres = new String[] {"White walker", "Paul walker", "Medusa", "Kraken", "Ifrit", "Leviatan", "Minotauro", "Illidan", "Arthas", "Jon Snow"};
			
			for (int i = 1; i <= 10; i++) {
				PaqueteNPC npc = new PaqueteNPC();
				
				NonPlayableCharacter npcChar = new NonPlayableCharacter(nombres[i-1], i, i-1);
				
				
				npc.setNombre(npcChar.getNombre());
				npc.setEstado(Estado.estadoJuego);							
				npc.setId(i);
				
				npc.setFrame(1);
				
				npc.setPosX(x[i-1]);
				npc.setPosY(y[i-1]);
				
				npc.setDireccion(6);
				
				npc.setFuerza(npcChar.getFuerza());			
				npc.setNivel(npcChar.getNivel());
				npc.setDificultad(i-1);
				npc.setSaludTope(npcChar.getSaludTope());

				Servidor.getNPsCreados().put(i, npc);
			}
			
			
			
		} catch (IOException e) {
			Servidor.log.append("Fallo generando NPCs." + System.lineSeparator());
		}
	}
	
}
