package servidor;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
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
import org.hibernate.criterion.Projections;

import dominio.Inventario;
import dominio.Item;
import dominio.Mochila;
import mensajeria.PaquetePersonaje;
import mensajeria.PaqueteUsuario;

public class ConectorHibernate {

	private String url = "primeraBase.bd";
	SessionFactory factory;

	public void connect() {
		try {
			Servidor.getLog().append("Estableciendo conexión con la base de datos..." + System.lineSeparator());

			Configuration cfg = new Configuration();
			cfg.configure("hibernate.cfg.xml");
			this.factory = cfg.buildSessionFactory();

			Servidor.getLog().append("Conexión con la base de datos establecida con éxito." + System.lineSeparator());
		} catch (Exception ex) {
			Servidor.getLog().append("Fallo al intentar establecer la conexión con la base de datos. " + ex.getMessage()
					+ System.lineSeparator());
		}
	}

	public void close() {
		try {
			this.factory.close();
		} catch (Exception ex) {
			Servidor.getLog()
					.append("Error al intentar cerrar la conexión con la base de datos." + System.lineSeparator());
			Logger.getLogger(ConectorHibernate.class.getName()).log(Level.SEVERE, null, ex);
		}
	}


	/* ABAJO DE ACA YA CON HIBERNATE PAPA*/
	public PaquetePersonaje getPersonaje(PaqueteUsuario paqueteUsuario) throws IOException {
		PaquetePersonaje paquetePersonaje = null;
		Session session = null;		
		int i = 2;
		int j = 0;
		
		try {
			paqueteUsuario = getUsuario(paqueteUsuario.getUsername()); 
							
			session = factory.openSession();

			//Personaje
			CriteriaBuilder cbPersonaje = session.getCriteriaBuilder();
			CriteriaQuery<PaquetePersonaje> cqPersonaje = cbPersonaje.createQuery(PaquetePersonaje.class);
			Root<PaquetePersonaje> rpPersonaje = cqPersonaje.from(PaquetePersonaje.class);
			cqPersonaje.select(rpPersonaje).where(cbPersonaje.equal(rpPersonaje.get("id"), paqueteUsuario.getIdPj()));
			paquetePersonaje = session.createQuery(cqPersonaje).getSingleResult();

			//Mochila
			CriteriaBuilder cbMochila = session.getCriteriaBuilder();
			CriteriaQuery<Mochila> cqMochila = cbMochila.createQuery(Mochila.class);
			Root<Mochila> rpMochila = cqMochila.from(Mochila.class);
			cqMochila.select(rpMochila).where(cbMochila.equal(rpMochila.get("idMochila"), paqueteUsuario.getIdPj()));
			Mochila mochila = session.createQuery(cqMochila).getSingleResult();

			//Items
			CriteriaBuilder cbItem = session.getCriteriaBuilder();
			CriteriaQuery<Item> cqItem = cbItem.createQuery(Item.class);
			Root<Item> rpItem = cqItem.from(Item.class);
			

			while (j <= 9) {
				if (mochila.getById(i) != -1) {
					cqItem.select(rpItem).where(cbItem.equal(rpItem.get("idItem"), mochila.getById(i)));						
					Item item = session.createQuery(cqItem).getSingleResult();

					if(item != null) {
						paquetePersonaje.anadirItem(item.getIdItem(),
								item.getNombre(), item.getWearLocation(),
								item.getBonusSalud(), item.getBonusEnergia(),
								item.getBonusFuerza(), item.getBonusDestreza(),
								item.getBonusInteligencia(), item.getFoto(),
								item.getFotoEquipado());
					}
				}
				i++;
				j++;
			}
		} catch (Exception e) {
			Servidor.getLog().append("Fallo al intentar obtener el personaje " + paqueteUsuario.getIdPj()
					+ System.lineSeparator());
		} finally {
			if (session != null)
				session.close();
		}
		return paquetePersonaje;	
	}

	public boolean loguearUsuario(PaqueteUsuario user) {
		boolean resultado = false;
		Session session = null;

		try {
			session = factory.openSession();

			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PaqueteUsuario> cq = cb.createQuery(PaqueteUsuario.class);

			Root<PaqueteUsuario> rp = cq.from(PaqueteUsuario.class);
			cq.select(rp).where(cb.like(rp.get("username"), user.getUsername()));
			
			try {			
				resultado = session.createQuery(cq).getSingleResult() != null;
			}catch (NoResultException nre){
				//Ignore this because as per your logic this is ok!
			}

		} catch (Exception e) {
			Servidor.getLog()
					.append("El usuario " + user.getUsername() + " fallo al iniciar sesión." + System.lineSeparator());
		} finally {
			if (session != null)
				session.close();
		}

		return resultado;
	}

	public boolean registrarUsuario(PaqueteUsuario user) {
		boolean resultado = false;
		Session session = null;
		Transaction tx = null;

		try {
			session = factory.openSession();

			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PaqueteUsuario> cq = cb.createQuery(PaqueteUsuario.class);

			Root<PaqueteUsuario> rp = cq.from(PaqueteUsuario.class);
			cq.select(rp).where(cb.like(rp.get("username"), user.getUsername()));

			boolean yaExiste = false;
			try {			
				yaExiste = session.createQuery(cq).getSingleResult() != null;
			}catch (NoResultException nre){
				//Ignore this because as per your logic this is ok!
			}
			
			if(!yaExiste) {
				tx = session.beginTransaction();
				session.save(user);
				tx.commit();
				
				resultado = true;
			} else {
				Servidor.getLog().append(
						"El usuario " + user.getUsername() + " ya se encuentra en uso." + System.lineSeparator());	

				if (tx != null)
					tx.rollback();
			}			
		} catch (Exception e) {
			Servidor.getLog()
			.append("Eror al intentar registrar el usuario " + user.getUsername() + System.lineSeparator());
			System.err.println(e.getMessage());
		} finally {
			if (session != null)
				session.close();
		}

		return resultado;
	}

