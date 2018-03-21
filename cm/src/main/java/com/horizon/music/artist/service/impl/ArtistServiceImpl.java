package com.horizon.music.artist.service.impl;

import com.horizon.music.artist.dao.ArtistDAO;
import com.horizon.music.artist.sql.IMusicSqlTemplate;
import com.horizon.music.artist.vo.Music;
import com.horizon.music.artist.vo.UserArtist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.horizon.music.artist.service.IArtistService;

import java.util.List;

@Service("artistService")
public class ArtistServiceImpl implements IArtistService {
    @Autowired
    private ArtistDAO artistDAO;

    public void setArtistDAO(ArtistDAO artistDAO) {
        this.artistDAO = artistDAO;
    }

    public List<Music> getAllMusicList() {
        return (List<Music>)artistDAO.findByVO(new Music(), IMusicSqlTemplate.GET_ALL_MUSICLIST, Music.class);
    }

    public void addUserArtist(UserArtist userArtist){
        artistDAO.save(IMusicSqlTemplate.SAVE_USER_ARTIST,userArtist);
    }

}
