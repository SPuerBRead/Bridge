package bridge.mapper;


import bridge.model.DnsRecordRebind;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DnsRecordRebindMapper {

    @Insert("insert into dns_record_rebind(id,subdomain,time,ip1,ip2,logid) values(#{id},#{subdomain},#{time},#{ip1},#{ip2},#{logid})")
    void insert(DnsRecordRebind dnsRecordRebind);

    @Select("select * from dns_record_rebind where subdomain = #{subdomain} limit 1")
    DnsRecordRebind selectDnsRecordRebindBySubdomain(String subdomain);

    @Select("select * from dns_record_rebind where subdomain like concat('%',#{0},'%') order by time desc")
    List<DnsRecordRebind> getAll(String subdomain);

    @Select("select * from dns_record_rebind where id = #{id} limit 1")
    DnsRecordRebind selectDnsRecordRebindByID(String id);

    @Delete("delete from dns_record_rebind where id=#{id}")
    void deleteOneByID(String id);

    @Delete("delete from dns_record_rebind where logid=#{logid}")
    void deleteAllDnsRecordRebindBylogID(int logid);

    @Update("update dns_record_rebind set subdomain=#{subdomain},ip1=#{ip1},ip1=#{ip2},time=#{time}, where id=#{id}")
    void updateByID(DnsRecordRebind dnsRecordRebind);
}
