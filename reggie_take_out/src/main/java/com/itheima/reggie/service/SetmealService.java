package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface SetmealService extends IService<Setmeal> {

    //新增套餐同时保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐和关联
    public void removeWithDish(List<Long> ids);

    //修改套餐前要进行数据回显
    public SetmealDto getByIdWithSetmeal(Long id);

    //修改套餐
    public void updateWithSetmeal(SetmealDto setmealDto);


}

