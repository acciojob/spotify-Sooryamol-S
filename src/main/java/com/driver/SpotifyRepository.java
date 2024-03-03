package com.driver;

import java.util.*;

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
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;

    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = findArtist(artistName);
        Album album = new Album(title);
        albums.add(album);

        if (artist != null) {
            List<Album> artistAlbums = artistAlbumMap.getOrDefault(artist, new ArrayList<>());
            artistAlbums.add(album);
            artistAlbumMap.put(artist, artistAlbums);
        }

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = findAlbum(albumName);

        if (album == null) {
            throw new Exception("Album does not exist");
        }

        Song song = new Song(title, length);
        songs.add(song);

        List<Song> albumSongs = albumSongMap.getOrDefault(album, new ArrayList<>());
        albumSongs.add(song);
        albumSongMap.put(album, albumSongs);

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = findUserByMobile(mobile);

        if (user == null) {
            throw new Exception("User does not exist");
        }

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        creatorPlaylistMap.put(user, playlist);

        List<Song> playlistSongs = getSongsByLength(length);
        playlistSongMap.put(playlist, playlistSongs);

        List<User> playlistListeners = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());
        playlistListeners.add(user);
        playlistListenerMap.put(playlist, playlistListeners);

        List<Playlist> userPlaylists = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        userPlaylists.add(playlist);
        userPlaylistMap.put(user, userPlaylists);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = findUserByMobile(mobile);

        if (user == null) {
            throw new Exception("User does not exist");
        }

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        creatorPlaylistMap.put(user, playlist);

        List<Song> playlistSongs = getSongsByTitles(songTitles);
        playlistSongMap.put(playlist, playlistSongs);

        List<User> playlistListeners = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());
        playlistListeners.add(user);
        playlistListenerMap.put(playlist, playlistListeners);

        List<Playlist> userPlaylists = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        userPlaylists.add(playlist);
        userPlaylistMap.put(user, userPlaylists);

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = findUserByMobile(mobile);

        if (user == null) {
            throw new Exception("User does not exist");
        }

        Playlist playlist = findPlaylistByTitle(playlistTitle);

        if (playlist == null) {
            throw new Exception("Playlist does not exist");
        }

        List<User> playlistListeners = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());

        if (!playlistListeners.contains(user)) {
            playlistListeners.add(user);
            playlistListenerMap.put(playlist, playlistListeners);
        }

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = findUserByMobile(mobile);

        if (user == null) {
            throw new Exception("User does not exist");
        }

        Song song = findSongByTitle(songTitle);

        if (song == null) {
            throw new Exception("Song does not exist");
        }

        List<User> likedUsers = songLikeMap.getOrDefault(song, new ArrayList<>());

        if (!likedUsers.contains(user)) {
            likedUsers.add(user);
            songLikeMap.put(song, likedUsers);

            Artist artist = findArtistBySong(song);
            if (artist != null) {
                artist.setLikes(artist.getLikes() + 1);
            }

            song.setLikes(song.getLikes() + 1);
        }

        return song;
    }

    public String mostPopularArtist() {
        Artist mostPopularArtist = null;
        int maxLikes = -1;

        for (Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()) {
            Artist artist = entry.getKey();
            int likes = artist.getLikes();

            if (likes > maxLikes) {
                maxLikes = likes;
                mostPopularArtist = artist;
            }
        }

        return mostPopularArtist != null ? mostPopularArtist.getName() : null;
    }

    public String mostPopularSong() {
        Song mostPopularSong = null;
        int maxLikes = -1;

        for (Map.Entry<Song, List<User>> entry : songLikeMap.entrySet()) {
            Song song = entry.getKey();
            int likes = entry.getValue().size();

            if (likes > maxLikes) {
                maxLikes = likes;
                mostPopularSong = song;
            }
        }

        return mostPopularSong != null ? mostPopularSong.getTitle() : null;
    }

    private List<Song> getSongsByLength(int length) {
        List<Song> result = new ArrayList<>();

        for (Map.Entry<Album, List<Song>> entry : albumSongMap.entrySet()) {
            List<Song> songs = entry.getValue();

            for (Song song : songs) {
                if (song.getLength() == length) {
                    result.add(song);
                }
            }
        }

        return result;
    }

    private List<Song> getSongsByTitles(List<String> songTitles) {
        List<Song> result = new ArrayList<>();

        for (String title : songTitles) {
            Song song = findSongByTitle(title);
            if (song != null) {
                result.add(song);
            }
        }

        return result;
    }

    private User findUserByMobile(String mobile) {
        for (User user : users) {
            if (user.getMobile().equals(mobile)) {
                return user;
            }
        }
        return null;
    }

    private Artist findArtist(String name) {
        for (Artist artist : artists) {
            if (artist.getName().equals(name)) {
                return artist;
            }
        }
        return null;
    }

    private Album findAlbum(String title) {
        for (Album album : albums) {
            if (album.getTitle().equals(title)) {
                return album;
            }
        }
        return null;
    }

    private Playlist findPlaylistByTitle(String title) {
        for (Playlist playlist : playlists) {
            if (playlist.getTitle().equals(title)) {
                return playlist;
            }
        }
        return null;
    }

    private Song findSongByTitle(String title) {
        for (Song song : songs) {
            if (song.getTitle().equals(title)) {
                return song;
            }
        }
        return null;
    }

    private Artist findArtistBySong(Song song) {
        for (Map.Entry<Artist, List<Album>> entry : artistAlbumMap.entrySet()) {
            List<Song> songs = albumSongMap.get(entry.getValue());

            if (songs != null && songs.contains(song)) {
                return entry.getKey();
            }
        }
        return null;
    }
}



