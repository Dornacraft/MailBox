package fr.dornacraft.mailbox.sql;

import com.mysql.jdbc.Connection;

public abstract class DAO<T> {

	private Connection connection;
	
	public DAO() {//TODO init connexion
		
	}
	
	public abstract T create(T obj);

	public abstract T find(T obj);

	public abstract T update(T obj);

	public abstract void delete(T obj);
	
	public Connection getConnection() {
		return this.connection;
	}

}
