package com.driver;

import java.util.*;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("spotify")
public class SpotifyController {

    //Autowire will not work in this case, no need to change this and add autowire
    SpotifyService spotifyService = new SpotifyService();

    @PostMapping("/add-user")
    public String createUser(@RequestParam(name = "name") String name, @RequestParam(name = "mobile") String mobile){
        // create the user with given name and number
        spotifyService.createUser(name, mobile);
        return "Success";
    }

    @PostMapping("/add-artist")
    public String createArtist(@RequestParam(name = "name") String name) {
        // create the artist with given name
        spotifyService.createArtist(name);
        return "Success";
    }

    @PostMapping("/add-album")
    public String createAlbum(@RequestParam(name = "title") String title, @RequestParam(name = "artistName") String artistName){
        // If the artist does not exist, first create an artist with given name
        if (!spotifyService.artistExists(artistName)) {
            spotifyService.createArtist(artistName);
        }
        // Create an album with given title and artist
        spotifyService.createAlbum(title, artistName);
        return "Success";
    }

    @PostMapping("/add-song")
    public String createSong(@RequestParam(name = "title") String title, @RequestParam(name = "albumName") String albumName, @RequestParam(name = "length") int length) {
        try {
            // If the album does not exist in the database, throw "Album does not exist" exception
            if (!spotifyService.albumExists(albumName)) {
                throw new Exception("Album does not exist");
            }
            // Create and add the song to the respective album
            spotifyService.createSong(title, albumName, length);
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/add-playlist-on-length")
    public String createPlaylistOnLength(@RequestParam(name = "mobile") String mobile, @RequestParam(name = "title") String title, @RequestParam(name = "length") int length) {
        try {
            // Create a playlist with given title and add all songs having the given length in the database to that playlist
            // The creator of the playlist will be the given user and will also be the only listener at the time of playlist creation
            // If the user does not exist, throw "User does not exist" exception
            if (!spotifyService.userExists(mobile)) {
                throw new Exception("User does not exist");
            }
            spotifyService.createPlaylistOnLength(mobile, title, length);
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PostMapping("/add-playlist-on-name")
    public String createPlaylistOnName(@RequestParam(name = "mobile") String mobile, @RequestParam(name = "title") String title, @RequestParam(name = "songTitles") List<String> songTitles) {
        try {
            // Create a playlist with given title and add all songs having the given titles in the database to that playlist
            // The creator of the playlist will be the given user and will also be the only listener at the time of playlist creation
            // If the user does not exist, throw "User does not exist" exception
            if (!spotifyService.userExists(mobile)) {
                throw new Exception("User does not exist");
            }
            spotifyService.createPlaylistOnName(mobile, title, songTitles);
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PutMapping("/find-playlist")
    public String findPlaylist(@RequestParam(name = "mobile") String mobile, @RequestParam(name = "playlistTitle") String playlistTitle) {
        try {
            // Find the playlist with given title and add the user as a listener to that playlist and update the user accordingly
            // If the user is the creator or already a listener, do nothing
            // If the user does not exist, throw "User does not exist" exception
            // If the playlist does not exist, throw "Playlist does not exist" exception
            // Return the playlist after updating
            if (!spotifyService.userExists(mobile)) {
                throw new Exception("User does not exist");
            }
            if (!spotifyService.playlistExists(playlistTitle)) {
                throw new Exception("Playlist does not exist");
            }
            spotifyService.findPlaylist(mobile, playlistTitle);
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @PutMapping("/like-song")
    public String likeSong(@RequestParam(name = "mobile") String mobile, @RequestParam(name = "songTitle") String songTitle) {
        try {
            // The user likes the given song. The corresponding artist of the song gets auto-liked
            // A song can be liked by a user only once. If a user tries to like a song multiple times, do nothing
            // However, an artist can indirectly have multiple likes from a user if the user has liked multiple songs of that artist.
            // If the user does not exist, throw "User does not exist" exception
            // If the song does not exist, throw "Song does not exist" exception
            // Return the song after updating
            if (!spotifyService.userExists(mobile)) {
                throw new Exception("User does not exist");
            }
            if (!spotifyService.songExists(songTitle)) {
                throw new Exception("Song does not exist");
            }
            spotifyService.likeSong(mobile, songTitle);
            return "Success";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @GetMapping("/popular-artist")
    public String mostPopularArtist() {
        // Return the artist name with maximum likes
        return spotifyService.mostPopularArtist();
    }

    @GetMapping("/popular-song")
    public String mostPopularSong() {
        // Return the song title with maximum likes
        return spotifyService.mostPopularSong();
    }
}