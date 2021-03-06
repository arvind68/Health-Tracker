package com.psquickit.dto;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the filestore database table.
 * 
 */
@Entity
@Table(name="Filestore")
@NamedQuery(name="Filestore.findAll", query="SELECT f FROM FileStoreDTO f")
public class FileStoreDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Long id;

	private Long createdBy;

	private Timestamp createdOn;

	private String documentType;

	private String fileName;

	private String location;

	private Long updatedBy;

	private Timestamp updatedOn;

	private String version;

	//bi-directional many-to-one association to Diagnosisreportfile
	@OneToMany(mappedBy="filestore")
	private List<DiagnosisReportFileDTO> diagnosisreportfiles;

	//bi-directional many-to-one association to Prescriptionfile
	@OneToMany(mappedBy="filestore")
	private List<PrescriptionFileDTO> prescriptionfiles;

	//bi-directional many-to-one association to Testreportfile
	@OneToMany(mappedBy="filestore")
	private List<TestReportFileDTO> testreportfiles;

	//bi-directional many-to-one association to User
	@OneToMany(mappedBy="filestore")
	private List<UserDTO> users;

	public FileStoreDTO() {
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

	public String getDocumentType() {
		return this.documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
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

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<DiagnosisReportFileDTO> getDiagnosisreportfiles() {
		return this.diagnosisreportfiles;
	}

	public void setDiagnosisreportfiles(List<DiagnosisReportFileDTO> diagnosisreportfiles) {
		this.diagnosisreportfiles = diagnosisreportfiles;
	}

	public DiagnosisReportFileDTO addDiagnosisreportfile(DiagnosisReportFileDTO diagnosisreportfile) {
		getDiagnosisreportfiles().add(diagnosisreportfile);
		diagnosisreportfile.setFilestore(this);

		return diagnosisreportfile;
	}

	public DiagnosisReportFileDTO removeDiagnosisreportfile(DiagnosisReportFileDTO diagnosisreportfile) {
		getDiagnosisreportfiles().remove(diagnosisreportfile);
		diagnosisreportfile.setFilestore(null);

		return diagnosisreportfile;
	}

	public List<PrescriptionFileDTO> getPrescriptionfiles() {
		return this.prescriptionfiles;
	}

	public void setPrescriptionfiles(List<PrescriptionFileDTO> prescriptionfiles) {
		this.prescriptionfiles = prescriptionfiles;
	}

	public PrescriptionFileDTO addPrescriptionfile(PrescriptionFileDTO prescriptionfile) {
		getPrescriptionfiles().add(prescriptionfile);
		prescriptionfile.setFilestore(this);

		return prescriptionfile;
	}

	public PrescriptionFileDTO removePrescriptionfile(PrescriptionFileDTO prescriptionfile) {
		getPrescriptionfiles().remove(prescriptionfile);
		prescriptionfile.setFilestore(null);

		return prescriptionfile;
	}

	public List<TestReportFileDTO> getTestreportfiles() {
		return this.testreportfiles;
	}

	public void setTestreportfiles(List<TestReportFileDTO> testreportfiles) {
		this.testreportfiles = testreportfiles;
	}

	public TestReportFileDTO addTestreportfile(TestReportFileDTO testreportfile) {
		getTestreportfiles().add(testreportfile);
		testreportfile.setFilestore(this);

		return testreportfile;
	}

	public TestReportFileDTO removeTestreportfile(TestReportFileDTO testreportfile) {
		getTestreportfiles().remove(testreportfile);
		testreportfile.setFilestore(null);

		return testreportfile;
	}

	public List<UserDTO> getUsers() {
		return this.users;
	}

	public void setUsers(List<UserDTO> users) {
		this.users = users;
	}

	public UserDTO addUser(UserDTO user) {
		getUsers().add(user);
		user.setProfileImageFileStore(this);

		return user;
	}

	public UserDTO removeUser(UserDTO user) {
		getUsers().remove(user);
		user.setProfileImageFileStore(null);

		return user;
	}

}