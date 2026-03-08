package com.vocaldoc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vocaldoc.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
