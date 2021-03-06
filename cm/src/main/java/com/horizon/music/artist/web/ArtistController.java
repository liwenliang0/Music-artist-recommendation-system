package com.horizon.music.artist.web;

import javax.servlet.http.HttpServletRequest;

import com.horizon.music.artist.vo.Music;
import com.horizon.music.artist.vo.UserArtist;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.horizon.music.artist.service.IArtistService;

import java.util.List;

@Controller
@RequestMapping("/sys/user")
public class ArtistController {

	/**
	 * 用于输出log
	 */
	private static Logger log = Logger.getLogger(ArtistController.class);
	
	@Autowired
	private IArtistService artistService;

	
	@RequestMapping(value = "/initArtistList")
	public String initArtistList() throws Exception {
		log.info("ArtistController --> initArtistList()");
		return "music/index";
	}
	@ResponseBody
	@RequestMapping(value = "/playMusic", method = RequestMethod.POST )
	public String playMusic(@RequestBody UserArtist userArtist) throws Exception {
		try {
			artistService.addUserArtist(userArtist);
		} catch (Exception e) {
			log.error(e);
			return "failed";
		}
		return "succ";
	}

	@RequestMapping(value = "/musiclist")
	public @ResponseBody List<Music> musiclist() {
		List<Music> result = artistService.getAllMusicList();
		return result;
	}
	
	public void setArtistService(IArtistService artistService) {
		this.artistService = artistService;
	}
	
}
