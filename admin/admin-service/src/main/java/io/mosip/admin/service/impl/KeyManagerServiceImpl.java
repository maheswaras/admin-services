package io.mosip.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.admin.controller.KeyManagerController;
import io.mosip.admin.dto.GenerateCsrDto;
import io.mosip.admin.dto.GenerateCsrResponseDto;
import io.mosip.admin.dto.UploadCertificateDto;
import io.mosip.admin.dto.UploadCertificateResponseDto;
import io.mosip.admin.packetstatusupdater.exception.MasterDataServiceException;
import io.mosip.admin.packetstatusupdater.util.AuditUtil;
import io.mosip.admin.packetstatusupdater.util.EventEnum;
import io.mosip.admin.service.KeyManagerService;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;

@Service
public class KeyManagerServiceImpl implements KeyManagerService {

	@Value("${mosip.kernel.keymanager.generatecsr}")
	private String generateCsr;

	@Value("${mosip.kernel.keymanager.getcertificate}")
	private String getCertificate;

	@Value("${mosip.kernel.keymanager.uploadcertificate}")
	private String uploadCertificate;

	@Value("${mosip.kernel.keymanager.uploadotherdomaincertificate}")
	private String uploadOtherDomainCertificate;

	@Autowired
	private AuditUtil audit;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	ObjectMapper mapper;

	@Override
	public ResponseWrapper<GenerateCsrResponseDto> generateCsr(RequestWrapper<GenerateCsrDto> generateCSRDto) {
		HttpHeaders h = new HttpHeaders();
		h.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<RequestWrapper<GenerateCsrDto>> httpEntity = new HttpEntity<>(generateCSRDto, h);
		ResponseEntity<ResponseWrapper> response = restTemplate.exchange(generateCsr, HttpMethod.POST, httpEntity,
				ResponseWrapper.class);
		if (null != response.getBody() && null != response.getBody().getErrors()) {
			audit.setAuditRequestDto(EventEnum.GENERATE_CSR_ERROR, null);
			throw new MasterDataServiceException(((ServiceError) response.getBody().getErrors().get(0)).getErrorCode(),
					((ServiceError) response.getBody().getErrors().get(0)).getMessage());

		}
		return response.getBody();

	}

	@Override
	public ResponseWrapper<GenerateCsrResponseDto> generateCsrCertificate(String applicationId, String referenceId) {

		HttpHeaders h = new HttpHeaders();
		h.setContentType(MediaType.APPLICATION_JSON);
		UriComponentsBuilder uribuilder = UriComponentsBuilder.fromUriString(getCertificate)
				.queryParam("applicationId", applicationId).queryParam("referenceId", referenceId);
		HttpEntity<RequestWrapper> httpReq = new HttpEntity<>(null, h);
		ResponseEntity<ResponseWrapper> response = restTemplate.exchange(uribuilder.toUriString(), HttpMethod.GET,
				httpReq, ResponseWrapper.class);

		if (null != response.getBody() && null != response.getBody().getErrors()) {
			audit.setAuditRequestDto(EventEnum.GENERATE_CSR_CERTIFICATE_ERROR, null);
			throw new MasterDataServiceException(((ServiceError) response.getBody().getErrors().get(0)).getErrorCode(),
					((ServiceError) response.getBody().getErrors().get(0)).getMessage());

		}
		return response.getBody();

	}

	@Override
	public ResponseWrapper<UploadCertificateResponseDto> uploadCertificate(
			RequestWrapper<UploadCertificateDto> uploadCertificateDto, boolean otherDomain) {
		String url = null;
		if (otherDomain)
			url = uploadOtherDomainCertificate;
		else
			url = uploadCertificate;
		HttpHeaders h = new HttpHeaders();
		h.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<RequestWrapper<UploadCertificateDto>> httpEntity = new HttpEntity<>(uploadCertificateDto, h);
		ResponseEntity<ResponseWrapper> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity,
				ResponseWrapper.class);
		if (null != response.getBody() && null != response.getBody().getErrors()) {
			if (otherDomain)
				audit.setAuditRequestDto(EventEnum.UPLOAD_OTHER_DOMAIN_CERTIFICATE_ERROR, null);
			else
				audit.setAuditRequestDto(EventEnum.UPLOAD_CERTIFICATE_ERROR, null);
			throw new MasterDataServiceException(((ServiceError) response.getBody().getErrors().get(0)).getErrorCode(),
					((ServiceError) response.getBody().getErrors().get(0)).getMessage());

		}

		return response.getBody();
	}

}