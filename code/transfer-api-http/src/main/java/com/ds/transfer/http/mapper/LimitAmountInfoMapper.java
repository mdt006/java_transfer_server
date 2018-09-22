package com.ds.transfer.http.mapper;
import java.util.List;
import com.ds.transfer.http.entity.LimitAmountInfo;

public interface LimitAmountInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(LimitAmountInfo record);

    int insertSelective(LimitAmountInfo record);

    LimitAmountInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LimitAmountInfo record);

    int updateByPrimaryKey(LimitAmountInfo record);
    
    List<LimitAmountInfo> selectByExample();
}