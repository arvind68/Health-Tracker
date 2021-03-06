package com.psquickit.managerImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.psquickit.common.HandledException;
import com.psquickit.common.UserType;
import com.psquickit.dao.DegreeMasterDAO;
import com.psquickit.dao.DoctorDegreeDAO;
import com.psquickit.dao.DoctorMciDAO;
import com.psquickit.dao.DoctorSpecializationDAO;
import com.psquickit.dao.DoctorUserDAO;
import com.psquickit.dao.MCIMasterDAO;
import com.psquickit.dao.SpecializationMasterDAO;
import com.psquickit.dao.UserDAO;
import com.psquickit.dto.DegreeMasterDTO;
import com.psquickit.dto.DoctorDegreeDTO;
import com.psquickit.dto.DoctorMciDTO;
import com.psquickit.dto.DoctorSpecializationDTO;
import com.psquickit.dto.DoctorUserDTO;
import com.psquickit.dto.FileStoreDTO;
import com.psquickit.dto.MciMasterDTO;
import com.psquickit.dto.SpecializationMasterDTO;
import com.psquickit.dto.UserDTO;
import com.psquickit.manager.AuthenticationManager;
import com.psquickit.manager.DoctorUserManager;
import com.psquickit.manager.FileStoreManager;
import com.psquickit.pojo.user.Degree;
import com.psquickit.pojo.user.DoctorDegree;
import com.psquickit.pojo.user.DoctorMci;
import com.psquickit.pojo.user.DoctorSpecialization;
import com.psquickit.pojo.user.DoctorUserDetailResponse;
import com.psquickit.pojo.user.DoctorUserDetails;
import com.psquickit.pojo.user.DoctorUserRegisterRequest;
import com.psquickit.pojo.user.DoctorUserRegisterResponse;
import com.psquickit.pojo.user.DoctorUserUpdateRequest;
import com.psquickit.pojo.user.DoctorUserUpdateResponse;
import com.psquickit.pojo.user.ListAllDegreeResponse;
import com.psquickit.pojo.user.ListAllMciResponse;
import com.psquickit.pojo.user.ListAllSpecializationResponse;
import com.psquickit.pojo.user.Mci;
import com.psquickit.pojo.user.Specialization;
import com.psquickit.util.ServiceUtils;

@Service
public class DoctorUserManagerImpl implements DoctorUserManager {

	private static Logger logger = Logger.getLogger(DoctorUserManagerImpl.class);

	@Autowired
	public UserDAO userDAO;

	@Autowired
	public DoctorUserDAO doctorUserDAO;

	@Autowired
	public DegreeMasterDAO degreeMasterDAO;

	@Autowired
	public MCIMasterDAO mciMasterDAO;

	@Autowired
	public SpecializationMasterDAO specializationMasterDAO;

	@Autowired
	public DoctorSpecializationDAO doctorSpecializationDAO;

	@Autowired
	public DoctorDegreeDAO doctorDegreeDAO;

	@Autowired
	public DoctorMciDAO doctorMciDAO;
	
	@Autowired
	FileStoreManager fileStoreManager;
	
	@Autowired
	AuthenticationManager authManager;

	@Override
	@Transactional(rollbackOn=Exception.class)
	public DoctorUserRegisterResponse registerDoctor(String secretToken, DoctorUserRegisterRequest request, MultipartFile profilePic)
			throws Exception {
		registerDoctorValidation(secretToken, request);
		
		FileStoreDTO profilePicFileStoreDTO = null;
		if (profilePic != null) {
			profilePicFileStoreDTO = fileStoreManager.uploadFile(profilePic.getInputStream(), profilePic.getContentType(), profilePic.getOriginalFilename());
		}
		
		return registerDoctor(secretToken, request, profilePicFileStoreDTO);
	}

	private void registerDoctorValidation(String secretToken, DoctorUserRegisterRequest request) throws Exception {
		authManager.validateSecretToken(secretToken);
		UserDTO userDTO = userDAO.checkUIDExist(request.getUid());
		if (userDTO != null) {
			throw new HandledException("DUPLICATE_USER_REGISTRATION", "Duplicate User Registration");
		}
	}

