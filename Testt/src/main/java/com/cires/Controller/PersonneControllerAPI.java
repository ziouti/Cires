package com.cires.Controller;


import com.cires.config.JwtTokenUtil;
import com.cires.entity.Personne;
import com.cires.service.PersonneService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;


@RestController
@CrossOrigin()
@RequestMapping("/api/users")
public class PersonneControllerAPI {
    @Autowired
    PersonneService personneService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private PersonneService userDetailsService;

  

    @GetMapping("/generate")
    public ResponseEntity<byte[]> affichertt(@RequestParam int count) {
        JSONArray personnes = personneService.generatePersonnesJsonArray(count);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(personnes);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        byte[] isr = json.getBytes();
        String fileName = "personnes.json";
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentLength(isr.length);
        respHeaders.setContentType(new MediaType("text", "json"));
        respHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        respHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return new ResponseEntity<byte[]>(isr, respHeaders, HttpStatus.OK);

    }

    @PostMapping("/batch")
    public ResponseEntity<String> uploadFile(@RequestParam("jsonFile") MultipartFile jsonFile) {
        String message = "";
        try {
            personneService.saveUsers(jsonFile);

            message = "Uploaded the file successfully: " + jsonFile.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "Could not upload the file: " + jsonFile.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }
    
    @PostMapping(value = "/auth")
    public ResponseEntity<Object> createAuthenticationToken(@RequestBody LoginRequest loginRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        UserDetails userdetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        Personne personne = personneService.findByUsername(loginRequest.getUsername());
        String token = jwtTokenUtil.generateToken(userdetails);
        httpServletResponse.setHeader("typ", "jwt");
        HashMap<String, Object> response = new HashMap<>();
        response.put("access_token", token);
        response.put("Role", jwtTokenUtil.getRolesFromToken(token));
        response.put("email", personne.getEmail());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/me")
    public ResponseEntity<Object> myDetails() {
        Personne personne = userDetailsService.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
        return new ResponseEntity<>(personne, HttpStatus.OK);
    }

    @GetMapping("/{username}")
    public ResponseEntity<Object> myDetails(@PathVariable(value = "username") String username) {
        Personne personne = userDetailsService.findByUsername(username);
        return new ResponseEntity<>(personne, HttpStatus.OK);
    }


}

