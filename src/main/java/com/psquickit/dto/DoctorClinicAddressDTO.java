package com.psquickit.dto;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the doctorclinicaddress database table.
 * 
 */
@Entity
@Table(name="Doctorclinicaddress")
@NamedQuery(name="Doctorclinicaddress.findAll", query="SELECT d FROM DoctorClinicAddressDTO d")
public class DoctorClinicAddressDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Long id;

	private Long createdBy;

	private Timestamp createdOn;

	private Long updatedBy;

	private Timestamp updatedOn;

	//bi-directional many-to-one association to Address
	@ManyToOne
	@JoinColumn(name="AddressId")
	private AddressDTO address;

	//bi-directional many-to-one association to Doctoruser
	@ManyToOne
	@JoinColumn(name="DoctorUserId")
	private DoctorUserDTO doctoruser;

	public DoctorClinicAddressDTO() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedOn() {
		return this.createdOn;
	}

	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}

	public Long getUpdatedBy() {
		return this.updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Timestamp getUpdatedOn() {
		return this.updatedOn;
	}

	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}

	public AddressDTO getAddress() {
		return this.address;
	}

	public void setAddress(AddressDTO address) {
		this.address = address;
	}

	public DoctorUserDTO getDoctoruser() {
		return this.doctoruser;
	}

	public void setDoctoruser(DoctorUserDTO doctoruser) {
		this.doctoruser = doctoruser;
	}

}