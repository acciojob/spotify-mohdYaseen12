package com.driver;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class SpotifyService {

    //Auto-wire will not work in this case, no need to change this and add autowire

    SpotifyRepository spotifyRepository = new SpotifyRepository();

    public User createUser(String name, String mobile){
        return spotifyRepository.createUser(name,mobile);

    }

    public Artist createArtist(String name) {
        return spotifyRepository.createArtist(name);

    }

    public Album createAlbum(String title, String artistName) {
        return spotifyRepository.createAlbum(title,artistName);


    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        List<Album> albumList = spotifyRepository.getAllALbum();
        for(Album album : albumList){
            if(!album.getTitle().equals(albumName)){
                throw new Exception("Album does not exist");
            }
        }
        Song song = spotifyRepository.createSong(title,albumName,length);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        List<User> userList = spotifyRepository.getAllUser();
        for(User user : userList){
            if(!user.getMobile().equals(mobile))
                throw new Exception("User does not exist");
        }
        Playlist playlist = spotifyRepository.createPlaylistOnLength(mobile, title, length);
        return playlist;
    }


    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        List<User> userList = spotifyRepository.getAllUser();
        for(User user : userList){
            if(!user.getMobile().equals(mobile))
                throw new Exception("User does not exist");
        }
        Playlist playlist = spotifyRepository.createPlaylistOnName(mobile, title, songTitles);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
       Playlist playlist = spotifyRepository.findPlaylist(mobile, playlistTitle);
       return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        Song song = spotifyRepository.likeSong(mobile, songTitle);
        return song;
    }

    public String mostPopularArtist() {
        return spotifyRepository.mostPopularArtist();

    }

    public String mostPopularSong() {
        return spotifyRepository.mostPopularSong();

    }
}
