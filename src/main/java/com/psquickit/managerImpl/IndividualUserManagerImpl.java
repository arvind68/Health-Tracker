package com.psquickit.managerImpl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Charsets;
import com.psquickit.common.HandledException;
import com.psquickit.dao.UserDAO;
import com.psquickit.dto.FileStoreDTO;
import com.psquickit.dto.UserDTO;
import com.psquickit.manager.AuthenticationManager;
import com.psquickit.manager.FileStoreManager;
import com.psquickit.manager.IndividualUserManager;
import com.psquickit.pojo.user.BasicUserDetails;
import com.psquickit.pojo.user.IndividualUserRegisterRequest;
import com.psquickit.pojo.user.IndividualUserRegisterResponse;
import com.psquickit.pojo.user.IndividualUserUpdateRequest;
import com.psquickit.pojo.user.IndividualUserUpdateResponse;
import com.psquickit.pojo.user.UserDetailResponse;
import com.psquickit.util.CommonUtils;
import com.psquickit.util.ServiceUtils;

@Service
public class IndividualUserManagerImpl implements IndividualUserManager {

	private static Logger logger = Logger.getLogger(IndividualUserManagerImpl.class);

	@Autowired
	UserDAO userDAO;

	@Autowired
	FileStoreManager fileStoreManager;
	
	@Autowired
	AuthenticationManager authManager;
	
	@Override
	@Transactional(rollbackOn=Exception.class)
	public IndividualUserRegisterResponse registerUser(String secretToken, IndividualUserRegisterRequest request,
			MultipartFile profilePic) throws Exception {
		
		registerUserValidation(secretToken, request);
		
		FileStoreDTO profilePicFileStoreDTO = null;
		if (profilePic != null) {
			profilePicFileStoreDTO = fileStoreManager.uploadFile(profilePic.getInputStream(), profilePic.getContentType(), profilePic.getOriginalFilename());
		}
		
		return registerUser(request, profilePicFileStoreDTO);
	}

	private void registerUserValidation(String secretToken, IndividualUserRegisterRequest request) throws Exception {
		authManager.validateSecretToken(secretToken);
		UserDTO userDTO = userDAO.checkUIDExist(request.getUid());
		if (userDTO != null) {
			throw new HandledException("USER_ALREADY_REGISTERED",
					"User already exist. Please try again with different UID");
		}
	}

	private IndividualUserRegisterResponse registerUser(IndividualUserRegisterRequest request,
			FileStoreDTO profilePicFileStoreDTO) {
		UserDTO userDTO;
		userDTO = UserCommonManagerImpl.createUserDTO(request, profilePicFileStoreDTO);
		userDAO.save(userDTO);
		IndividualUserRegisterResponse response = new IndividualUserRegisterResponse();
		return ServiceUtils.setResponse(response, true, "Register User");
	}

	@Override
	@Transactional(rollbackOn=Exception.class)
	public IndividualUserRegisterResponse registerUser(String secretToken, IndividualUserRegisterRequest request) throws Exception {
		registerUserValidation(secretToken, request);
		
		FileStoreDTO profilePicFileStoreDTO = null;
		if (request.getProfileImg() != null) {
			InputStream is = new ByteArrayInputStream(request.getProfileImg().getBytes());
			profilePicFileStoreDTO = fileStoreManager.uploadFile(is, "application/image", request.getUid());
		}
		
		return registerUser(request, profilePicFileStoreDTO);
	}

	@Override
	@Transactional(rollbackOn=Exception.class)
	public IndividualUserUpdateResponse updateUser(String authToken, IndividualUserUpdateRequest request,
			MultipartFile profilePic) throws Exception {
		
		UserDTO userDTO = updateUserValidation(authToken, request);
		
		FileStoreDTO profilePicFileStoreDTO = userDTO.getProfileImageFileStore();
		if (profilePic != null) {
			if (profilePicFileStoreDTO != null) {
				fileStoreManager.updateFile(profilePicFileStoreDTO, profilePic.getInputStream(), profilePic.getContentType(), profilePic.getOriginalFilename());
			} else {
				profilePicFileStoreDTO = fileStoreManager.uploadFile(profilePic.getInputStream(), profilePic.getContentType(), profilePic.getOriginalFilename());
			}
		}
		
		return updateUser(request, userDTO, profilePicFileStoreDTO);
	}