	private DoctorUserRegisterResponse registerDoctor(String secretToken, DoctorUserRegisterRequest request,
			FileStoreDTO profilePicFileStoreDTO) {
		UserDTO userDTO;
		userDTO = UserCommonManagerImpl.createUserDTO(request, profilePicFileStoreDTO);
		userDTO = userDAO.save(userDTO);
		
		DoctorUserDTO doctorUserDTO = createDoctorDTO(request, userDTO, profilePicFileStoreDTO);
		doctorUserDTO = doctorUserDAO.save(doctorUserDTO);
		saveDoctorDegrees(request.getDegrees(), doctorUserDTO);
		saveDoctorMcis(request.getMciReg(), doctorUserDTO);
		saveDoctorSpecializations(request.getSpecialization(), doctorUserDTO);
		
		DoctorUserRegisterResponse response = new DoctorUserRegisterResponse();
		return ServiceUtils.setResponse(response, true, "Register Doctor User");
	}
	
	@Override
	@Transactional(rollbackOn=Exception.class)
	public DoctorUserRegisterResponse registerDoctor(String secretToken, DoctorUserRegisterRequest request) throws Exception {
		registerDoctorValidation(secretToken, request);
		
		FileStoreDTO profilePicFileStoreDTO = null;
		if (request.getProfileImg() != null) {
			InputStream is = new ByteArrayInputStream(request.getProfileImg().getBytes());
			profilePicFileStoreDTO = fileStoreManager.uploadFile(is, "application/image", request.getUid());
		}
		return registerDoctor(secretToken, request, profilePicFileStoreDTO);
	}

	private void saveDoctorSpecializations(List<DoctorSpecialization> listSpecialization, DoctorUserDTO doctorUserDTO) {
		List<DoctorSpecializationDTO> listDoctorSpecializationDTO = Lists.newArrayList();
		for (DoctorSpecialization speciliazation : listSpecialization) {
			DoctorSpecializationDTO doctorSpecializationDTO = new DoctorSpecializationDTO();
			doctorSpecializationDTO.setDoctorUser(doctorUserDTO);
			doctorSpecializationDTO.setSpecializationMaster(specializationMasterDAO.findOne(Long.parseLong(speciliazation.getId())));
			listDoctorSpecializationDTO.add(doctorSpecializationDTO);
		}
		doctorSpecializationDAO.save(listDoctorSpecializationDTO);
	}

	private void saveDoctorDegrees(List<DoctorDegree> listDegree, DoctorUserDTO doctorUserDTO) {
		List<DoctorDegreeDTO> listDoctorDegreeDTO = Lists.newArrayList();
		for (DoctorDegree degree : listDegree) {
			DoctorDegreeDTO doctorDegreeDTO = new DoctorDegreeDTO();
			doctorDegreeDTO.setDoctorUser(doctorUserDTO);
			doctorDegreeDTO.setDegreeMaster(degreeMasterDAO.findOne(Long.parseLong(degree.getId())));
			listDoctorDegreeDTO.add(doctorDegreeDTO);
		}
		doctorDegreeDAO.save(listDoctorDegreeDTO);
	}

	private void saveDoctorMcis(List<DoctorMci> listMCI, DoctorUserDTO doctorUserDTO) {
		List<DoctorMciDTO> listDoctorMciDTO = Lists.newArrayList();
		for (DoctorMci mci : listMCI) {
			DoctorMciDTO doctorMciDTO = new DoctorMciDTO();
			doctorMciDTO.setDoctorUser(doctorUserDTO);
			doctorMciDTO.setMciMaster(mciMasterDAO.findOne(Long.parseLong(mci.getId())));
			doctorMciDTO.setRegistrationNumber(mci.getRegistrationNumber());
			listDoctorMciDTO.add(doctorMciDTO);
		}
		doctorMciDAO.save(listDoctorMciDTO);
	}

