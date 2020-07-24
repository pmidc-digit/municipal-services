/*
package org.egov.pgr.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.UserInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.repository.ServiceRequestRepository;
import org.egov.pgr.web.models.RequestSearchCriteria;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.pgr.web.models.user.UserDetailResponse;
import org.egov.pgr.web.models.user.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.egov.pgr.util.PGRConstants.USERTYPE_CITIZEN;


@Slf4j
@Service
public class UserService {


    private ObjectMapper mapper;

    private ServiceRequestRepository serviceRequestRepository;

    private PGRConfiguration config;


    private TradeUtil tradeUtil;

    private TLRepository repository;

    @Autowired
    public UserService(ObjectMapper mapper, ServiceRequestRepository serviceRequestRepository, PGRConfiguration config, TradeUtil tradeUtil, TLRepository repository) {
        this.mapper = mapper;
        this.serviceRequestRepository = serviceRequestRepository;
        this.config = config;
        this.tradeUtil=tradeUtil;
        this.repository=repository;
    }


    */
/**
     * Creates users with uuid as username if uuid is already present for the user
     * in the request then the user is updated
     * @param request TradeLciense create or update request
     *//*


    public void createUser(TradeLicenseRequest request,boolean isBPARoleAddRequired){
        List<TradeLicense> licenses = request.getLicenses();
        RequestInfo requestInfo = request.getRequestInfo();
        Role role = getCitizenRole(licenses.get(0).getTenantId());
        licenses.forEach(tradeLicense -> {

           */
