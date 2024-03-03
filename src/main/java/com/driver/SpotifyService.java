package com.driver;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SpotifyService {

    SpotifyRepository spotifyRepository = new SpotifyRepository();

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        spotifyRepository.users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        spotifyRepository.artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = getOrCreateArtist(artistName);
        Album album = new Album(title);
        spotifyRepository.albums.add(album);

        // Update the artist's album list
        List<Album> artistAlbums = spotifyRepository.artistAlbumMap.getOrDefault(artist, new ArrayList<>());
        artistAlbums.add(album);
        spotifyRepository.artistAlbumMap.put(artist, artistAlbums);

        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = getAlbumByName(albumName);

        Song song = new Song(title, length);
        spotifyRepository.songs.add(song);

        // Update the album's song list
        List<Song> albumSongs = spotifyRepository.albumSongMap.getOrDefault(album, new ArrayList<>());
        albumSongs.add(song);
        spotifyRepository.albumSongMap.put(album, albumSongs);

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = getUserByMobile(mobile);
        Playlist playlist = new Playlist(title);
        spotifyRepository.playlists.add(playlist);

        // Add songs to the playlist based on length
        List<Song> songsToAdd = new ArrayList<>();
        for (Song song : spotifyRepository.songs) {
            if (song.getLength() == length) {
                songsToAdd.add(song);
            }
        }

        // Update the playlist's song list
        spotifyRepository.playlistSongMap.put(playlist, songsToAdd);

        // Set the playlist as the only listener for now
        List<User> playlistListeners = new ArrayList<>();
        playlistListeners.add(user);
        spotifyRepository.playlistListenerMap.put(playlist, playlistListeners);

        // Update user's playlist list
        List<Playlist> userPlaylists = spotifyRepository.userPlaylistMap.getOrDefault(user, new ArrayList<>());
        userPlaylists.add(playlist);
        spotifyRepository.userPlaylistMap.put(user, userPlaylists);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = getUserByMobile(mobile);
        Playlist playlist = new Playlist(title);
        spotifyRepository.playlists.add(playlist);

        // Add songs to the playlist based on titles
        List<Song> songsToAdd = new ArrayList<>();
        for (String songTitle : songTitles) {
            Song song = getSongByTitle(songTitle);
            songsToAdd.add(song);
        }

        // Update the playlist's song list
        spotifyRepository.playlistSongMap.put(playlist, songsToAdd);

        // Set the playlist as the only listener for now
        List<User> playlistListeners = new ArrayList<>();
        playlistListeners.add(user);
        spotifyRepository.playlistListenerMap.put(playlist, playlistListeners);

        // Update user's playlist list
        List<Playlist> userPlaylists = spotifyRepository.userPlaylistMap.getOrDefault(user, new ArrayList<>());
        userPlaylists.add(playlist);
        spotifyRepository.userPlaylistMap.put(user, userPlaylists);

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = getUserByMobile(mobile);
        Playlist playlist = getPlaylistByTitle(playlistTitle);

        // Check if the user is the creator or already a listener
        if (!spotifyRepository.creatorPlaylistMap.getOrDefault(user, new Playlist("")).equals(playlist)
                && !spotifyRepository.playlistListenerMap.getOrDefault(playlist, new ArrayList<>()).contains(user)) {

            // Add the user as a listener
            List<User> playlistListeners = spotifyRepository.playlistListenerMap.getOrDefault(playlist, new ArrayList<>());
            playlistListeners.add(user);
            spotifyRepository.playlistListenerMap.put(playlist, playlistListeners);

            // Update user's playlist list
            List<Playlist> userPlaylists = spotifyRepository.userPlaylistMap.getOrDefault(user, new ArrayList<>());
            userPlaylists.add(playlist);
            spotifyRepository.userPlaylistMap.put(user, userPlaylists);
        }

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = getUserByMobile(mobile);
        Song song = getSongByTitle(songTitle);

        // Check if the user has already liked the song
        if (!spotifyRepository.songLikeMap.getOrDefault(song, new ArrayList<>()).contains(user)) {
            // Like the song
            int currentLikes = song.getLikes();
            song.setLikes(currentLikes + 1);

            // Auto-like the corresponding artist
            Artist artist = getArtistBySong(song);
            int currentArtistLikes = artist.getLikes();
            artist.setLikes(currentArtistLikes + 1);

            // Update the user's liked songs list
            List<User> likedUsers = spotifyRepository.songLikeMap.getOrDefault(song, new ArrayList<>());
            likedUsers.add(user);
            spotifyRepository.songLikeMap.put(song, likedUsers);
        }

        return song;
    }

    public String mostPopularArtist() {
        // Find the artist with the maximum likes
        int maxLikes = -1;
        Artist mostPopularArtist = null;
        for (Artist artist : spotifyRepository.artists) {
            int artistLikes = artist.getLikes();
            if (artistLikes > maxLikes) {
                maxLikes = artistLikes;
                mostPopularArtist = artist;
            }
        }

        return mostPopularArtist != null ? mostPopularArtist.getName() : "No artist found";
    }

    public String mostPopularSong() {
        // Find the song with the maximum likes
        int maxLikes = -1;
        Song mostPopularSong = null;
        for (Song song : spotifyRepository.songs) {
            int songLikes = song.getLikes();
            if (songLikes > maxLikes) {
                maxLikes = songLikes;
                mostPopularSong = song;
            }
        }

        return mostPopularSong != null ? mostPopularSong.getTitle() : "No song found";
    }

    // New methods for existence checks
    public boolean artistExists(String artistName) {
        for (Artist artist : spotifyRepository.artists) {
            if (artist.getName().equals(artistName)) {
                return true;
            }
        }
        return false;
    }

    public boolean userExists(String mobile) {
        for (User user : spotifyRepository.users) {
            if (user.getMobile().equals(mobile)) {
                return true;
            }
        }
        return false;
    }

    public boolean songExists(String songTitle) {
        for (Song song : spotifyRepository.songs) {
            if (song.getTitle().equals(songTitle)) {
                return true;
            }
        }
        return false;
    }

    public boolean albumExists(String albumName) {
        for (Album album : spotifyRepository.albums) {
            if (album.getTitle().equals(albumName)) {
                return true;
            }
        }
        return false;
    }

    public boolean playlistExists(String playlistTitle) {
        for (Playlist playlist : spotifyRepository.playlists) {
            if (playlist.getTitle().equals(playlistTitle)) {
                return true;
            }
        }
        return false;
    }

    // Helper methods for getting entities by name
    private Artist getOrCreateArtist(String artistName) {
        for (Artist artist : spotifyRepository.artists) {
            if (artist.getName().equals(artistName)) {
                return artist;
            }
        }
        return createArtist(artistName);
    }

    private Album getAlbumByName(String albumName) throws Exception {
        for (Album album : spotifyRepository.albums) {
            if (album.getTitle().equals(albumName)) {
                return album;
            }
        }
        throw new Exception("Album does not exist");
    }

    private User getUserByMobile(String mobile) throws Exception {
        for (User user : spotifyRepository.users) {
            if (user.getMobile().equals(mobile)) {
                return user;
            }
        }
        throw new Exception("User does not exist");
    }

    private Song getSongByTitle(String songTitle) throws Exception {
        for (Song song : spotifyRepository.songs) {
            if (song.getTitle().equals(songTitle)) {
                return song;
            }
        }
        throw new Exception("Song does not exist");
    }

    private Playlist getPlaylistByTitle(String playlistTitle) throws Exception {
        for (Playlist playlist : spotifyRepository.playlists) {
            if (playlist.getTitle().equals(playlistTitle)) {
                return playlist;
            }
        }
        throw new Exception("Playlist does not exist");
    }
    // Add this method to your SpotifyService class
    // Existing methods...




    // Existing methods...

    private Album getAlbum(Song song) throws Exception {
        for (Map.Entry<Album, List<Song>> entry : spotifyRepository.albumSongMap.entrySet()) {
            if (entry.getValue().contains(song)) {
                return entry.getKey();
            }
        }
        throw new Exception("Album not found for the song");
    }

    private Artist getArtistBySong(Song song) throws Exception {
        Album album = getAlbum(song);
        for (Map.Entry<Artist, List<Album>> entry : spotifyRepository.artistAlbumMap.entrySet()) {
            if (entry.getValue().contains(album)) {
                return entry.getKey();
            }
        }
        throw new Exception("Artist not found for the song");
    }


}
