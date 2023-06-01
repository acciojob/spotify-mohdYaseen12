package com.driver;

import java.util.*;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);
//        userPlaylistMap.put(user,new ArrayList<>());
        return user;

    }

    public Artist createArtist(String name) {
//        for(Artist ar : artists){
//            if(ar.equals(name)){
//                return ar;
//            }
//        }

        Artist artist = new Artist(name);
        artist.setLikes(0);
        artists.add(artist);

//        artistAlbumMap.put(artist,new ArrayList<>());
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = isArtistPresent(artistName);
        Album album = new Album(title);
        album.setReleaseDate(new Date());
        albums.add(album); // adding album into album list
        artistAlbumMap.get(artist).add(album); // mapping artist with album

        albumSongMap.put(album,new ArrayList<>()); // mapping album with list of songs

        return album;
    }

    private Artist isArtistPresent(String artistName) {
        for(Artist artist : artists){
            if(artist.equals(artistName)){
                return artist;
            }
        }
        return new Artist(artistName);
    }

    public Song createSong(String title, String albumName, int length) throws Exception{

            Album album = findAlbum(albumName);
            if(album == null)
                throw new Exception("Album does not exist");
            else{
                Song song = new Song(title,length);
                songs.add(song); // adding new song to song list
                albumSongMap.get(album).add(song); // adding song in specific album
                songLikeMap.put(song,new ArrayList<>());
                return song;
            }
    }

    private Album findAlbum(String albumName) throws Exception {
        for(Album album : albums){
            if(album.getTitle().equals(albumName)) return album;
        }
        return null;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {

        User user = findUser(mobile);
        if(user == null)
            throw new Exception("User does not exist");

        else{
            Playlist playlist = new Playlist(title);
            List<Song> songList = getSongsByGIvenLength(length);

            playlistSongMap.put(playlist,songList);

            playlistListenerMap.put(playlist,new ArrayList<>());
            playlistListenerMap.get(playlist).add(user);

            creatorPlaylistMap.put(user,playlist);

            userPlaylistMap.get(user).add(playlist);
            playlists.add(playlist);
            return playlist;
        }

    }

    private List<Song> getSongsByGIvenLength(int length) {
        List<Song> list = new ArrayList<>();
        for(Song song : songs){
            if(song.getLength() == length){
                list.add(song);
            }
        }
        return list;
    }

    private User findUser(String mobile) {
        for(User user : users){
            if(user.getMobile().equals(mobile))
                return user;
        }
        return null;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUser(mobile);
        if(user == null){
            throw new Exception("User does not exist");
        }
        else{
            Playlist playlist = new Playlist(title);
            List<Song> songList = getSongsByName(title);

            playlistSongMap.put(playlist,songList);

            playlistListenerMap.put(playlist,new ArrayList<>());
            playlistListenerMap.get(playlist).add(user);

            creatorPlaylistMap.put(user,playlist);

            userPlaylistMap.get(user).add(playlist);
            playlists.add(playlist);
            return playlist;

        }

    }

    private List<Song> getSongsByName(String title) {
        List<Song> songList = new ArrayList<>();
        for(Song song : songs){
            if(song.getTitle().equals(title)){
                if(!songList.contains(song))
                    songList.add(song);
            }
        }
        return songList;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        Playlist playlist = isPlaylistPresent(playlistTitle);
        if(playlist == null){
            throw new Exception("Playlist does not exist");
        }
        User user = isUserPresent(mobile);
        if(user == null){
            throw new Exception("User does not exist");
        }

        boolean isUserCreatorOrListener = isUserCreator(user,playlist) || isUserListener(user,playlist);

        if(isUserCreatorOrListener) return playlist;

        playlistListenerMap.get(playlist).add(user);
        userPlaylistMap.get(user).add(playlist);
        return playlist;
    }

    private boolean isUserListener(User user, Playlist playlist) {
        return playlistListenerMap.get(playlist).contains(user);
    }

    private boolean isUserCreator(User user, Playlist playlist) {
        if(creatorPlaylistMap.containsKey(user)){
            return creatorPlaylistMap.get(user).equals(playlist);
        }
        return false;
    }

    private User isUserPresent(String mobile) {
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                return user;
            }
        }
        return null;
    }

    private Playlist isPlaylistPresent(String playlistTitle) {
        for(Playlist playlist : playlists){
            if(playlist.getTitle().equals(playlistTitle)){
                return playlist;
            }
        }
        return null;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        // 1. The user likes the given song. The corresponding artist of the song gets auto-liked
        // 2. A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        // 3. However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        // 4. If the user does not exist, throw "User does not exist" exception
        // 5. If the song does not exist, throw "Song does not exist" exception
        // 6. Return the song after updating
        User user = isUserPresent(mobile);
        if(user == null){
            throw new Exception("User does not exist");
        }

        Song song = isSongPresent(songTitle);
        if(song == null){
            throw new Exception("Song does not exist");
        }

        Album album = getAlbumOfSong(song);
        Artist artist = getArtistOfAlbum(album);

        int songLikes = 0;
        int artistLikes = 0;

        if(songLikeMap.containsKey(song)){
            if(!songLikeMap.get(song).contains(user)){
                songLikeMap.get(song).add(user);
                songLikes = song.getLikes() + 1;
                artistLikes = artist.getLikes() + 1;
                song.setLikes(songLikes);
                artist.setLikes(artistLikes);

            }
        }
        else{
            songLikeMap.put(song,new ArrayList<>());
            songLikes = song.getLikes() + 1;
            artistLikes = artist.getLikes() + 1;
            song.setLikes(songLikes);
            artist.setLikes(artistLikes);

        }
        return song;
    }

    private Artist getArtistOfAlbum(Album album) {
        for(Artist artist : artistAlbumMap.keySet()){
            if(artistAlbumMap.get(artist).contains(album))
                return artist;
        }
        return null;
    }

    private Album getAlbumOfSong(Song song) {
        for(Album album : albumSongMap.keySet()){
            if(albumSongMap.get(album).contains(song))
                return album;
        }
        return null;
    }

    private Song isSongPresent(String songTitle) {
        for(Song song : songs){
            if(song.getTitle().equals(songTitle))
                return song;
        }
        return null;
    }

    public String mostPopularArtist() {
        //Return the artist name with maximum likes
        int maxLike = Integer.MIN_VALUE;
        String name = "";
        for(Artist artist : artists){
            if(artist.getLikes() > maxLike){
                name = artist.getName();
                maxLike = artist.getLikes();
            }
        }
        return name;
    }

    public String mostPopularSong() {
        //return the song title with maximum likes
        String songName = "";
        int maxLike = Integer.MIN_VALUE;
        for(Song song : songs){
            if(song.getLikes() > maxLike){
                maxLike = song.getLikes();
                songName = song.getTitle();
            }
        }
        return songName;
    }

//    public List<Album> getAllALbum() {
//        return albums;
//    }
//
//    public List<User> getAllUser() {
//        return users;
//    }
}