/* Set<String> listOfMobileNumbers = getMobileNumbers(tradeLicense.getTradeLicenseDetail().getOwners()
                    ,requestInfo,tradeLicense.getTenantId());*//*


            tradeLicense.getTradeLicenseDetail().getOwners().forEach(owner ->
            {
                OwnerInfo ownerInfoBackup=owner;
                String businessService = tradeLicense.getBusinessService();
                if (businessService == null)
                    businessService = businessService_TL;
                switch (businessService) {
                    case businessService_BPA:
                        UserDetailResponse userDetailResponse = searchByUserName(owner.getMobileNumber(), getStateLevelTenant(tradeLicense.getTenantId()));
                        if (!userDetailResponse.getUser().isEmpty()) {
                            User user = userDetailResponse.getUser().get(0);
                            owner = addNotNullFieldsFromOwner(user, owner);
                        }
                        break;
                }
                if (owner.getUuid() == null) {
                    addUserDefaultFields(tradeLicense.getTenantId(), role, owner, businessService);
                    //  UserDetailResponse userDetailResponse = userExists(owner,requestInfo);
                    StringBuilder uri = new StringBuilder(config.getUserHost())
                            .append(config.getUserContextPath())
                            .append(config.getUserCreateEndpoint());
                    setUserName(owner,businessService);

                    UserDetailResponse userDetailResponse = userCall(new CreateUserRequest(requestInfo, owner), uri);
                    if (userDetailResponse.getUser().get(0).getUuid() == null) {
                        throw new CustomException("INVALID USER RESPONSE", "The user created has uuid as null");
                    }
                    log.info("owner created --> " + userDetailResponse.getUser().get(0).getUuid());
                    setOwnerFields(owner, userDetailResponse, requestInfo);
                }
                 else {
                    UserDetailResponse userDetailResponse = userExists(owner,requestInfo);
                    if(userDetailResponse.getUser().isEmpty())
                        throw new CustomException("INVALID USER","The uuid "+owner.getUuid()+" does not exists");
                    StringBuilder uri =new StringBuilder(config.getUserHost());
                    uri=uri.append(config.getUserContextPath()).append(config.getUserUpdateEndpoint());
                    OwnerInfo user = new OwnerInfo();
                    user.addUserWithoutAuditDetail(owner);
                    addNonUpdatableFields(user,userDetailResponse.getUser().get(0));
                   if (isBPARoleAddRequired) {
                        List<String> licenseeTyperRole = tradeUtil.getusernewRoleFromMDMS(tradeLicense, requestInfo);
                        for (String rolename : licenseeTyperRole) {
                            user.addRolesItem(Role.builder().code(rolename).name(rolename).tenantId(tradeLicense.getTenantId()).build());
                        }
                   }
                    userDetailResponse = userCall( new CreateUserRequest(requestInfo,user),uri);
                    switch (businessService)
                    {
                        case businessService_BPA:
                            owner=ownerInfoBackup;
                            break;
                    }
                    setOwnerFields(owner,userDetailResponse,requestInfo);
                }
            });
        });
    }

    public void upsertUser(ServiceRequest request){

        UserInfo userInfo = request.getService().getCitizen();

        // Search on mobile number as user name
        UserDetailResponse userDetailResponse = searchUser(null, userInfo.getMobileNumber());
        if (!userDetailResponse.getUser().isEmpty()) {
            UserInfo user = userDetailResponse.getUser().get(0);
        }

    }

    private OwnerInfo addNotNullFieldsFromOwner(User user,OwnerInfo owner)
    {
        OwnerInfo newowner = new OwnerInfo();
        newowner.setUuid(getFromOwnerIfNotNull(user.getUuid(),owner.getUuid()));
        newowner.setId((owner.getId()==null)?user.getId():owner.getId());
        newowner.setUserName(getFromOwnerIfNotNull(user.getUserName(),owner.getUserName()));
        newowner.setPassword(getFromOwnerIfNotNull(user.getPassword(),owner.getPassword()));
        newowner.setSalutation(getFromOwnerIfNotNull(user.getSalutation(),owner.getSalutation()));
        newowner.setName(getFromOwnerIfNotNull(user.getName(),owner.getName()));
        newowner.setGender(getFromOwnerIfNotNull(user.getGender(),owner.getGender()));
        newowner.setMobileNumber(getFromOwnerIfNotNull(user.getMobileNumber(),owner.getMobileNumber()));
        newowner.setEmailId(getFromOwnerIfNotNull(user.getEmailId(),owner.getEmailId()));
        newowner.setAltContactNumber(getFromOwnerIfNotNull(user.getAltContactNumber(),owner.getAltContactNumber()));
        newowner.setPan(getFromOwnerIfNotNull(user.getPan(),owner.getPan()));
        newowner.setAadhaarNumber(getFromOwnerIfNotNull(user.getAadhaarNumber(),owner.getAadhaarNumber()));
        newowner.setPermanentAddress(getFromOwnerIfNotNull(user.getPermanentAddress(),owner.getPermanentAddress()));
        newowner.setPermanentCity(getFromOwnerIfNotNull(user.getPermanentCity(),owner.getPermanentCity()));
        newowner.setPermanentPincode(getFromOwnerIfNotNull(user.getPermanentPincode(),owner.getPermanentPincode()));
        newowner.setCorrespondenceAddress(getFromOwnerIfNotNull(user.getCorrespondenceAddress(),owner.getCorrespondenceAddress()));
        newowner.setCorrespondenceCity(getFromOwnerIfNotNull(user.getCorrespondenceCity(),owner.getCorrespondenceCity()));
        newowner.setCorrespondencePincode(getFromOwnerIfNotNull(user.getCorrespondencePincode(),owner.getCorrespondencePincode()));
        newowner.setActive((owner.getActive()==null)?user.getActive():owner.getActive());
        newowner.setDob((owner.getDob()!=null)?owner.getDob():user.getDob());
        newowner.setPwdExpiryDate((owner.getPwdExpiryDate()==null)?user.getPwdExpiryDate():owner.getPwdExpiryDate());
        newowner.setLocale(getFromOwnerIfNotNull(user.getLocale(),owner.getLocale()));
        newowner.setType(getFromOwnerIfNotNull(user.getType(),owner.getType()));
        newowner.setRoles(user.getRoles());
        newowner.setAccountLocked((owner.getAccountLocked()==null)?user.getAccountLocked():owner.getAccountLocked());
        newowner.setFatherOrHusbandName(getFromOwnerIfNotNull(user.getFatherOrHusbandName(),owner.getFatherOrHusbandName()));
        newowner.setBloodGroup(getFromOwnerIfNotNull(user.getBloodGroup(),owner.getBloodGroup()));
        newowner.setIdentificationMark(getFromOwnerIfNotNull(user.getIdentificationMark(),owner.getIdentificationMark()));
        newowner.setPhoto(getFromOwnerIfNotNull(user.getPhoto(),owner.getPhoto()));
        newowner.setTenantId(getFromOwnerIfNotNull(user.getTenantId(),owner.getTenantId()));
        return  newowner;
    }

    private String getFromOwnerIfNotNull(String fromuser,String fromowner)
    {
        if(fromowner!=null)
        {
            return fromowner;
        }
        return fromuser;
    }
    */
