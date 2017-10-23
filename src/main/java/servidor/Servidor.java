package servidor;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import mensajeria.PaqueteMensaje;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaqueteNPC;
import mensajeria.PaquetePersonaje;
import npc.ControlNPC;

/**
 * Servidor WOME <br/>
 * En esta clase se inicializa el servidor y los threads necesarios para la
 * ejecución del mismo.
 */
public class Servidor extends Thread {

    private static ArrayList<EscuchaCliente> clientesConectados = new ArrayList<>();

    private static Map<Integer, PaqueteMovimiento> ubicacionPersonajes = new HashMap<>();
    private static Map<Integer, PaquetePersonaje> personajesConectados = new HashMap<>();
    private static Map<Integer, PaqueteNPC> npcsCreados = new HashMap<>();

    private static Thread server;
    private static ServerSocket serverSocket;
    private static Conector conexionDB;
    private final int serverPort = 55050;

    private static JTextArea log;

    private static AtencionConexiones atencionConexiones;
    private static AtencionMovimientos atencionMovimientos;

    /**
     * Programa principal.
     *
     * @param args
     *            argumentos
     */
    public static void main(final String[] args) {
        cargarInterfaz();
    }

    /**
     * Carga la interfaz del servidor.
     */
    private static void cargarInterfaz() {
        final int ancho = 700;
        final int alto = 640;
        final int altoLog = 520;
        final int resta = 25;
        final int anchoLog = ancho - resta;
        final int fontSizeMedium = 16;
        final int fontSizeSmall = 13;
        final int aRestar = 70;

        final JFrame ventana = new JFrame("Servidor WOME");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(ancho, alto);
        ventana.setResizable(false);
        ventana.setLocationRelativeTo(null);
        ventana.setLayout(null);
        ventana.setIconImage(Toolkit.getDefaultToolkit().getImage("src/main/java/servidor/server.png"));
        final JLabel titulo = new JLabel("Log del servidor...");
        titulo.setFont(new Font("Courier New", Font.BOLD, fontSizeMedium));

        final int x1 = 10;
        final int y1 = 0;
        final int width1 = 200;
        final int height1 = 30;
        titulo.setBounds(x1, y1, width1, height1);

        ventana.add(titulo);

        log = new JTextArea();
        log.setEditable(false);
        log.setFont(new Font("Times New Roman", Font.PLAIN, fontSizeSmall));
        final JScrollPane scroll = new JScrollPane(log, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        final int x = 10;
        final int y = 40;
        scroll.setBounds(x, y, anchoLog, altoLog);
        ventana.add(scroll);

        final JButton botonIniciar = new JButton();
        final JButton botonDetener = new JButton();
        botonIniciar.setText("Iniciar");

        final int x2 = 220;
        final int y2 = alto - aRestar;
        final int width2 = 100;
        final int height2 = 30;
        botonIniciar.setBounds(x2, y2, width2, height2);
        botonIniciar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                server = new Thread(new Servidor());
                server.start();
                botonIniciar.setEnabled(false);
                botonDetener.setEnabled(true);
            }
        });

        ventana.add(botonIniciar);

        botonDetener.setText("Detener");

        final int x3 = 360;
        final int y3 = alto - aRestar;
        final int width3 = 100;
        final int height3 = 30;
        botonDetener.setBounds(x3, y3, width3, height3);
        botonDetener.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    server.interrupt();
                    atencionConexiones.interrupt();
                    atencionMovimientos.interrupt();
                    for (final EscuchaCliente cliente : clientesConectados) {
                        cliente.getSalida().close();
                        cliente.getEntrada().close();
                        cliente.getSocket().close();
                    }
                    serverSocket.close();
                    log.append("El servidor se ha detenido." + System.lineSeparator());
                } catch (final IOException e1) {
                    log.append("Fallo al intentar detener el servidor." + System.lineSeparator());
                }
                if (conexionDB != null) {
                    conexionDB.close();
                }
                botonDetener.setEnabled(false);
                botonIniciar.setEnabled(true);
            }
        });
        botonDetener.setEnabled(false);
        ventana.add(botonDetener);

        ventana.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        ventana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent evt) {
                if (serverSocket != null) {
                    try {
                        server.interrupt();
                        atencionConexiones.interrupt();
                        atencionMovimientos.interrupt();
                        for (final EscuchaCliente cliente : clientesConectados) {
                            cliente.getSalida().close();
                            cliente.getEntrada().close();
                            cliente.getSocket().close();
                        }
                        serverSocket.close();
                        log.append("El servidor se ha detenido." + System.lineSeparator());
                    } catch (final IOException e) {
                        log.append("Fallo al intentar detener el servidor." + System.lineSeparator());
                        System.exit(1);
                    }
                }
                if (conexionDB != null) {
                    conexionDB.close();
                }
                System.exit(0);
            }
        });

        ventana.setVisible(true);
    }

    /**
     * Ejecuta el thread principal del servidor
     */
    @Override
    public void run() {
        try {

            conexionDB = new Conector();
            conexionDB.connect();

            log.append("Iniciando el servidor..." + System.lineSeparator());
            serverSocket = new ServerSocket(serverPort);
            log.append("Servidor esperando conexiones..." + System.lineSeparator());
            String ipRemota;

            atencionConexiones = new AtencionConexiones();
            atencionMovimientos = new AtencionMovimientos();

            atencionConexiones.start();
            atencionMovimientos.start();

            ControlNPC.generarNPCs();

            while (true) {
                final Socket cliente = serverSocket.accept();
                ipRemota = cliente.getInetAddress().getHostAddress();
                log.append(ipRemota + " se ha conectado" + System.lineSeparator());

                final ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                final ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

                final EscuchaCliente atencion = new EscuchaCliente(ipRemota, cliente, entrada, salida);
                atencion.start();
                clientesConectados.add(atencion);
            }
        } catch (final Exception e) {
            log.append("Fallo la conexión." + System.lineSeparator());
        }
    }

    /**
     * Envio de un mensaje al usuario
     *
     * @param pqm
     *            paquete mensaje
     * @return boolean
     */
    public static boolean mensajeAUsuario(PaqueteMensaje pqm) {

        for (Map.Entry<Integer, PaquetePersonaje> personaje : personajesConectados.entrySet()) {
            if (personaje.getValue().getNombre().equals(pqm.getUserReceptor())) {
                Servidor.log.append(
                        pqm.getUserEmisor() + " envió un mensaje a " + pqm.getUserReceptor() + System.lineSeparator());
                return true;
            }
        }

        Servidor.log.append(pqm.getUserEmisor() + " NO envió un mensaje a " + pqm.getUserReceptor()
                + " ya que este se encuentra desconectado" + System.lineSeparator());
        return false;

        /*
         * Código viejo boolean result = true; boolean noEncontro = true; for
         * (Map.Entry<Integer, PaquetePersonaje> personaje :
         * personajesConectados.entrySet()) { if(noEncontro &&
         * (!personaje.getValue().getNombre().equals(pqm.getUserReceptor()))) { result =
         * false; } else { result = true; noEncontro = false; } } // Si existe inicio
         * sesion if (result) { Servidor.log.append(pqm.getUserEmisor() +
         * " envió mensaje a " + pqm.getUserReceptor() + System.lineSeparator()); return
         * true; } else { // Si no existe informo y devuelvo false
         * Servidor.log.append("El mensaje para " + pqm.getUserReceptor() +
         * " no se envió, ya que se encuentra desconectado." + System.lineSeparator());
         * return false; }
         */
    }

    /**
     * Envio de un mensaje a todos
     *
     * @param contador
     *            contador
     * @return bool
     */
    public static boolean mensajeAAll(PaqueteMensaje pqm, int contador) {
        int personajesConectadosQty = personajesConectados.size();

        if (personajesConectadosQty == contador) {
            Servidor.log.append(
                    pqm.getUserEmisor() + " ha enviado un mensaje a todos los usuarios" + System.lineSeparator());
            return true;
        } else if (personajesConectadosQty > contador) {
            Servidor.log.append("Se ha/n conectado " + (personajesConectadosQty - contador) + " nuevo/s usuario/s. "
                    + pqm.getUserEmisor() + " ha enviado un mensaje a todos los restantes" + System.lineSeparator());
            return false;
        } else {
            Servidor.log.append("Se ha/n desconectado " + (contador - personajesConectadosQty) + " usuario/s. "
                    + pqm.getUserEmisor() + " ha enviado un mensaje a todos los restantes" + System.lineSeparator());
            return false;
        }

        /*
         * Codigo viejo boolean result = true; if(personajesConectados.size() !=
         * contador+1) { result = false; } // Si existe inicio sesion if (result) {
         * Servidor.log.append("Se ha enviado un mensaje a todos los usuarios" +
         * System.lineSeparator()); return true; } else { // Si no existe informo y
         * devuelvo false Servidor.log.
         * append("Uno o más de todos los usuarios se ha desconectado, se ha mandado el mensaje a los demas."
         * + System.lineSeparator()); return false; }
         */
    }

    /**
     * Getter de clientes conectados
     *
     * @return ArrayList<EscuchaCliente>
     */
    public static ArrayList<EscuchaCliente> getClientesConectados() {
        return clientesConectados;
    }

    /**
     * Getter de ubicacion de personajes
     *
     * @return Map<Integer, PaqueteMovimiento>
     */
    public static Map<Integer, PaqueteMovimiento> getUbicacionPersonajes() {
        return ubicacionPersonajes;
    }

    /**
     * Getter de personajes conectados
     *
     * @return Map<Integer, PaquetePersonaje>
     */
    public static Map<Integer, PaquetePersonaje> getPersonajesConectados() {
        return personajesConectados;
    }

    /**
     * Getter de npcs creados
     *
     * @return Map<Integer, PaqueteNPC>
     */
    public static Map<Integer, PaqueteNPC> getNPsCreados() {
        return npcsCreados;
    }

    /**
     * Getter de conector DB
     *
     * @return Conector
     */
    public static Conector getConector() {
        return conexionDB;
    }

    /**
     * Thread atencion de conexiones
     *
     * @return AtencionConexiones thread
     */
    public static AtencionConexiones getAtencionConexiones() {
        return atencionConexiones;
    }

    /**
     * Thread atencion de movimientos
     *
     * @return AtencionMovimientos thread
     */
    public static AtencionMovimientos getAtencionMovimientos() {
        return atencionMovimientos;
    }

    /**
     * Getter del log en pantalla
     *
     * @return JTextArea
     */
    public static JTextArea getLog() {
        return log;
    }

}
