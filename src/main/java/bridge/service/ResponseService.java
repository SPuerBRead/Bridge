package bridge.service;


import bridge.mapper.ResponseMapper;
import bridge.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ResponseService {

    @Autowired
    private ResponseMapper responseMapper;

    public Response getResponseByID(String id) {
        return responseMapper.selectResponseByID(id);
    }

    public Response getResponseBySubdomain(String subdomain) {
        return responseMapper.selectResponseBySubdomain(subdomain);
    }

    public void addResponse(Response response) {
        responseMapper.insert(response);
    }

    public List getAllResponse(String userDomain) {
        return responseMapper.getAll(userDomain);
    }

    public void updateResponseAByID(Response response) {
        responseMapper.updateByID(response);
    }

    public void delOneResponse(String id) {
        responseMapper.deleteOneByID(id);
    }

    public void delAllResponse(int logid) {
        responseMapper.deleteAllBylogID(logid);
    }
}