/**
     * Sets the immutable fields from search to update request
     * @param user The user to be updated
     * @param userFromSearchResult The current user details according to searcvh
     *//*

    private void addNonUpdatableFields(User user,User userFromSearchResult){
        user.setUserName(userFromSearchResult.getUserName());
        user.setId(userFromSearchResult.getId());
        user.setActive(userFromSearchResult.getActive());
        user.setPassword(userFromSearchResult.getPassword());
    }



    private UserDetailResponse searchUser(String accountId, String userName){
        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(USERTYPE_CITIZEN);

        if(StringUtils.isEmpty(accountId) && StringUtils.isEmpty(userName))
            return null;

        if(!StringUtils.isEmpty(accountId))
            userSearchRequest.setUuid(Collections.singletonList(accountId));

        if(!StringUtils.isEmpty(userName))
            userSearchRequest.setUserName(userName);

        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userCall(userSearchRequest,uri);
    }




    */
/**
     * Sets the username as uuid
     * @param owner The owner to whom the username is to assigned
     *//*

    private void setUserName(UserInfo owner,String businessService){
        String username = UUID.randomUUID().toString();
        switch (businessService) {
            case businessService_BPA:
                username = owner.getMobileNumber();
                break;
        }
        owner.setUserName(username);
    }



    private Set<String> getMobileNumbers(List<OwnerInfo> owners,RequestInfo requestInfo,String tenantId){
        Set<String> listOfMobileNumbers = new HashSet<>();
        owners.forEach(owner -> {listOfMobileNumbers.add(owner.getMobileNumber());});
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setRequestInfo(requestInfo);
        userSearchRequest.setTenantId(tenantId);
        userSearchRequest.setUserType("CITIZEN");
        Set<String> availableMobileNumbers = new HashSet<>();

        listOfMobileNumbers.forEach(mobilenumber -> {
            userSearchRequest.setMobileNumber(mobilenumber);
            UserDetailResponse userDetailResponse =  userCall(userSearchRequest,uri);
            if(CollectionUtils.isEmpty(userDetailResponse.getUser()))
                availableMobileNumbers.add(mobilenumber);
        });
        return availableMobileNumbers;
    }


    */
/**
     * Sets ownerfields from the userResponse
     * @param owner The owner from tradeLicense
     * @param userDetailResponse The response from user search
     * @param requestInfo The requestInfo of the request
     *//*

    private void setOwnerFields(OwnerInfo owner, UserDetailResponse userDetailResponse,RequestInfo requestInfo){
        owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
        owner.setId(userDetailResponse.getUser().get(0).getId());
        owner.setUserName((userDetailResponse.getUser().get(0).getUserName()));
        owner.setCreatedBy(requestInfo.getUserInfo().getUuid());
        owner.setLastModifiedBy(requestInfo.getUserInfo().getUuid());
        owner.setCreatedDate(System.currentTimeMillis());
        owner.setLastModifiedDate(System.currentTimeMillis());
        owner.setActive(userDetailResponse.getUser().get(0).getActive());
    }


    */
/**
     * Sets the role,type,active and tenantId for a Citizen
     * @param tenantId TenantId of the property
     * @param role The role of the user set in this case to CITIZEN
     * @param owner The user whose fields are to be set
     *//*

    private void addUserDefaultFields(String tenantId, Role role, OwnerInfo owner, String businessService){
        owner.setActive(true);
        owner.setTenantId(tenantId.split("\\.")[0]);
        owner.setRoles(Collections.singletonList(role));
        owner.setType("CITIZEN");
        switch (businessService)
        {
            // for mseva notifications
            case businessService_BPA:
                owner.setPermanentCity(tenantId.split("\\.")[0]);
                break;
        }
    }


    */
/**
     * Creates citizen role
     * @return Role object for citizen
     *//*

    private Role getCitizenRole(String tenantId){
        Role role = new Role();
        role.setCode("CITIZEN");
        role.setName("Citizen");
        role.setTenantId(getStateLevelTenant(tenantId));
        return role;
    }

    private String getStateLevelTenant(String tenantId){
        return tenantId.split("\\.")[0];
    }


    */
/**
     * Returns UserDetailResponse by calling user service with given uri and object
     * @param userRequest Request object for user service
     * @param uri The address of the endpoint
     * @return Response from user service as parsed as userDetailResponse
     *//*

    private UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
        String dobFormat = null;
        if(uri.toString().contains(config.getUserSearchEndpoint())  || uri.toString().contains(config.getUserUpdateEndpoint()))
            dobFormat="yyyy-MM-dd";
        else if(uri.toString().contains(config.getUserCreateEndpoint()))
            dobFormat = "dd/MM/yyyy";
        try{
            LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, userRequest);
            parseResponse(responseMap,dobFormat);
            UserDetailResponse userDetailResponse = mapper.convertValue(responseMap,UserDetailResponse.class);
            return userDetailResponse;
        }
        catch(IllegalArgumentException  e)
        {
            throw new CustomException("IllegalArgumentException","ObjectMapper not able to convertValue in userCall");
        }
    }



    */
