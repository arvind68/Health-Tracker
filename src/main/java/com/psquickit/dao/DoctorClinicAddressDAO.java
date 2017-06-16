package com.psquickit.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.psquickit.dto.ClinicAddressDTO;
import com.psquickit.dto.DoctorDegreeDTO;

@Repository
public interface DoctorClinicAddressDAO extends JpaRepository<ClinicAddressDTO, Long> {

	@Query("Select d from ClinicAddressDTO d where d.addressId.id = :id")
	public List<ClinicAddressDTO> listClinicAddressById(@Param("id") Long id);
}
