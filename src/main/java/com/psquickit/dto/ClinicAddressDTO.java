package com.psquickit.dto;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "doctorclinicaddress")
public class ClinicAddressDTO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", unique = true, nullable = false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "DoctorUserId")
	private DoctorUserDTO doctorUserDTO;

	@ManyToOne
	@JoinColumn(name = "AddressId")
	private AddressDTO addressId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DoctorUserDTO getDoctorUserDTO() {
		return doctorUserDTO;
	}

	public void setDoctorUserDTO(DoctorUserDTO doctorUserDTO) {
		this.doctorUserDTO = doctorUserDTO;
	}

	public AddressDTO getAddressId() {
		return addressId;
	}

	public void setAddressId(AddressDTO addressId) {
		this.addressId = addressId;
	}
	
	
	
}
