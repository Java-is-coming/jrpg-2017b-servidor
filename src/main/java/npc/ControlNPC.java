package npc;

import java.io.IOException;

import estados.Estado;
import mensajeria.Comando;
import mensajeria.PaqueteNPC;
import servidor.Servidor;

public class ControlNPC {

	public static void generarNPCs() {
		try {
			
	
			
			for (int i = 1; i < 5; i++) {
				PaqueteNPC npc = new PaqueteNPC();
				npc.setNombre("Gustavo Gato " + i);
				npc.setEstado(Estado.estadoJuego);							
				npc.setId(i);
				npc.setFrame(1);
				npc.setPosX(60*(i+1));
				npc.setPosY(60*(i+1));
				npc.setDireccion(1);
				npc.setSaludTope(250);
				npc.setFuerza(50);			
				

				Servidor.getNPsCreados().put(i, npc);
			}
			
			
			
		} catch (IOException e) {
			Servidor.log.append("Fallo generando NPCs." + System.lineSeparator());
		}
	}
	
}