	private <T extends DoctorUserDetails> DoctorUserDTO createDoctorDTO(T request, UserDTO userDTO, FileStoreDTO profilePicFileStoreDTO) {
		DoctorUserDTO doctorUserDTO = new DoctorUserDTO();
		return updateDoctorUserDTO(request, doctorUserDTO, userDTO);
	}

	@Override
	@Transactional
	public ListAllDegreeResponse listAllDegree(String secretToken) throws Exception {
		authManager.validateSecretToken(secretToken);
		ListAllDegreeResponse response = new ListAllDegreeResponse();
		List<DegreeMasterDTO> degreeMasterDTO = degreeMasterDAO.findAll();
		List<Degree> list = Lists.newArrayList();
		for (DegreeMasterDTO degrees : degreeMasterDTO) {
			Degree degree = new Degree();
			degree.setId(degrees.getId().toString());
			degree.setTitle(degrees.getDegreeName());
			list.add(degree);
		}
		response.getDegrees().addAll(list);
		return ServiceUtils.setResponse(response, true, "List all degrees");
	}

	@Override
	@Transactional
	public ListAllMciResponse listAllMci(String secretToken) throws Exception {
		authManager.validateSecretToken(secretToken);
		ListAllMciResponse response = new ListAllMciResponse();
		List<MciMasterDTO> listMciMasterDTO = mciMasterDAO.findAll();
		List<Mci> listMci = Lists.newArrayList();
		for (MciMasterDTO mciMatserDTO : listMciMasterDTO) {
			Mci mci = new Mci();
			mci.setId(mciMatserDTO.getId().toString());
			mci.setTitle(mciMatserDTO.getMciName());
			listMci.add(mci);
		}
		response.getMcis().addAll(listMci);
		return ServiceUtils.setResponse(response, true, "List all MCI");
	}

	@Override
	@Transactional
	public ListAllSpecializationResponse listAllSpecialization(String secretToken) throws Exception {
		authManager.validateSecretToken(secretToken);
		ListAllSpecializationResponse response = new ListAllSpecializationResponse();
		List<SpecializationMasterDTO> listSplMasterDTO = specializationMasterDAO.findAll();
		List<Specialization> listSpecialization = Lists.newArrayList();
		for (SpecializationMasterDTO spiDTO : listSplMasterDTO) {
			Specialization specialization = new Specialization();
			specialization.setId(spiDTO.getId().toString());
			specialization.setTitle(spiDTO.getSpecializationName());
			listSpecialization.add(specialization);
		}
		response.getSpeciliazations().addAll(listSpecialization);
		return ServiceUtils.setResponse(response, true, "List all specializations");
	}

	@Override
	@Transactional(rollbackOn=Exception.class)
	public DoctorUserUpdateResponse updateDoctor(String authToken, DoctorUserUpdateRequest request, MultipartFile profilePic)
			throws Exception {
		DoctorUserDTO doctorUserDTO = updateUserValidation(authToken, request);
		
		FileStoreDTO profilePicFileStoreDTO = doctorUserDTO.getUser().getProfileImageFileStore();
		if (profilePic != null) {
			if (profilePicFileStoreDTO != null) {
				fileStoreManager.updateFile(profilePicFileStoreDTO, profilePic.getInputStream(), profilePic.getContentType(), profilePic.getOriginalFilename());
			} else {
				profilePicFileStoreDTO = fileStoreManager.uploadFile(profilePic.getInputStream(), profilePic.getContentType(), profilePic.getOriginalFilename());
			}
		}
		
		return updateDoctor(request, doctorUserDTO, profilePicFileStoreDTO);
	}

	private DoctorUserDTO updateUserValidation(String authToken, DoctorUserUpdateRequest request)
			throws Exception, HandledException {
		long userId = authManager.getUserId(authToken);
		
		DoctorUserDTO doctorUserDTO = doctorUserDAO.getDoctorUserByUserId(userId);
		if (doctorUserDTO == null) {
			throw new HandledException("USER_DOES_NOT_EXIST", "User does not exist");
		}
		if (!request.getUid().equalsIgnoreCase(doctorUserDTO.getUser().getAadhaarNumber())) {
			throw new HandledException("CANNOT_UPDATE_UID", "Aadhaar number cannot be updated");
		}
		return doctorUserDTO;
	}

