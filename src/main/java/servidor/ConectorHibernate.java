package servidor;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import dominio.Inventario;
import dominio.Item;
import dominio.Mochila;
import dominio.NonPlayableCharacter;
import mensajeria.PaqueteNPC;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

/**
 * Clase encargada de la comunicacion con hibernate
 */
public class ConectorHibernate {

    private SessionFactory factory;

    /**
     * Efectua la conexion
     */
    public void connect() {
        try {
            Servidor.getLog().append("Estableciendo conexión con la base de datos..." + System.lineSeparator());

            // Evitamos logs innecesarios.
            java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);

            final Configuration cfg = new Configuration();
            cfg.configure("hibernate.cfg.xml");
            this.setFactory(cfg.buildSessionFactory());

            Servidor.getLog().append("Conexión con la base de datos establecida con éxito." + System.lineSeparator());
        } catch (final Exception ex) {
            final String msg = ex.getMessage();
            Servidor.getLog().append("Fallo al conectar con la base de datos. " + msg + System.lineSeparator());
        }
    }

    /**
     * Cierra la conexion
     */
    public void close() {
        try {
            this.getFactory().close();
        } catch (final Exception ex) {
            Servidor.getLog()
                    .append("Error al intentar cerrar la conexión con la base de datos." + System.lineSeparator());
            Logger.getLogger(ConectorHibernate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get de personaje
     *
     * @param pU
     *            usuario a buscar
     * @return PaquetePersonaje personaje obtenido
     * @throws IOException
     *             error
     */
    public PaquetePersonaje getPersonaje(final PaqueteUsuario pU) throws IOException {
        PaquetePersonaje paquetePersonaje = null;
        Session session = null;
        int i = 2;
        int j = 0;

        PaqueteUsuario paqueteUsuario = null;
        try {
            paqueteUsuario = getUsuario(pU.getUsername());

            session = getFactory().openSession();

            // Personaje
            final CriteriaBuilder cbPersonaje = session.getCriteriaBuilder();
            final CriteriaQuery<PaquetePersonaje> cqPersonaje = cbPersonaje.createQuery(PaquetePersonaje.class);
            final Root<PaquetePersonaje> rpPersonaje = cqPersonaje.from(PaquetePersonaje.class);
            cqPersonaje.select(rpPersonaje).where(cbPersonaje.equal(rpPersonaje.get("id"), paqueteUsuario.getIdPj()));
            paquetePersonaje = session.createQuery(cqPersonaje).getSingleResult();

            // Mochila
            final CriteriaBuilder cbMochila = session.getCriteriaBuilder();
            final CriteriaQuery<Mochila> cqMochila = cbMochila.createQuery(Mochila.class);
            final Root<Mochila> rpMochila = cqMochila.from(Mochila.class);
            cqMochila.select(rpMochila).where(cbMochila.equal(rpMochila.get("idMochila"), paqueteUsuario.getIdPj()));
            final Mochila mochila = session.createQuery(cqMochila).getSingleResult();

            // Items
            final CriteriaBuilder cbItem = session.getCriteriaBuilder();
            final CriteriaQuery<Item> cqItem = cbItem.createQuery(Item.class);
            final Root<Item> rpItem = cqItem.from(Item.class);

            final int maxItems = 9;
            while (j <= maxItems) {
                if (mochila.getById(i) != -1) {
                    cqItem.select(rpItem).where(cbItem.equal(rpItem.get("idItem"), mochila.getById(i)));
                    final Item item = session.createQuery(cqItem).getSingleResult();

                    if (item != null) {
                        paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getWearLocation(),
                                item.getBonusSalud(), item.getBonusEnergia(), item.getBonusFuerza(),
                                item.getBonusDestreza(), item.getBonusInteligencia(), item.getFoto(),
                                item.getFotoEquipado());
                    }
                }
                i++;
                j++;
            }
        } catch (final Exception e) {
            Servidor.getLog().append(
                    "Fallo al intentar obtener el personaje " + paqueteUsuario.getIdPj() + System.lineSeparator());
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return paquetePersonaje;
    }

    /**
     * Login
     *
     * @param user
     *            usuario a loguear
     * @return boolean resultado
     */
    public boolean loguearUsuario(final PaqueteUsuario user) {
        boolean resultado = false;
        Session session = null;

        try {
            session = getFactory().openSession();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<PaqueteUsuario> cq = cb.createQuery(PaqueteUsuario.class);

            final Root<PaqueteUsuario> rp = cq.from(PaqueteUsuario.class);
            cq.select(rp).where(cb.equal(rp.get("username"), user.getUsername()),
                    cb.equal(rp.get("password"), user.getPassword()));

            try {
                resultado = session.createQuery(cq).getSingleResult() != null;
            } catch (final NoResultException nre) {
                // Ignore this because as per your logic this is ok!
                Servidor.getLog().append(System.lineSeparator());
            }

        } catch (final Exception e) {
            Servidor.getLog()
                    .append("El usuario " + user.getUsername() + " fallo al iniciar sesión." + System.lineSeparator());
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    /**
     * Registrar usuario
     *
     * @param user
     *            paquete usuario
     * @return boolean resultado
     */
    public boolean registrarUsuario(final PaqueteUsuario user) {
        boolean resultado = false;
        Session session = null;
        Transaction tx = null;

        try {
            session = getFactory().openSession();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<PaqueteUsuario> cq = cb.createQuery(PaqueteUsuario.class);

            final Root<PaqueteUsuario> rp = cq.from(PaqueteUsuario.class);
            cq.select(rp).where(cb.equal(rp.get("username"), user.getUsername()));

            boolean yaExiste = false;
            try {
                yaExiste = session.createQuery(cq).getSingleResult() != null;
            } catch (final NoResultException nre) {
                Servidor.getLog().append(System.lineSeparator());
            }

            if (!yaExiste) {
                tx = session.beginTransaction();
                session.save(user);
                tx.commit();

                resultado = true;
            } else {
                Servidor.getLog().append(
                        "El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());
            }
        } catch (final Exception e) {
            Servidor.getLog()
                    .append("Eror al intentar registrar el usuario " + user.getUsername() + System.lineSeparator());
            System.err.println(e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    /**
     * Registrar personaje
     *
     * @param paquetePersonaje
     *            personaje a registrar
     * @param paqueteUsuario
     *            usuario del personaje a registrar
     * @return boolean resultado
     */
    public boolean registrarPersonaje(final PaquetePersonaje paquetePersonaje, final PaqueteUsuario paqueteUsuario) {
        Session session = null;
        boolean resultado = false;

        try {
            session = getFactory().openSession();

            // Personaje
            session.save(paquetePersonaje);

            // Updateo el usuario
            paqueteUsuario.setIdPj(paquetePersonaje.getId());
            session.update(paqueteUsuario);

            // Inventario
            final Inventario inventario = new Inventario(paquetePersonaje.getId());
            session.save(inventario);

            // Mochila
            final Mochila mochila = new Mochila(paquetePersonaje.getId());
            session.save(mochila);

            // Actualizo con los datos
            paquetePersonaje.setIdInventario(inventario.getIdInventario());
            paquetePersonaje.setIdMochila(mochila.getIdMochila());
            session.update(mochila);

            resultado = true;
        } catch (final Exception e) {
            Servidor.getLog().append(
                    "Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());

            resultado = false;
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return resultado;
    }

    /**
     * Actualizar personaje
     *
     * @param paquetePersonaje
     *            paquete de personaje a actualizar
     */
    public void actualizarPersonaje(final PaquetePersonaje paquetePersonaje) {
        Session session = null;
        try {
            session = getFactory().openSession();

            // Personaje
            session.update(paquetePersonaje);

            // Items
            paquetePersonaje.eliminarItems();

            final CriteriaBuilder cbMochila = session.getCriteriaBuilder();
            final CriteriaQuery<Mochila> cqMochila = cbMochila.createQuery(Mochila.class);
            final Root<Mochila> rpMochila = cqMochila.from(Mochila.class);
            cqMochila.select(rpMochila).where(cbMochila.equal(rpMochila.get("idMochila"), paquetePersonaje.getId()));

            final Mochila mochila = session.createQuery(cqMochila).getSingleResult();
            if (mochila != null) {
                final CriteriaBuilder cbItem = session.getCriteriaBuilder();
                final CriteriaQuery<Item> cqItem = cbItem.createQuery(Item.class);
                final Root<Item> rpItem = cqItem.from(Item.class);

                int i = 2;
                int j = 1;

                final int maxItems = 9;
                while (j <= maxItems) {
                    if (mochila.getById(i) != -1) {
                        cqItem.select(rpItem).where(cbItem.equal(rpItem.get("idItem"), mochila.getById(i)));
                        final Item item = session.createQuery(cqItem).getSingleResult();

                        if (item != null) {
                            paquetePersonaje.anadirItem(item.getIdItem(), item.getNombre(), item.getWearLocation(),
                                    item.getBonusSalud(), item.getBonusEnergia(), item.getBonusFuerza(),
                                    item.getBonusDestreza(), item.getBonusInteligencia(), item.getFoto(),
                                    item.getFotoEquipado());
                        }
                    }
                    i++;
                    j++;
                }
                Servidor.getLog().append("El personaje " + paquetePersonaje.getNombre()
                        + " se ha actualizado con éxito." + System.lineSeparator());
            }

        } catch (final Exception e) {
            Servidor.getLog().append(
                    "Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Actualizar nuevo nivel
     *
     * @param paquetePersonaje
     *            personaje a actualizar
     */
    public void actualizarPersonajeSubioNivel(final PaquetePersonaje paquetePersonaje) {
        Session session = null;
        try {
            session = getFactory().openSession();

            // Personaje
            session.update(paquetePersonaje);

            Servidor.getLog().append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
                    + System.lineSeparator());

        } catch (final Exception e) {
            Servidor.getLog().append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
                    + System.lineSeparator());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Obtiene la informacion del usuario
     *
     * @param usuario
     *            nombre
     * @return PaqueteUsuario usuario
     */
    public PaqueteUsuario getUsuario(final String usuario) {
        PaqueteUsuario paqueteUsuario = null;
        Session session = null;
        try {
            session = getFactory().openSession();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<PaqueteUsuario> cq = cb.createQuery(PaqueteUsuario.class);

            final Root<PaqueteUsuario> rp = cq.from(PaqueteUsuario.class);
            cq.select(rp).where(cb.equal(rp.get("username"), usuario));
            paqueteUsuario = session.createQuery(cq).getSingleResult();

        } catch (final Exception e) {
            Servidor.getLog().append("Fallo al intentar obtener el personaje " + usuario + System.lineSeparator());
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return paqueteUsuario;
    }

    /**
     * Actualizar inventario
     *
     * @param paquetePersonaje
     *            paquete
     */
    public void actualizarInventario(final PaquetePersonaje paquetePersonaje) {
        Session session = null;
        try {
            session = getFactory().openSession();

            final Mochila mochila = new Mochila(paquetePersonaje.getId());

            int i = 0;
            while (i < paquetePersonaje.getCantItems()) {
                mochila.setInt(i + 1, paquetePersonaje.getItemID(i));
                i++;
            }

            // Inventario
            session.update(mochila);

            Servidor.getLog().append("El personaje " + paquetePersonaje.getNombre()
                    + " ha actualizado con éxito su inventario." + System.lineSeparator());

        } catch (final Exception e) {
            Servidor.getLog().append("Fallo al intentar actualizar el inventario del personaje "
                    + paquetePersonaje.getNombre() + System.lineSeparator());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Actualizar inventario
     *
     * @param idPersonaje
     *            id de personaje
     */
    public void actualizarInventario(final int idPersonaje) {
        actualizarInventario(Servidor.getPersonajesConectados().get(idPersonaje));
    }

    /**
     * Obtiene los NPC
     *
     * @return List<PaqueteNPC> npcs
     */
    public List<PaqueteNPC> getNPCs() {
        List<PaqueteNPC> npcs = null;

        Session session = null;
        try {
            session = getFactory().openSession();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<PaqueteNPC> cq = cb.createQuery(PaqueteNPC.class);

            final Root<PaqueteNPC> rp = cq.from(PaqueteNPC.class);
            cq.select(rp);

            npcs = session.createQuery(cq).getResultList();

            for (final PaqueteNPC npc : npcs) {
                final NonPlayableCharacter npChar = new NonPlayableCharacter(npc.getNombre(), npc.getNivel(),
                        npc.getDificultad());

                npc.setFuerza(npChar.getFuerza());
                npc.setSaludTope(npChar.getSaludTope());
                npc.setXYRandom();
            }
        } catch (final Exception e) {
            Servidor.getLog().append("Fallo al intentar los NPC " + System.lineSeparator());
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return npcs;
    }

    /**
     * Obtiene el NPC por ID
     *
     * @param id
     *            de NPC
     *
     * @return PaqueteNPC npc
     */
    public PaqueteNPC getNPC(final int id) {
        PaqueteNPC npc = null;

        Session session = null;
        try {
            session = getFactory().openSession();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<PaqueteNPC> cq = cb.createQuery(PaqueteNPC.class);

            final Root<PaqueteNPC> rp = cq.from(PaqueteNPC.class);
            cq.select(rp).where(cb.equal(rp.get("id"), id));
            npc = session.createQuery(cq).getSingleResult();

            final NonPlayableCharacter npChar = new NonPlayableCharacter(npc.getNombre(), npc.getNivel(),
                    npc.getDificultad());

            npc.setFuerza(npChar.getFuerza());
            npc.setSaludTope(npChar.getSaludTope());
            npc.setXYRandom();
        } catch (final Exception e) {
            Servidor.getLog().append("Fallo al intentar los el NPC para el ID: " + id + System.lineSeparator());
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return npc;
    }

    /**
     * Obtiene el NPC por nombre
     *
     * @param nombre
     *            de NPC
     *
     * @return PaqueteNPC npc
     */
    public PaqueteNPC getNPC(final String nombre) {
        PaqueteNPC npc = null;

        Session session = null;
        try {
            session = getFactory().openSession();

            final CriteriaBuilder cb = session.getCriteriaBuilder();
            final CriteriaQuery<PaqueteNPC> cq = cb.createQuery(PaqueteNPC.class);

            final Root<PaqueteNPC> rp = cq.from(PaqueteNPC.class);
            cq.select(rp).where(cb.equal(rp.get("nombre"), nombre));
            npc = session.createQuery(cq).getSingleResult();

            final NonPlayableCharacter npChar = new NonPlayableCharacter(npc.getNombre(), npc.getNivel(),
                    npc.getDificultad());

            npc.setFuerza(npChar.getFuerza());
            npc.setSaludTope(npChar.getSaludTope());
            npc.setXYRandom();

        } catch (final Exception e) {
            Servidor.getLog().append("Fallo al intentar los el NPC para el nombre: " + nombre + System.lineSeparator());
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return npc;
    }

    /**
     * @return the factory
     */
    SessionFactory getFactory() {
        return factory;
    }

    /**
     * @param factory
     *            the factory to set
     */
    void setFactory(final SessionFactory factory) {
        this.factory = factory;
    }
}
