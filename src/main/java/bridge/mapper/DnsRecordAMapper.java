package bridge.mapper;


import bridge.model.DnsRecordA;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DnsRecordAMapper {

    @Insert("insert into dns_record_a(id,subdomain,time,ip,logid) values(#{id},#{subdomain},#{time},#{ip},#{logid})")
    void insert(DnsRecordA dnsRecordA);

    @Select("select * from dns_record_a where subdomain = #{subdomain} limit 1")
    DnsRecordA selectDnsRecordABySubdomain(String subdomain);

    @Select("select * from dns_record_a where subdomain like concat('%',#{0},'%') order by time desc")
    List<DnsRecordA> getAll(String subdomain);

    @Select("select * from dns_record_a where id = #{id} limit 1")
    DnsRecordA selectDnsRecordAByID(String id);

    @Delete("delete from dns_record_a where id=#{id}")
    void deleteOneByID(String id);

    @Delete("delete from dns_record_a where logid=#{logid}")
    void deleteAllDnsRecordABylogID(int logid);

    @Update("update dns_record_a set subdomain=#{subdomain},ip=#{ip},time=#{time} where id=#{id}")
    void updateByID(DnsRecordA dnsRecordA);
}
