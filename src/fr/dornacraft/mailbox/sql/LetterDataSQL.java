package fr.dornacraft.mailbox.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import fr.dornacraft.mailbox.Data;
import fr.dornacraft.mailbox.LetterData;
import fr.dornacraft.mailbox.LetterType;

public class LetterDataSQL extends DAO<LetterData> {

	private static final String TABLE_NAME = "MailBox_LetterData";

	private LetterDataSQL() {
		super();

		try {
			PreparedStatement query = this.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS	" + TABLE_NAME
					+ " (id BIGINT NOT NULL, type VARCHAR(255) NOT NULL, content TEXT NOT NULL, isRead BOOLEAN NOT NULL DEFAULT '0', PRIMARY KEY(id))");
			query.executeUpdate();
			query.close();

		} catch (SQLException e) {
			System.out.println(e);
		}
	}

	private static LetterDataSQL INSTANCE = new LetterDataSQL();

	public static LetterDataSQL getInstance() {
		return INSTANCE;
	}

	private String toSQLData(List<String> list) {
		StringBuilder sb = new StringBuilder();

		for (String page : list) {
			sb.append(String.format("%s\n", page));
		}

		return sb.toString();
	}

	private List<String> contentFromSQLData(String str) {
		return Arrays.asList(StringUtils.split(str, "\n"));
	}

	/*
	 * table format ?: - [id: int] [type: String(LetterType name)] [content: String]
	 * [isRead: Boolean]
	 * 
	 */

	@Override
	public LetterData create(LetterData obj) {
		LetterData res = null;

		try {
			Data data = DataSQL.getInstance().create(obj);
			obj.setId(data.getId());

			PreparedStatement query = super.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?)");
			query.setLong(1, obj.getId());
			query.setString(2, obj.getLetterType().name());
			query.setString(3, toSQLData(obj.getContent()));
			query.setBoolean(4, obj.getIsRead());

			query.execute();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	/*
	 * table name format ?: - [id: int] [type: String(LetterType name)] [content:
	 * String] [isRead: Boolean]
	 * 
	 */

	@Override
	public LetterData find(Long i) {
		LetterData res = null;

		try {
			PreparedStatement query = super.getConnection()
					.prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, i);
			ResultSet resultset = query.executeQuery();

			if (resultset.next()) {
				Data data = DataSQL.getInstance().find(i);
				LetterType type = LetterType.valueOf(resultset.getString("type"));
				List<String> content = contentFromSQLData(resultset.getString("content"));
				Boolean isRead = resultset.getBoolean("isRead");

				if (data != null) {
					res = new LetterData(data, type, content, isRead);

				}

			}
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public LetterData update(LetterData obj) {
		LetterData res = null;
		try {
			DataSQL.getInstance().update(obj);
			PreparedStatement query = super.getConnection()
					.prepareStatement("UPDATE " + TABLE_NAME + " SET content = ?, type = ?, isRead = ? WHERE id = ?");
			query.setString(1, toSQLData(obj.getContent()));
			query.setString(2, obj.getLetterType().name());
			query.setBoolean(3, obj.getIsRead());
			query.setLong(4, obj.getId());

			query.executeUpdate();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public void delete(LetterData obj) {
		try {
			DataSQL.getInstance().delete(obj);
			PreparedStatement query = super.getConnection()
					.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE id = ?");
			query.setLong(1, obj.getId());
			query.execute();
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
