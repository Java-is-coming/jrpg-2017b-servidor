package servidor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

/**
 *
 * Clase usada para la comunicación con la DB
 *
 */
public class Conector {

    private final String url = "primeraBase.bd";
    private Connection connect;

    /**
     * Conecta con la DB
     */
    public void connect() {
        try {
            Servidor.getLog().append("Estableciendo conexión con la base de datos..." + System.lineSeparator());
            connect = DriverManager.getConnection("jdbc:sqlite:" + url);
            Servidor.getLog().append("Conexión con la base de datos establecida con éxito." + System.lineSeparator());
        } catch (final SQLException ex) {
            Servidor.getLog()
                    .append("Fallo al conectar con la base de datos. " + ex.getMessage() + System.lineSeparator());
        }
    }

    /**
     * Cierra conexion
     */
    public void close() {
        try {
            connect.close();
        } catch (final SQLException ex) {
            Servidor.getLog()
                    .append("Error al intentar cerrar la conexión con la base de datos." + System.lineSeparator());
            Logger.getLogger(Conector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Regitra un usuario
     *
     * @param user
     *            usuario a registrar
     * @return bool
     */
    public boolean registrarUsuario(final PaqueteUsuario user) {
        ResultSet result = null;
        try {
            final PreparedStatement st1 = connect.prepareStatement("SELECT * FROM registro WHERE usuario= ? ");
            st1.setString(1, user.getUsername());
            result = st1.executeQuery();

            if (!result.next()) {

                final PreparedStatement st = connect
                        .prepareStatement("INSERT INTO registro (usuario, password, idPersonaje) VALUES (?,?,?)");
                st.setString(1, user.getUsername());
                st.setString(2, user.getPassword());
                st.setInt(3, user.getIdPj());
                st.execute();
                Servidor.getLog()
                        .append("El usuario " + user.getUsername() + " se ha registrado." + System.lineSeparator());
                return true;
            } else {
                Servidor.getLog().append(
                        "El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());
                return false;
            }
        } catch (final SQLException ex) {
            Servidor.getLog()
                    .append("Eror al intentar registrar el usuario " + user.getUsername() + System.lineSeparator());
            System.err.println(ex.getMessage());
            return false;
        }

    }

    /**
     * Registra un personaje
     *
     * @param paquetePersonaje
     *            personaje a registrar
     * @param paqueteUsuario
     *            usuario al que pertenece
     * @return bool
     */
    public boolean registrarPersonaje(final PaquetePersonaje paquetePersonaje, final PaqueteUsuario paqueteUsuario) {

        try {

            // Registro al personaje en la base de datos
            final PreparedStatement stRegistrarPersonaje = connect.prepareStatement(
                    "INSERT INTO personaje (idInventario, idMochila,casta,raza,fuerza,destreza,inteligencia,saludTope,energiaTope,nombre,experiencia,nivel,idAlianza) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    PreparedStatement.RETURN_GENERATED_KEYS);
            stRegistrarPersonaje.setInt(1, -1);
            stRegistrarPersonaje.setInt(2, -1);
            stRegistrarPersonaje.setString(3, paquetePersonaje.getCasta());
            stRegistrarPersonaje.setString(4, paquetePersonaje.getRaza());
            stRegistrarPersonaje.setInt(5, paquetePersonaje.getFuerza());
            stRegistrarPersonaje.setInt(6, paquetePersonaje.getDestreza());
            stRegistrarPersonaje.setInt(7, paquetePersonaje.getInteligencia());
            stRegistrarPersonaje.setInt(8, paquetePersonaje.getSaludTope());
            stRegistrarPersonaje.setInt(9, paquetePersonaje.getEnergiaTope());
            stRegistrarPersonaje.setString(10, paquetePersonaje.getNombre());
            stRegistrarPersonaje.setInt(11, 0);
            stRegistrarPersonaje.setInt(12, 1);
            stRegistrarPersonaje.setInt(13, -1);
            stRegistrarPersonaje.execute();

            // Recupero la última key generada
            final ResultSet rs = stRegistrarPersonaje.getGeneratedKeys();
            if (rs != null && rs.next()) {

                // Obtengo el id
                final int idPersonaje = rs.getInt(1);

                // Le asigno el id al paquete personaje que voy a devolver
                paquetePersonaje.setId(idPersonaje);

                // Le asigno el personaje al usuario
                final PreparedStatement stAsignarPersonaje = connect
                        .prepareStatement("UPDATE registro SET idPersonaje=? WHERE usuario=? AND password=?");
                stAsignarPersonaje.setInt(1, idPersonaje);
                stAsignarPersonaje.setString(2, paqueteUsuario.getUsername());
                stAsignarPersonaje.setString(3, paqueteUsuario.getPassword());
                stAsignarPersonaje.execute();

                // Por ultimo registro el inventario y la mochila
                if (this.registrarInventarioMochila(idPersonaje)) {
                    Servidor.getLog().append("El usuario " + paqueteUsuario.getUsername() + " ha creado el personaje "
                            + paquetePersonaje.getId() + System.lineSeparator());
                    return true;
                } else {
                    Servidor.getLog()
                            .append("Error al registrar la mochila y el inventario del usuario "
                                    + paqueteUsuario.getUsername() + " con el personaje" + paquetePersonaje.getId()
                                    + System.lineSeparator());
                    return false;
                }
            }
            return false;

        } catch (final SQLException e) {
            Servidor.getLog().append(
                    "Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());
            return false;
        }

    }

    /**
     * Registra inventario
     *
     * @param idInventarioMochila
     *            id inventario
     * @return bool
     */
    public boolean registrarInventarioMochila(final int idInventarioMochila) {
        try {
            // Preparo la consulta para el registro el inventario en la base de
            // datos
            final PreparedStatement stRegistrarInventario = connect.prepareStatement(
                    "INSERT INTO inventario(idInventario,manos1,manos2,pie,cabeza,pecho,accesorio) VALUES (?,-1,-1,-1,-1,-1,-1)");
            stRegistrarInventario.setInt(1, idInventarioMochila);

            // Preparo la consulta para el registro la mochila en la base de
            // datos
            final PreparedStatement stRegistrarMochila = connect.prepareStatement(
                    "INSERT INTO mochila(idMochila,item1,item2,item3,item4,item5,item6,item7,item8,item9,item10,item11,item12,item13,item14,item15,item16,item17,item18,item19,item20) VALUES(?,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)");
            stRegistrarMochila.setInt(1, idInventarioMochila);

            // Registro inventario y mochila
            stRegistrarInventario.execute();
            stRegistrarMochila.execute();

            // Le asigno el inventario y la mochila al personaje
            final PreparedStatement stAsignarPersonaje = connect
                    .prepareStatement("UPDATE personaje SET idInventario=?, idMochila=? WHERE idPersonaje=?");
            stAsignarPersonaje.setInt(1, idInventarioMochila);
            stAsignarPersonaje.setInt(2, idInventarioMochila);
            stAsignarPersonaje.setInt(3, idInventarioMochila);
            stAsignarPersonaje.execute();

            Servidor.getLog()
                    .append("Se ha registrado el inventario de " + idInventarioMochila + System.lineSeparator());
            return true;

        } catch (final SQLException e) {
            Servidor.getLog()
                    .append("Error al registrar el inventario de " + idInventarioMochila + System.lineSeparator());
            return false;
        }
    }

    /**
     * Busca información de logueo de usuario y determina si es correcta la
     * ingresada.
     *
     * @param user
     *            usuario a loguear
     * @return bool
     */
    public boolean loguearUsuario(final PaqueteUsuario user) {
        ResultSet result = null;
        try {
            // Busco usuario y contraseña
            final PreparedStatement st = connect
                    .prepareStatement("SELECT * FROM registro WHERE usuario = ? AND password = ? ");
            st.setString(1, user.getUsername());
            st.setString(2, user.getPassword());
            result = st.executeQuery();

            // Si existe inicio sesion
            if (result.next()) {
                Servidor.getLog()
                        .append("El usuario " + user.getUsername() + " ha iniciado sesión." + System.lineSeparator());
                return true;
            }

            // Si no existe informo y devuelvo false
            Servidor.getLog().append("El usuario " + user.getUsername()
                    + " ha realizado un intento fallido de inicio de sesión." + System.lineSeparator());
            return false;

        } catch (final SQLException e) {
            Servidor.getLog()
                    .append("El usuario " + user.getUsername() + " fallo al iniciar sesión." + System.lineSeparator());
            return false;
        }

    }

    /**
     * Actualiza personaje en DB
     *
     * @param paquetePersonaje
     *            personaje a actualizar
     */
    public void actualizarPersonaje(final PaquetePersonaje paquetePersonaje) {
        try {
            int i = 2;
            int j = 1;
            final PreparedStatement stActualizarPersonaje = connect.prepareStatement(
                    "UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=? "
                            + "  WHERE idPersonaje=?");

            stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
            stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
            stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
            stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
            stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
            stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
            stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
            stActualizarPersonaje.setInt(8, paquetePersonaje.getId());
            stActualizarPersonaje.executeUpdate();

            final PreparedStatement stDameItemsID = connect
                    .prepareStatement("SELECT * FROM mochila WHERE idMochila = ?");
            stDameItemsID.setInt(1, paquetePersonaje.getId());
            final ResultSet resultadoItemsID = stDameItemsID.executeQuery();
            final PreparedStatement stDatosItem = connect.prepareStatement("SELECT * FROM item WHERE idItem = ?");
            ResultSet resultadoDatoItem = null;
            paquetePersonaje.eliminarItems();

            while (j <= 9) {
                if (resultadoItemsID.getInt(i) != -1) {
                    stDatosItem.setInt(1, resultadoItemsID.getInt(i));
                    resultadoDatoItem = stDatosItem.executeQuery();

                    paquetePersonaje.anadirItem(resultadoDatoItem.getInt("idItem"),
                            resultadoDatoItem.getString("nombre"), resultadoDatoItem.getInt("wereable"),
                            resultadoDatoItem.getInt("bonusSalud"), resultadoDatoItem.getInt("bonusEnergia"),
                            resultadoDatoItem.getInt("bonusFuerza"), resultadoDatoItem.getInt("bonusDestreza"),
                            resultadoDatoItem.getInt("bonusInteligencia"), resultadoDatoItem.getString("foto"),
                            resultadoDatoItem.getString("fotoEquipado"));
                }
                i++;
                j++;
            }
            Servidor.getLog().append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
                    + System.lineSeparator());
        } catch (final SQLException e) {
            Servidor.getLog().append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
                    + System.lineSeparator());
        }

    }

    /**
     * Obtiene info de personaje
     *
     * @param user
     *            usuario correspondiente
     * @return paquetePersonaje
     */
    public PaquetePersonaje getPersonaje(final PaqueteUsuario user) {
        ResultSet result = null;
        ResultSet resultadoItemsID = null;
        ResultSet resultadoDatoItem = null;
        PaquetePersonaje personaje = null;
        int i = 2;
        int j = 0;
        try {
            // Selecciono el personaje de ese usuario
            final PreparedStatement st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
            st.setString(1, user.getUsername());
            result = st.executeQuery();

            // Obtengo el id
            final int idPersonaje = result.getInt("idPersonaje");

            // Selecciono los datos del personaje
            final PreparedStatement stSeleccionarPersonaje = connect
                    .prepareStatement("SELECT * FROM personaje WHERE idPersonaje = ?");
            stSeleccionarPersonaje.setInt(1, idPersonaje);
            result = stSeleccionarPersonaje.executeQuery();
            // Traigo los id de los items correspondientes a mi personaje
            final PreparedStatement stDameItemsID = connect
                    .prepareStatement("SELECT * FROM mochila WHERE idMochila = ?");
            stDameItemsID.setInt(1, idPersonaje);
            resultadoItemsID = stDameItemsID.executeQuery();
            // Traigo los datos del item
            final PreparedStatement stDatosItem = connect.prepareStatement("SELECT * FROM item WHERE idItem = ?");

            // Obtengo los atributos del personaje
            personaje = new PaquetePersonaje();
            personaje.setId(idPersonaje);
            personaje.setRaza(result.getString("raza"));
            personaje.setCasta(result.getString("casta"));
            personaje.setFuerza(result.getInt("fuerza"));
            personaje.setInteligencia(result.getInt("inteligencia"));
            personaje.setDestreza(result.getInt("destreza"));
            personaje.setEnergiaTope(result.getInt("energiaTope"));
            personaje.setSaludTope(result.getInt("saludTope"));
            personaje.setNombre(result.getString("nombre"));
            personaje.setExperiencia(result.getInt("experiencia"));
            personaje.setNivel(result.getInt("nivel"));

            while (j <= 9) {
                if (resultadoItemsID.getInt(i) != -1) {
                    stDatosItem.setInt(1, resultadoItemsID.getInt(i));
                    resultadoDatoItem = stDatosItem.executeQuery();
                    personaje.anadirItem(resultadoDatoItem.getInt("idItem"), resultadoDatoItem.getString("nombre"),
                            resultadoDatoItem.getInt("wereable"), resultadoDatoItem.getInt("bonusSalud"),
                            resultadoDatoItem.getInt("bonusEnergia"), resultadoDatoItem.getInt("bonusFuerza"),
                            resultadoDatoItem.getInt("bonusDestreza"), resultadoDatoItem.getInt("bonusInteligencia"),
                            resultadoDatoItem.getString("foto"), resultadoDatoItem.getString("fotoEquipado"));
                }
                i++;
                j++;
            }

        } catch (final SQLException | IOException ex) {
            Servidor.getLog()
                    .append("Fallo al intentar recuperar el personaje " + user.getUsername() + System.lineSeparator());
            Servidor.getLog().append(ex.getMessage() + System.lineSeparator());
        }

        return personaje;

    }

    /**
     * Obtiene info de un usuario
     *
     * @param usuario
     *            nombre de usuario
     * @return paqueteusuario
     */
    public PaqueteUsuario getUsuario(final String usuario) {
        ResultSet result = null;
        PreparedStatement st;

        try {
            st = connect.prepareStatement("SELECT * FROM registro WHERE usuario = ?");
            st.setString(1, usuario);
            result = st.executeQuery();

            final String password = result.getString("password");
            final int idPersonaje = result.getInt("idPersonaje");

            final PaqueteUsuario paqueteUsuario = new PaqueteUsuario();
            paqueteUsuario.setUsername(usuario);
            paqueteUsuario.setPassword(password);
            paqueteUsuario.setIdPj(idPersonaje);

            return paqueteUsuario;
        } catch (final SQLException e) {
            Servidor.getLog().append("Fallo al intentar recuperar el usuario " + usuario + System.lineSeparator());
            Servidor.getLog().append(e.getMessage() + System.lineSeparator());
        }

        return new PaqueteUsuario();
    }

    /**
     * Actualiza inventario de un personaje
     *
     * @param paquetePersonaje
     *            personaje a actualizar
     */
    public void actualizarInventario(final PaquetePersonaje paquetePersonaje) {
        int i = 0;
        PreparedStatement stActualizarMochila;
        try {
            stActualizarMochila = connect.prepareStatement(
                    "UPDATE mochila SET item1=? ,item2=? ,item3=? ,item4=? ,item5=? ,item6=? ,item7=? ,item8=? ,item9=? "
                            + ",item10=? ,item11=? ,item12=? ,item13=? ,item14=? ,item15=? ,item16=? ,item17=? ,item18=? ,item19=? ,item20=? WHERE idMochila=?");
            while (i < paquetePersonaje.getCantItems()) {
                stActualizarMochila.setInt(i + 1, paquetePersonaje.getItemID(i));
                i++;
            }
            for (int j = paquetePersonaje.getCantItems(); j < 20; j++) {
                stActualizarMochila.setInt(j + 1, -1);
            }
            stActualizarMochila.setInt(21, paquetePersonaje.getId());
            stActualizarMochila.executeUpdate();

        } catch (final SQLException e) {
            Servidor.getLog().append("Fallo al intentar actualizar inventario de  " + paquetePersonaje.getNombre()
                    + System.lineSeparator());
        }
    }

    /**
     * Actualiza inventario de un personaje
     *
     * @param idPersonaje
     *            personaje a actualizar
     */
    public void actualizarInventario(final int idPersonaje) {
        int i = 0;
        final PaquetePersonaje paquetePersonaje = Servidor.getPersonajesConectados().get(idPersonaje);
        PreparedStatement stActualizarMochila;
        try {
            stActualizarMochila = connect.prepareStatement(
                    "UPDATE mochila SET item1=? ,item2=? ,item3=? ,item4=? ,item5=? ,item6=? ,item7=? ,item8=? ,item9=? "
                            + ",item10=? ,item11=? ,item12=? ,item13=? ,item14=? ,item15=? ,item16=? ,item17=? ,item18=? ,item19=? ,item20=? WHERE idMochila=?");
            while (i < paquetePersonaje.getCantItems()) {
                stActualizarMochila.setInt(i + 1, paquetePersonaje.getItemID(i));
                i++;
            }
            if (paquetePersonaje.getCantItems() < 9) {
                int itemGanado = new Random().nextInt(29);
                itemGanado += 1;
                stActualizarMochila.setInt(paquetePersonaje.getCantItems() + 1, itemGanado);
                for (int j = paquetePersonaje.getCantItems() + 2; j < 20; j++) {
                    stActualizarMochila.setInt(j, -1);
                }
            } else {
                for (int j = paquetePersonaje.getCantItems() + 1; j < 20; j++) {
                    stActualizarMochila.setInt(j, -1);
                }
            }
            stActualizarMochila.setInt(21, paquetePersonaje.getId());
            stActualizarMochila.executeUpdate();

        } catch (final SQLException e) {
            Servidor.getLog().append("Falló al intentar actualizar inventario de" + idPersonaje + "\n");
        }
    }

    /**
     * Sube a un personaje de nivel
     *
     * @param paquetePersonaje
     *            personaje a subir
     */
    public void actualizarPersonajeSubioNivel(final PaquetePersonaje paquetePersonaje) {
        try {
            final PreparedStatement stActualizarPersonaje = connect.prepareStatement(
                    "UPDATE personaje SET fuerza=?, destreza=?, inteligencia=?, saludTope=?, energiaTope=?, experiencia=?, nivel=? "
                            + "  WHERE idPersonaje=?");

            stActualizarPersonaje.setInt(1, paquetePersonaje.getFuerza());
            stActualizarPersonaje.setInt(2, paquetePersonaje.getDestreza());
            stActualizarPersonaje.setInt(3, paquetePersonaje.getInteligencia());
            stActualizarPersonaje.setInt(4, paquetePersonaje.getSaludTope());
            stActualizarPersonaje.setInt(5, paquetePersonaje.getEnergiaTope());
            stActualizarPersonaje.setInt(6, paquetePersonaje.getExperiencia());
            stActualizarPersonaje.setInt(7, paquetePersonaje.getNivel());
            stActualizarPersonaje.setInt(8, paquetePersonaje.getId());

            stActualizarPersonaje.executeUpdate();

            Servidor.getLog().append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
                    + System.lineSeparator());

        } catch (final SQLException e) {
            Servidor.getLog().append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
                    + System.lineSeparator());
        }
    }
}
