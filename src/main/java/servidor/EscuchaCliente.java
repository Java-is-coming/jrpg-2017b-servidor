package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.google.gson.Gson;

import comandos.ComandosServer;
import mensajeria.Comando;
import mensajeria.Paquete;
import mensajeria.PaqueteAtacar;
import mensajeria.PaqueteAtacarNPC;
import mensajeria.PaqueteBatalla;
import mensajeria.PaqueteDeMovimientos;
import mensajeria.PaqueteDePersonajes;
import mensajeria.PaqueteFinalizarBatalla;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaqueteNPC;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

/**
 * Escucha, recibe y responde mensajes a los clientes
 *
 */
public class EscuchaCliente extends Thread {

	private final Socket socket;
	private final ObjectInputStream entrada;
	private final ObjectOutputStream salida;
	private int idPersonaje;
	private final Gson gson = new Gson();

	private PaquetePersonaje paquetePersonaje;
	private PaqueteMovimiento paqueteMovimiento;
	private PaqueteBatalla paqueteBatalla;
	private PaqueteAtacar paqueteAtacar;
	private PaqueteAtacarNPC paqueteAtacarNPC;
	private PaqueteFinalizarBatalla paqueteFinalizarBatalla;
	private PaqueteUsuario paqueteUsuario;
	private PaqueteDeMovimientos paqueteDeMovimiento;
	private PaqueteDePersonajes paqueteDePersonajes;
	private PaqueteNPC paqueteNPC;

	/**
	 * Constructor
	 *
	 * @param ip
	 *            del cliente destino
	 * @param socket
	 *            socket correspondiente
	 * @param entrada
	 *            inputstream
	 * @param salida
	 *            outputstream
	 * @throws IOException
	 *             exception
	 */
	public EscuchaCliente(final String ip, final Socket socket, final ObjectInputStream entrada,
			final ObjectOutputStream salida) throws IOException {
		this.socket = socket;
		this.entrada = entrada;
		this.salida = salida;
		paquetePersonaje = new PaquetePersonaje();
	}

	/**
	 * Ejecución del thread
	 */
	@Override
	public void run() {
		try {
			ComandosServer comand;
			Paquete paquete;
			// final Paquete paqueteSv = new Paquete(null, 0);
			paqueteUsuario = new PaqueteUsuario();

			String cadenaLeida = (String) entrada.readObject();
			paquete = gson.fromJson(cadenaLeida, Paquete.class);

			while (!(paquete.getComando() == Comando.DESCONECTAR)) {

				comand = (ComandosServer) paquete.getObjeto(Comando.NOMBREPAQUETE);
				comand.setCadena(cadenaLeida);
				comand.setEscuchaCliente(this);
				comand.ejecutar();
				cadenaLeida = (String) entrada.readObject();
				paquete = gson.fromJson(cadenaLeida, Paquete.class);
			}

			entrada.close();
			salida.close();
			socket.close();

			Servidor.getPersonajesConectados().remove(paquetePersonaje.getId());
			Servidor.getUbicacionPersonajes().remove(paquetePersonaje.getId());
			Servidor.getClientesConectados().remove(this);

			for (final EscuchaCliente conectado : Servidor.getClientesConectados()) {
				paqueteDePersonajes = new PaqueteDePersonajes(Servidor.getPersonajesConectados());
				paqueteDePersonajes.setComando(Comando.CONEXION);
				conectado.salida.writeObject(gson.toJson(paqueteDePersonajes, PaqueteDePersonajes.class));
			}

			Servidor.getLog().append(paquete.getIp() + " se ha desconectado." + System.lineSeparator());

		} catch (IOException | ClassNotFoundException e) {
			Servidor.getLog().append("Error de conexion: " + e.getMessage() + System.lineSeparator());
		}
	}

	/**
	 * Devuelve el socket del cliente
	 *
	 * @return Socket devuelve el socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * Input stream
	 *
	 * @return ObjectInputStream stream de entrada
	 */
	public ObjectInputStream getEntrada() {
		return entrada;
	}

	/**
	 * OutPut stream
	 *
	 * @return ObjectOutPutStream stream de salida
	 */
	public ObjectOutputStream getSalida() {
		return salida;
	}

	/**
	 * Devuelve el paquete del personaje
	 *
	 * @return PaquetePersonaje paquete
	 */
	public PaquetePersonaje getPaquetePersonaje() {
		return paquetePersonaje;
	}

	/**
	 * Devuelve id personaje
	 *
	 * @return int Id personaje
	 */
	public int getIdPersonaje() {
		return idPersonaje;
	}

	/**
	 * Devuelve el paquete movimiento
	 *
	 * @return PaqueteMovimiento paquete
	 */
	public PaqueteMovimiento getPaqueteMovimiento() {
		return paqueteMovimiento;
	}