	public boolean registrarPersonaje(PaquetePersonaje paquetePersonaje, PaqueteUsuario paqueteUsuario) {
		Session session = null;
		boolean resultado = false;
		
		try {
			session = factory.openSession();
			
			//Personaje
			session.save(paquetePersonaje);
			
			//Updateo el usuario
			paqueteUsuario.setIdPj(paquetePersonaje.getId());
			session.update(paqueteUsuario);
			
			//Inventario
			Inventario inventario = new Inventario(paquetePersonaje.getId());
			session.save(inventario);

			//Mochila
			Mochila mochila = new Mochila(paquetePersonaje.getId());
			session.save(mochila);
			
			//Actualizo con los datos
			paquetePersonaje.setIdInventario(inventario.getIdInventario());
			paquetePersonaje.setIdMochila(mochila.getIdMochila());
			session.update(mochila);

			resultado = true;
		} catch (Exception e) {
			Servidor.getLog().append(
					"Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());
			
			resultado = false;
		} finally {
			if (session != null)
				session.close();
		}
		
		return resultado;
	}
	
	public void actualizarPersonaje(PaquetePersonaje paquetePersonaje) {
		Session session = null;		
		try {
			session = factory.openSession();
			
			//Personaje
			session.update(paquetePersonaje);
			
			//Items
			paquetePersonaje.eliminarItems();
			
			CriteriaBuilder cbMochila = session.getCriteriaBuilder();
			CriteriaQuery<Mochila> cqMochila = cbMochila.createQuery(Mochila.class);
			Root<Mochila> rpMochila = cqMochila.from(Mochila.class);
			cqMochila.select(rpMochila).where(cbMochila.equal(rpMochila.get("idMochila"), paquetePersonaje.getId()));
						
			Mochila mochila = session.createQuery(cqMochila).getSingleResult();
			if(mochila != null) {
				CriteriaBuilder cbItem = session.getCriteriaBuilder();
				CriteriaQuery<Item> cqItem = cbItem.createQuery(Item.class);
				Root<Item> rpItem = cqItem.from(Item.class);
				
				int i = 2;
				int j = 1;
				
				while (j <= 9) {
					if (mochila.getById(i) != -1) {
						cqItem.select(rpItem).where(cbItem.equal(rpItem.get("idItem"), mochila.getById(i)));						
						Item item = session.createQuery(cqItem).getSingleResult();

						if(item != null) {
							paquetePersonaje.anadirItem(item.getIdItem(),
									item.getNombre(), item.getWearLocation(),
									item.getBonusSalud(), item.getBonusEnergia(),
									item.getBonusFuerza(), item.getBonusDestreza(),
									item.getBonusInteligencia(), item.getFoto(),
									item.getFotoEquipado());
						}
					}
					i++;
					j++;
				}
				Servidor.getLog().append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
						+ System.lineSeparator());
			}
			
		} catch (Exception e) {
			Servidor.getLog().append(
					"Error al intentar crear el personaje " + paquetePersonaje.getNombre() + System.lineSeparator());
			
		} finally {
			if (session != null)
				session.close();
		}		
	}

	public void actualizarPersonajeSubioNivel(PaquetePersonaje paquetePersonaje) {
		Session session = null;		
		try {
			session = factory.openSession();
			
			//Personaje
			session.update(paquetePersonaje);

			Servidor.getLog().append("El personaje " + paquetePersonaje.getNombre() + " se ha actualizado con éxito."
					+ System.lineSeparator());
			;
		} catch (Exception e) {
			Servidor.getLog().append("Fallo al intentar actualizar el personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
		} finally {
			if (session != null)
				session.close();
		}
	}

	public PaqueteUsuario getUsuario(String usuario) {
		PaqueteUsuario paqueteUsuario = null;
		Session session = null;		
		try {
			session = factory.openSession();

			CriteriaBuilder cb = session.getCriteriaBuilder();
			CriteriaQuery<PaqueteUsuario> cq = cb.createQuery(PaqueteUsuario.class);

			Root<PaqueteUsuario> rp = cq.from(PaqueteUsuario.class);
			cq.select(rp).where(cb.like(rp.get("username"), usuario));
			paqueteUsuario = session.createQuery(cq).getSingleResult();
			
		} catch (Exception e) {
			Servidor.getLog().append("Fallo al intentar obtener el personaje " + usuario
					+ System.lineSeparator());
		} finally {
			if (session != null)
				session.close();
		}
		return paqueteUsuario;		
	}

	public void actualizarInventario(PaquetePersonaje paquetePersonaje) {
		Session session = null;		
		try {
			session = factory.openSession();
			
			Mochila mochila = new Mochila(paquetePersonaje.getId());
			
			int i = 0;
			while (i < paquetePersonaje.getCantItems()) {
				mochila.setInt(i + 1, paquetePersonaje.getItemID(i));
				i++;
			}		
			
			//Inventario
			session.update(mochila);

			Servidor.getLog().append("El personaje " + paquetePersonaje.getNombre() + " ha actualizado con éxito su inventario."
					+ System.lineSeparator());
			;
		} catch (Exception e) {
			Servidor.getLog().append("Fallo al intentar actualizar el inventario del personaje " + paquetePersonaje.getNombre()
					+ System.lineSeparator());
		} finally {
			if (session != null)
				session.close();
		}
	}

	public void actualizarInventario(int idPersonaje) {
		actualizarInventario(Servidor.getPersonajesConectados().get(idPersonaje));
	}
}
