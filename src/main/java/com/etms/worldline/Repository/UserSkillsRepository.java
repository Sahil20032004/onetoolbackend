package com.etms.worldline.Repository;

import com.etms.worldline.model.SkillSet;
import com.etms.worldline.model.User;
import com.etms.worldline.model.UserSkills;
import com.etms.worldline.payload.request.skilldetails;
import com.etms.worldline.payload.response.Userdetails;
import com.etms.worldline.payload.response.lackingskilldetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UserSkillsRepository extends JpaRepository<UserSkills,Long> {
    @Query("select new com.etms.worldline.payload.request.skilldetails(u.skillSet.skillName,MAX(u.skill_level)) from UserSkills u where u.user.user_id=:userId GROUP BY u.skillSet.skillName")
    List<skilldetails> findtheempskillsbyid(@Param("userId") Long userId);
    @Query("select new com.etms.worldline.payload.request.skilldetails(u.skillSet.skillName,u.skill_level) from UserSkills u where u.user.user_id=:userId")
    List<skilldetails> findtheallempskillsbyid(@Param("userId") Long userId);
    @Query("select new com.etms.worldline.payload.response.lackingskilldetails(u.skillSet.skillName,u.skill_level,u.needskill_level) from UserSkills u where u.user.user_id=:userId and u.isTraining=true")
    List<lackingskilldetails> findtheallemplackingskillsbyid(@Param("userId") Long userId);
    @Query("select u from UserSkills u where u.user.user_id=:userId")
    List<UserSkills> findbyuserid(@Param("userId") Long userId);
    @Query(value = "SELECT latest_skill_levels.user_id, ROUND(AVG(latest_skill_levels.skill_level)) AS rounded_avg_skill_level " +
            "FROM (SELECT us.user_id, us.skill_id, MAX(us.skill_level) AS skill_level " +
            "FROM user_skills us " +
            "JOIN employee u ON u.user_id = us.user_id " +
            "JOIN skill_set s ON s.id = us.skill_id " +
            "WHERE " +
            "u.joining_date <= CURRENT_DATE - INTERVAL '90 days' " +
            "GROUP BY us.user_id, us.skill_id) latest_skill_levels " +
            "GROUP BY latest_skill_levels.user_id",
            nativeQuery = true)
    List<Object[]> getUserSkilldatas();
    @Query("SELECT new com.etms.worldline.payload.response.Userdetails(u.user_id,u.username,u.gender) from User u where u.roleName='USER'")
    List<Userdetails> getUsers();
    @Query(value = "SELECT latest_skill_levels.user_id, ROUND(AVG(latest_skill_levels.skill_level)) AS rounded_avg_skill_level " +
            "FROM (SELECT us.user_id, us.skill_id, MAX(us.skill_level) AS skill_level " +
            "FROM user_skills us " +
            "JOIN employee u ON u.user_id = us.user_id " +
            "JOIN skill_set s ON s.id = us.skill_id " +
            "WHERE u.gbl = :gbl " +
            "AND u.joining_date <= CURRENT_DATE - INTERVAL '90 days' " +
            "GROUP BY us.user_id, us.skill_id) latest_skill_levels " +
            "GROUP BY latest_skill_levels.user_id",
            nativeQuery = true)
    List<Object[]> getUserSkilldatasforGBL(@Param("gbl") String gbl);
    @Query(value = "SELECT latest_skill_levels.user_id, ROUND(AVG(latest_skill_levels.skill_level)) AS rounded_avg_skill_level " +
            "FROM (SELECT us.user_id, us.skill_id, MAX(us.skill_level) AS skill_level " +
            "FROM user_skills us " +
            "JOIN employee u ON u.user_id = us.user_id " +
            "JOIN skill_set s ON s.id = us.skill_id " +
            "WHERE u.gbl = :gbl " +
            "AND u.isget = true "+
            "AND u.batch = :batch "+
            "GROUP BY us.user_id, us.skill_id) latest_skill_levels " +
            "GROUP BY latest_skill_levels.user_id",
            nativeQuery = true)
    List<Object[]> getGETSkilldatasforGBL(@Param("gbl") String gbl,@Param("batch") String batch);
    @Query("SELECT u.user_id,u.functional_skill_level FROM User u WHERE u.gbl=:gbl AND u.isget=true AND u.batch=:batch")
    List<Object[]> getFunctionalSkillforEmployees(@Param("gbl") String gbl, @Param("batch") String batch);
    //    @Modifying
//    @Query("UPDATE UserSkills u SET u.skill_level=:skillLevel WHERE u.user.user_id=:userId AND u.skillSet.id=:skillId")
//    int updateSkillLevel(@Param("userId") Long userId,@Param("skillId") Long skillId, @Param("skillLevel") int skillLevel);
@Modifying
@Query("UPDATE UserSkills u SET u.skill_level=:skillLevel WHERE u.user.user_id=:userId AND u.skillSet.id=:skillId")
int updateSkillLevel(@Param("userId") Long userId,@Param("skillId") Long skillId, @Param("skillLevel") int skillLevel);
    @Modifying
    @Query("UPDATE UserSkills u SET u.needskill_level=:needskilllevel,u.isTraining=true WHERE u.user.user_id=:userId AND u.skillSet.id=:skillId")
    int updateTrainingandSetskilllevel(@Param("userId") Long userId,@Param("skillId") Long skillId, @Param("needskilllevel") int needskilllevel);
    @Query("SELECT us.skillSet.skillName, COUNT(us.user) AS lackingUsersCount " +
            "FROM UserSkills us " +
            "WHERE us.needskill_level > us.skill_level " +
            "GROUP BY us.skillSet.skillName " +
            "ORDER BY COUNT(us.user) DESC")
    List<Object[]> findTop4SkillsLackingByUsers();
    @Query("SELECT us.skillSet.skillName, COUNT(us.user) AS lackingUsersCount " +
            "FROM UserSkills us " +
            "JOIN us.user u " +
            "WHERE us.needskill_level > us.skill_level " +
            "AND u.gbl = :gbl " +
            "GROUP BY us.skillSet.skillName " +
            "ORDER BY COUNT(us.user) DESC")
    List<Object[]> findTop4SkillsLackingByUserswithgbl(@Param("gbl") String gbl);
    @Query("SELECT us.skillSet.skillName, COUNT(us.user) AS UsersCount " +
            "FROM UserSkills us " +
            "WHERE (us.skill_level >= us.needskill_level) " +
            "GROUP BY us.skillSet.skillName " +
            "ORDER BY COUNT(us.user) DESC")
    List<Object[]> findTop4SkillsultilizedByUsers();
    @Query("SELECT us.skillSet.skillName, COUNT(us.user) AS UsersCount " +
            "FROM UserSkills us " +
            "JOIN us.user u " +
            "WHERE (us.skill_level >= us.needskill_level) " +
            "AND u.gbl = :gbl " +
            "GROUP BY us.skillSet.skillName " +
            "ORDER BY COUNT(us.user) DESC")
    List<Object[]> findTop4SkillsutilisedByUserswithgbl(@Param("gbl") String gbl);
    @Query(value = "SELECT s.skill_name AS skillName, COUNT(us.user_id) AS utilizedEmployees " +
            "FROM skill_set s " +
            "LEFT JOIN user_skills us ON s.id = us.skill_id " +
            "GROUP BY s.skill_name " +
            "ORDER BY utilizedEmployees DESC", nativeQuery = true)
    List<Object[]> findAlltheskillmosttoleastutilised();
    @Query(value = "SELECT s.skill_name AS skillName, COUNT(us.user_id) AS utilizedEmployees " +
            "FROM skill_set s " +
            "LEFT JOIN user_skills us ON s.id = us.skill_id " +
            "JOIN employee e ON us.user_id = e.user_id " +
            "WHERE e.gbl = :gbl " +
            "GROUP BY s.skill_name " +
            "ORDER BY utilizedEmployees DESC", nativeQuery = true)
    List<Object[]> findAlltheskillmosttoleastutilisedbygbl(@Param("gbl") String gbl);
}
