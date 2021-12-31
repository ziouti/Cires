package com.cires.service;


import com.cires.DAO.PersonneDAO;
import com.cires.entity.Personne;
import com.github.javafaker.Faker;
import com.google.gson.Gson;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;


@Service
public class PersonneService implements SPersonneinterface, UserDetailsService {
    //@Qualifier(smia dial la class)
    PersonneDAO dao;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public PersonneService(PersonneDAO dao, PasswordEncoder passwordEncoder) {
        this.dao = dao;
        this.passwordEncoder = passwordEncoder;
    }

    
    // to read a file : tu lui donne un file et 
    public static String readJsonFile(MultipartFile file) {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                jsonData += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return jsonData;
    }

    /**
     * @param count
     * @return
     */
    public JSONArray generatePersonnesJsonArray(int count) {
        JSONArray personnes = new JSONArray();
        for (int i = 0; i < count; i++) {
            Faker faker = new Faker();
            JSONObject personne = new JSONObject();
            personne.put("firstName", faker.name().firstName());
            personne.put("lastName", faker.name().lastName());
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            personne.put("birthDate", format1.format(faker.date().birthday(21, 90)));
            personne.put("city", faker.address().city());
            personne.put("country", faker.address().country());
            personne.put("avatar", faker.avatar().image());
            personne.put("company", faker.company().name());
            personne.put("jobPosition", faker.job().position());
            personne.put("mobile", faker.phoneNumber().cellPhone());
            personne.put("username", faker.name().username());
            personne.put("email", faker.internet().emailAddress());
            personne.put("password", passwordEncoder.encode("12345"));
            personne.put("role", faker.random().nextInt(0, 1));

            personnes.add(personne);
        }
        return personnes;

    }

    public void saveUsers(MultipartFile jsonFile) {
        JSONParser jsonParser = new JSONParser();
        String jsonString = readJsonFile(jsonFile);
        Gson gson = new Gson();
        JSONArray personnesListJson = null;
        try {
            personnesListJson = (JSONArray) jsonParser.parse(jsonString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (int i = 0; i < personnesListJson.size(); i++) {

            Personne pers = gson.fromJson(personnesListJson.get(i).toString(), Personne.class);
            dao.save(pers);
        }

    }

    public Personne findByUsername(String username) {
        return dao.findByUsername(username);

    }

    public void saveUser(Personne user) {
        dao.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Personne user = dao.findByUsername(username);
        if (null != user) {
            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), getAuthority(user));
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    private Set<SimpleGrantedAuthority> getAuthority(Personne user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        SimpleGrantedAuthority simpleGrantedAuthority = null;
        if (user.getRole() == 1) {
            simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");
        }
        if (user.getRole() == 0) {
            simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        }
        authorities.add(simpleGrantedAuthority);
        return authorities;
    }
}
