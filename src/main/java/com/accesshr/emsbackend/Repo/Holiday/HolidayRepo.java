package com.accesshr.emsbackend.Repo.Holiday;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.accesshr.emsbackend.Entity.Holiday;

public interface HolidayRepo extends JpaRepository<Holiday, Integer> {


	void deleteAllById(int id);

	Holiday getAllById(int id);
	@Modifying
    @Transactional
	@Query("DELETE FROM Holiday c WHERE c.date <= :currentDate")
	void deletePreviousHoliday(@Param("currentDate") LocalDate currentDate);
	
}
