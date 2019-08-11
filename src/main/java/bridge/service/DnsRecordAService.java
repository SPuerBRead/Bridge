package bridge.service;


import bridge.mapper.DnsRecordAMapper;
import bridge.model.DnsRecordA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DnsRecordAService {

    @Autowired
    private DnsRecordAMapper dnsRecordAMapper;

    public void addDnsRecordA(DnsRecordA dnsRecordA) {
        dnsRecordAMapper.insert(dnsRecordA);
    }

    public DnsRecordA getDnsRecordABySubdomain(String subdomain) {
        return dnsRecordAMapper.selectDnsRecordABySubdomain(subdomain);
    }

    public List getAllDnsRecordA(String userDomain) {
        return dnsRecordAMapper.getAll(userDomain);
    }

    public DnsRecordA getDnsRecordAByID(String id) {
        return dnsRecordAMapper.selectDnsRecordAByID(id);
    }

    public void delOneDnsRecordA(String id) {
        dnsRecordAMapper.deleteOneByID(id);
    }

    public void delAllDnsRecordA(int logid) {
        dnsRecordAMapper.deleteAllDnsRecordABylogID(logid);
    }

    public void updateDnsRecordAByID(DnsRecordA dnsRecordA) {
        dnsRecordAMapper.updateByID(dnsRecordA);
    }
}
