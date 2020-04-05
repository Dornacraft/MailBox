package fr.dornacraft.mailbox.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.dornacraft.mailbox.Data;
import fr.dornacraft.mailbox.DataFactory;

public class DataSQL extends DAO<Data> {
	
	private static final String TABLE_NAME = "MailBox_Data";
	
	public DataSQL() {
		super();

		try {
			PreparedStatement query = this.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS	" + TABLE_NAME
					+ " (id BIGINT NOT NULL AUTO_INCREMENT, uuid VARCHAR(255), author VARCHAR(255), object VARCHAR(255), creationDate TIMESTAMP, PRIMARY KEY(id))");
			query.executeUpdate();
			query.close();
			
		} catch (SQLException e) {
			System.out.println(e);
		}

	}

	private static DataSQL INSTANCE = new DataSQL();
	public static DataSQL getInstance() {
		return INSTANCE;
	}
	
	/*
	 * table name format ?:
	 *  - [id: int] [uuid: String] [author: String] [object: String] [creationDate: Date]
	 * 
	 */
	
	public List<Data> getDataList(Player player){//TODO change parameter to UUID
		List<Data> res = new ArrayList<>();
		
		try {
			PreparedStatement query = getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE uuid = ?");
			query.setString(1, player.getUniqueId().toString());
			ResultSet rs = query.executeQuery();
			
			while (rs.next()) {
				Long id = rs.getLong("id");
				UUID uuid = UUID.fromString(rs.getString("uuid"));
				String author = rs.getString("author");
				String object = rs.getString("object");
				Timestamp creationDate = rs.getTimestamp("creationDate");
				
				Data tempData = new DataFactory(id, uuid, author, object, creationDate);
				res.add(tempData );
			}
			
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();

		}
		
		
		return res;
	}
	
	@Override
	public Data create(Data obj) {
		Data res = null;
		
		try {
			obj.setCreationDate(Timestamp.from(Instant.now()));
			PreparedStatement query = super.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " (uuid, author, object, creationDate) VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			query.setString(1, obj.getUuid().toString());
			query.setString(2, obj.getAuthor());
			query.setString(3, obj.getObject());
			query.setTimestamp(4, obj.getCreationDate());

			query.execute();

			ResultSet tableKeys = query.getGeneratedKeys();
			if(tableKeys.next()) {
				obj.setId(tableKeys.getLong(1));
			}
			
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}

	@Override
	public Data find(Long i) {
		Data res = null;
		
		try {
			PreparedStatement query = super.getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, i);
			ResultSet resultset = query.executeQuery();
			
			if(resultset.next() ) {
				UUID uuid = UUID.fromString(resultset.getString("uuid"));
				String author = resultset.getString("author");
				String object = resultset.getString("object");
				Timestamp creationDate = resultset.getTimestamp("creationDate");
				res = new DataFactory(i, uuid, author, object, creationDate);
				
			}
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	@Override
	public Data update(Data obj) {
		Data res = null;
		try {
			PreparedStatement query = super.getConnection().prepareStatement("UPDATE " + TABLE_NAME + " SET uuid = ?, author = ?, object = ?, creationDate = ? WHERE id = ?");
			query.setString(1, obj.getUuid().toString());
			query.setString(2, obj.getAuthor());
			query.setString(3, obj.getObject());
			query.setTimestamp(4, obj.getCreationDate());
			query.setLong(5, obj.getId());

			query.executeUpdate();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public void delete(Data obj) {
		try {
			PreparedStatement query = super.getConnection().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1,  obj.getId());
			query.execute();
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