	/**
	 * Setea el paquete movimiento
	 *
	 * @param paqueteMovimiento
	 *            paquete
	 */
	public void setPaqueteMovimiento(final PaqueteMovimiento paqueteMovimiento) {
		this.paqueteMovimiento = paqueteMovimiento;
	}

	/**
	 * Devuelve el paquete batalla
	 *
	 * @return PaqueteBatalla paquete
	 */
	public PaqueteBatalla getPaqueteBatalla() {
		return paqueteBatalla;
	}

	/**
	 * Setea el paquete batalla
	 *
	 * @param paqueteBatalla
	 *            paquete
	 */
	public void setPaqueteBatalla(final PaqueteBatalla paqueteBatalla) {
		this.paqueteBatalla = paqueteBatalla;
	}

	/**
	 * Paquete atacar
	 *
	 * @return PaqueteAtacar paquete
	 */
	public PaqueteAtacar getPaqueteAtacar() {
		return paqueteAtacar;
	}

	/**
	 * Paquete atacar
	 *
	 * @param paqueteAtacar
	 *            paquete
	 */
	public void setPaqueteAtacar(final PaqueteAtacar paqueteAtacar) {
		this.paqueteAtacar = paqueteAtacar;
	}

	/**
	 * Paquete atacar
	 *
	 * @return PaqueteAtacarNPC paquete
	 */
	public PaqueteAtacarNPC getPaqueteAtacarNPC() {
		return paqueteAtacarNPC;
	}

	/**
	 * Paquete atacar
	 *
	 * @param PaqueteAtacarNPC
	 *            paquete
	 */
	public void setPaqueteAtacarNPC(final PaqueteAtacarNPC paqueteAtacarNPC) {
		this.paqueteAtacarNPC = paqueteAtacarNPC;
	}

	/**
	 * Paquete finalizar batalla
	 *
	 * @return PaqueteFinalizarBatalla paquete
	 */
	public PaqueteFinalizarBatalla getPaqueteFinalizarBatalla() {
		return paqueteFinalizarBatalla;
	}

	/**
	 * Set Paquete finalizar batalla
	 *
	 * @param paqueteFinalizarBatalla
	 *            paquete
	 */
	public void setPaqueteFinalizarBatalla(final PaqueteFinalizarBatalla paqueteFinalizarBatalla) {
		this.paqueteFinalizarBatalla = paqueteFinalizarBatalla;
	}

	/**
	 * Get paquete de movimiento
	 *
	 * @return PaqueteDeMovimientos
	 */
	public PaqueteDeMovimientos getPaqueteDeMovimiento() {
		return paqueteDeMovimiento;
	}

	/**
	 * Set paquete de movimientos
	 *
	 * @param paqueteDeMovimiento
	 *            paquete
	 */
	public void setPaqueteDeMovimiento(final PaqueteDeMovimientos paqueteDeMovimiento) {
		this.paqueteDeMovimiento = paqueteDeMovimiento;
	}

	/**
	 * Personajes
	 *
	 * @return PaqueteDePersonajes paquete
	 */
	public PaqueteDePersonajes getPaqueteDePersonajes() {
		return paqueteDePersonajes;
	}

	/**
	 * Personajes
	 *
	 * @param paqueteDePersonajes
	 *            paquete
	 */
	public void setPaqueteDePersonajes(final PaqueteDePersonajes paqueteDePersonajes) {
		this.paqueteDePersonajes = paqueteDePersonajes;
	}

	/**
	 * Id personaje
	 *
	 * @param idPersonaje
	 *            id
	 */
	public void setIdPersonaje(final int idPersonaje) {
		this.idPersonaje = idPersonaje;
	}

	/**
	 * Personaje
	 *
	 * @param paquetePersonaje
	 *            paquete
	 *
	 */
	public void setPaquetePersonaje(final PaquetePersonaje paquetePersonaje) {
		this.paquetePersonaje = paquetePersonaje;
	}

	/**
	 * Paquete usuario
	 *
	 * @return PaqueteUsuario paquete
	 *
	 */
	public PaqueteUsuario getPaqueteUsuario() {
		return paqueteUsuario;
	}

	/**
	 * Paquete usuario
	 *
	 * @param paqueteUsuario
	 *            paquete
	 */
	public void setPaqueteUsuario(final PaqueteUsuario paqueteUsuario) {
		this.paqueteUsuario = paqueteUsuario;
	}

	/**
	 * Paquete NPC
	 *
	 * @return PaqueteNPC paquete
	 *
	 */
	public PaqueteNPC getPaqueteNPC() {
		return paqueteNPC;
	}

	/**
	 * Paquete NPC
	 *
	 * @param paqueteNPC
	 *            paquete
	 */
	public void setPaqueteNPC(final PaqueteNPC paqueteNPC) {
		this.paqueteNPC = paqueteNPC;
	}
}
