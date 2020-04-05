package fr.dornacraft.mailbox.sql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;

import org.bukkit.craftbukkit.libs.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import fr.dornacraft.mailbox.Data;
import fr.dornacraft.mailbox.ItemData;

public class ItemDataSQL extends DAO<ItemData>{
	
	private static final String TABLE_NAME = "MailBox_ItemData";
	
	private ItemDataSQL() {
		super();
		
		try {
			PreparedStatement query = this.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS	" + TABLE_NAME
					+ " (id BIGINT NOT NULL, durationInSeconds BIGINT, itemStack TEXT, PRIMARY KEY(id))");
			query.executeUpdate();
			query.close();
			
		} catch (SQLException e) {
			System.out.println(e);
		}
		
	}
	
	private String toBase64(ItemStack itemstack) {
		String res = null;
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

			dataOutput.writeObject(itemstack);
			dataOutput.close();
			res = Base64Coder.encodeLines(outputStream.toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	private ItemStack fromBase64(String data) {
		ItemStack res = null;
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
			BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

			res = (ItemStack) dataInput.readObject();

			dataInput.close();

		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

		return res;
	}
	
	public static Integer compareToNow(Timestamp ts, Duration duration) {

		LocalDateTime date = ts.toLocalDateTime(); // to datetime
		LocalDateTime added = date.plus(duration);
		return added.compareTo(LocalDateTime.now());

	}
	
	private static ItemDataSQL INSTANCE = new ItemDataSQL();
	
	public static ItemDataSQL getInstance() {
		return INSTANCE;
	}
	
	/*
	 * table name format ?:
	 *  - [id: int] [duration: Long] [itemStack: String(?)]
	 * 
	 */
	
	@Override
	public ItemData create(ItemData obj) {
		ItemData res = null;

		try {
			Data data = DataSQL.getInstance().create(obj);
			obj.setId(data.getId());
			
			PreparedStatement query = super.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " (id, durationInSeconds, itemStack) VALUES(?, ?, ?)");
			query.setLong(1, obj.getId());
			query.setLong(2, obj.getDuration().getSeconds());
			query.setString(3, toBase64(obj.getItem()));
			
			query.execute();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public ItemData find(Long i) {
		ItemData res = null;
		
		try {
			PreparedStatement query = super.getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, i);
			ResultSet resultset = query.executeQuery();
			
			if(resultset.next() ) {
				Data data = DataSQL.getInstance().find(i);
				ItemStack itemstack = fromBase64(resultset.getString("itemStack"));
				Duration duration = Duration.ofSeconds(resultset.getLong("durationInSeconds"));
				if(data != null) {
					res = new ItemData(data, itemstack, duration);
					
				}
				
			}
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}

	@Override
	public ItemData update(ItemData obj) {
		ItemData res = null;
		try {
			DataSQL.getInstance().update(obj);
			PreparedStatement query = super.getConnection().prepareStatement("UPDATE " + TABLE_NAME + " SET itemStack = ?, durationInSeconds = ? WHERE id = ?");
			query.setString(1, toBase64(obj.getItem()) );
			query.setLong(2, obj.getDuration().toMillis() / 1000);
			query.setLong(3, obj.getId());

			query.executeUpdate();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public void delete(ItemData obj) {
		try {
			DataSQL.getInstance().delete(obj);
			PreparedStatement query = super.getConnection().prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, obj.getId());
			query.execute();
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}