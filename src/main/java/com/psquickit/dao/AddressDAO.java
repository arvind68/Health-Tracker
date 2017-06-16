package com.psquickit.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.psquickit.dto.AddressDTO;

@Repository
public interface AddressDAO extends JpaRepository<AddressDTO, Long> {
	@Query("Select d from AddressDTO d where d.id = :id")
	public List<AddressDTO> listDegreeByDoctorId(@Param("id") Long id);
}