	private DoctorUserUpdateResponse updateDoctor(DoctorUserUpdateRequest request, DoctorUserDTO doctorUserDTO,
			FileStoreDTO profilePicFileStoreDTO) {
		UserDTO userDTO = UserCommonManagerImpl.updateUserDTO(request, doctorUserDTO.getUser(), profilePicFileStoreDTO);
		doctorUserDTO = updateDoctorUserDTO(request, doctorUserDTO, userDTO);
		doctorUserDAO.save(doctorUserDTO);
		
		updateDoctorDegrees(request.getDegrees(), doctorUserDTO);
		updateDoctorSpecialization(request.getSpecialization(), doctorUserDTO);
		updateDoctorMci(request.getMciReg(), doctorUserDTO);
		
		DoctorUserUpdateResponse response = new DoctorUserUpdateResponse();
		
		return ServiceUtils.setResponse(response, true, "Update doctor user details");
	}

	@Override
	@Transactional(rollbackOn=Exception.class)
	public DoctorUserUpdateResponse updateDoctor(String authToken, DoctorUserUpdateRequest request) throws Exception {
		DoctorUserDTO doctorUserDTO = updateUserValidation(authToken, request);
		
		FileStoreDTO profilePicFileStoreDTO = doctorUserDTO.getUser().getProfileImageFileStore();
		if (request.getProfileImg() != null) {
			InputStream is = new ByteArrayInputStream(request.getProfileImg().getBytes());
			if (profilePicFileStoreDTO != null) {
				fileStoreManager.updateFile(profilePicFileStoreDTO, is, "application/image", request.getUid());
			} else {
				profilePicFileStoreDTO = fileStoreManager.uploadFile(is, "application/image", request.getUid());
			}
		}
		
		return updateDoctor(request, doctorUserDTO, profilePicFileStoreDTO);
	}
	
	private void updateDoctorMci(List<DoctorMci> mciList, DoctorUserDTO doctorUserDTO) {
		List<DoctorMciDTO> doctorMciDTOs = doctorMciDAO.listMciByDoctorId(doctorUserDTO.getId());
		doctorMciDAO.delete(doctorMciDTOs);
		saveDoctorMcis(mciList, doctorUserDTO);
		
	}

	private void updateDoctorSpecialization(List<DoctorSpecialization> specializationList, DoctorUserDTO doctorUserDTO) {
		List<DoctorSpecializationDTO> doctorSpecializationDTOs = doctorSpecializationDAO.listSpecializationByDoctorId(doctorUserDTO.getId());
		doctorSpecializationDAO.delete(doctorSpecializationDTOs);
		saveDoctorSpecializations(specializationList, doctorUserDTO);
		
	}

	private void updateDoctorDegrees(List<DoctorDegree> degreesList, DoctorUserDTO doctorUserDTO) {
		List<DoctorDegreeDTO> doctorDegreeDTOs = doctorDegreeDAO.listDegreeByDoctorId(doctorUserDTO.getId());
		doctorDegreeDAO.delete(doctorDegreeDTOs);
		saveDoctorDegrees(degreesList, doctorUserDTO);
		
	}

	private <T extends DoctorUserDetails> DoctorUserDTO updateDoctorUserDTO(T request, DoctorUserDTO doctorUserDTO, UserDTO userDTO) {
		doctorUserDTO.setUser(userDTO);
		doctorUserDTO.setClinicAddress(request.getClinicAddress());
		doctorUserDTO.setClinicContactNumber(request.getClinicContactNo());
		doctorUserDTO.setClinicAlternateContactNumber(request.getAlternateContactNo());
		doctorUserDTO.setPracticeArea(request.getPracticeArea());
		doctorUserDTO.setInPersonConsultant(request.getInPersonConsultant());
		doctorUserDTO.setEConsultant(request.getEConsultant());
		return doctorUserDTO;
	}
	
