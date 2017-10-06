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
			
			for (int i = 1; i < 5; i++) {
				PaqueteNPC npc = new PaqueteNPC();
				
				NonPlayableCharacter npcChar = new NonPlayableCharacter("Santiago Trunke771 " + i, 10, i);
				
				
				npc.setNombre("Santiago Trunke771 " + i);
				npc.setEstado(Estado.estadoJuego);							
				npc.setId(i);
				npc.setFrame(1);
				npc.setPosX(100*(i+1));
				npc.setPosY(100*(i+1));
				npc.setDireccion(3);
				npc.setFuerza(npcChar.getFuerza());			
				npc.setNivel(npcChar.getNivel());
				npc.setDificultad(i);
				npc.setSaludTope(npcChar.getSaludTope());

				Servidor.getNPsCreados().put(i, npc);
			}
			
			
			
		} catch (IOException e) {
			Servidor.log.append("Fallo generando NPCs." + System.lineSeparator());
		}
	}
	
}
