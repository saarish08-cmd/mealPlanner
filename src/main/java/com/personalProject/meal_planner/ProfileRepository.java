package com.personalProject.meal_planner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    List<Profile> findByCookieId(String cookieId); //Get cookieId
}