	@Override
	@Transactional
	public DoctorUserDetailResponse getDoctorUserDetail(String authToken) throws Exception {
		DoctorUserDetailResponse response = new DoctorUserDetailResponse();
		long userId = authManager.getUserId(authToken);
		UserDTO userDTO = userDAO.findOne(userId);
		if (UserType.fromName(userDTO.getUserType()) == UserType.DOCTOR_USER) {
			DoctorUserDTO doctorUserDTO = doctorUserDAO.getDoctorUserByUserId(userDTO.getId());
			List<DoctorDegreeDTO> doctorDegreeDTOs = doctorDegreeDAO.listDegreeByDoctorId(doctorUserDTO.getId());
			List<DoctorSpecializationDTO> doctorSpecializationDTOs = doctorSpecializationDAO.listSpecializationByDoctorId(doctorUserDTO.getId());
			List<DoctorMciDTO> doctorMciDTOs = doctorMciDAO.listMciByDoctorId(doctorUserDTO.getId());
			DoctorUserDetails details = new DoctorUserDetails();
			
			FileStoreDTO profilePicFileStoreDTO = userDTO.getProfileImageFileStore();
			String profileImage = fileStoreManager.retrieveFile(profilePicFileStoreDTO).asCharSource(Charsets.UTF_8).read();
			
			details = UserCommonManagerImpl.toBasicUserDetails(details, doctorUserDTO.getUser(), profileImage);
			details.setClinicAddress(doctorUserDTO.getClinicAddress());
			details.setPracticeArea(doctorUserDTO.getPracticeArea());
			details.setInPersonConsultant(doctorUserDTO.getInPersonConsultant());
			details.setEConsultant(doctorUserDTO.getEConsultant());
			details.setClinicContactNo(doctorUserDTO.getClinicContactNumber());
			details.getDegrees().addAll(toDoctorDegree(doctorDegreeDTOs));
			details.getSpecialization().addAll(toDoctorSpecialization(doctorSpecializationDTOs));
			details.getMciReg().addAll(toDoctorMci(doctorMciDTOs));
			response.setDoctorUserDetails(details);
		} else {
			throw new HandledException("NOT_A_DOCTOR_USER", "Not a doctor user");
		}
		
		return ServiceUtils.setResponse(response, true, "Get User Details");
	}

	private List<DoctorMci> toDoctorMci(List<DoctorMciDTO> doctorMciDTOs) {
		List<DoctorMci> list = Lists.newArrayList();
		for (DoctorMciDTO dto: doctorMciDTOs) {
			DoctorMci mci = new DoctorMci();
			mci.setId(Long.toString(dto.getId()));
			mci.setRegistrationNumber(dto.getRegistrationNumber());
			mci.setTitle(dto.getMciMaster().getMciName());
			list.add(mci);
		}
		return list;
	}

	private List<DoctorSpecialization> toDoctorSpecialization(
			List<DoctorSpecializationDTO> doctorSpecializationDTOs) {
		List<DoctorSpecialization> list = Lists.newArrayList();
		for (DoctorSpecializationDTO dto: doctorSpecializationDTOs) {
			DoctorSpecialization s = new DoctorSpecialization();
			s.setId(Long.toString(dto.getId()));
			s.setTitle(dto.getSpecializationMaster().getSpecializationName());
			list.add(s);
		}
		return list;
	}

	private List<DoctorDegree> toDoctorDegree(List<DoctorDegreeDTO> doctorDegreeDTOs) {
		List<DoctorDegree> list = Lists.newArrayList();
		for (DoctorDegreeDTO dto: doctorDegreeDTOs) {
			DoctorDegree s = new DoctorDegree();
			s.setId(Long.toString(dto.getId()));
			s.setTitle(dto.getDegreeMaster().getDegreeName());
			list.add(s);
		}
		return list;
	}
	
}
