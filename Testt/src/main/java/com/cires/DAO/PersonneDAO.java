package com.cires.DAO;




import org.springframework.data.jpa.repository.JpaRepository;
import com.cires.entity.Personne;


public interface PersonneDAO extends JpaRepository<Personne, String> {
	Personne findByUsername(String username);
	
	
}