	private IndividualUserUpdateResponse updateUser(IndividualUserUpdateRequest request, UserDTO userDTO,
			FileStoreDTO profilePicFileStoreDTO) {
		userDTO = UserCommonManagerImpl.updateUserDTO(request, userDTO, profilePicFileStoreDTO);
		userDAO.save(userDTO);
		IndividualUserUpdateResponse response = new IndividualUserUpdateResponse();
		response.setId(userDTO.getAadhaarNumber());
		return ServiceUtils.setResponse(response, true, "Update User");
	}

	private UserDTO updateUserValidation(String authToken, IndividualUserUpdateRequest request)
			throws Exception, HandledException {
		long userId = authManager.getUserId(authToken);
		
		UserDTO userDTO = userDAO.findOne(userId);
		if (userDTO == null) {
			throw new HandledException("USER_DOES_NOT_EXIST", "User does not exist.");
		}
		if (!request.getUid().equalsIgnoreCase(userDTO.getAadhaarNumber())) {
			throw new HandledException("CANNOT_UPDATE_UID", "Aadhaar number cannot be updated");
		}
		return userDTO;
	}
	
	@Override
	@Transactional(rollbackOn=Exception.class)
	public IndividualUserUpdateResponse updateUser(String authToken, IndividualUserUpdateRequest request) throws Exception {
		UserDTO userDTO = updateUserValidation(authToken, request);
		
		FileStoreDTO profilePicFileStoreDTO = userDTO.getProfileImageFileStore();
		InputStream is = new ByteArrayInputStream(request.getProfileImg().getBytes());
		if (profilePicFileStoreDTO != null) {
			fileStoreManager.updateFile(profilePicFileStoreDTO, is, "application/image", request.getUid());
		} else {
			profilePicFileStoreDTO = fileStoreManager.uploadFile(is, "application/image", request.getUid());
		}
		
		return updateUser(request, userDTO, profilePicFileStoreDTO);
	}
	
	@Override
	@Transactional
	public UserDetailResponse getUserDetail(String authToken) throws Exception {
		UserDetailResponse response = new UserDetailResponse();
		long userId = authManager.getUserId(authToken);
		UserDTO dto = userDAO.findOne(userId);
		FileStoreDTO profilePicFileStoreDTO = dto.getProfileImageFileStore();
		String profileImage = fileStoreManager.retrieveFile(profilePicFileStoreDTO).asCharSource(Charsets.UTF_8).read();
		response.setUserDetails(UserCommonManagerImpl.toBasicUserDetails(new BasicUserDetails(), dto, profileImage));
		return ServiceUtils.setResponse(response, true, "Get User Details");
	}
	
	@Override
	@Transactional
	public void getProfilePhoto(String authToken, HttpServletResponse httpResponse) throws Exception {
		long userId = authManager.getUserId(authToken);
		UserDTO dto = userDAO.findOne(userId);
		FileStoreDTO profilePicFileStoreDTO = dto.getProfileImageFileStore();
		httpResponse.setContentType(profilePicFileStoreDTO.getDocumentType());
		httpResponse.setHeader(CommonUtils.CONTENT_DISPOSITION, CommonUtils.CONTENT_DISPOSITION_ATTACHMENT + profilePicFileStoreDTO.getFileName());
		try (OutputStream outputStream = httpResponse.getOutputStream()) {
			outputStream.write(fileStoreManager.retrieveFile(profilePicFileStoreDTO).read());
		}
	}	
}
