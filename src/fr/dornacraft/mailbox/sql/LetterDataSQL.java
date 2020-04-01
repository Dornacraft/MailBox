package fr.dornacraft.mailbox.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.dornacraft.mailbox.Data;
import fr.dornacraft.mailbox.LetterData;

public class LetterDataSQL extends DAO<LetterData> {

	private final String TABLE_NAME = "MAilBox_LetterData";

	private static LetterDataSQL INSTANCE = new LetterDataSQL();

	private LetterDataSQL() {

	}
	
	/*
	 * table name format ?:
	 *  - [id: int] [type: String(LetterType name)] [content: String] [isRead: Boolean]
	 * 
	 */

	public static LetterDataSQL getInstance() {
		return INSTANCE;
	}
	
	private String toSQLData(List<String> list) {
		StringBuilder sb = new StringBuilder();
		
		for(String page : list) {
			sb.append(String.format("[%s]", page));
		}
		
		return sb.toString();
	}
	
	private List<String> contentFromSQLData(String str){ //TODO changer "\n"
		List<String> res = new ArrayList<>();
		Pattern p = Pattern.compile("\\[(.*?)\\]"); //[azerazer ] [page 2aez r ]
		Matcher m = p.matcher(str);
		
		while(m.find()) {
			String group = m.group();
			res.add(group);
		}
		
		return res;
	}
	
	@Override
	public LetterData create(LetterData obj) {
		LetterData res = null;

		try {
			DataSQL dataSQL = new DataSQL();
			Data newData = dataSQL.create(obj);
			obj.setId(newData.getId());
			
			PreparedStatement query = super.getConnection().prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES(?, ?, ?, ?)");
			query.setInt(1, obj.getId());
			query.setString(2, obj.getLetterType().name());
			query.setString(3, toSQLData(obj.getContent()) );
			query.setBoolean(4, obj.getIsRead());
			
			query.execute();
			query.close();
			res = obj;

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return res;
	}

	@Override
	public LetterData find(LetterData obj) {
		LetterData res = null;
		
		try {
			PreparedStatement query = super.getConnection().prepareStatement("SELECT * FROM " + TABLE_NAME + " WHERE id = ?");
			query.setInt(1, obj.getId());
			ResultSet resultset = query.executeQuery();
			
			if(resultset.next() ) {
				res = obj;
			}
			query.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}

	@Override
	public LetterData update(LetterData obj) { //TODO revoir ce code, / supprimer
		LetterData res = null;
		try {
			PreparedStatement query = super.getConnection().prepareStatement("UPDATE " + TABLE_NAME + " content = ?, type = ?, isRead = ? WHERE id = ?");
			query.setString(1, toSQLData(obj.getContent()));
			query.setString(2, obj.getLetterType().name());
			query.setBoolean(3, obj.getIsRead());
			query.setInt(4, obj.getId());

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
		// TODO Auto-generated method stub

	}

}