/**
     * Parses date formats to long for all users in responseMap
     * @param responeMap LinkedHashMap got from user api response
     *//*

    private void parseResponse(LinkedHashMap responeMap,String dobFormat){
        List<LinkedHashMap> users = (List<LinkedHashMap>)responeMap.get("user");
        String format1 = "dd-MM-yyyy HH:mm:ss";
        if(users!=null){
            users.forEach( map -> {
                        map.put("createdDate",dateTolong((String)map.get("createdDate"),format1));
                        if((String)map.get("lastModifiedDate")!=null)
                            map.put("lastModifiedDate",dateTolong((String)map.get("lastModifiedDate"),format1));
                        if((String)map.get("dob")!=null)
                            map.put("dob",dateTolong((String)map.get("dob"),dobFormat));
                        if((String)map.get("pwdExpiryDate")!=null)
                            map.put("pwdExpiryDate",dateTolong((String)map.get("pwdExpiryDate"),format1));
                    }
            );
        }
    }

    */
/**
     * Converts date to long
     * @param date date to be parsed
     * @param format Format of the date
     * @return Long value of date
     *//*

    private Long dateTolong(String date,String format){
        SimpleDateFormat f = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = f.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return  d.getTime();
    }


    */
/**
     * Call search in user service based on ownerids from criteria
     * @param criteria The search criteria containing the ownerids
     * @param requestInfo The requestInfo of the request
     * @return Search response from user service based on ownerIds
     *//*

    public UserDetailResponse getUser(TradeLicenseSearchCriteria criteria,RequestInfo requestInfo){
        UserSearchRequest userSearchRequest = getUserSearchRequest(criteria,requestInfo);
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        UserDetailResponse userDetailResponse = userCall(userSearchRequest,uri);
        return userDetailResponse;
    }


    */
/**
     * Creates userSearchRequest from tradeLicenseSearchCriteria
     * @param criteria The tradeLcienseSearch criteria
     * @param requestInfo The requestInfo of the request
     * @return The UserSearchRequest based on ownerIds
     *//*

    private UserSearchRequest getUserSearchRequest(RequestSearchCriteria criteria, RequestInfo requestInfo){
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setRequestInfo(requestInfo);
        userSearchRequest.setTenantId(criteria.getTenantId());
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType("CITIZEN");
        if(!CollectionUtils.isEmpty(criteria.getOwnerIds()))
            userSearchRequest.setUuid(criteria.getOwnerIds());
        return userSearchRequest;
    }



    private UserDetailResponse searchByUserName(String userName,String tenantId){
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        userSearchRequest.setUserType("CITIZEN");
        userSearchRequest.setUserName(userName);
        userSearchRequest.setTenantId(tenantId);
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userCall(userSearchRequest,uri);

    }


    */
/**
     * Updates user if present else creates new user
     * @param request TradeLicenseRequest received from update
     *//*

    public void updateUser(TradeLicenseRequest request){
        List<TradeLicense> licenses = request.getLicenses();

        RequestInfo requestInfo = request.getRequestInfo();
        licenses.forEach(license -> {
                license.getTradeLicenseDetail().getOwners().forEach(owner -> {
                    UserDetailResponse userDetailResponse = isUserUpdatable(owner,requestInfo);
                    OwnerInfo user = new OwnerInfo();
                    StringBuilder uri  = new StringBuilder(config.getUserHost());
                    if(CollectionUtils.isEmpty(userDetailResponse.getUser())) {
                        uri = uri.append(config.getUserContextPath()).append(config.getUserCreateEndpoint());
                        user.addUserWithoutAuditDetail(owner);
                        user.setUserName(owner.getMobileNumber());
                    }
                    else
                    {   owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
                        uri=uri.append(config.getUserContextPath()).append(config.getUserUpdateEndpoint());
                        user.addUserWithoutAuditDetail(owner);
                    }
                    userDetailResponse = userCall( new CreateUserRequest(requestInfo,user),uri);
                    setOwnerFields(owner,userDetailResponse,requestInfo);
                });
            });
    }


    private UserDetailResponse isUserUpdatable(OwnerInfo owner,RequestInfo requestInfo){
        UserSearchRequest userSearchRequest =new UserSearchRequest();
        userSearchRequest.setTenantId(owner.getTenantId());
        userSearchRequest.setMobileNumber(owner.getMobileNumber());
        userSearchRequest.setUuid(Collections.singletonList(owner.getUuid()));
        userSearchRequest.setRequestInfo(requestInfo);
        userSearchRequest.setActive(true);
        userSearchRequest.setUserType(owner.getType());
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userCall(userSearchRequest,uri);
    }
}
*/