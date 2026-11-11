package com.photography.demo.controller;

import com.photography.demo.model.Photo;
import com.photography.demo.repository.PhotoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/photos")
//@CrossOrigin(origins = "*")
public class PhotoController {

    private final PhotoRepository photoRepository;

    // Constructor injection
    public PhotoController(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Photo>> getAllPhotos(){
        List<Photo> photos = photoRepository.findAll();
        return ResponseEntity.ok(photos); // Returns HTTP 200 OK with the list
    }

    @PostMapping
    public ResponseEntity<Photo> createPhoto(@RequestBody Photo photo){
        Photo SavedPhoto = photoRepository.save(photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(SavedPhoto); // Returns HTTP 201 Created
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        if (photoRepository.existsById(id)) {
            photoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
