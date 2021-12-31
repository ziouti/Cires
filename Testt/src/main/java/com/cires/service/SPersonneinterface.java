package com.cires.service;


import org.json.simple.JSONArray;
import org.springframework.web.multipart.MultipartFile;

import com.cires.entity.Personne;


    public interface SPersonneinterface {
	public Personne findByUsername(String username);
	public void saveUser(Personne user);
	public JSONArray generatePersonnesJsonArray( int count) ;
	public void saveUsers (MultipartFile jsonFile);

}
