package com.mindskip.xzs.repository;

import com.mindskip.xzs.domain.Config;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ConfigMapper {
    @Select("SELECT * FROM t_config")
    List<Config> selectAll();

    @Update("UPDATE t_config SET config_value = #{value} WHERE config_key = #{key}")
    void updateConfigValue(@Param("key") String key, @Param("value") String value);
} 