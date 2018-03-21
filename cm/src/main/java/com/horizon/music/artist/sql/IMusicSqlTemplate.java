package com.horizon.music.artist.sql;

/**
 * 
 * Title:<br>
 * Description: Music sql<br>
 * Date: 2012-10-20 <br>
 * Copyright (c) 2012 <br>
 * 
 * @author 
 */
public interface IMusicSqlTemplate {

	static final String GET_ALL_MUSICLIST ="SELECT * FROM TB_SYS_MUSIC_INFO" ;

	static final String SAVE_USER_ARTIST = "INSERT INTO TB_SYS_USER_MUSIC (USER_ID,ARTIST_ID,MUSIC_ID) VALUES(:userID, :artistId,:musicId)";
}
